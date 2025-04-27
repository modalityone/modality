package one.modality.event.frontoffice.activities.videos;

import dev.webfx.platform.util.Objects;
import dev.webfx.stack.i18n.I18nKeys;
import one.modality.base.shared.entities.ScheduledItem;

/**
 * @author Bruno Salmon
 */
final class VideoState {

    static String getVideoStatusI18nKey(ScheduledItem videoScheduledItem) {
        // Video status lifecycle:
        // 1. ON_TIME (=> 2. CANCELLED) => 3. COUNTDOWN (=> 4. DELAYED) => 5. LIVE_NOW => 6. SOON_AVAILABLE (=> 7. DELAYED) => 8. AVAILABLE => 9. EXPIRED

        // 2. CANCELLED
        if (videoScheduledItem.getProgramScheduledItem().isCancelled())
            return VideosI18nKeys.VideoCancelled;

        VideoTimes videoTimes = new VideoTimes(videoScheduledItem);

        // 1. ON_TIME
        if (videoTimes.isNowBeforeCountdownStart()) { // More than 3 hours before the session
            return VideosI18nKeys.OnTime;
        }

        // 3. COUNTDOWN
        if (videoTimes.isNowBetweenCountdownStartAndLiveNowStart()) {
            return VideosI18nKeys.StartingIn1;
        }

        // 4. DELAYED (eventually)
        if (videoScheduledItem.isVodDelayed()) {
            return I18nKeys.upperCase(VideosI18nKeys.VideoDelayed);
        }

        // 5. LIVE_NOW
        if (videoTimes.isNowBetweenLiveNowStartAndSessionEnd()) {
            return VideosI18nKeys.LiveNow;
        }

        // 6. SOON_AVAILABLE
        if (!videoScheduledItem.isPublished() && videoTimes.isNowBeforeMaxNormalProcessingEnd()) {
            return VideosI18nKeys.RecordingSoonAvailable;
        }

        // 7. DELAYED (eventually) or 8. AVAILABLE_UNTIL
        if (videoTimes.isNowBeforeExpirationDate()) {
            return videoScheduledItem.isPublished() ? VideosI18nKeys.Available : VideosI18nKeys.VideoDelayed;
        }

        // 9. EXPIRED
        return VideosI18nKeys.Expired;
    }

    static boolean isVideoExpired(ScheduledItem scheduledItem) {
        return Objects.areEquals(getVideoStatusI18nKey(scheduledItem), VideosI18nKeys.Expired);
    }

}
