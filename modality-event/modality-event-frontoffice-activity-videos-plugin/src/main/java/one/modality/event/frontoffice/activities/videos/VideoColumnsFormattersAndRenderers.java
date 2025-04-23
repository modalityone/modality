package one.modality.event.frontoffice.activities.videos;

import dev.webfx.extras.cell.renderer.ValueRendererRegistry;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.time.format.LocalizedTime;
import dev.webfx.extras.type.PrimType;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.time.Times;
import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.i18n.I18nKeys;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.domainmodel.formatter.FormatterRegistry;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.layout.VBox;
import one.modality.base.client.time.FrontOfficeTimeFormats;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.markers.EntityHasStartAndEndTime;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * @author David Hello
 */
final class VideoColumnsFormattersAndRenderers {

    static {
        // videoDate
        FormatterRegistry.registerFormatter("videoDate", PrimType.STRING, date ->
                LocalizedTime.formatMonthDayProperty((LocalDate) date, FrontOfficeTimeFormats.VOD_TODAY_MONTH_DAY_FORMAT));
        // videoTimeRange
        FormatterRegistry.registerFormatter("videoTimeRange", PrimType.STRING, timeRange -> {
            Object[] times = (Object[]) timeRange;
            return LocalizedTime.formatLocalTimeRangeProperty((LocalTime) times[0], (LocalTime) times[1], FrontOfficeTimeFormats.VIDEO_DAY_TIME_FORMAT);
        });
        // videoStatus
        ValueRendererRegistry.registerValueRenderer("videoStatus", (value, context) -> {
            ScheduledItem videoScheduledItem = (ScheduledItem) value;
            String i18nKey = VideoState.getVideoStatusI18nKey(videoScheduledItem);
            Button actionButton = Bootstrap.dangerButton(I18nControls.newButton(VideosI18nKeys.Watch));
            actionButton.setGraphicTextGap(10);
            Label statusLabel = I18nControls.newLabel(I18nKeys.upperCase(i18nKey));
            Label availableUntilLabel = new Label();
            hideLabel(availableUntilLabel);
            computeStatusLabelAndWatchButton(videoScheduledItem, statusLabel, availableUntilLabel, actionButton, context.getAppContext());
            VBox vBoxStatusAndButtonContainer = new VBox(
                    actionButton,
                    statusLabel,
                    availableUntilLabel
            );
            vBoxStatusAndButtonContainer.setPadding(new Insets(10));
            vBoxStatusAndButtonContainer.setAlignment(Pos.CENTER);
            return vBoxStatusAndButtonContainer;
        });
    }

    static void registerRenderers() {
        // Actually done (only once) in the static initializer above
    }

    // PRIVATE API

