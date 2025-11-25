package one.modality.hotel.backoffice.activities.household.gantt.canvas;

import one.modality.hotel.backoffice.activities.household.gantt.model.GanttBedData;
import one.modality.hotel.backoffice.activities.household.gantt.model.GanttRoomData;

/**
 * Wrapper class that represents a parent row in the Gantt chart.
 * Can be either a Room (for single rooms or collapsed multi-bed rooms)
 * or a Bed (for expanded multi-bed rooms).
 *
 * This allows beds to appear in the same column as room names by making them parents.
 *
 * @author Claude Code Assistant
 */
public class GanttParentRow {
    private final GanttRoomData room;
    private final GanttBedData bed;
    private final boolean isBed;
    private final int bedIndex; // For bed rows, the index (0-based) of the bed

    /**
     * Creates a room parent row.
     */
    public static GanttParentRow forRoom(GanttRoomData room) {
        return new GanttParentRow(room, null, false, -1);
    }

    /**
     * Creates a bed parent row.
     */
    public static GanttParentRow forBed(GanttRoomData room, GanttBedData bed, int bedIndex) {
        return new GanttParentRow(room, bed, true, bedIndex);
    }

    private GanttParentRow(GanttRoomData room, GanttBedData bed, boolean isBed, int bedIndex) {
        this.room = room;
        this.bed = bed;
        this.isBed = isBed;
        this.bedIndex = bedIndex;
    }

    public GanttRoomData getRoom() {
        return room;
    }

    public GanttBedData getBed() {
        return bed;
    }

    public boolean isBed() {
        return isBed;
    }

    public int getBedIndex() {
        return bedIndex;
    }

    public String getCategory() {
        return room.getCategory();
    }

    /**
     * Returns the display name for this parent row.
     * For rooms: room name
     * For beds: "  ↳ Bed A" (indented with bed letter), or "  ↳ Overbooking" for overbooking beds
     */
    public String getDisplayName() {
        if (isBed) {
            if (bed.isOverbooking()) {
                return "  ↳ Overbooking";
            }
            char bedLetter = (char) ('A' + bedIndex);
            return "  ↳ Bed " + bedLetter;
        }
        return room.getName();
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
}
