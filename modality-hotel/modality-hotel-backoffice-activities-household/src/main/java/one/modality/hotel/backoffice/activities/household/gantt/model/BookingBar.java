package one.modality.hotel.backoffice.activities.household.gantt.model;

/**
 * Value object representing a booking bar to be rendered.
 * Contains all information needed to display a booking bar without UI coupling.
 *
 * @param hasLateArrival Guest should have arrived but hasn't been marked as arrived
 * @param bookingData    Reference to full booking data for tooltips
 */
public record BookingBar(BookingStatus status, BookingPosition position, int occupancy, int totalCapacity,
                         boolean hasConflict, String guestInfo, boolean hasComments, boolean hasTurnover,
                         boolean hasLateArrival, GanttBookingData bookingData) {
    public BookingBar(BookingStatus status, BookingPosition position, int occupancy, int totalCapacity,
                      boolean hasConflict, String guestInfo, boolean hasComments, boolean hasTurnover) {
        this(status, position, occupancy, totalCapacity, hasConflict, guestInfo, hasComments, hasTurnover, false, null);
    }

    public BookingBar(BookingStatus status, BookingPosition position, int occupancy, int totalCapacity,
                      boolean hasConflict, String guestInfo, boolean hasComments, boolean hasTurnover,
                      GanttBookingData bookingData) {
        this(status, position, occupancy, totalCapacity, hasConflict, guestInfo, hasComments, hasTurnover, false, bookingData);
    }

}
