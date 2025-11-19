package one.modality.catering.backoffice.activities.kitchen.model;

import dev.webfx.platform.console.Console;
import one.modality.base.shared.entities.Item;
import one.modality.catering.backoffice.activities.kitchen.AttendanceCounts;

import java.time.LocalDate;
import java.util.*;

/**
 * Presentation model for the Kitchen UI.
 * Transforms KitchenData into UI-friendly structures (AttendanceCounts).
 * Immutable after construction.
 *
 * @author Claude Code (Refactored from KitchenActivity)
 */
public final class KitchenDisplayModel {

    private static final Set<String> ALLOWED_MEALS = Set.of("Breakfast", "Lunch", "Dinner");

    private final AttendanceCounts attendanceCounts;
    private final Set<String> displayedMealNames;
    private final Map<String, String> dietaryOptionSvgs;

    private KitchenDisplayModel(
            AttendanceCounts attendanceCounts,
            Set<String> displayedMealNames,
            Map<String, String> dietaryOptionSvgs) {
        this.attendanceCounts = attendanceCounts;
        this.displayedMealNames = Collections.unmodifiableSet(displayedMealNames);
        this.dietaryOptionSvgs = Collections.unmodifiableMap(dietaryOptionSvgs);
    }

    public AttendanceCounts getAttendanceCounts() {
        return attendanceCounts;
    }

    public Set<String> getDisplayedMealNames() {
        return displayedMealNames;
    }

    public Map<String, String> getDietaryOptionSvgs() {
        return dietaryOptionSvgs;
    }

    public boolean hasData() {
        return !dietaryOptionSvgs.isEmpty();
    }

    /**
     * Factory method to create KitchenDisplayModel from KitchenData.
     * Transforms raw data into UI-ready structures.
     */
    public static KitchenDisplayModel from(KitchenData kitchenData) {
        Console.log("Processing kitchen data into display model");

        AttendanceCounts attendanceCounts = new AttendanceCounts();
        Set<String> displayedMealNames = new HashSet<>();
        Map<String, String> dietaryOptionSvgs = new HashMap<>();

        // Store virtual dietary option SVGs (Total and ?)
        String totalSvg = kitchenData.getDietaryOptionSvg("Total");
        if (totalSvg != null) {
            attendanceCounts.storeDietaryOptionSvg("Total", totalSvg);
            attendanceCounts.storeDietaryOptionOrder("Total", 10001);
        }

        String unknownSvg = kitchenData.getDietaryOptionSvg("?");
        if (unknownSvg != null) {
            attendanceCounts.storeDietaryOptionSvg("?", unknownSvg);
            attendanceCounts.storeDietaryOptionOrder("?", 10000);
        }

        // Store actual dietary items SVGs and orders
        for (Item dietaryItem : kitchenData.getDietaryItems()) {
            Console.log("Debug: Diet Item - Name: " + dietaryItem.getName() + ", Code: " + dietaryItem.getCode());
            attendanceCounts.storeDietaryOptionOrder(dietaryItem.getCode(), dietaryItem.getOrd());

            String svg = kitchenData.getDietaryOptionSvg(dietaryItem.getCode());
            if (svg != null) {
                attendanceCounts.storeDietaryOptionSvg(dietaryItem.getCode(), svg);
                String keyText = dietaryItem.getName() + " (" + dietaryItem.getCode() + ")";
                dietaryOptionSvgs.put(keyText, svg);
            }
        }

        // Process attendance counts for each date/meal/dietary combination
        for (LocalDate date : kitchenData.getDates()) {
            List<Item> mealsForDate = kitchenData.getMealsForDate(date);
            Console.log("Debug: Date " + date + " - Found " + mealsForDate.size() + " meals: "
                    + mealsForDate.stream().map(Item::getName).collect(java.util.stream.Collectors.joining(", ")));

            for (Item meal : mealsForDate) {
                // Filter to only show Breakfast, Lunch, and Dinner
                if (!ALLOWED_MEALS.contains(meal.getName())) {
                    continue;
                }

                displayedMealNames.add(meal.getName());

                // Add counts for virtual items (Total and ?)
                Item totalItem = kitchenData.getTotalVirtualItem();
                if (totalItem != null) {
                    int totalCount = kitchenData.getAttendanceCount(date, meal, totalItem);
                    attendanceCounts.add(date, meal.getName(), "Total", totalCount);
                }

                Item unknownItem = kitchenData.getUnknownVirtualItem();
                if (unknownItem != null) {
                    int unknownCount = kitchenData.getAttendanceCount(date, meal, unknownItem);
                    attendanceCounts.add(date, meal.getName(), "?", unknownCount);
                }

                // Add counts for actual dietary items
                for (Item dietaryItem : kitchenData.getDietaryItems()) {
                    int count = kitchenData.getAttendanceCount(date, meal, dietaryItem);
                    attendanceCounts.add(date, meal.getName(), dietaryItem.getCode(), count);
                }
            }
        }

        Console.log("Unique dates found: " + attendanceCounts.getDates().size());
        Console.log("Displayed meals: " + displayedMealNames);

        return new KitchenDisplayModel(attendanceCounts, displayedMealNames, dietaryOptionSvgs);
    }

    /**
     * Returns an empty display model for when no data is available.
     */
    public static KitchenDisplayModel empty() {
        return new KitchenDisplayModel(
                new AttendanceCounts(),
                Collections.emptySet(),
                Collections.emptyMap());
    }
}
