package one.modality.event.frontoffice.activities.videos;

import dev.webfx.platform.util.Objects;
import dev.webfx.platform.util.time.Times;
import dev.webfx.stack.i18n.I18nKeys;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.markers.EntityHasStartAndEndTime;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author Bruno Salmon
 */
final class VideoState {

    static String getVideoStatusI18nKey(ScheduledItem scheduledItem) {
        // Video status lifecycle:
        // 1. ON_TIME => 2. COUNTDOWN (=> 3. DELAYED) => 4. LIVE_NOW => 5. SOON_AVAILABLE (=> 6. DELAYED) => 7. AVAILABLE => 8. EXPIRED

        Event event = scheduledItem.getEvent();
        LocalDateTime expirationDate = Objects.coalesce(scheduledItem.getExpirationDate(), event.getVodExpirationDate());
        EntityHasStartAndEndTime startAndEndTimeHolder = Objects.coalesce(scheduledItem.getProgramScheduledItem().getTimeline(), scheduledItem.getProgramScheduledItem());
        LocalDateTime sessionStart = scheduledItem.getDate().atTime(startAndEndTimeHolder.getStartTime());
        LocalDateTime sessionEnd = scheduledItem.getDate().atTime(startAndEndTimeHolder.getEndTime());

        LocalDateTime liveNowStart = sessionStart.minusMinutes(2);
        LocalDateTime countDownStart = sessionStart.minusHours(3);
        LocalDateTime maxNormalProcessingEnd = sessionEnd.plusMinutes(getVodProcessingTimeMinute(scheduledItem));

        LocalDateTime nowInEventTimezone = Event.nowInEventTimezone();

        // 1. ON_TIME
        if (nowInEventTimezone.isBefore(countDownStart)) { // More than 3 hours before the session
            return VideosI18nKeys.OnTime;
        }

        // 2. COUNTDOWN
        if (Times.isBetween(nowInEventTimezone, countDownStart, liveNowStart)) {
            return VideosI18nKeys.StartingIn1;
        }

        // 3. DELAYED (eventually)
        if (scheduledItem.isVodDelayed()) {
            return I18nKeys.upperCase(VideosI18nKeys.VideoDelayed);
        }

        // 4. LIVE_NOW
        if (Times.isBetween(nowInEventTimezone, liveNowStart, sessionEnd)) {
            return VideosI18nKeys.LiveNow;
        }

        // 5. SOON_AVAILABLE
        if (!scheduledItem.isPublished() && nowInEventTimezone.isBefore(maxNormalProcessingEnd)) {
            return VideosI18nKeys.RecordingSoonAvailable;
        }

        // 6. DELAYED (eventually) or 7. AVAILABLE_UNTIL
        if (expirationDate == null || Times.isFuture(expirationDate, Event.getEventClock())) {
            return scheduledItem.isPublished() ? VideosI18nKeys.Available : VideosI18nKeys.VideoDelayed;
        }

        // 8. EXPIRED
        return VideosI18nKeys.Expired;
    }

    static boolean isVideoExpired(ScheduledItem scheduledItem) {
        return Objects.areEquals(getVideoStatusI18nKey(scheduledItem), VideosI18nKeys.Expired);
    }

    private static int getVodProcessingTimeMinute(ScheduledItem currentVideo) {
        int vodProcessingTimeMinute = 60;
        if (currentVideo.getEvent().getVodProcessingTimeMinutes() != null)
            vodProcessingTimeMinute = currentVideo.getEvent().getVodProcessingTimeMinutes();
        return vodProcessingTimeMinute;
    }


    static String formatDuration(Duration duration) { // Not sure if it's the best place for this method, but ok for now
        //TODO: use LocalizedTime instead
        if (duration == null)
            return "xx:xx";
        int hours = (int) duration.toHours();
        int minutes = ((int) duration.toMinutes()) % 60;
        int seconds = ((int) duration.toSeconds()) % 60;
        return (hours < 10 ? "0" : "") + hours + ":" + (minutes < 10 ? "0" : "") + minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
    }
}
