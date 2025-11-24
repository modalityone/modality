package one.modality.hotel.backoffice.activities.household.gantt.presenter;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import one.modality.hotel.backoffice.activities.household.gantt.model.GanttRoomData;
import one.modality.hotel.backoffice.activities.household.gantt.model.RoomStatus;
import one.modality.hotel.backoffice.activities.household.gantt.model.RoomType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Manages filtering logic for Gantt calendar rooms.
 * Tracks active filter states and applies filters to room list.
 *
 * @author Claude Code Assistant
 */
public class GanttFilterManager {

    // Active filter sets (empty = show all)
    private final ObservableSet<RoomStatus> activeStatusFilters = FXCollections.observableSet(new HashSet<>());
    private final ObservableSet<RoomType> activeRoomTypeFilters = FXCollections.observableSet(new HashSet<>());

    /**
     * Toggles a status filter on/off
     */
    public void toggleStatusFilter(RoomStatus status) {
        if (activeStatusFilters.contains(status)) {
            activeStatusFilters.remove(status);
        } else {
            activeStatusFilters.add(status);
        }
    }

    /**
     * Toggles a room type filter on/off
     */
    public void toggleRoomTypeFilter(RoomType roomType) {
        if (activeRoomTypeFilters.contains(roomType)) {
            activeRoomTypeFilters.remove(roomType);
        } else {
            activeRoomTypeFilters.add(roomType);
        }
    }

    /**
     * Clears all active filters
     */
    public void clearAllFilters() {
        activeStatusFilters.clear();
        activeRoomTypeFilters.clear();
    }

    /**
     * Checks if a status filter is active
     */
    public boolean isStatusFilterActive(RoomStatus status) {
        return activeStatusFilters.contains(status);
    }

    /**
     * Checks if a room type filter is active
     */
    public boolean isRoomTypeFilterActive(RoomType roomType) {
        return activeRoomTypeFilters.contains(roomType);
    }

    /**
     * Applies active filters to a room list
     */
    public List<GanttRoomData> applyFilters(List<GanttRoomData> rooms) {
        return rooms.stream()
                .filter(this::passesFilters)
                .collect(Collectors.toList());
    }

    /**
     * Checks if a room passes all active filters
     */
    private boolean passesFilters(GanttRoomData room) {
        // If no filters active, show all
        boolean passesStatusFilter = activeStatusFilters.isEmpty() || activeStatusFilters.contains(room.getStatus());
        boolean passesRoomTypeFilter = activeRoomTypeFilters.isEmpty() || activeRoomTypeFilters.contains(room.getRoomType());

        return passesStatusFilter && passesRoomTypeFilter;
    }

    /**
     * Gets observable set of active status filters (for UI binding)
     */
    public ObservableSet<RoomStatus> getActiveStatusFilters() {
        return activeStatusFilters;
    }

    /**
     * Gets observable set of active room type filters (for UI binding)
     */
    public ObservableSet<RoomType> getActiveRoomTypeFilters() {
        return activeRoomTypeFilters;
    }

    /**
     * Checks if any filters are active
     */
    public boolean hasActiveFilters() {
        return !activeStatusFilters.isEmpty() || !activeRoomTypeFilters.isEmpty();
    }
}
