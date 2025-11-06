package one.modality.event.backoffice.activities.program;

import one.modality.base.client.util.TimeValidationUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for converting dates to and from string representations in the program module.
 * This class manages the conversion between comma-separated date strings (stored in the database)
 * and LocalDate objects (used in the application). The DayTemplate entity stores selected dates
 * as a string field in the format "2024-01-15, 2024-01-16, 2024-01-17".
 *
 * <p>Key responsibilities:
 * <ul>
 *   <li>Add dates to comma-separated string</li>
 *   <li>Remove dates from comma-separated string</li>
 *   <li>Parse comma-separated string into List of LocalDate</li>
 *   <li>Check date existence in string</li>
 *   <li>Validate time format (delegates to shared utility)</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * // Add a date to existing dates string
 * String dates = "2024-01-15, 2024-01-16";
 * dates = DatesToStringConversion.addDate(dates, LocalDate.of(2024, 1, 17));
 * // Result: "2024-01-15, 2024-01-16, 2024-01-17"
 *
 * // Parse dates string into list
 * List&lt;LocalDate&gt; dateList = DatesToStringConversion.getDateList(dates);
 * </pre>
 *
 * @author David Hello
 */
final class DatesToStringConversion {

    /**
     * Standard time formatter for displaying times in HH:mm format (e.g., "14:30").
     * Used throughout the program module for consistent time formatting.
     */
    static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private DatesToStringConversion() {
        // Utility class - no instantiation allowed
    }

    /**
     * Adds a date to a comma-separated dates string if it doesn't already exist.
     * The method ensures no duplicate dates are added. If the date already exists
     * in the string, the original string is returned unchanged.
     *
     * @param existingDates The existing comma-separated dates string (can be null or empty)
     * @param date The date to add (must not be null)
     * @return Updated comma-separated dates string with the new date appended (if not already present)
     *
     * @see #removeDate(String, LocalDate)
     */
    public static String addDate(String existingDates, LocalDate date) {
        // Handle null input by converting to empty string
        if (existingDates == null) {
            existingDates = "";
        }

        // Parse the existing dates into a list
        List<LocalDate> dateList = getDateList(existingDates);

        // Only add the date if it's not already in the list (prevent duplicates)
        if (!dateList.contains(date)) {
            if (!existingDates.isEmpty()) {
                existingDates += ", ";  // Add separator before new date
            }
            existingDates += date.toString();  // Append the new date in ISO format (yyyy-MM-dd)
        }

        return existingDates;
    }

    /**
     * Removes a date from a comma-separated dates string.
     * If the date is not present in the string, the returned string will be
     * the same as the original (minus the date that wasn't there).
     *
     * @param existingDates The existing comma-separated dates string
     * @param date The date to remove
     * @return Updated comma-separated dates string with the specified date removed
     *
     * @see #addDate(String, LocalDate)
     */
    public static String removeDate(String existingDates, LocalDate date) {
        // Parse the existing dates into a list
        List<LocalDate> dateList = getDateList(existingDates);

        // Filter out the date to be removed
        dateList = dateList.stream()
            .filter(d -> !d.equals(date))
            .collect(Collectors.toList());

        // Convert the list back to a comma-separated string
        return dateList.stream()
            .map(LocalDate::toString)
            .collect(Collectors.joining(", "));
    }

    /**
     * Converts a comma-separated dates string into a list of LocalDate objects.
     * The method handles:
     * <ul>
     *   <li>null or empty strings (returns empty list)</li>
     *   <li>Extra whitespace around dates (trimmed)</li>
     *   <li>Empty segments (filtered out)</li>
     *   <li>ISO date format parsing (yyyy-MM-dd)</li>
     * </ul>
     *
     * <p>Expected format: "2024-01-15, 2024-01-16, 2024-01-17"
     *
     * @param dates Comma-separated string of dates in ISO format (can be null or empty)
     * @return List of LocalDate objects parsed from the string (empty list if input is null/empty)
     * @throws java.time.format.DateTimeParseException if any date segment cannot be parsed
     */
    public static List<LocalDate> getDateList(String dates) {
        // Return empty list for null or empty input
        if (dates == null || dates.trim().isEmpty()) {
            return List.of();
        }

        // Split by comma-space, trim each segment, filter out empty strings, and parse to LocalDate
        return Arrays.stream(dates.split(", "))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(LocalDate::parse)  // Parse ISO format (yyyy-MM-dd)
            .collect(Collectors.toList());
    }

    /**
     * Validates if the given text is a valid LocalTime format.
     * This method delegates to the shared {@link TimeValidationUtil} to avoid code duplication.
     * The validation accepts standard time formats like "14:30" or "14:30:00".
     *
     * @param text The text to validate (can be null)
     * @return true if the text can be parsed as a valid LocalTime, false otherwise
     *
     * @see TimeValidationUtil#isLocalTimeTextValid(String)
     */
    public static boolean isLocalTimeTextValid(String text) {
        return TimeValidationUtil.isLocalTimeTextValid(text);
    }
}