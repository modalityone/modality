package one.modality.catering.backoffice.activities.kitchen;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Data structure for storing and querying attendance counts organized by date, meal, and dietary option.
 * Provides efficient lookup and aggregation of meal attendance data with support for dietary option metadata.
 *
 * <p>This class maintains:
 * <ul>
 *   <li>Attendance counts indexed by (date, meal, dietaryOption)</li>
 *   <li>Dietary option display order for consistent UI sorting</li>
 *   <li>SVG graphics for dietary option icons</li>
 *   <li>Human-readable names for dietary option codes</li>
 * </ul>
 *
 * @author Bruno Salmon
 */
public class AttendanceCounts {

    private final Map<LocalDate, List<Row>> rows = new HashMap<>();
    private final Map<String, Integer> dietaryOptionOrders = new HashMap<>();
    private final Map<String, String> dietaryOptionSvgs = new HashMap<>();
    private final Map<String, String> dietaryOptionNames = new HashMap<>();

    /**
     * Adds an attendance count for a specific date, meal, and dietary option combination.
     *
     * @param date the date of the meal
     * @param meal the meal name (e.g., "Breakfast", "Lunch")
     * @param dietaryOption the dietary option code (e.g., "V", "VG", "GF", "Total", "?")
     * @param count the number of attendees
     */
    public void add(LocalDate date, String meal, String dietaryOption, int count) {
        if (!rows.containsKey(date)) {
            rows.put(date, new ArrayList<>());
        }
        rows.get(date).add(new Row(meal, dietaryOption, count));
    }

    /**
     * Stores the display order for a dietary option to ensure consistent sorting in the UI.
     *
     * @param dietaryOption the dietary option code
     * @param order the ordinal value for sorting (lower values appear first)
     */
    public void storeDietaryOptionOrder(String dietaryOption, int order) {
        dietaryOptionOrders.put(dietaryOption, order);
    }

    /**
     * Returns all dates that have attendance data.
     *
     * @return a set of dates with recorded attendance
     */
    public Set<LocalDate> getDates() {
        return rows.keySet();
    }

    /**
     * Returns all dietary options sorted by their display order.
     *
     * @return a list of dietary option codes in display order
     */
    public List<String> getSortedDietaryOptions() {
        return rows.values().stream()
                .flatMap(Collection::stream)
                .map(Row::getDietaryOption)
                .distinct()
                .sorted((dietaryOption1, dietaryOption2) -> {
                    int ordinal1 = dietaryOptionOrders.getOrDefault(dietaryOption1, Integer.MAX_VALUE);
                    int ordinal2 = dietaryOptionOrders.getOrDefault(dietaryOption2, Integer.MAX_VALUE);
                    return Integer.compare(ordinal1, ordinal2);
                })
                .collect(Collectors.toList());
    }

    /**
     * Returns all unique meal names found in the attendance data.
     *
     * @return a list of meal names
     */
    public List<String> getUniqueMeals() {
        return rows.values().stream()
                .flatMap(Collection::stream)
                .map(Row::getMeal)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Stores the SVG icon graphic for a dietary option.
     *
     * @param dietaryOption the dietary option code
     * @param svg the SVG path data or complete SVG graphic string
     */
    public void storeDietaryOptionSvg(String dietaryOption, String svg) {
        dietaryOptionSvgs.put(dietaryOption, svg);
    }

    /**
     * Retrieves the SVG icon graphic for a dietary option.
     *
     * @param dietaryOption the dietary option code
     * @return the SVG graphic string, or null if not found
     */
    public String getSvgForDietaryOption(String dietaryOption) {
        return dietaryOptionSvgs.get(dietaryOption);
    }

    /**
     * Stores a human-readable name for a dietary option code.
     *
     * @param dietaryOptionCode the dietary option code (e.g., "?")
     * @param name the display name (e.g., "Unknown diet")
     */
    public void storeDietaryOptionName(String dietaryOptionCode, String name) {
        dietaryOptionNames.put(dietaryOptionCode, name);
    }

    /**
     * Retrieves the human-readable name for a dietary option code.
     *
     * @param dietaryOptionCode the dietary option code
     * @return the display name, or null if not found
     */
    public String getNameForDietaryOption(String dietaryOptionCode) {
        return dietaryOptionNames.get(dietaryOptionCode);
    }

    /**
     * Retrieves the attendance count for a specific date, meal, and dietary option combination.
     *
     * @param date the date of the meal
     * @param meal the meal name
     * @param dietaryOption the dietary option code
     * @return the attendance count, or 0 if no data found
     */
    public int getCount(LocalDate date, String meal, String dietaryOption) {
        for (Row row : rows.getOrDefault(date, Collections.emptyList())) {
            if (row.getMeal().equals(meal) && row.getDietaryOption().equals(dietaryOption)) {
                return row.getCount();
            }
        }
        return 0;
    }

    private static class Row {
        private final String meal;
        private final String dietaryOption;
        private final int count;

        public Row(String meal, String dietaryOption, int count) {
            this.meal = meal;
            this.dietaryOption = dietaryOption;
            this.count = count;
        }

        public String getMeal() {
            return meal;
        }

        public String getDietaryOption() {
            return dietaryOption;
        }

        public int getCount() {
            return count;
        }
    }
}
