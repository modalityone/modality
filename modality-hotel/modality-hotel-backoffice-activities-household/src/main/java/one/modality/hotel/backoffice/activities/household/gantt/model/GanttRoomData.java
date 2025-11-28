package one.modality.hotel.backoffice.activities.household.gantt.model;

import one.modality.base.shared.entities.Resource;

import java.util.List;

/**
 * Interface representing room data for the Gantt view.
 * Decouples the view from specific data sources (sample data, database entities, etc.).
 *
 * @author Claude Code Assistant
 */
public interface GanttRoomData {
    String getId();
    String getName();
    String getCategory();
    RoomStatus getStatus();
    RoomType getRoomType();
    String getRoomComments();

    /**
     * Returns the Resource entity for this room.
     * Used to update lastCleaningDate and lastInspectionDate for housekeeping operations.
     * May return null if the room doesn't have an associated Resource entity.
     */
    Resource getResource();

    /**
     * Returns the actual room capacity from the database (max field).
     * This is the true number of beds, excluding overbooking beds.
     */
    int getCapacity();

    /**
     * For single rooms, returns bookings directly on the room.
     * For multi-bed rooms, returns empty list (use getBeds() instead).
     */
    List<? extends GanttBookingData> getBookings();

    /**
     * For multi-bed rooms, returns the list of beds.
     * For single rooms, returns empty list.
     */
    List<? extends GanttBedData> getBeds();
}
