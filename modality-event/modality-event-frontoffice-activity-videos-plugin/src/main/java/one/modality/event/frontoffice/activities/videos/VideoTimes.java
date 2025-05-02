package one.modality.event.frontoffice.activities.videos;

import dev.webfx.platform.util.Objects;
import dev.webfx.platform.util.time.Times;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.markers.EntityHasStartAndEndTime;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * @author Bruno Salmon
 */
final class VideoTimes {

    private final LocalDateTime sessionStart;
    private final LocalDateTime sessionEnd;
    private final LocalDateTime showLivestreamStart;
    private final LocalDateTime showLivestreamEnd;
    private final LocalDateTime countdownStart;
    private final LocalDateTime liveNowStart;
    private final LocalDateTime maxNormalProcessingEnd;
    private final LocalDateTime expirationDate;
    private final LocalDateTime nowInEventTimezone = Event.nowInEventTimezone();

    VideoTimes(ScheduledItem videoScheduledItem) {
        ScheduledItem programScheduledItem = videoScheduledItem.getProgramScheduledItem();
        EntityHasStartAndEndTime startAndEndTimeHolder = Objects.coalesce(programScheduledItem.getTimeline(), programScheduledItem);
        sessionStart = videoScheduledItem.getDate().atTime(startAndEndTimeHolder.getStartTime());
        sessionEnd = videoScheduledItem.getDate().atTime(startAndEndTimeHolder.getEndTime());
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

    public LocalDateTime getCountdownStart() {
        return countdownStart;
    }

    public LocalDateTime getSessionEnd() {
        return sessionEnd;
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
