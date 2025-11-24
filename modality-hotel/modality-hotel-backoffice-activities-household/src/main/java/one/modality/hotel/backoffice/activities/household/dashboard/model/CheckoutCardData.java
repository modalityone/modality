package one.modality.hotel.backoffice.activities.household.dashboard.model;

/**
 * Data transfer object for checkout cards.
 *
 * @author Claude Code Assistant
 */
public class CheckoutCardData {
    private final String roomName;
    private final String buildingName;
    private final String guestName;
    private final boolean hasSameDayArrival;
    private final Object documentLine;

    public CheckoutCardData(String roomName, String buildingName, String guestName,
                            boolean hasSameDayArrival, Object documentLine) {
        this.roomName = roomName;
        this.buildingName = buildingName;
        this.guestName = guestName;
        this.hasSameDayArrival = hasSameDayArrival;
        this.documentLine = documentLine;
    }

    public String getRoomName() { return roomName; }
    public String getBuildingName() { return buildingName; }
    public String getGuestName() { return guestName; }
    public boolean hasSameDayArrival() { return hasSameDayArrival; }
    public Object getDocumentLine() { return documentLine; }
}
