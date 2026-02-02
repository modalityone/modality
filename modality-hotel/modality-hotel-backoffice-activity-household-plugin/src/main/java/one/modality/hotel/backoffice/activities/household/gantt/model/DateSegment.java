package one.modality.hotel.backoffice.activities.household.gantt.model;

import java.time.LocalDate;

/**
 * Represents a continuous date segment within a booking.
 * Used to support bookings with attendance gaps where the guest
 * doesn't stay on certain nights.
 * <p>
 * For example, a booking from June 14-25 with gaps on June 16-17 would have:
 * - Segment 1: June 14-16 (guest leaves morning of 16th)
 * - Segment 2: June 18-25 (guest returns on 18th)
 */
public record DateSegment(LocalDate startDate, LocalDate endDate) {

    /**
     * Gets the start date of this segment (check-in date for this segment).
     */
    @Override
    public LocalDate startDate() {
        return startDate;
    }

    /**
     * Gets the end date of this segment (check-out date for this segment).
     */
    @Override
    public LocalDate endDate() {
        return endDate;
    }

    @Override
    public String toString() {
        return "DateSegment[" + startDate + " to " + endDate + "]";
    }
}