    private static void computeStatusLabelAndWatchButton(ScheduledItem scheduledItem, Label statusLabel, Label availableUntilLabel, Button actionButton, BrowsingHistory history) {
        Runnable refresher = () -> computeStatusLabelAndWatchButton(scheduledItem, statusLabel, availableUntilLabel, actionButton, history);
        //THE STATE
        LocalDateTime sessionStart;
        LocalDateTime sessionEnd;
        LocalDate sessionDate = scheduledItem.getDate();
        ScheduledItem programScheduledItem = scheduledItem.getProgramScheduledItem();
        EntityHasStartAndEndTime startAndEndTimeHolder;
        Event event = scheduledItem.getEvent();
        if (event.isRecurringWithVideo()) {
            startAndEndTimeHolder = programScheduledItem;
        } else {
            startAndEndTimeHolder = programScheduledItem.getTimeline();
        }
        sessionStart = sessionDate.atTime(startAndEndTimeHolder.getStartTime());
        sessionEnd = sessionDate.atTime(startAndEndTimeHolder.getEndTime());
        // For now, we manage the case when the livestream link is unique for the whole event, which is the case with Castr, which is the platform we generally use
        // TODO: manage the case when the livestream link is not global but per session, which happens on platform like youtube, etc.

        // FIRST CASE LIVESTREAM HAS NOT ENDED ------------------------//

        // SUBCASE A: The live is not finished, we display the play button 2 minutes before the beginning
        hideButton(actionButton);
        LocalDateTime nowInEventTimezone = Event.nowInEventTimezone();
        if (Times.isBetween(nowInEventTimezone, sessionStart.minusMinutes(2), sessionEnd)) {
            I18nControls.bindI18nProperties(statusLabel, I18nKeys.upperCase(VideosI18nKeys.LiveNow));
            Duration duration = Duration.between(nowInEventTimezone, sessionEnd);
            if (duration.getSeconds() > 0) {
                //Here the event is started but not finished
                scheduleRefreshUI(duration.getSeconds(), refresher);
            }
            return;
        }

        // SUBCASE B: the live hasn't started yet, and we're more than 2 minutes before the start
        if (nowInEventTimezone.isBefore(sessionStart)) {
            Duration duration = Duration.between(nowInEventTimezone, sessionStart);

            //If we are less than 3 hours before the session
            if (duration.getSeconds() > 0 && duration.getSeconds() < 3600 * 3) {
                I18nControls.bindI18nProperties(statusLabel, I18nKeys.upperCase(VideosI18nKeys.StartingIn1), VideoState.formatDuration(duration));
                //We refresh every second
                scheduleRefreshUI(1, refresher);
                //If we are less than 30 minutes before the session, we display the play button
                if (duration.getSeconds() < 60 * 30) {
                  //  showButton(actionButton,e -> BROWSING_HISTORY.push(Page3LivestreamPlayerRouting.getLivestreamPath(scheduledItem.getEventId())));
                    return;
                }
                return;
            } else { //Here we are more than 3 hours before the session
                I18nControls.bindI18nProperties(statusLabel, I18nKeys.upperCase(VideosI18nKeys.OnTime));
                scheduleRefreshUI(60, refresher);
                hideButton(actionButton);
                showLabel(statusLabel);
                return;
            }
        }

        // 2ND CASE - LIVESTREAM IS FINISHED ------------------------//
        // SubCase A: the video has expired
        LocalDateTime expirationDate = event.getVodExpirationDate();
        //We look if the current video is expired
        if (scheduledItem.getExpirationDate() != null) {
            expirationDate = scheduledItem.getExpirationDate();
        }
        if (expirationDate != null && Times.isPast(expirationDate, Event.getEventClock())) {
            //TODO: when we know how we will manage the timezone, we adapt to take into account the different timezone
            //TODO: when a push notification is sent we have to update this also.
            I18nControls.bindI18nProperties(statusLabel, I18nKeys.upperCase(VideosI18nKeys.Expired));
            hideButton(actionButton);
            showLabel(statusLabel);
            return;
        }

        // SubCase B: The recording has been published (and not expired)
        if (scheduledItem.isPublished()) {
            I18nControls.bindI18nProperties(statusLabel, I18nKeys.upperCase(VideosI18nKeys.Available));
            showButton(actionButton,e -> {
                history.push(Page4SessionVideoPlayerRouting.getVideoOfSessionPath(scheduledItem.getId()));
                transformButtonFromPlayToPlayAgain(actionButton);
            });
            if(scheduledItem.getExpirationDate()!=null) {
                I18nControls.bindI18nProperties(availableUntilLabel, VideosI18nKeys.VideoAvailableUntil1,LocalizedTime.formatLocalDateTimeProperty(expirationDate,"dd MMM '-' HH.mm"));
                showLabel(availableUntilLabel);
            }
            hideLabel(statusLabel);

            if (expirationDate != null) {
                //We schedule a refresh so the UI is updated when the expirationDate is reached
                Duration duration = Duration.between(nowInEventTimezone, expirationDate);
                if (duration.getSeconds() > 0) {
                    scheduleRefreshUI(duration.getSeconds(), refresher);
                }
            }
            return;
        }

        // SubCase C: The recording has been delayed
        if (scheduledItem.isVodDelayed()) {
            I18nControls.bindI18nProperties(statusLabel, I18nKeys.upperCase(VideosI18nKeys.VideoDelayed));
            hideButton(actionButton);
            showLabel(statusLabel);
            scheduleRefreshUI(60, refresher);
            return;
        }

        // SubCase D: The live has ended, we're waiting for the video to be published
        if (!scheduledItem.isPublished()) {
            //The default value of the processing time if this parameter has not been entered
            int vodProcessingTimeMinute = getVodProcessingTimeMinute(scheduledItem);
            if (nowInEventTimezone.isAfter(sessionEnd.plusMinutes(vodProcessingTimeMinute))) {
                I18nControls.bindI18nProperties(statusLabel, I18nKeys.upperCase(VideosI18nKeys.VideoDelayed));
                hideButton(actionButton);
                showLabel(statusLabel);
                //A push notification will tell us when the video recording will be available
                return;
            }

            I18nControls.bindI18nProperties(statusLabel, I18nKeys.upperCase(VideosI18nKeys.RecordingSoonAvailable));
            hideButton(actionButton);
            showLabel(statusLabel);
            //A push notification will tell us when the video recording is available
        }
    }

    private static void hideLabel(Labeled label) {
        Layouts.setManagedAndVisibleProperties(label, false);
    }

    private static void showLabel(Labeled label) {
        Layouts.setManagedAndVisibleProperties(label, true);
    }

    private static void hideButton(Button button) {
        hideLabel(button);
        button.setOnAction(null);
    }

    private static void showButton(Button button, EventHandler<ActionEvent> actionEvent) {
        showLabel(button);
        button.setOnAction(actionEvent);
    }

    private static void transformButtonFromPlayToPlayAgain(Button actionButton) {
        I18nControls.bindI18nProperties(actionButton, VideosI18nKeys.WatchAgain);
        actionButton.getStyleClass().clear();
        Bootstrap.secondaryButton(actionButton);
    }

    private static int getVodProcessingTimeMinute(ScheduledItem currentVideo) {
        int vodProcessingTimeMinute = 60;
        if (currentVideo.getEvent().getVodProcessingTimeMinutes() != null)
            vodProcessingTimeMinute = currentVideo.getEvent().getVodProcessingTimeMinutes();
        return vodProcessingTimeMinute;
    }

    private static void scheduleRefreshUI(long delaySeconds, Runnable refresher) {
        long delayMillis = delaySeconds * 1000;
        if (delaySeconds > 59) {
            //If we want to refresh more than 1 minute, we add a second to make sure the calculation has time to proceed before the refresh
            delayMillis = delayMillis + 1000;
        }
        UiScheduler.scheduleDelay(delayMillis, refresher);
    }
}
