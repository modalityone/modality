package one.modality.event.frontoffice.activities.videostreaming;

import dev.webfx.extras.cell.renderer.ValueRendererRegistry;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.time.format.LocalizedTime;
import dev.webfx.extras.type.PrimType;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.I18nKeys;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.i18n.spi.impl.I18nSubKey;
import dev.webfx.stack.orm.domainmodel.formatter.FormatterRegistry;
import dev.webfx.stack.orm.entity.binding.EntityBindings;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.layout.VBox;
import one.modality.base.client.i18n.BaseI18nKeys;
import one.modality.base.client.messaging.ModalityMessaging;
import one.modality.base.client.time.FrontOfficeTimeFormats;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.ScheduledItem;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * @author Bruno Salmon
 * @author David Hello
 */
final class VideoFormattersAndRenderers {

    static void registerRenderers() {
        // Actually done (only once) in the static initializer below
    }

    static {
        // videoSection format
        FormatterRegistry.registerFormatter("videoSection", PrimType.STRING, scheduledItem ->
            VideoState.getVideoStatusI18nKey((ScheduledItem) scheduledItem));
        // videoDate format
        FormatterRegistry.registerFormatter("videoDate", PrimType.STRING, date ->
            LocalizedTime.formatMonthDayProperty((LocalDate) date, FrontOfficeTimeFormats.VOD_TODAY_MONTH_DAY_FORMAT));
        // videoTimeRange format
        FormatterRegistry.registerFormatter("videoTimeRange", PrimType.STRING, timeRange -> {
            Object[] times = (Object[]) timeRange;
            LocalTime startEventLocalTime = (LocalTime) times[0];
            LocalTime endEventLocalTime = (LocalTime) times[1];
            StringProperty videoTimeRangeProperty = new SimpleStringProperty();
            FXProperties.runNowAndOnPropertyChange(eventTimeSelected -> {
                LocalTime startDisplayTime = eventTimeSelected ? startEventLocalTime : TimeZoneSwitch.convertEventLocalTimeToUserLocalTime(startEventLocalTime);
                LocalTime endDisplayTime = eventTimeSelected ? endEventLocalTime : TimeZoneSwitch.convertEventLocalTimeToUserLocalTime(endEventLocalTime);
                videoTimeRangeProperty.bind(LocalizedTime.formatLocalTimeRangeProperty(startDisplayTime, endDisplayTime, FrontOfficeTimeFormats.VIDEO_DAY_TIME_FORMAT));
            }, TimeZoneSwitch.eventLocalTimeSelectedProperty());
            return videoTimeRangeProperty;
        });
        FormatterRegistry.registerFormatter("allProgramGroup", PrimType.STRING, scheduledItem ->
            I18n.i18nTextProperty(VideoState.getAllProgramVideoGroupI18nKey((ScheduledItem) scheduledItem))
        );
        // videoName renderer
        ValueRendererRegistry.registerValueRenderer("videoName", (value /* expecting ScheduledItem */, context) -> {
            ScheduledItem video = (ScheduledItem) value; // value = 'this' = video ScheduledItem
            Label nameLabel = new Label();
            if (VideoState.isVideoCancelled(video)) {
                I18nControls.bindI18nProperties(nameLabel, VideoStreamingI18nKeys.SessionCancelled);
                nameLabel.getStyleClass().add("session-cancelled");
            } else {
                // Note: normally we should first try to translate `video` and then `programScheduledItem`, but the
                // `video` has the name repeated for some reason (but no label), while what we want is the label
                // defined on the program in this case, so for now we just use the `programScheduledItem` directly
                I18nControls.bindI18nTextProperty(nameLabel, new I18nSubKey("expression: i18n(this)", video.getProgramScheduledItem()));
                nameLabel.getStyleClass().add("name");
            }
            return Controls.setupTextWrapping(nameLabel, true, true);
        });
        // videoStatus renderer
        ValueRendererRegistry.registerValueRenderer("videoStatus", (value, context) -> {
            ScheduledItem videoScheduledItem = (ScheduledItem) value;

            Button actionButton = Bootstrap.dangerButton(I18nControls.newButton(VideoStreamingI18nKeys.Watch));
            actionButton.setGraphicTextGap(10);
            Label statusLabel = new Label();
            Label availableUntilLabel = new Label();
            Controls.setupTextWrapping(availableUntilLabel, true, false);
            computeStatusLabelAndWatchButton(videoScheduledItem, statusLabel, availableUntilLabel, actionButton, context.getAppContext(), true);
            VBox vBoxStatusAndButtonContainer = new VBox(10,
                actionButton,
                statusLabel,
                availableUntilLabel
            );
            //If the publishedProperty change, we Update the button
            ModalityMessaging.getFrontOfficeEntityMessaging().listenEntityChanges(videoScheduledItem.getStore());
            BooleanProperty isPublishedProperty = EntityBindings.getBooleanFieldProperty(videoScheduledItem, ScheduledItem.published);
            FXProperties.runOnPropertyChange(() -> Platform.runLater(() -> computeStatusLabelAndWatchButton(videoScheduledItem, statusLabel, availableUntilLabel, actionButton, context.getAppContext(), false)), isPublishedProperty);
            vBoxStatusAndButtonContainer.setPadding(new Insets(10));
            vBoxStatusAndButtonContainer.setAlignment(Pos.CENTER);
            return vBoxStatusAndButtonContainer;
        });

    }

