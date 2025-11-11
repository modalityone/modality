package one.modality.event.frontoffice.activities.videostreaming;

import dev.webfx.extras.i18n.I18nKeys;
import one.modality.base.client.i18n.BaseI18nKeys;
import one.modality.base.shared.entities.ScheduledItem;

/**
 * @author Bruno Salmon
 */
final class VideoState {

    static boolean isVideoCancelled(ScheduledItem videoScheduledItem) {
        return videoScheduledItem.getProgramScheduledItem().isCancelled();
    }

    static Object getVideoStatusI18nKey(ScheduledItem videoScheduledItem) {
        // Video status lifecycle:
        // 1. ON_TIME (=> 2. CANCELLED) => 3. COUNTDOWN (=> 4. DELAYED) => 5. LIVE_NOW => 6. SOON_AVAILABLE (=> 7. DELAYED) => 8. AVAILABLE => 9. EXPIRED
        // For livestream-only events (vodExpirationDate = null): 1. ON_TIME => 2. COUNTDOWN => 3. LIVE_NOW => 4. SESSION_COMPLETED

        // 2. CANCELLED
        if (isVideoCancelled(videoScheduledItem))
            return VideoStreamingI18nKeys.VideoCancelled;

        VideoLifecycle videoLifecycle = new VideoLifecycle(videoScheduledItem);

        // Check if this is a livestream-only event (no video recording)
        boolean isLivestreamOnly = videoScheduledItem.getEvent().getVodExpirationDate() == null;

        // 8. AVAILABLE or 9. EXPIRED (the 2 possible states once published -> priority states)
        if (videoScheduledItem.isPublished()) {
            return videoLifecycle.isNowBeforeExpirationDate() ? BaseI18nKeys.Available : VideoStreamingI18nKeys.Expired;
        }

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

        // For livestream-only events, after the session ends, show "Session completed"
        if (isLivestreamOnly) {
            return VideoStreamingI18nKeys.SessionCompleted;
        }

        // 6. SOON_AVAILABLE
        if (!videoScheduledItem.isPublished() && videoLifecycle.isNowBeforeMaxNormalProcessingEnd()) {
            return VideoStreamingI18nKeys.RecordingSoonAvailable;
        }

        // 7. DELAYED (eventually)
        if (videoLifecycle.isNowBeforeExpirationDate()) {
            return VideoStreamingI18nKeys.VideoDelayed;
        }

        // 9. EXPIRED
        return VideoStreamingI18nKeys.Expired;
    }

    static Object getAllProgramVideoGroupI18nKey(ScheduledItem videoScheduledItem) {
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

    static int getAllProgramVideoGroupOrder(Object groupI18nKey) {
        if (BaseI18nKeys.Today.equals(groupI18nKey))
            return 0;
        if (BaseI18nKeys.Available.equals(groupI18nKey))
            return 1;
        if (BaseI18nKeys.Upcoming.equals(groupI18nKey))
            return 2;
        return 3;
    }

}
