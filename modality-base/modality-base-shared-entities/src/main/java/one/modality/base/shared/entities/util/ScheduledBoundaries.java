package one.modality.base.shared.entities.util;

import one.modality.base.shared.entities.ItemFamily;
import one.modality.base.shared.entities.ScheduledBoundary;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.Timeline;
import one.modality.base.shared.entities.markers.EntityHasStartAndEndTime;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * @author Bruno Salmon
 */
public final class ScheduledBoundaries {

    public static LocalDate getDate(ScheduledBoundary scheduledBoundary) {
        LocalDate date = scheduledBoundary.getDate();
        if (date != null)
            return date;

        ScheduledItem scheduledItem = scheduledBoundary.getScheduledItem();
        if (scheduledItem != null)
            return scheduledItem.getDate();

        return null;
    }

    public static EntityHasStartAndEndTime getStartAndEndDateHolder(ScheduledBoundary scheduledBoundary) {
        ScheduledItem scheduledItem = scheduledBoundary.getScheduledItem();
        if (scheduledItem != null)
            return scheduledItem;

        return scheduledBoundary.getTimeline();
    }

    public static LocalTime getStartTime(ScheduledBoundary scheduledBoundary) {
        EntityHasStartAndEndTime startAndEndDateHolder = getStartAndEndDateHolder(scheduledBoundary);
        return startAndEndDateHolder != null ? startAndEndDateHolder.getStartTime() : null;
    }

    public static LocalTime getEndTime(ScheduledBoundary scheduledBoundary) {
        EntityHasStartAndEndTime startAndEndDateHolder = getStartAndEndDateHolder(scheduledBoundary);
        return startAndEndDateHolder != null ? startAndEndDateHolder.getEndTime() : null;
    }

    public ItemFamily getItemFamily(ScheduledBoundary scheduledBoundary) {
        ScheduledItem scheduledItem = scheduledBoundary.getScheduledItem();
        if (scheduledItem != null)
            return scheduledItem.getItem().getFamily();

        Timeline timeline = scheduledBoundary.getTimeline();
        return timeline != null ? timeline.getItemFamily() : null;
    }
}
