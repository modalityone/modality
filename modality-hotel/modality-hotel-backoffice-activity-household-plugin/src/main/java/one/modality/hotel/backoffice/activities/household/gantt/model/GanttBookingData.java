package one.modality.hotel.backoffice.activities.household.gantt.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Interface representing booking data for the Gantt view.
 * <p>
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
     * Returns the date segments for this booking.
     * For bookings without gaps, returns a single segment covering [startDate, endDate).
     * For bookings with gaps, returns multiple segments representing the actual stay periods.
     * <p>
     * Example with gap:
     * - Overall booking: June 14-25
     * - Gap: June 16-17 (guest leaves)
     * - Segments: [June 14-16), [June 18-25)
     *
     * @return List of date segments, never null, always at least one segment for valid bookings
     */
    List<DateSegment> getDateSegments();

}
