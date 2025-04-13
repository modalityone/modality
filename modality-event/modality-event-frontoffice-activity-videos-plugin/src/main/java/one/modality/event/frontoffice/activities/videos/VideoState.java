package one.modality.event.frontoffice.activities.videos;

import dev.webfx.platform.util.Objects;
import dev.webfx.platform.util.time.Times;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.markers.HasEndTime;

import java.time.LocalDateTime;

/**
 * @author Bruno Salmon
 */
final class VideoState {

    static String getVideoStatusI18nKey(ScheduledItem scheduledItem) {
        Event event = scheduledItem.getEvent();
        LocalDateTime expirationDate = Objects.coalesce(scheduledItem.getExpirationDate(), event.getVodExpirationDate());
        String i18nKey = VideosI18nKeys.OnTime;
        if (expirationDate != null && Times.isPast(expirationDate, Event.getEventClock())) {
            i18nKey = VideosI18nKeys.Expired;
        } else if (scheduledItem.isPublished()) {
            i18nKey = VideosI18nKeys.Available;
        } else if (scheduledItem.isVodDelayed()) {
            i18nKey = VideosI18nKeys.VideoDelayed;
        } else {
            HasEndTime endTimeHolder = scheduledItem.getProgramScheduledItem();
            if (!event.isRecurringWithVideo())
                endTimeHolder = scheduledItem.getProgramScheduledItem().getTimeline();
            LocalDateTime sessionEnd = scheduledItem.getDate().atTime(endTimeHolder.getEndTime());
            if (Event.nowInEventTimezone().isAfter(sessionEnd)) {
                int vodProcessingTimeMinute = Objects.coalesce( event.getVodProcessingTimeMinutes(), 60);
                if (Event.nowInEventTimezone().isAfter(sessionEnd.plusMinutes(vodProcessingTimeMinute)))
                    i18nKey = VideosI18nKeys.VideoDelayed;
                else
                    i18nKey = VideosI18nKeys.RecordingSoonAvailable;
            }
        }
        return i18nKey;
    }

    static boolean isVideoExpired(ScheduledItem scheduledItem) {
        return Objects.areEquals(getVideoStatusI18nKey(scheduledItem), VideosI18nKeys.Expired);
    }
}
