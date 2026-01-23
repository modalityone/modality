package one.modality.booking.frontoffice.bookingpage.util;

import dev.webfx.stack.orm.entity.Entities;
import one.modality.base.client.time.ModalityDates;
import one.modality.base.shared.entities.*;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.ecommerce.document.service.DocumentAggregate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Utility class for formatting dates associated with booking document lines.
 * Centralizes date computation and formatting logic used across booking forms.
 *
 * <p>Uses {@link ModalityDates} for the actual date series formatting to ensure
 * consistent date display throughout the application.</p>
 *
 * @author Bruno Salmon
 */
public final class BookingDateFormatter {

    private BookingDateFormatter() {
        // Utility class - prevent instantiation
    }

    /**
     * Computes a formatted dates string from the attendances associated with a document line
     * within a WorkingBooking context.
     *
     * @param workingBooking The working booking containing attendance data
     * @param line The document line to get dates for
     * @return Formatted date string (e.g., "15/01-20/01") or null if no dates found
     */
    public static String computeDatesFromAttendances(WorkingBooking workingBooking, DocumentLine line) {
        if (workingBooking == null || line == null) return null;

        // Get attendances using robust matching (by Site+Item primary keys)
        List<Attendance> lineAttendances = getLineAttendancesRobust(workingBooking.getLastestDocumentAggregate(), line);
        if (lineAttendances == null || lineAttendances.isEmpty()) return null;

        // Collect and sort all dates
        List<LocalDate> dates = lineAttendances.stream()
            .map(Attendance::getDate)
            .filter(Objects::nonNull)
            .sorted()
            .distinct()
            .collect(Collectors.toList());

        // Use centralized formatting from ModalityDates
        return ModalityDates.formatDateSeries(dates);
    }

    /**
     * Computes formatted dates for a document line from a DocumentAggregate.
     * First checks for stored dates in the database, then computes from attendances.
     *
     * @param documentAggregate The document aggregate containing attendance data
     * @param line The document line to get dates for
     * @return Formatted date string or null if no dates found
     */
    public static String computeDatesForDocumentLine(DocumentAggregate documentAggregate, DocumentLine line) {
        if (documentAggregate == null || line == null) return null;

        // First check if dates are stored in the database
        String storedDates = line.getDates();
        if (storedDates != null && !storedDates.isEmpty()) {
            return storedDates;
        }

        // Compute dates from attendances
        List<LocalDate> dates = documentAggregate.getLineAttendancesStream(line)
            .map(a -> a.getScheduledItem() != null ? a.getScheduledItem().getDate() : null)
            .filter(Objects::nonNull)
            .sorted()
            .distinct()
            .collect(Collectors.toList());

        if (dates.isEmpty()) return null;

        // Use centralized formatting from ModalityDates
        return ModalityDates.formatDateSeries(dates);
    }

    /**
     * Gets attendances for a document line using robust matching.
     * First tries the standard getLineAttendances (which uses Objects.equals on DocumentLine).
     * If that returns empty, falls back to matching by Site+Item primary keys.
     *
     * <p>This handles cases where DocumentLine entities are in different EntityStores and
     * standard equals comparison fails even for logically equivalent lines.</p>
     *
     * @param documentAggregate The document aggregate containing attendances
     * @param line The document line to match
     * @return List of attendances for the line, or empty list if none found
     */
    public static List<Attendance> getLineAttendancesRobust(DocumentAggregate documentAggregate, DocumentLine line) {
        if (documentAggregate == null || line == null) return Collections.emptyList();

        // First try the standard method
        List<Attendance> attendances = documentAggregate.getLineAttendances(line);
        if (attendances != null && !attendances.isEmpty()) {
            return attendances;
        }

        // Fallback: match by Site+Item primary keys (more robust across EntityStores)
        Site lineSite = line.getSite();
        Item lineItem = line.getItem();
        if (lineSite == null || lineItem == null) return Collections.emptyList();

        Object lineSitePk = lineSite.getPrimaryKey();
        Object lineItemPk = lineItem.getPrimaryKey();
        if (lineSitePk == null || lineItemPk == null) return Collections.emptyList();

        return documentAggregate.getAttendances().stream()
            .filter(a -> {
                DocumentLine attLine = a.getDocumentLine();
                if (attLine == null) return false;
                Site attSite = attLine.getSite();
                Item attItem = attLine.getItem();
                if (attSite == null || attItem == null) return false;
                return Entities.samePrimaryKey(attSite, lineSite) && Entities.samePrimaryKey(attItem, lineItem);
            })
            .collect(Collectors.toList());
    }

    // ========================================
    // DATE RANGE UTILITIES
    // ========================================

    /**
     * Formats a date range for display.
     * <ul>
     *   <li>Single date: "Jul 1"</li>
     *   <li>Same month: "Jul 1-8"</li>
     *   <li>Different months: "Jul 1 - Aug 8"</li>
     * </ul>
     *
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return formatted date range string, or empty string if both null
     */
    public static String formatDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) return "";
        if (startDate == null) return formatSingleDate(endDate);
        if (endDate == null) return formatSingleDate(startDate);
        if (startDate.equals(endDate)) return formatSingleDate(startDate);

        DateTimeFormatter monthDay = DateTimeFormatter.ofPattern("MMM d");
        DateTimeFormatter dayOnly = DateTimeFormatter.ofPattern("d");

        if (startDate.getMonth() == endDate.getMonth() && startDate.getYear() == endDate.getYear()) {
            // Same month: "Jul 1-8"
            return startDate.format(monthDay) + "-" + endDate.format(dayOnly);
        } else {
            // Different months: "Jul 1 - Aug 8"
            return startDate.format(monthDay) + " - " + endDate.format(monthDay);
        }
    }

    /**
     * Formats a single date for display.
     *
     * @param date the date to format
     * @return formatted date string (e.g., "Jul 1"), or empty string if null
     */
    public static String formatSingleDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DateTimeFormatter.ofPattern("MMM d"));
    }

    /**
     * Builds a list of dates from start (inclusive) to end (exclusive).
     * Used for building lists of accommodation nights, meal days, etc.
     *
     * @param start the start date (inclusive)
     * @param end the end date (exclusive)
     * @return list of dates, or empty list if either date is null
     */
    public static List<LocalDate> buildDateList(LocalDate start, LocalDate end) {
        List<LocalDate> dates = new ArrayList<>();
        if (start != null && end != null && !start.isAfter(end)) {
            LocalDate current = start;
            while (current.isBefore(end)) {
                dates.add(current);
                current = current.plusDays(1);
            }
        }
        return dates;
    }

    /**
     * Counts the number of nights between two dates.
     *
     * @param arrival the arrival date
     * @param departure the departure date
     * @return number of nights, or 0 if either date is null
     */
    public static long countNights(LocalDate arrival, LocalDate departure) {
        if (arrival == null || departure == null) return 0;
        long nights = ChronoUnit.DAYS.between(arrival, departure);
        return Math.max(0, nights);
    }

    /**
     * Builds a list of dates from start (inclusive) to end (inclusive).
     *
     * @param start the start date (inclusive)
     * @param end the end date (inclusive)
     * @return list of dates, or empty list if either date is null
     */
    public static List<LocalDate> buildDateListInclusive(LocalDate start, LocalDate end) {
        if (start == null || end == null) return Collections.emptyList();
        return buildDateList(start, end.plusDays(1));
    }
}
