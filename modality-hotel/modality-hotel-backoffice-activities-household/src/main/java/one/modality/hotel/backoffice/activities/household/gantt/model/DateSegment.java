package one.modality.hotel.backoffice.activities.household.gantt.model;

import java.time.LocalDate;

/**
 * Represents a continuous date segment within a booking.
 * Used to support bookings with attendance gaps where the guest
 * doesn't stay on certain nights.
 *
 * For example, a booking from June 14-25 with gaps on June 16-17 would have:
 * - Segment 1: June 14-16 (guest leaves morning of 16th)
 * - Segment 2: June 18-25 (guest returns on 18th)
 */
public final class DateSegment {

    private final LocalDate startDate;
    private final LocalDate endDate;

    public DateSegment(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * Gets the start date of this segment (check-in date for this segment).
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * Gets the end date of this segment (check-out date for this segment).
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    @Override
    public String toString() {
        return "DateSegment[" + startDate + " to " + endDate + "]";
    }
}
