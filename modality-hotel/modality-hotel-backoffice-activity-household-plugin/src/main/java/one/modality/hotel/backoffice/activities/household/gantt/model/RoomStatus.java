package one.modality.hotel.backoffice.activities.household.gantt.model;

/**
 * Room status enumeration matching the design specification
 */
public enum RoomStatus {
    OCCUPIED,    // Red dot - guests in room
    TO_CLEAN,    // Orange dot - needs housekeeping
    TO_INSPECT,  // Gold dot - awaiting inspection
    READY        // Green dot - available for guests
}
