package one.modality.hotel.backoffice.activities.household.gantt.model;

/**
 * Booking status enumeration matching the design specification
 */
public enum BookingStatus {
    CONFIRMED,    // Blue - future booking
    OCCUPIED,     // Red - guest currently in room (arrived)
    DEPARTED,     // Gray - guest has left
    UNCONFIRMED   // Blue with question mark - tentative
}
