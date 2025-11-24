package one.modality.hotel.backoffice.activities.household.dashboard.model;

/**
 * Data transfer object for arrival/check-in cards.
 *
 * @author Claude Code Assistant
 */
public class ArrivalCardData {
    private final String roomName;
    private final String buildingName;
    private final String guestName;
    private final String eventName;
    private final String specialRequests;
    private final Object documentLine;

    public ArrivalCardData(String roomName, String buildingName, String guestName,
                           String eventName, String specialRequests, Object documentLine) {
        this.roomName = roomName;
        this.buildingName = buildingName;
        this.guestName = guestName;
        this.eventName = eventName;
        this.specialRequests = specialRequests;
        this.documentLine = documentLine;
    }

    public String getRoomName() { return roomName; }
    public String getBuildingName() { return buildingName; }
    public String getGuestName() { return guestName; }
    public String getEventName() { return eventName; }
    public String getSpecialRequests() { return specialRequests; }
    public Object getDocumentLine() { return documentLine; }

    public boolean hasSpecialRequests() {
        return specialRequests != null && !specialRequests.trim().isEmpty();
    }
}
