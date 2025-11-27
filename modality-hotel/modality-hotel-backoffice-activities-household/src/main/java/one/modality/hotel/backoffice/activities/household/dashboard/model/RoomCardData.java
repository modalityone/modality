package one.modality.hotel.backoffice.activities.household.dashboard.model;

import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.Resource;

import java.time.LocalDate;

/**
 * Data transfer object for room cards (cleaning/inspection).
 * Represents a room that needs cleaning or inspection.
 * <p>
 * The Resource entity (accessed via ResourceConfiguration.getResource()) stores:
 * - lastCleaningDate: when the room was last marked as cleaned
 * - lastInspectionDate: when the room was last marked as inspected
 *
 * @author Claude Code Assistant
 */
public record RoomCardData(String roomName, String buildingName, String guestName, String eventName,
                           RoomCardStatus status, boolean checkoutComplete, LocalDate nextCheckinDate,
                           boolean sameDayNextCheckin, boolean tomorrowNextCheckin, DocumentLine documentLine,
                           Resource resource) {
}
