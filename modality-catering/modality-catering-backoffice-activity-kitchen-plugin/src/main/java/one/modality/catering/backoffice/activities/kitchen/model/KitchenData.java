package one.modality.catering.backoffice.activities.kitchen.model;

import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.ScheduledItem;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Immutable data model representing all kitchen data for a given time period.
 * Holds scheduled items, meals, dietary options, and attendance counts.
 * Uses pre-computed indexes for O(1) lookups.
 *
 * @author Bruno Salmon (refactored)
 */
public class KitchenData {

    private final List<Item> dietaryItems;
    private final Map<ScheduledItemKey, Integer> attendanceCounts;
    private final Map<String, String> dietaryOptionSvgs;
    private final Item totalVirtualItem;
    private final Item unknownVirtualItem;

    // Pre-computed caches for O(1) lookup
    private final Set<LocalDate> cachedDates;
    private final Map<LocalDate, List<Item>> mealsByDate;

    private KitchenData(Builder builder) {
        List<ScheduledItem> scheduledItems = Collections.unmodifiableList(builder.scheduledItems);
        this.dietaryItems = Collections.unmodifiableList(builder.dietaryItems);
        this.attendanceCounts = Collections.unmodifiableMap(builder.attendanceCounts);
        this.dietaryOptionSvgs = Collections.unmodifiableMap(builder.dietaryOptionSvgs);
        this.totalVirtualItem = builder.totalVirtualItem;
        this.unknownVirtualItem = builder.unknownVirtualItem;

        // Pre-compute dates set (O(1) lookup instead of streaming every time)
        this.cachedDates = scheduledItems.stream()
                .map(ScheduledItem::getDate)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Pre-compute meals indexed by date (O(1) lookup instead of filtering every time)
        Map<LocalDate, List<Item>> tempMealsByDate = new HashMap<>();
        for (ScheduledItem si : scheduledItems) {
            LocalDate date = si.getDate();
            Item meal = si.getItem();
            if (date != null && meal != null) {
                tempMealsByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(meal);
            }
        }
        // Sort and deduplicate meals for each date
        for (Map.Entry<LocalDate, List<Item>> entry : tempMealsByDate.entrySet()) {
            List<Item> meals = entry.getValue().stream()
                    .distinct()
                    .sorted(Comparator.comparingInt(Item::getOrd))
                    .collect(Collectors.toList());
            entry.setValue(meals);
        }
        this.mealsByDate = Collections.unmodifiableMap(tempMealsByDate);
    }

    /**
     * Returns all dates that have attendance data.
     * O(1) operation using pre-computed cache.
     */
    public Set<LocalDate> getDates() {
        return cachedDates;
    }

    /**
     * Returns meals for a specific date.
     * O(1) operation using pre-computed index.
     */
    public List<Item> getMealsForDate(LocalDate date) {
        return mealsByDate.getOrDefault(date, Collections.emptyList());
    }

    public int getAttendanceCount(LocalDate date, Item meal, Item dietaryOption) {
        ScheduledItemKey key = new ScheduledItemKey(date, meal, dietaryOption);
        return attendanceCounts.getOrDefault(key, 0);
    }

    public String getDietaryOptionSvg(String dietaryOptionCode) {
        return dietaryOptionSvgs.get(dietaryOptionCode);
    }

    public List<Item> getDietaryItems() {
        return dietaryItems;
    }

    public Item getTotalVirtualItem() {
        return totalVirtualItem;
    }

    public Item getUnknownVirtualItem() {
        return unknownVirtualItem;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<ScheduledItem> scheduledItems = new ArrayList<>();
        private List<Item> dietaryItems = new ArrayList<>();
        private final Map<ScheduledItemKey, Integer> attendanceCounts = new HashMap<>();
        private final Map<String, String> dietaryOptionSvgs = new HashMap<>();
        private Item totalVirtualItem;
        private Item unknownVirtualItem;

        public Builder scheduledItems(List<ScheduledItem> scheduledItems) {
            this.scheduledItems = scheduledItems;
            return this;
        }

        public Builder mealItems() {
            return this;
        }

        public Builder dietaryItems(List<Item> dietaryItems) {
            this.dietaryItems = dietaryItems;
            return this;
        }

        public void addAttendanceCount(LocalDate date, Item meal, Item dietaryOption, int count) {
            ScheduledItemKey key = new ScheduledItemKey(date, meal, dietaryOption);
            attendanceCounts.put(key, count);
        }

        public void dietaryOptionSvg(String code, String svg) {
            dietaryOptionSvgs.put(code, svg);
        }

        public void totalVirtualItem(Item totalVirtualItem) {
            this.totalVirtualItem = totalVirtualItem;
        }

        public void unknownVirtualItem(Item unknownVirtualItem) {
            this.unknownVirtualItem = unknownVirtualItem;
        }

        public KitchenData build() {
            return new KitchenData(this);
        }
    }

    /**
     * Key for identifying unique combinations of date, meal, and dietary option
     */
    private static class ScheduledItemKey {
        private final LocalDate date;
        private final Item meal;
        private final Item dietaryOption;

        public ScheduledItemKey(LocalDate date, Item meal, Item dietaryOption) {
            this.date = date;
            this.meal = meal;
            this.dietaryOption = dietaryOption;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ScheduledItemKey that)) return false;
            return Objects.equals(date, that.date) &&
                   Objects.equals(meal, that.meal) &&
                   Objects.equals(dietaryOption, that.dietaryOption);
        }

        @Override
        public int hashCode() {
            return Objects.hash(date, meal, dietaryOption);
        }
    }
}
