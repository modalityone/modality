package one.modality.event.frontoffice.activities.videos;

import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.time.format.LocalizedTime;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.time.Times;
import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.i18n.I18nKeys;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.entity.binding.EntityBindings;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import one.modality.base.client.messaging.ModalityMessaging;
import one.modality.base.client.time.FrontOfficeTimeFormats;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.Timeline;
import one.modality.base.shared.entities.markers.EntityHasStartAndEndTime;
import one.modality.base.shared.entities.markers.HasEndTime;
import one.modality.base.shared.entities.markers.HasStartTime;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author David Hello
 */
final class Page2EventDayScheduleView {

    private static final double DATE_PREF_SIZE = 150;
    private static final double STATUS_PREF_SIZE = 150;
    private static final double NAME_PREF_SIZE = 250;
    private static final double TIME_PREF_SIZE = 150;
    private static final double REMARK_PREF_SIZE = 300;
    private static final double BUTTON_PREF_SIZE = 300;

    private final LocalDate day;
    private final List<ScheduledItem> dayScheduledVideos;
    private final BrowsingHistory browsingHistory;

    private final MonoPane dateMonoPane = new MonoPane();
    private final MonoPane statusMonoPane = new MonoPane();
    private final VBox nameVBox = new VBox();
    private final VBox timeVBox = new VBox();
    private final MonoPane remarkMonoPane = new MonoPane();
    private final MonoPane actionButtonMonoPane = new MonoPane();
    private final MonoPane remarkHeaderMonoPane = new MonoPane();
    private final Separator separator1 = new Separator();
    private final HBox mainLine = new HBox();
    private final VBox mainVBox = new VBox();

    public Page2EventDayScheduleView(LocalDate day, List<ScheduledItem> dayScheduledVideos, BrowsingHistory browsingHistory, boolean displayHeader) {
        this.day = day;
        this.dayScheduledVideos = dayScheduledVideos;
        this.browsingHistory = browsingHistory;
        buildUi(displayHeader);
    }

    Region getView() {
        return mainVBox;
    }