    // PRIVATE API

    private static void computeStatusLabelAndWatchButton(ScheduledItem videoScheduledItem, Label statusLabel, Label availableUntilLabel, Button actionButton, VideoStreamingActivity videoStreamingActivity, boolean initial) {
        // Stopping refreshing labels and button once removed from the scene (due to responsive design)
        if (!initial && statusLabel.getScene() == null)
            return;

        Runnable refresher = () -> computeStatusLabelAndWatchButton(videoScheduledItem, statusLabel, availableUntilLabel, actionButton, videoStreamingActivity, false);

        // Setting default visibilities for most cases (to be changed in specific cases)
        hideButton(actionButton);
        showLabel(statusLabel);
        hideLabel(availableUntilLabel);

        String statusI18nKey = VideoState.getVideoStatusI18nKey(videoScheduledItem);
        Object statusI18nArg = null;
        VideoLifecycle videoLifecycle = new VideoLifecycle(videoScheduledItem);
        boolean hideOrShowLivestreamButton = false;

        switch (statusI18nKey) {
            case VideoStreamingI18nKeys.OnTime:
                scheduleRefreshAt(videoLifecycle.getCountdownStart(), refresher);
                break;
            case VideoStreamingI18nKeys.StartingIn1:
                statusI18nArg = formatDuration(videoLifecycle.durationBetweenNowAndSessionStart());
                scheduleRefreshSeconds(1, refresher); // We refresh the countdown every second
                hideOrShowLivestreamButton = true;
                break;
            case VideoStreamingI18nKeys.LiveNow:
                scheduleRefreshDuration(videoLifecycle.durationBetweenNowAndSessionEnd(), refresher);
                hideOrShowLivestreamButton = true;
                break;
            case VideoStreamingI18nKeys.RecordingSoonAvailable:
            case VideoStreamingI18nKeys.VideoDelayed:
                scheduleRefreshSeconds(60, refresher); // Maybe different in 1 min due to push notification
                break;
            case BaseI18nKeys.Available:
                hideLabel(statusLabel);
                showButton(actionButton, e -> {
                    videoStreamingActivity.setWatchingVideo(videoLifecycle);
                    transformButtonFromPlayToPlayAgain(actionButton);
                });
                LocalDateTime expirationDate = videoLifecycle.getExpirationDate();
                if (expirationDate != null) {
                    I18nControls.bindI18nProperties(availableUntilLabel, VideoStreamingI18nKeys.VideoAvailableUntil1, LocalizedTime.formatLocalDateTimeProperty(expirationDate, "dd MMM '-' HH.mm"));
                    showLabel(availableUntilLabel);
                    // We schedule a refresh so the UI is updated when the expirationDate is reached
                    scheduleRefreshAt(expirationDate, refresher);
                }
        }
        if (hideOrShowLivestreamButton) {
            // In case a user clicked on a previous recorded video, we need to display a button so he can go back to the livestream
            if (videoStreamingActivity.isSameVideoAsAlreadyWatching(videoLifecycle))
                hideButton(actionButton);
            else
                showButton(actionButton, e -> videoStreamingActivity.setWatchingVideo(videoLifecycle));
            // We may also need to update the button again when the user changes the watching video
            if (!actionButton.getProperties().containsKey("watchingVideoItemPropertyListener")) { // we install that listener only once
                actionButton.getProperties().put("watchingVideoItemPropertyListener",
                    FXProperties.runOnPropertyChange(refresher, videoStreamingActivity.watchingVideoItemProperty()));
            }
        }

        I18nControls.bindI18nProperties(statusLabel, I18nKeys.upperCase(statusI18nKey), statusI18nArg);
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
        I18nControls.bindI18nProperties(actionButton, VideoStreamingI18nKeys.WatchAgain);
        actionButton.getStyleClass().clear();
        Bootstrap.secondaryButton(actionButton);
    }

    private static void scheduleRefreshAt(LocalDateTime scheduleDataTime, Runnable refresher) {
        scheduleRefreshDuration(Duration.between(Event.nowInEventTimezone(), scheduleDataTime), refresher);
    }

    private static void scheduleRefreshDuration(Duration duration, Runnable refresher) {
        scheduleRefreshMillis(duration.toMillis(), refresher);
    }

    private static void scheduleRefreshSeconds(long delaySeconds, Runnable refresher) {
        if (delaySeconds > 59) {
            //If we want to refresh more than 1 minute, we add a second to make sure the calculation has time to proceed before the refresh
            delaySeconds++;
        }
        scheduleRefreshMillis(delaySeconds * 1000, refresher);
    }

    private static void scheduleRefreshMillis(long delayMillis, Runnable refresher) {
        if (delayMillis > 0)
            UiScheduler.scheduleDelay(delayMillis, refresher);
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
