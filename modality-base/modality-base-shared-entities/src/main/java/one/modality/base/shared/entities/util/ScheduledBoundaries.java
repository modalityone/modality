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

    public static LocalTime getTime(ScheduledBoundary scheduledBoundary) {
        if (scheduledBoundary.isAtStartTime())
            return getStartTime(scheduledBoundary);
        return getEndTime(scheduledBoundary);
    }

    public static EntityHasStartAndEndTime getStartAndEndDateHolder(ScheduledBoundary scheduledBoundary) {
        ScheduledItem scheduledItem = scheduledBoundary.getScheduledItem();
        if (scheduledItem != null)
            return scheduledItem;

        return scheduledBoundary.getTimeline();
    }

    public static LocalTime getStartTime(ScheduledBoundary scheduledBoundary) {
        EntityHasStartAndEndTime startAndEndDateHolder = getStartAndEndDateHolder(scheduledBoundary);
        LocalTime startTime = startAndEndDateHolder != null ? startAndEndDateHolder.getStartTime() : null;
        return startTime != null ? startTime : LocalTime.MIN;
    }

    public static LocalTime getEndTime(ScheduledBoundary scheduledBoundary) {
        EntityHasStartAndEndTime startAndEndDateHolder = getStartAndEndDateHolder(scheduledBoundary);
        LocalTime endTime = startAndEndDateHolder != null ? startAndEndDateHolder.getEndTime() : null;
        return endTime != null ? endTime : LocalTime.MAX;
    }

    public ItemFamily getItemFamily(ScheduledBoundary scheduledBoundary) {
        ScheduledItem scheduledItem = scheduledBoundary.getScheduledItem();
        if (scheduledItem != null)
            return scheduledItem.getItem().getFamily();

        Timeline timeline = scheduledBoundary.getTimeline();
        return timeline != null ? timeline.getItemFamily() : null;
    }
}
