package one.modality.hotel.backoffice.activities.household.gantt.model;

/**
 * Value object representing a booking bar to be rendered.
 * Contains all information needed to display a booking bar without UI coupling.
 */
public class BookingBar {
    private final BookingStatus status;
    private final BookingPosition position;
    private final int occupancy;
    private final int totalCapacity;
    private final boolean hasConflict;
    private final String guestInfo;
    private final boolean hasComments;
    private final boolean hasTurnover;
    private final boolean hasLateArrival; // Guest should have arrived but hasn't been marked as arrived
    private final GanttBookingData bookingData; // Reference to full booking data for tooltips

    public BookingBar(BookingStatus status, BookingPosition position, int occupancy, int totalCapacity,
                      boolean hasConflict, String guestInfo) {
        this(status, position, occupancy, totalCapacity, hasConflict, guestInfo, false, false, false, null);
    }

    public BookingBar(BookingStatus status, BookingPosition position, int occupancy, int totalCapacity,
                      boolean hasConflict, String guestInfo, boolean hasComments, boolean hasTurnover) {
        this(status, position, occupancy, totalCapacity, hasConflict, guestInfo, hasComments, hasTurnover, false, null);
    }

    public BookingBar(BookingStatus status, BookingPosition position, int occupancy, int totalCapacity,
                      boolean hasConflict, String guestInfo, boolean hasComments, boolean hasTurnover,
                      GanttBookingData bookingData) {
        this(status, position, occupancy, totalCapacity, hasConflict, guestInfo, hasComments, hasTurnover, false, bookingData);
    }

    public BookingBar(BookingStatus status, BookingPosition position, int occupancy, int totalCapacity,
                      boolean hasConflict, String guestInfo, boolean hasComments, boolean hasTurnover,
                      boolean hasLateArrival, GanttBookingData bookingData) {
        this.status = status;
        this.position = position;
        this.occupancy = occupancy;
        this.totalCapacity = totalCapacity;
        this.hasConflict = hasConflict;
        this.guestInfo = guestInfo;
        this.hasComments = hasComments;
        this.hasTurnover = hasTurnover;
        this.hasLateArrival = hasLateArrival;
        this.bookingData = bookingData;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public BookingPosition getPosition() {
        return position;
    }

    public int getOccupancy() {
        return occupancy;
    }

    public int getTotalCapacity() {
        return totalCapacity;
    }

    public boolean hasConflict() {
        return hasConflict;
    }

    public String getGuestInfo() {
        return guestInfo;
    }

    public boolean hasComments() {
        return hasComments;
    }

    public boolean hasTurnover() {
        return hasTurnover;
    }

    public boolean hasLateArrival() {
        return hasLateArrival;
    }

    public boolean isMultiOccupancy() {
        return totalCapacity > 1;
    }

    public GanttBookingData getBookingData() {
        return bookingData;
    }
}
