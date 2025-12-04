package one.modality.hotel.backoffice.activities.household.gantt.presenter;

import one.modality.hotel.backoffice.activities.household.gantt.model.DateSegment;
import one.modality.hotel.backoffice.activities.household.gantt.model.GanttBookingData;

import java.time.LocalDate;
import java.util.List;

/**
 * Utility class for checking booking date ranges with gap support.
 * Centralizes logic for determining if a booking is active on a given date.
 *
 * @author Claude Code Assistant
 */
public final class BookingDateUtil {

    private BookingDateUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Checks if a booking is active on a given date, considering date segments for gap bookings.
     * <p>
     * For bookings without gaps: checks if date is in [startDate, endDate)
     * For bookings with gaps: checks if date falls within any of the date segments (inclusive)
     * <p>
     * This method properly handles attendance gaps, showing interruptions in the Gantt bars.
     *
     * @param booking The booking to check
     * @param date The date to check
     * @return true if the guest is present on this date (accounting for gaps)
     */
    public static boolean isBookingActiveOnDate(GanttBookingData booking, LocalDate date) {
        // Get date segments (handles both gap and non-gap bookings)
        List<DateSegment> segments = booking.getDateSegments();

        if (segments == null || segments.isEmpty()) {
            // Fallback to simple date range check if no segments available
            return !date.isBefore(booking.getStartDate()) && !date.isAfter(booking.getEndDate());
        }

        // Check if date falls within any segment
        for (DateSegment segment : segments) {
            // Segment uses inclusive interval: [startDate, endDate]
            // Guest is present on date if: startDate <= date <= endDate
            if (!date.isBefore(segment.startDate()) && !date.isAfter(segment.endDate())) {
                return true;
            }
        }

        return false;
    }
}