    private void buildUi(boolean displayHeader) {

        //   dateMonoPane.setMinWidth(40);
        dateMonoPane.setPrefWidth(DATE_PREF_SIZE);
        dateMonoPane.setMaxWidth(DATE_PREF_SIZE);

        statusMonoPane.setMinWidth(40);
        statusMonoPane.setPrefWidth(STATUS_PREF_SIZE);
        statusMonoPane.setMaxWidth(STATUS_PREF_SIZE);

        nameVBox.setMinWidth(40);
        nameVBox.setPrefWidth(NAME_PREF_SIZE);
        nameVBox.setMaxWidth(NAME_PREF_SIZE);

        timeVBox.setMinWidth(40);
        timeVBox.setPrefWidth(TIME_PREF_SIZE);
        timeVBox.setMaxWidth(TIME_PREF_SIZE);

        remarkMonoPane.setMinWidth(40);
        remarkMonoPane.setPrefWidth(REMARK_PREF_SIZE);
        remarkMonoPane.setMaxWidth(REMARK_PREF_SIZE);

        actionButtonMonoPane.setMinWidth(40);
        actionButtonMonoPane.setPrefWidth(BUTTON_PREF_SIZE);
        actionButtonMonoPane.setMaxWidth(80);


        if (displayHeader) {
            addHeaderRow();
        } else {
            if (!Boolean.TRUE.equals(dayScheduledVideos.get(0).getEvent().isRecurring())) // isRecurring() may return null if the type is not set
                addInvisibleSeparator();
        }

        Label dateLabel = new Label();
        dateLabel.textProperty().bind(LocalizedTime.formatLocalDateProperty(day, FrontOfficeTimeFormats.VIDEO_DAY_DATE_FORMAT));
        dateLabel.setWrapText(true);

        dateMonoPane.setContent(dateLabel);

        // Add a listener to the width property of the HBox
        mainVBox.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            remarkHeaderMonoPane.setVisible(newWidth.doubleValue() > 1000);
            remarkMonoPane.setManaged(newWidth.doubleValue() > 1000);
        });


        // Use the inner class to populate the grid
        dayScheduledVideos.forEach((s) -> {
            VideoSchedulePopulator populator = new VideoSchedulePopulator(s);
            // Old code: ModalityMessaging.addFrontOfficeMessageBodyHandler(e -> populator.updateVODButton(e));
            // New code (not yet working):
            ModalityMessaging.getFrontOfficeEntityMessaging().listenEntityChanges(s.getStore());
            populator.populateVideoRow();
        });
        mainVBox.setAlignment(Pos.CENTER);
    }

    private void addHeaderRow() {
        Label dateHeaderLabel = Bootstrap.h4(Bootstrap.textPrimary(I18nControls.newLabel(VideosI18nKeys.Date)));
        Label statusHeaderLabel = Bootstrap.h4(Bootstrap.textPrimary(I18nControls.newLabel(VideosI18nKeys.Status)));
        Label nameHeaderLabel = Bootstrap.h4(Bootstrap.textPrimary(I18nControls.newLabel(VideosI18nKeys.Name)));
        Label timeZoneHeaderLabel = Bootstrap.h4(Bootstrap.textPrimary(I18nControls.newLabel(VideosI18nKeys.TimeZoneUK)));
        Label gmtTimeHeaderLabel = Bootstrap.small(Bootstrap.textPrimary(I18nControls.newLabel(VideosI18nKeys.GMTZoneUK)));
        Label remarksHeaderLabel = Bootstrap.h4(Bootstrap.textPrimary(I18nControls.newLabel(VideosI18nKeys.Remarks)));

        MonoPane dateMonoPane = new MonoPane(dateHeaderLabel);
        dateMonoPane.setMinWidth(DATE_PREF_SIZE);
        dateMonoPane.setAlignment(Pos.CENTER_LEFT);

        MonoPane statusMonoPane = new MonoPane(statusHeaderLabel);
        statusMonoPane.setMinWidth(STATUS_PREF_SIZE);
        statusMonoPane.setAlignment(Pos.CENTER_LEFT);

        MonoPane nameMonoPane = new MonoPane(nameHeaderLabel);
        nameMonoPane.setMinWidth(NAME_PREF_SIZE);
        nameMonoPane.setAlignment(Pos.CENTER_LEFT);

        VBox timeVBox = new VBox(timeZoneHeaderLabel, gmtTimeHeaderLabel);
        timeVBox.setMinWidth(TIME_PREF_SIZE);
        timeVBox.setAlignment(Pos.CENTER_LEFT);

        remarkHeaderMonoPane.setContent(remarksHeaderLabel);
        remarkHeaderMonoPane.setMinWidth(100);
        remarkHeaderMonoPane.setAlignment(Pos.CENTER_LEFT);

        separator1.setPadding(new Insets(5, 0, 25, 0));

        HBox line = new HBox(5, dateMonoPane, statusMonoPane, nameMonoPane, timeVBox, remarkHeaderMonoPane);
        mainVBox.getChildren().addAll(line, separator1);
    }

    private void addInvisibleSeparator() {
        //separator2.setVisible(false);
        //separator2.setPadding(new Insets(20, 0, 20, 0));
        //mainVBox.getChildren().add(separator2);
    }


    // Inner class to handle populating video schedule rows
    private class VideoSchedulePopulator {

        private final Label statusLabel = I18nControls.newLabel(I18nKeys.upperCase(VideosI18nKeys.OnTime));
        private final Button actionButton = Bootstrap.dangerButton(I18nControls.newButton(VideosI18nKeys.Watch));
        private final ScheduledItem scheduledItem;
        private final BooleanProperty attendanceIsAttendedProperty;

        public VideoSchedulePopulator(ScheduledItem s) {
            actionButton.setGraphicTextGap(10);
            actionButton.setCursor(Cursor.HAND);
            actionButton.setMinWidth(150);
            statusLabel.setWrapText(true);
            statusLabel.setPadding(new Insets(0, 10, 0, 0));
            scheduledItem = s;
            attendanceIsAttendedProperty = EntityBindings.getBooleanFieldProperty(scheduledItem, "attended");
            BooleanProperty scheduledItemPublishedProperty = EntityBindings.getBooleanFieldProperty(scheduledItem, ScheduledItem.published);
            if (attendanceIsAttendedProperty.get()) {
                transformButtonFromPlayToPlayAgain(actionButton);
            }
            attendanceIsAttendedProperty.addListener(e ->
                UiScheduler.scheduleDelay(3000, () -> {
                    if (attendanceIsAttendedProperty.get()) {
                        I18nControls.bindI18nProperties(actionButton, VideosI18nKeys.WatchAgain);
                        actionButton.getStyleClass().clear();
                        Bootstrap.secondaryButton(actionButton);
                    }
                }));
            scheduledItemPublishedProperty.addListener(e -> Platform.runLater(this::computeStatusLabelAndWatchButton));
        }

        private void transformButtonFromPlayToPlayAgain(Button actionButton) {
            I18nControls.bindI18nProperties(actionButton, VideosI18nKeys.WatchAgain);
            actionButton.getStyleClass().clear();
            Bootstrap.secondaryButton(actionButton);
        }

        public void populateVideoRow() {
            //we initialise statusLabel and actionButton
            computeStatusLabelAndWatchButton();

            if (statusLabel != null) {
                statusMonoPane.setContent(statusLabel);
            }
            // Name label
            //If the name of the video scheduledItem has been overwritten, we use it, otherwise, we use the name of the programScheduledItem
            ScheduledItem programScheduledItem = scheduledItem.getProgramScheduledItem();
            String name = programScheduledItem.getName();
            if (scheduledItem.getName() != null && !scheduledItem.getName().isBlank()) {
                name = scheduledItem.getName();
            }
            Label nameLabel = new Label(name);
            nameLabel.setWrapText(true);
            nameLabel.setPadding(new Insets(0, 10, 0, 0));

            LocalDateTime nowInEventTimezone = Event.nowInEventTimezone();

            // Handle expiration date
            LocalDateTime expirationDate = scheduledItem.getExpirationDate();
            if (expirationDate != null) {
                boolean available = expirationDate.isAfter(nowInEventTimezone);
                Label expirationDateLabel = Bootstrap.small(Bootstrap.textDanger(I18nControls.newLabel(
                    available ? VideosI18nKeys.VideoAvailableUntil1 : VideosI18nKeys.VideoExpiredOn1,
                    LocalizedTime.formatLocalDateTime(expirationDate, FrontOfficeTimeFormats.VOD_EXPIRATION_DATE_TIME_FORMAT)
                )));
                expirationDateLabel.setWrapText(true);

                nameVBox.setAlignment(Pos.TOP_LEFT);
                nameVBox.getChildren().addAll(nameLabel, expirationDateLabel);
            } else {
                nameVBox.getChildren().add(nameLabel);
            }
            nameVBox.setAlignment(Pos.CENTER_LEFT);
            // Time label
            HasStartTime startTimeHolder;
            HasEndTime endTimeHolder;
            if (scheduledItem.getEvent().isRecurringWithVideo()) {
                startTimeHolder = programScheduledItem;
                endTimeHolder = programScheduledItem;
            } else {
                Timeline timeline = programScheduledItem.getTimeline();
                startTimeHolder = timeline;
                endTimeHolder = timeline;
            }
            Label timeLabel = I18nControls.newLabel("{0} - {1}",
                LocalizedTime.formatLocalTime(startTimeHolder.getStartTime(), FrontOfficeTimeFormats.VIDEO_DAY_TIME_FORMAT),
                LocalizedTime.formatLocalTime(endTimeHolder.getEndTime(), FrontOfficeTimeFormats.VIDEO_DAY_TIME_FORMAT));
            timeVBox.setAlignment(Pos.CENTER_LEFT);
            timeVBox.getChildren().add(timeLabel);

            // Remarks label
            Label remarkLabel = new Label(scheduledItem.getComment());
            remarkLabel.getStyleClass().add(Bootstrap.TEXT_INFO);
            remarkLabel.setWrapText(true);
            remarkLabel.setPadding(new Insets(0, 10, 0, 0));
            remarkMonoPane.setAlignment(Pos.CENTER_LEFT);
            remarkMonoPane.setContent(remarkLabel);

            // Button
            actionButtonMonoPane.setContent(actionButton);
            mainLine.setAlignment(Pos.CENTER_LEFT);
            mainLine.getChildren().addAll(dateMonoPane, statusMonoPane, nameVBox, timeVBox, remarkMonoPane, actionButtonMonoPane);

            // Separator
            Separator sessionSeparator = new Separator();
            if (scheduledItem.getEvent().getType().getRecurringItem() == null) {
                sessionSeparator.setPadding(new Insets(35, 0, 15, 0));
            } else {
                sessionSeparator.setPadding(new Insets(0, 0, 0, 0));
            }
            mainVBox.getChildren().addAll(mainLine, sessionSeparator);
        }

        private void computeStatusLabelAndWatchButton() {
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

            //The live is currently playing, we display this 2 minutes before the beginning
            LocalDateTime nowInEventTimezone = Event.nowInEventTimezone();
            if (Times.isBetween(nowInEventTimezone, sessionStart.minusMinutes(2), sessionEnd)) {
                I18nControls.bindI18nProperties(statusLabel, I18nKeys.upperCase(VideosI18nKeys.LiveNow));
                actionButton.setOnAction(e -> browsingHistory.push(Page3LivestreamPlayerRouting.getLivestreamPath(scheduledItem.getEventId())));
                actionButton.setVisible(true);
                Duration duration = Duration.between(nowInEventTimezone, sessionEnd);
                if (duration.getSeconds() > 0)
                    scheduleRefreshUI(duration.getSeconds());
                return;
            }

            //The session has not started yet
            if (nowInEventTimezone.isBefore(sessionStart)) {
                Duration duration = Duration.between(nowInEventTimezone, sessionStart);

                //We display the countdown 3 hours before the session
                if (duration.getSeconds() > 0 && duration.getSeconds() < 3600 * 3) {
                    I18nControls.bindI18nProperties(statusLabel, I18nKeys.upperCase(VideosI18nKeys.StartingIn1), VideoState.formatDuration(duration));
                    //We refresh every second
                    scheduleRefreshUI(1);
                    //We display the play button 30 minutes before the session
                    if (duration.getSeconds() < 60 * 30) {
                        actionButton.setOnAction(e -> browsingHistory.push(Page3LivestreamPlayerRouting.getLivestreamPath(scheduledItem.getEventId())));
                        actionButton.setVisible(true);
                    } else {
                        hideActionButton();
                    }
                } else {
                    I18nControls.bindI18nProperties(statusLabel, I18nKeys.upperCase(VideosI18nKeys.OnTime));
                    scheduleRefreshUI(60);
                    hideActionButton();
                }
                return;
            }

            //Case of the video expired
            LocalDateTime expirationDate = event.getVodExpirationDate();
            //We look if the current video is expired
            if (scheduledItem.getExpirationDate() != null) {
                expirationDate = scheduledItem.getExpirationDate();
            }
            if (expirationDate != null && Times.isPast(expirationDate, Event.getEventClock())) {
                //TODO: when we know how we will manage the timezone, we adapt to take into account the different timezone
                //TODO: when a push notification is sent we have to update this also.
                I18nControls.bindI18nProperties(statusLabel, I18nKeys.upperCase(VideosI18nKeys.Expired));
                hideActionButton();
                return;
            }

            //The recording of the video has been published
            if (scheduledItem.isPublished()) {
                I18nControls.bindI18nProperties(statusLabel, I18nKeys.upperCase(VideosI18nKeys.Available));
                actionButton.setOnAction(e -> {
                    browsingHistory.push(Page4SessionVideoPlayerRouting.getVideoOfSessionPath(scheduledItem.getId()));
                    transformButtonFromPlayToPlayAgain(actionButton);
                });
                actionButton.setVisible(true);
                if (expirationDate != null) {
                    //We schedule a refresh so the UI is updated when the expirationDate is reached
                    Duration duration = Duration.between(nowInEventTimezone, expirationDate);
                    if (duration.getSeconds() > 0) {
                        scheduleRefreshUI(duration.getSeconds());
                    }
                }
                return;
            }

            // in case the video delayed
            if (scheduledItem.isVodDelayed()) {
                I18nControls.bindI18nProperties(statusLabel, I18nKeys.upperCase(VideosI18nKeys.VideoDelayed));
                hideActionButton();
                scheduleRefreshUI(60);
                return;
            }

            //The live has ended, we're waiting for the video to be published
            if (!scheduledItem.isPublished()) {
                //The default value of the processing time if this parameter has not been entered
                int vodProcessingTimeMinute = getVodProcessingTimeMinute(scheduledItem);
                if (nowInEventTimezone.isAfter(sessionEnd.plusMinutes(vodProcessingTimeMinute))) {
                    I18nControls.bindI18nProperties(statusLabel, I18nKeys.upperCase(VideosI18nKeys.VideoDelayed));
                    hideActionButton();
                    //A push notification will tell us when the video recording is available
                    return;
                }

                I18nControls.bindI18nProperties(statusLabel, I18nKeys.upperCase(VideosI18nKeys.RecordingSoonAvailable));
                hideActionButton();
                //A push notification will tell us when the video recording is available
            }
        }

        private void scheduleRefreshUI(long delaySeconds) {
            long delayMillis = delaySeconds * 1000;
            if (delaySeconds > 59) {
                //If we want to refresh more than 1 minute, we add a second to make sure the calculation has time to proceed before the refresh
                delayMillis = delayMillis + 1000;
            }
            UiScheduler.scheduleDelay(delayMillis, this::computeStatusLabelAndWatchButton);
        }


        private void hideActionButton() {
            actionButton.setVisible(false);
            actionButton.setOnAction(null);
        }

        private int getVodProcessingTimeMinute(ScheduledItem currentVideo) {
            int vodProcessingTimeMinute = 60;
            if (currentVideo.getEvent().getVodProcessingTimeMinutes() != null)
                vodProcessingTimeMinute = currentVideo.getEvent().getVodProcessingTimeMinutes();
            return vodProcessingTimeMinute;
        }
    }

}
