package one.modality.hotel.backoffice.activities.household.gantt.model;

/**
 * Enum representing the position of a booking bar within its span
 */
public enum BookingPosition {
    ARRIVAL,    // First day of booking
    MIDDLE,     // Days between arrival and departure
    DEPARTURE,  // Last day of booking
    SINGLE      // Single-day booking
}
