package one.modality.event.frontoffice.activities.videostreaming;

import dev.webfx.platform.util.Objects;
import dev.webfx.platform.util.time.Times;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.markers.EntityHasStartAndEndTime;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * @author Bruno Salmon
 */
final class VideoLifecycle {

    private final ScheduledItem videoScheduledItem;
    private final LocalDateTime sessionStart;
    private final LocalDateTime sessionEnd;
    private final LocalDateTime showLivestreamStart;
    private final LocalDateTime showLivestreamEnd;
    private final LocalDateTime countdownStart;
    private final LocalDateTime liveNowStart;
    private final LocalDateTime maxNormalProcessingEnd;
    private final LocalDateTime expirationDate;
    private final LocalDateTime nowInEventTimezone = Event.nowInEventTimezone();

    VideoLifecycle(ScheduledItem videoScheduledItem) {
        this.videoScheduledItem = videoScheduledItem;
        LocalDate videoDate = videoScheduledItem.getDate();
        // Start & end times can be specified at different places (listed in order of priority):
        // - videoScheduledItem
        // - videoScheduledItem.timeline
        // - videoScheduledItem.programScheduledItem
        // - videoScheduledItem.programScheduledItem.timeline
        EntityHasStartAndEndTime startAndEndTimeHolder = videoScheduledItem;
        if (startAndEndTimeHolder.getStartTime() == null) {
            startAndEndTimeHolder = videoScheduledItem.getTimeline();
            if (startAndEndTimeHolder == null || startAndEndTimeHolder.getStartTime() == null) {
                ScheduledItem programScheduledItem = videoScheduledItem.getProgramScheduledItem();
                startAndEndTimeHolder = programScheduledItem;
                if (startAndEndTimeHolder.getStartTime() == null) {
                    startAndEndTimeHolder = programScheduledItem.getTimeline();
                }
            }
        }
        sessionStart = videoDate.atTime(startAndEndTimeHolder.getStartTime());
        sessionEnd = videoDate.atTime(startAndEndTimeHolder.getEndTime());
        // Starting to show the livestream 20 min before the session
        showLivestreamStart = sessionStart.minusMinutes(20);
        // Stopping to show the livestream 30 min after the session
        showLivestreamEnd = sessionEnd.plusMinutes(30);
        // Starting the countdown 3 hours before the session
        countdownStart = sessionStart.minusHours(3);
        // Starting to show "LIVE NOW" 2 min before the session
        liveNowStart = sessionStart.minusMinutes(2);
        maxNormalProcessingEnd = sessionEnd.plusMinutes(getVodProcessingTimeMinute(videoScheduledItem));
        expirationDate = Objects.coalesce(videoScheduledItem.getExpirationDate(), videoScheduledItem.getEvent().getVodExpirationDate());
    }

    public ScheduledItem getVideoScheduledItem() {
        return videoScheduledItem;
    }

    public LocalDateTime getCountdownStart() {
        return countdownStart;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    boolean isNowBeforeCountdownStart() {
        return nowInEventTimezone.isBefore(countdownStart);
    }

    boolean isNowBetweenCountdownStartAndLiveNowStart() {
        return Times.isBetween(nowInEventTimezone, countdownStart, liveNowStart);
    }

    boolean isNowBeforeShowLivestreamStart() {
        return nowInEventTimezone.isBefore(showLivestreamStart);
    }

    boolean isNowBetweenShowLivestreamStartAndShowLivestreamEnd() {
        return Times.isBetween(nowInEventTimezone, showLivestreamStart, showLivestreamEnd);
    }

    Duration durationBetweenNowAndSessionStart() {
        return Duration.between(nowInEventTimezone, sessionStart);
    }

    Duration durationBetweenNowAndSessionEnd() {
        return Duration.between(nowInEventTimezone, sessionEnd);
    }

    long durationMillisBetweenNowAndShowLivestreamStart() {
        return ChronoUnit.MILLIS.between(nowInEventTimezone, showLivestreamStart);
    }

    long durationMillisBetweenNowAndShowLivestreamEnd() {
        return ChronoUnit.MILLIS.between(nowInEventTimezone, showLivestreamEnd);
    }

    boolean isNowBetweenLiveNowStartAndSessionEnd() {
        return Times.isBetween(nowInEventTimezone, liveNowStart, sessionEnd);
    }

    boolean isNowBeforeMaxNormalProcessingEnd() {
        return nowInEventTimezone.isBefore(maxNormalProcessingEnd);
    }

    boolean isNowBeforeExpirationDate() {
        return expirationDate == null || Times.isFuture(expirationDate, Event.getEventClock());
    }

    boolean isLiveToday() {
        return liveNowStart.toLocalDate().equals(nowInEventTimezone.toLocalDate());
    }

    boolean isLiveUpcoming() {
        return liveNowStart.isAfter(nowInEventTimezone);
    }

    private static int getVodProcessingTimeMinute(ScheduledItem currentVideo) {
        int vodProcessingTimeMinute = 60;
        if (currentVideo.getEvent().getVodProcessingTimeMinutes() != null)
            vodProcessingTimeMinute = currentVideo.getEvent().getVodProcessingTimeMinutes();
        return vodProcessingTimeMinute;
    }

}
