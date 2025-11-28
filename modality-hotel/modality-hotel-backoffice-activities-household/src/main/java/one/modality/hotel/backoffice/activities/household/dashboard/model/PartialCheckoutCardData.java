package one.modality.hotel.backoffice.activities.household.dashboard.model;

import one.modality.base.shared.entities.DocumentLine;

import java.util.List;
import java.util.stream.Collectors;

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
    private final List<DocumentLine> checkingOutDocumentLines;

    /**
     * Constructor with DocumentLine list for detailed guest information.
     */
    public PartialCheckoutCardData(String roomName, String buildingName,
                                   List<DocumentLine> checkingOutDocumentLines, String remaining) {
        this.roomName = roomName;
        this.buildingName = buildingName;
        this.checkingOutDocumentLines = checkingOutDocumentLines;
        this.remaining = remaining;
        // Generate newline-separated names for display (null-safe)
        this.checkingOut = checkingOutDocumentLines.stream()
                .filter(dl -> dl != null && dl.getDocument() != null)
                .map(dl -> dl.getDocument().getFullName())
                .collect(Collectors.joining("\n"));
    }

    public String getRoomName() { return roomName; }
    public String getBuildingName() { return buildingName; }
    public String getCheckingOut() { return checkingOut; }
    public String getRemaining() { return remaining; }
    public List<DocumentLine> getCheckingOutDocumentLines() { return checkingOutDocumentLines; }
    public int getCheckingOutCount() { return checkingOutDocumentLines != null ? checkingOutDocumentLines.size() : 0; }
}
