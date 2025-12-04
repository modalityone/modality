package one.modality.hotel.backoffice.activities.household.dashboard.presenter;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import one.modality.hotel.backoffice.activities.household.dashboard.model.RoomCardData;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages filtering logic for dashboard cards.
 * Centralizes filter state and application.
 *
 * @author Claude Code Assistant
 */
public class DashboardFilterManager {

    // Filter Properties for "To Clean" section
    private final ObjectProperty<String> cleanStatusFilter = new SimpleObjectProperty<>("All");
    private final ObjectProperty<String> cleanRoomTypeFilter = new SimpleObjectProperty<>("All");
    private final ObjectProperty<String> cleanBuildingFilter = new SimpleObjectProperty<>("All");
    private final ObjectProperty<String> cleanCheckInFilter = new SimpleObjectProperty<>("All");

    // Filter Properties for "To Inspect" section
    private final ObjectProperty<String> inspectBuildingFilter = new SimpleObjectProperty<>("All");
    private final ObjectProperty<String> inspectCheckInFilter = new SimpleObjectProperty<>("All");

    public ObjectProperty<String> cleanStatusFilterProperty() { return cleanStatusFilter; }
    public ObjectProperty<String> cleanRoomTypeFilterProperty() { return cleanRoomTypeFilter; }
    public ObjectProperty<String> cleanBuildingFilterProperty() { return cleanBuildingFilter; }
    public ObjectProperty<String> cleanCheckInFilterProperty() { return cleanCheckInFilter; }
    public ObjectProperty<String> inspectBuildingFilterProperty() { return inspectBuildingFilter; }
    public ObjectProperty<String> inspectCheckInFilterProperty() { return inspectCheckInFilter; }

    /**
     * Applies cleaning filters to room cards
     */
    public List<RoomCardData> applyCleaningFilters(List<RoomCardData> cards) {
        return cards.stream()
                .filter(card -> {
                    // Status filter
                    if ("Ready".equals(cleanStatusFilter.get()) && card.status().name().equals("TO_CLEAN"))
                        return false;
                    if ("Pending".equals(cleanStatusFilter.get()) && card.checkoutComplete())
                        return false;

                    // Room type filter
                    if (!"All".equals(cleanRoomTypeFilter.get())) {
                        String filterRoomType = cleanRoomTypeFilter.get();
                        if (!card.buildingName().contains(filterRoomType))
                            return false;
                    }

                    // Building filter
                    if (!"All".equals(cleanBuildingFilter.get())) {
                        String filterBuilding = cleanBuildingFilter.get();
                        if (!card.buildingName().contains(filterBuilding))
                            return false;
                    }

                    // Check-in filter
                    if ("Today".equals(cleanCheckInFilter.get()) && !card.sameDayNextCheckin())
                        return false;
                    return !"Tomorrow".equals(cleanCheckInFilter.get()) || card.tomorrowNextCheckin();
                })
                .collect(Collectors.toList());
    }

    /**
     * Applies inspection filters to room cards
     */
    public List<RoomCardData> applyInspectionFilters(List<RoomCardData> cards) {
        return cards.stream()
                .filter(card -> {
                    // Building filter
                    if (!"All".equals(inspectBuildingFilter.get())) {
                        String filterBuilding = inspectBuildingFilter.get();
                        if (!card.buildingName().contains(filterBuilding))
                            return false;
                    }

                    // Check-in filter
                    if ("Today".equals(inspectCheckInFilter.get()) && !card.sameDayNextCheckin())
                        return false;
                    return !"Tomorrow".equals(inspectCheckInFilter.get()) || card.tomorrowNextCheckin();
                })
                .collect(Collectors.toList());
    }
}
