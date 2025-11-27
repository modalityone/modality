package one.modality.hotel.backoffice.activities.household.dashboard.model;

import one.modality.base.shared.entities.DocumentLine;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Data transfer object for checkout cards.
 * Supports both single checkouts and grouped checkouts (multiple guests in same room).
 *
 * @author Claude Code Assistant
 */
public class CheckoutCardData {
    private final String roomName;
    private final String buildingName;
    private final String guestName;
    private final boolean hasSameDayArrival;
    private final DocumentLine documentLine;
    private final List<DocumentLine> documentLines; // For grouped checkouts

    /**
     * Constructor for single checkout.
     */
    public CheckoutCardData(String roomName, String buildingName, String guestName,
                            boolean hasSameDayArrival, DocumentLine documentLine) {
        this.roomName = roomName;
        this.buildingName = buildingName;
        this.guestName = guestName;
        this.hasSameDayArrival = hasSameDayArrival;
        this.documentLine = documentLine;
        this.documentLines = null;
    }

    /**
     * Constructor for grouped checkout (multiple guests in same room).
     */
    public CheckoutCardData(String roomName, String buildingName, List<DocumentLine> documentLines,
                            boolean hasSameDayArrival) {
        this.roomName = roomName;
        this.buildingName = buildingName;
        this.documentLines = documentLines;
        this.hasSameDayArrival = hasSameDayArrival;
        // Generate newline-separated names for display (null-safe)
        this.guestName = documentLines.stream()
                .filter(dl -> dl != null && dl.getDocument() != null)
                .map(dl -> dl.getDocument().getFullName())
                .collect(Collectors.joining("\n"));
        this.documentLine = null;
    }

    public String getRoomName() { return roomName; }
    public String getBuildingName() { return buildingName; }
    public String getGuestName() { return guestName; }
    public boolean hasSameDayArrival() { return hasSameDayArrival; }
    public DocumentLine getDocumentLine() { return documentLine; }
    public List<DocumentLine> getDocumentLines() { return documentLines; }
    public boolean isGrouped() { return documentLines != null && !documentLines.isEmpty(); }

    /**
     * Checks if any guest in this checkout has special requests/needs.
     */
    public boolean hasSpecialRequests() {
        if (isGrouped()) {
            return documentLines.stream()
                    .filter(dl -> dl != null && dl.getDocument() != null)
                    .anyMatch(dl -> {
                        String request = dl.getDocument().getRequest();
                        return request != null && !request.trim().isEmpty();
                    });
        } else if (documentLine != null && documentLine.getDocument() != null) {
            String request = documentLine.getDocument().getRequest();
            return request != null && !request.trim().isEmpty();
        }
        return false;
    }
}
