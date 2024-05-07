package one.modality.event.frontoffice.activities.booking;

import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.ScheduledItem;

import java.time.LocalDateTime;
import java.util.List;

public class WorkingBooking {
    private List<ScheduledItem> scheduledItems;
    private List<DocumentLine> unscheduledLines;
    public List<ScheduledItem> getScheduledItems() {
        return scheduledItems;
    }

    public void setScheduledItems(List<ScheduledItem> scheduledItems) {
        this.scheduledItems = scheduledItems;
    }

    public List<DocumentLine> getUnscheduledLines() {
        return unscheduledLines;
    }

    public void setUnscheduledLines(List<DocumentLine> unscheduledLines) {
        this.unscheduledLines = unscheduledLines;
    }

    public LocalDateTime getArrivalDate() {
        if (scheduledItems== null || scheduledItems.isEmpty()) {
            return null;
        }
        LocalDateTime minDateTime = LocalDateTime.of(scheduledItems.get(0).getDate(), scheduledItems.get(0).getStartTime());
        for (ScheduledItem si : scheduledItems) {
            LocalDateTime currentDateTime = LocalDateTime.of(si.getDate(), si.getStartTime());
            if (currentDateTime.isBefore(minDateTime)) {
                minDateTime = currentDateTime;
            }
        }
        return minDateTime;
    }

    public LocalDateTime getDepartureDate() {
        if (scheduledItems== null || scheduledItems.isEmpty()) {
            return null;
        }
        LocalDateTime maxDateTime = LocalDateTime.of(scheduledItems.get(0).getDate(), scheduledItems.get(0).getEndTime());
        for (ScheduledItem si : scheduledItems) {
            LocalDateTime currentDateTime = LocalDateTime.of(si.getDate(), si.getEndTime());
            if (currentDateTime.isAfter(maxDateTime)) {
                maxDateTime = currentDateTime;
            }
        }
        return maxDateTime;
    }
}
