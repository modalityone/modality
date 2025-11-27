package one.modality.hotel.backoffice.activities.household.gantt.model;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * Interface representing booking data for the Gantt view.
 *
 * Supports bookings with attendance gaps (e.g., guest books June 14-25 but
 * doesn't stay on June 16-17). In such cases, getDateSegments() returns
 * multiple segments instead of a single continuous date range.
 */
public interface GanttBookingData {
    String getGuestName();
    String getFirstName();
    String getLastName();
    String getGender();
    String getEvent();
    LocalDate getStartDate();
    LocalDate getEndDate();
    BookingStatus getStatus();
    String getComments();
    List<String> getSpecialNeeds();

    /**
     * Returns true if the guest has been marked as arrived (checked in).
     */
    boolean isArrived();

    /**
     * Returns true if this booking has attendance gaps (nights when guest doesn't stay).
     * When true, getDateSegments() should be used instead of getStartDate()/getEndDate()
     * for accurate bar rendering.
     */
    default boolean hasAttendanceGaps() {
        return false;
    }

    /**
     * Returns the date segments for this booking.
     *
     * For bookings WITHOUT gaps: Returns a single segment [startDate, endDate]
     * For bookings WITH gaps: Returns multiple segments representing actual stay periods
     *
     * The Gantt bar renderer should draw one bar segment per DateSegment,
     * leaving blank space between segments to show the gaps.
     */
    default List<DateSegment> getDateSegments() {
        // Default implementation: single continuous segment
        LocalDate start = getStartDate();
        LocalDate end = getEndDate();
        if (start == null || end == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new DateSegment(start, end));
    }
}
