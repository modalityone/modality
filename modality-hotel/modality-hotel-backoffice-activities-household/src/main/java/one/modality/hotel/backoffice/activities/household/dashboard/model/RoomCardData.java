package one.modality.hotel.backoffice.activities.household.dashboard.model;

import java.time.LocalDate;

/**
 * Data transfer object for room cards (cleaning/inspection).
 * Represents a room that needs cleaning or inspection.
 *
 * @author Claude Code Assistant
 */
public class RoomCardData {
    private final String roomName;
    private final String buildingName;
    private final String guestName;
    private final String eventName;
    private final RoomCardStatus status;
    private final boolean checkoutComplete;
    private final LocalDate nextCheckinDate;
    private final boolean sameDayNextCheckin;
    private final boolean tomorrowNextCheckin;
    private final Object documentLine; // Database entity reference

    public RoomCardData(String roomName, String buildingName, String guestName, String eventName,
                        RoomCardStatus status, boolean checkoutComplete, LocalDate nextCheckinDate,
                        boolean sameDayNextCheckin, boolean tomorrowNextCheckin, Object documentLine) {
        this.roomName = roomName;
        this.buildingName = buildingName;
        this.guestName = guestName;
        this.eventName = eventName;
        this.status = status;
        this.checkoutComplete = checkoutComplete;
        this.nextCheckinDate = nextCheckinDate;
        this.sameDayNextCheckin = sameDayNextCheckin;
        this.tomorrowNextCheckin = tomorrowNextCheckin;
        this.documentLine = documentLine;
    }

    public String getRoomName() { return roomName; }
    public String getBuildingName() { return buildingName; }
    public String getGuestName() { return guestName; }
    public String getEventName() { return eventName; }
    public RoomCardStatus getStatus() { return status; }
    public boolean isCheckoutComplete() { return checkoutComplete; }
    public LocalDate getNextCheckinDate() { return nextCheckinDate; }
    public boolean isSameDayNextCheckin() { return sameDayNextCheckin; }
    public boolean isTomorrowNextCheckin() { return tomorrowNextCheckin; }
    public Object getDocumentLine() { return documentLine; }
}
