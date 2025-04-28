package one.modality.event.frontoffice.activities.videos;

import dev.webfx.extras.cell.renderer.ValueRendererRegistry;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.time.format.LocalizedTime;
import dev.webfx.extras.type.PrimType;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.Objects;
import dev.webfx.stack.i18n.I18nKeys;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.domainmodel.formatter.FormatterRegistry;
import javafx.beans.property.ObjectProperty;
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

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * @author David Hello
 */
final class VideoColumnsFormattersAndRenderers {

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
            return LocalizedTime.formatLocalTimeRangeProperty((LocalTime) times[0], (LocalTime) times[1], FrontOfficeTimeFormats.VIDEO_DAY_TIME_FORMAT);
        });
        // videoName renderer
        ValueRendererRegistry.registerValueRenderer("videoName", (value /* expecting ScheduledItem */, context) -> {
            ScheduledItem video = (ScheduledItem) value; // value = 'this' = video ScheduledItem
            Label nameLabel = new Label();
            if (video.getProgramScheduledItem().isCancelled()) {
                I18nControls.bindI18nProperties(nameLabel, VideosI18nKeys.SessionCancelled);
                nameLabel.getStyleClass().add("session-cancelled");
            } else {
                nameLabel.setText(Objects.coalesce(video.getName(), video.getProgramScheduledItem().getName()));
                nameLabel.getStyleClass().add("name");
            }
            return ValueRendererRegistry.renderLabeled(nameLabel, true, true);
        });
        // videoStatus renderer
        ValueRendererRegistry.registerValueRenderer("videoStatus", (value, context) -> {
            ScheduledItem videoScheduledItem = (ScheduledItem) value;
            Button actionButton = Bootstrap.dangerButton(I18nControls.newButton(VideosI18nKeys.Watch));
            actionButton.setGraphicTextGap(10);
            Label statusLabel = new Label();
            Label availableUntilLabel = new Label();
            computeStatusLabelAndWatchButton(videoScheduledItem, statusLabel, availableUntilLabel, actionButton, context.getAppContext());
            VBox vBoxStatusAndButtonContainer = new VBox(10,
                actionButton,
                statusLabel,
                availableUntilLabel
            );
            vBoxStatusAndButtonContainer.setPadding(new Insets(10));
            vBoxStatusAndButtonContainer.setAlignment(Pos.CENTER);
            return vBoxStatusAndButtonContainer;
        });
    }

    // PRIVATE API

    private static void computeStatusLabelAndWatchButton(ScheduledItem videoScheduledItem, Label statusLabel, Label availableUntilLabel, Button actionButton, ObjectProperty<ScheduledItem> watchVideoItemProperty) {
        Runnable refresher = () -> computeStatusLabelAndWatchButton(videoScheduledItem, statusLabel, availableUntilLabel, actionButton, watchVideoItemProperty);

        // Setting default visibilities for most cases (to be changed in specific cases)
        hideButton(actionButton);
        showLabel(statusLabel);
        hideLabel(availableUntilLabel);

        String statusI18nKey = VideoState.getVideoStatusI18nKey(videoScheduledItem);
        Object statusI18nArg = null;
        VideoTimes videoTimes = new VideoTimes(videoScheduledItem);

        switch (statusI18nKey) {
            case VideosI18nKeys.OnTime:
                scheduleRefreshUIAt(videoTimes.getCountdownStart(), refresher);
                break;
            case VideosI18nKeys.StartingIn1:
                statusI18nArg = formatDuration(videoTimes.durationBetweenNowAndSessionStart());
                scheduleRefreshUI(1, refresher); //We refresh the countdown every second
                break;
            case VideosI18nKeys.LiveNow:
                scheduleRefreshUIAt(videoTimes.getSessionEnd(), refresher);
                break;
            case VideosI18nKeys.RecordingSoonAvailable:
            case VideosI18nKeys.VideoDelayed:
                scheduleRefreshUI(60, refresher); // Will may be different in 1 min due to push notification
                break;
            case VideosI18nKeys.Available:
                hideLabel(statusLabel);
                showButton(actionButton, e -> {
                    watchVideoItemProperty.set(videoScheduledItem);
                    transformButtonFromPlayToPlayAgain(actionButton);
                });
                LocalDateTime expirationDate = videoTimes.getExpirationDate();
                if (expirationDate != null) {
                    I18nControls.bindI18nProperties(availableUntilLabel, VideosI18nKeys.VideoAvailableUntil1, LocalizedTime.formatLocalDateTimeProperty(expirationDate, "dd MMM '-' HH.mm"));
                    showLabel(availableUntilLabel);
                    //We schedule a refresh so the UI is updated when the expirationDate is reached
                    scheduleRefreshUIAt(expirationDate, refresher);
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
        I18nControls.bindI18nProperties(actionButton, VideosI18nKeys.WatchAgain);
        actionButton.getStyleClass().clear();
        Bootstrap.secondaryButton(actionButton);
    }

    private static void scheduleRefreshUIAt(LocalDateTime scheduleDataTime, Runnable refresher) {
        LocalDateTime nowInEventTimezone = Event.nowInEventTimezone();
        Duration duration = Duration.between(nowInEventTimezone, scheduleDataTime);
        if (duration.getSeconds() > 0) {
            scheduleRefreshUI(duration.getSeconds(), refresher);
        }
    }

    private static void scheduleRefreshUI(long delaySeconds, Runnable refresher) {
        long delayMillis = delaySeconds * 1000;
        if (delaySeconds > 59) {
            //If we want to refresh more than 1 minute, we add a second to make sure the calculation has time to proceed before the refresh
            delayMillis = delayMillis + 1000;
        }
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
