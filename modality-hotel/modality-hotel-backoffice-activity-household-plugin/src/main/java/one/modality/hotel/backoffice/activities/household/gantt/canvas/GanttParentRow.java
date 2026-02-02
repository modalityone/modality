package one.modality.hotel.backoffice.activities.household.gantt.canvas;

import one.modality.hotel.backoffice.activities.household.gantt.model.GanttBedData;
import one.modality.hotel.backoffice.activities.household.gantt.model.GanttRoomData;

/**
 * Wrapper class that represents a parent row in the Gantt chart.
 * Can be either a Room (for single rooms or collapsed multi-bed rooms)
 * or a Bed (for expanded multi-bed rooms).
 * <p>
 * This allows beds to appear in the same column as room names by making them parents.
 *
 * @author Claude Code Assistant
 */
public record GanttParentRow(GanttRoomData room, GanttBedData bed, boolean isBed, boolean expanded) {
    /**
     * Creates a room parent row.
     */
    public static GanttParentRow forRoom(GanttRoomData room, boolean expanded) {
        return new GanttParentRow(room, null, false, expanded);
    }

    /**
     * Creates a bed parent row.
     */
    public static GanttParentRow forBed(GanttRoomData room, GanttBedData bed) {
        return new GanttParentRow(room, bed, true, false);
    }

    // For bed rows, the index (0-based) of the bed

    public String getCategory() {
        return room.getCategory();
    }

    /**
     * Returns the zone name for zone-based grouping.
     * Returns "No Zone" if the room has no zone assigned.
     */
    public String getZone() {
        String zoneName = room.getZoneName();
        return zoneName != null && !zoneName.isEmpty() ? zoneName : "No Zone";
    }

    /**
     * Returns true if this is an overbooking bed row.
     */
    public boolean isOverbooking() {
        return isBed && bed != null && bed.isOverbooking();
    }

    /**
     * Returns true if this is a multi-bed room (not a bed row itself).
     */
    public boolean isMultiBedRoom() {
        return !isBed && !room.getBeds().isEmpty();
    }

    @Override
    public String toString() {
        if (isBed) {
            return "GanttParentRow[BED:" + (bed != null ? bed.getName() : "null") + " in room " + room.getName() + "]";
        } else {
            return "GanttParentRow[ROOM:" + room.getName() + ", beds=" + room.getBeds().size() +
                   ", zone=" + getZone() + ", category=" + getCategory() + ", expanded=" + expanded + "]";
        }
    }
}
