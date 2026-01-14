package one.modality.base.shared.entities.util;

import one.modality.base.shared.entities.ScheduledBoundary;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.markers.EntityHasStartAndEndTime;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * @author Bruno Salmon
 */
public final class ScheduledBoundaries {

    public static LocalDate getDate(ScheduledBoundary scheduledBoundary) {
        return getDate(scheduledBoundary, false);
    }

    public static LocalDate getDate(ScheduledBoundary scheduledBoundary, boolean isEndBoundary) {
        LocalDate date = scheduledBoundary.getDate();
        if (date == null) {
            ScheduledItem scheduledItem = scheduledBoundary.getScheduledItem();
            if (scheduledItem != null)
                date = scheduledItem.getDate();
        }
        // If it's an end boundary and the time is 00:00, the end actually refers to the previous day at midnight
        if (date != null && isEndBoundary && LocalTime.MIN.equals(getTime(scheduledBoundary))) {
            date = date.minusDays(1); // and getTime(scheduledBoundary, true) will return midnight
        }

        return date;
    }

    public static LocalTime getTime(ScheduledBoundary scheduledBoundary) {
        return getTime(scheduledBoundary, false);
    }

    public static LocalTime getTime(ScheduledBoundary scheduledBoundary, boolean isEndBoundary) {
        LocalTime time = scheduledBoundary.isAtStartTime() ? getStartTime(scheduledBoundary) : getEndTime(scheduledBoundary);
        // If it's an end boundary and the time is 00:00, the end actually refers to the previous day at midnight
        if (isEndBoundary && LocalTime.MIN.equals(time))
            time = LocalTime.MAX; // and getDate(scheduledBoundary, true) will return the previous day
        return time;
    }

    public static EntityHasStartAndEndTime getStartAndEndDateHolder(ScheduledBoundary scheduledBoundary) {
        ScheduledItem scheduledItem = scheduledBoundary.getScheduledItem();
        if (scheduledItem != null) {
            if (scheduledItem.getStartTime() != null)
                return scheduledItem;
            if (scheduledItem.getTimeline() != null)
                return scheduledItem.getTimeline();
        }

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

}
