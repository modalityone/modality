package one.modality.event.frontoffice.activities.videostreaming;

import dev.webfx.stack.i18n.I18nKeys;
import one.modality.base.client.i18n.BaseI18nKeys;
import one.modality.base.shared.entities.ScheduledItem;

/**
 * @author Bruno Salmon
 */
final class VideoState {

    static boolean isVideoCancelled(ScheduledItem videoScheduledItem) {
        return videoScheduledItem.getProgramScheduledItem().isCancelled();
    }

    static String getVideoStatusI18nKey(ScheduledItem videoScheduledItem) {
        // Video status lifecycle:
        // 1. ON_TIME (=> 2. CANCELLED) => 3. COUNTDOWN (=> 4. DELAYED) => 5. LIVE_NOW => 6. SOON_AVAILABLE (=> 7. DELAYED) => 8. AVAILABLE => 9. EXPIRED

        // 2. CANCELLED
        if (isVideoCancelled(videoScheduledItem))
            return VideoStreamingI18nKeys.VideoCancelled;

        VideoLifecycle videoLifecycle = new VideoLifecycle(videoScheduledItem);

        // 1. ON_TIME
        if (videoLifecycle.isNowBeforeCountdownStart()) { // More than 3 hours before the session
            return VideoStreamingI18nKeys.OnTime;
        }

        // 3. COUNTDOWN
        if (videoLifecycle.isNowBetweenCountdownStartAndLiveNowStart()) {
            return VideoStreamingI18nKeys.StartingIn1;
        }

        // 4. DELAYED (eventually)
        if (videoScheduledItem.isVodDelayed()) {
            return I18nKeys.upperCase(VideoStreamingI18nKeys.VideoDelayed);
        }

        // 5. LIVE_NOW
        if (videoLifecycle.isNowBetweenLiveNowStartAndSessionEnd()) {
            return VideoStreamingI18nKeys.LiveNow;
        }

        // 6. SOON_AVAILABLE
        if (!videoScheduledItem.isPublished() && videoLifecycle.isNowBeforeMaxNormalProcessingEnd()) {
            return VideoStreamingI18nKeys.RecordingSoonAvailable;
        }

        // 7. DELAYED (eventually) or 8. AVAILABLE_UNTIL
        if (videoLifecycle.isNowBeforeExpirationDate()) {
            return videoScheduledItem.isPublished() ? BaseI18nKeys.Available : VideoStreamingI18nKeys.VideoDelayed;
        }

        // 9. EXPIRED
        return VideoStreamingI18nKeys.Expired;
    }

    static String getAllProgramVideoGroupI18nKey(ScheduledItem videoScheduledItem) {
        VideoLifecycle videoLifecycle = new VideoLifecycle(videoScheduledItem);
        if (videoScheduledItem.isPublished() && videoLifecycle.isNowBeforeExpirationDate()) {
            return BaseI18nKeys.Available;
        }
        if (videoLifecycle.isLiveToday()) {
            return BaseI18nKeys.Today;
        }
        if (videoLifecycle.isLiveUpcoming()) {
            return BaseI18nKeys.Upcoming;
        }
        return BaseI18nKeys.Past;
    }

    static int getAllProgramVideoGroupOrder(String groupI18nKey) {
        switch (groupI18nKey) {
            case BaseI18nKeys.Today: return 0;
            case BaseI18nKeys.Available: return 1;
            case BaseI18nKeys.Upcoming: return 2;
            case BaseI18nKeys.Past:
            default:
                return 3;
        }
    }

}
