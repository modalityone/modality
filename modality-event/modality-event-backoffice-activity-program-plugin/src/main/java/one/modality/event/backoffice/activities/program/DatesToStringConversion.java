package one.modality.event.backoffice.activities.program;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author David Hello
 */
final class DatesToStringConversion {

    static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    // Static method to add a date
    public static String addDate(String existingDates, LocalDate date) {
        List<LocalDate> dateList = getDateList(existingDates);

        if(existingDates==null) {
            existingDates = "";
        }
        if (!dateList.contains(date)) {
            if (!existingDates.isEmpty()) {
                existingDates += ", ";  // Add a comma and space if it's not empty
            }
            existingDates += date.toString();  // Add the new date
        }

        return existingDates;  // Return the updated string
    }

    // Static method to remove a date
    public static String removeDate(String existingDates, LocalDate date) {
        List<LocalDate> dateList = getDateList(existingDates);

        dateList = dateList.stream()
            .filter(d -> !d.equals(date))  // Remove the date if it matches
            .collect(Collectors.toList());

        return dateList.stream()
            .map(LocalDate::toString)
            .collect(Collectors.joining(", "));  // Return the updated string
    }

    // Static method to check if a date exists
    public static boolean dateExists(String existingDates, LocalDate date) {
        List<LocalDate> dateList = getDateList(existingDates);
        return dateList.contains(date);
    }

    // Helper method to convert the dates string to a list of LocalDate objects
    public static List<LocalDate> getDateList(String dates) {
        if (dates == null || dates.isEmpty()) {
            return List.of();
        }

        return Arrays.stream(dates.split(", "))
            .map(LocalDate::parse)
            .collect(Collectors.toList());
    }

    //TODO: this method is a repetition of a method in ManageRecurringEventView - move it to a shared place
    public static boolean isLocalTimeTextValid(String text) {
        try {
            LocalTime.parse(text);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}