package one.modality.catering.backoffice.activities.kitchen;

import java.time.LocalDate;
import java.util.*;

/**
 * Data structure for storing and querying attendance counts organized by date, meal, and dietary option.
 * Provides efficient O(1) lookup and aggregation of meal attendance data with support for dietary option metadata.
 *
 * <p>This class maintains:
 * <ul>
 *   <li>Attendance counts indexed by (date, meal, dietaryOption) with O(1) lookup</li>
 *   <li>Dietary option display order for consistent UI sorting</li>
 *   <li>SVG graphics for dietary option icons</li>
 *   <li>Human-readable names for dietary option codes</li>
 *   <li>Cached unique meals and dietary options for fast retrieval</li>
 * </ul>
 *
 * @author Bruno Salmon
 */
public class AttendanceCounts {

    // O(1) lookup structure: date -> meal -> dietaryOption -> count
    private final Map<LocalDate, Map<String, Map<String, Integer>>> counts = new HashMap<>();

    // Cached unique values (populated during add())
    private final Set<String> uniqueMeals = new LinkedHashSet<>();
    private final Set<String> uniqueDietaryOptions = new LinkedHashSet<>();

    // Metadata maps
    private final Map<String, Integer> dietaryOptionOrders = new HashMap<>();
    private final Map<String, String> dietaryOptionSvgs = new HashMap<>();
    private final Map<String, String> dietaryOptionNames = new HashMap<>();

    // Cached sorted dietary options (invalidated on add, computed lazily)
    private List<String> cachedSortedDietaryOptions = null;

    /**
     * Adds an attendance count for a specific date, meal, and dietary option combination.
     * O(1) operation.
     *
     * @param date the date of the meal
     * @param meal the meal name (e.g., "Breakfast", "Lunch")
     * @param dietaryOption the dietary option code (e.g., "V", "VG", "GF", "Total", "?")
     * @param count the number of attendees
     */
    public void add(LocalDate date, String meal, String dietaryOption, int count) {
        counts.computeIfAbsent(date, k -> new HashMap<>())
              .computeIfAbsent(meal, k -> new HashMap<>())
              .put(dietaryOption, count);

        // Update cached unique values
        uniqueMeals.add(meal);
        uniqueDietaryOptions.add(dietaryOption);

        // Invalidate sorted cache
        cachedSortedDietaryOptions = null;
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
     * O(1) operation.
     *
     * @return a set of dates with recorded attendance
     */
    public Set<LocalDate> getDates() {
        return counts.keySet();
    }

    /**
     * Returns all dietary options sorted by their display order.
     * Uses lazy caching for O(1) subsequent calls.
     *
     * @return a list of dietary option codes in display order
     */
    public List<String> getSortedDietaryOptions() {
        if (cachedSortedDietaryOptions == null) {
            List<String> sorted = new ArrayList<>(uniqueDietaryOptions);
            sorted.sort((opt1, opt2) -> {
                int ord1 = dietaryOptionOrders.getOrDefault(opt1, Integer.MAX_VALUE);
                int ord2 = dietaryOptionOrders.getOrDefault(opt2, Integer.MAX_VALUE);
                return Integer.compare(ord1, ord2);
            });
            cachedSortedDietaryOptions = sorted;
        }
        return cachedSortedDietaryOptions;
    }

    /**
     * Returns all unique meal names found in the attendance data.
     * O(1) operation using cached set.
     *
     * @return a list of meal names
     */
    public List<String> getUniqueMeals() {
        return new ArrayList<>(uniqueMeals);
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
     * O(1) operation using nested HashMap lookup.
     *
     * @param date the date of the meal
     * @param meal the meal name
     * @param dietaryOption the dietary option code
     * @return the attendance count, or 0 if no data found
     */
    public int getCount(LocalDate date, String meal, String dietaryOption) {
        Map<String, Map<String, Integer>> mealMap = counts.get(date);
        if (mealMap == null) return 0;

        Map<String, Integer> dietMap = mealMap.get(meal);
        if (dietMap == null) return 0;

        return dietMap.getOrDefault(dietaryOption, 0);
    }
}
