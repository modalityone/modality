package one.modality.hotel.backoffice.activities.household.gantt.model;

import java.util.List;

/**
 * Interface representing bed data for multi-bed rooms in the Gantt view.
 *
 * @author Claude Code Assistant
 */
public interface GanttBedData {
    String getId();
    String getName();
    RoomStatus getStatus();
    List<? extends GanttBookingData> getBookings();

    /**
     * Returns true if this bed represents an overbooking situation.
     * Overbooking beds should be displayed with a danger/warning background.
     */
    default boolean isOverbooking() {
        return false;
    }
}
