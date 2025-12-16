package one.modality.hotel.backoffice.activities.household.dashboard.model;

import one.modality.base.shared.entities.DocumentLine;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Data transfer object for arrival/check-in cards.
 * Supports both single arrivals and grouped arrivals (multiple guests in same room).
 *
 * @author Claude Code Assistant
 */
public class ArrivalCardData {
    private final String roomName;
    private final String buildingName;
    private final String guestName;
    private final String eventName;
    private final String specialRequests;
    private final DocumentLine documentLine;
    private final List<DocumentLine> documentLines; // For grouped arrivals

    /**
     * Constructor for single arrival.
     */
    public ArrivalCardData(String roomName, String buildingName, String guestName,
                           String eventName, String specialRequests, DocumentLine documentLine) {
        this.roomName = roomName;
        this.buildingName = buildingName;
        this.guestName = guestName;
        this.eventName = eventName;
        this.specialRequests = specialRequests;
        this.documentLine = documentLine;
        this.documentLines = null;
    }

    /**
     * Constructor for grouped arrivals (multiple guests in same room).
     */
    public ArrivalCardData(String roomName, String buildingName, List<DocumentLine> documentLines,
                           String eventName) {
        this.roomName = roomName;
        this.buildingName = buildingName;
        this.documentLines = documentLines;
        this.eventName = eventName;
        // Generate newline-separated names for display (null-safe)
        this.guestName = documentLines.stream()
                .filter(dl -> dl != null && dl.getDocument() != null)
                .map(dl -> dl.getDocument().getFullName())
                .collect(Collectors.joining("\n"));
        // These are not used for grouped arrivals
        this.specialRequests = null;
        this.documentLine = null;
    }

    public String getRoomName() { return roomName; }
    public String getBuildingName() { return buildingName; }
    public String getGuestName() { return guestName; }
    public String getEventName() { return eventName; }
    public String getSpecialRequests() { return specialRequests; }
    public DocumentLine getDocumentLine() { return documentLine; }
    public List<DocumentLine> getDocumentLines() { return documentLines; }
    public boolean isGrouped() { return documentLines != null && !documentLines.isEmpty(); }

    public boolean hasSpecialRequests() {
        if (documentLines != null) {
            // For grouped arrivals, check if any guest has special requests (null-safe)
            return documentLines.stream()
                    .filter(dl -> dl != null && dl.getDocument() != null)
                    .anyMatch(dl -> {
                        String request = dl.getDocument().getRequest();
                        return request != null && !request.trim().isEmpty();
                    });
        }
        return specialRequests != null && !specialRequests.trim().isEmpty();
    }
}
