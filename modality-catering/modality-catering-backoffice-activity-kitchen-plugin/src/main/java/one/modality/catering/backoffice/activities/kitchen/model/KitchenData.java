package one.modality.catering.backoffice.activities.kitchen.model;

import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.ScheduledItem;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Immutable data model representing all kitchen data for a given time period.
 * Holds scheduled items, meals, dietary options, and attendance counts.
 *
 * @author Bruno Salmon (refactored)
 */
public class KitchenData {

    private final List<ScheduledItem> scheduledItems;
    private final List<Item> mealItems;
    private final List<Item> dietaryItems;
    private final Map<ScheduledItemKey, Integer> attendanceCounts;
    private final Map<String, String> dietaryOptionSvgs;
    private final Item totalVirtualItem;
    private final Item unknownVirtualItem;

    private KitchenData(Builder builder) {
        this.scheduledItems = Collections.unmodifiableList(builder.scheduledItems);
        this.mealItems = Collections.unmodifiableList(builder.mealItems);
        this.dietaryItems = Collections.unmodifiableList(builder.dietaryItems);
        this.attendanceCounts = Collections.unmodifiableMap(builder.attendanceCounts);
        this.dietaryOptionSvgs = Collections.unmodifiableMap(builder.dietaryOptionSvgs);
        this.totalVirtualItem = builder.totalVirtualItem;
        this.unknownVirtualItem = builder.unknownVirtualItem;
    }

    public Set<LocalDate> getDates() {
        return scheduledItems.stream()
                .map(ScheduledItem::getDate)
                .collect(Collectors.toSet());
    }

    public List<Item> getMealsForDate(LocalDate date) {
        return scheduledItems.stream()
                .filter(si -> si.getDate().equals(date))
                .map(ScheduledItem::getItem)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.comparingInt(Item::getOrd))
                .collect(Collectors.toList());
    }

    public List<Item> getSortedDietaryOptions() {
        List<Item> sorted = new ArrayList<>(dietaryItems);
        sorted.sort(Comparator.comparingInt(Item::getOrd));
        return sorted;
    }

    public int getAttendanceCount(LocalDate date, Item meal, Item dietaryOption) {
        ScheduledItemKey key = new ScheduledItemKey(date, meal, dietaryOption);
        return attendanceCounts.getOrDefault(key, 0);
    }

    public String getDietaryOptionSvg(String dietaryOptionCode) {
        return dietaryOptionSvgs.get(dietaryOptionCode);
    }

    public List<Item> getMealItems() {
        return mealItems;
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

    /**
     * Get all dietary options including virtual items (Total, ?)
     */
    public List<Item> getAllDietaryOptions() {
        List<Item> all = new ArrayList<>();
        if (unknownVirtualItem != null) all.add(unknownVirtualItem);
        all.addAll(dietaryItems);
        if (totalVirtualItem != null) all.add(totalVirtualItem);
        return all;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<ScheduledItem> scheduledItems = new ArrayList<>();
        private List<Item> mealItems = new ArrayList<>();
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
            this.mealItems = mealItems;
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
            if (!(o instanceof ScheduledItemKey)) return false;
            ScheduledItemKey that = (ScheduledItemKey) o;
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
