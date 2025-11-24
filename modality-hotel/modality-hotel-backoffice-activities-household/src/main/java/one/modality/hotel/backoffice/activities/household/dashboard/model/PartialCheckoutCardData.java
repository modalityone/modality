package one.modality.hotel.backoffice.activities.household.dashboard.model;

/**
 * Data transfer object for partial checkout cards.
 * Represents situations where some guests leave while others remain.
 *
 * @author Claude Code Assistant
 */
public class PartialCheckoutCardData {
    private final String roomName;
    private final String buildingName;
    private final String checkingOut;
    private final String remaining;

    public PartialCheckoutCardData(String roomName, String buildingName,
                                   String checkingOut, String remaining) {
        this.roomName = roomName;
        this.buildingName = buildingName;
        this.checkingOut = checkingOut;
        this.remaining = remaining;
    }

    public String getRoomName() { return roomName; }
    public String getBuildingName() { return buildingName; }
    public String getCheckingOut() { return checkingOut; }
    public String getRemaining() { return remaining; }
}
