package one.modality.event.frontoffice.activities.videostreaming;

import dev.webfx.extras.cell.renderer.ValueRendererRegistry;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.I18nKeys;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.time.format.LocalizedTime;
import dev.webfx.extras.type.PrimType;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.orm.domainmodel.formatter.FormatterRegistry;
import dev.webfx.stack.orm.entity.binding.EntityBindings;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableStringValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import one.modality.base.client.i18n.BaseI18nKeys;
import one.modality.base.client.i18n.I18nEntities;
import one.modality.base.client.message.receiver.ModalityEntityMessageReceiver;
import one.modality.base.client.time.FrontOfficeTimeFormats;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.util.ScheduledItems;
import one.modality.event.frontoffice.medias.MediaUtil;
import one.modality.event.frontoffice.medias.MediasI18nKeys;
import one.modality.event.frontoffice.medias.TimeZoneSwitch;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static one.modality.event.frontoffice.activities.videostreaming.VideoStreamingCssSelectors.*;

/**
 * @author Bruno Salmon
 * @author David Hello
 */
final class TimetableFormattersAndRenderers {

    // This padding is applied to the labels so that they have the same padding (at least vertically) as the buttons.
    // This is to make the rows the same height, whether there is a Watch button or a status label such as LIVE NOW.
    // The watch button is made via Bootstrap that applies default padding of 6 px on top and bottom, but buttons have
    // also a 1 px border, that's why we use 7 px on top and bottom for the label padding.
    // Note: the second benefit is that it makes it easier to click on the LIVE NOW hyperlink on mobiles
    private static final Insets LABEL_PADDING = new Insets(7);

    static void registerRenderers() {
        // Actually done (only once) in the static initializer below
    }

    static {
        // videoSection format
        FormatterRegistry.registerFormatter("videoSection", PrimType.STRING, scheduledItem ->
            VideoState.getVideoStatusI18nKey((ScheduledItem) scheduledItem));
        // videoDate format
        FormatterRegistry.registerFormatter("videoDate", PrimType.STRING, scheduledItem ->
            formatVideoDateOrTimes((ScheduledItem) scheduledItem, false));
        // videoTimeRange format
        FormatterRegistry.registerFormatter("videoTimeRange", PrimType.STRING, scheduledItem ->
            formatVideoDateOrTimes((ScheduledItem) scheduledItem, true));
        FormatterRegistry.registerFormatter("allProgramGroup", PrimType.STRING, scheduledItem ->
            I18n.i18nTextProperty(VideoState.getAllProgramVideoGroupI18nKey((ScheduledItem) scheduledItem))
        );
        // videoName renderer
        ValueRendererRegistry.registerValueRenderer("videoName", (value /* expecting ScheduledItem */, context) -> {
            ScheduledItem video = (ScheduledItem) value; // value = 'this' = video ScheduledItem
            Label nameLabel = new Label();
            nameLabel.setPadding(LABEL_PADDING);
            if (VideoState.isVideoCancelled(video)) {
                I18nControls.bindI18nProperties(nameLabel, MediasI18nKeys.SessionCancelled);
                nameLabel.getStyleClass().add(session_cancelled);
            } else {
                // Note: normally we should first try to translate `video` and then `programScheduledItem`, but the
                // `video` has the name repeated for some reason (but no label), while what we want is the label
                // defined on the program in this case, so for now we just use the `programScheduledItem` directly
                ScheduledItem namedVideo = video.getName() != null || video.getLabel() != null ? video : video.getProgramScheduledItem();
                I18nEntities.bindTranslatedEntityToTextProperty(nameLabel, namedVideo);
                nameLabel.getStyleClass().add(name);
            }
            return Controls.setupTextWrapping(nameLabel, true, true);
        });
        // videoStatus renderer
        ValueRendererRegistry.registerValueRenderer("videoStatus", (value, context) -> {
            ScheduledItem videoScheduledItem = (ScheduledItem) value;
            VideoStreamingActivity activity = context.getAppContext();

            ToggleButton watchButton = Bootstrap.dangerButton(I18nControls.newToggleButton(VideoStreamingI18nKeys.Watch));
            activity.timetable.watchButtonsGroup.registerItemButton(watchButton, videoScheduledItem, true);
            watchButton.setGraphicTextGap(10);
            Label statusLabel = new Label();
            statusLabel.setPadding(LABEL_PADDING);
            Label availableUntilLabel = new Label(); // No need to set padding as it is under the Watch button
            Hyperlink liveNowLink = I18nControls.newHyperlink(I18nKeys.upperCase(VideoStreamingI18nKeys.LiveNow));
            liveNowLink.setPadding(LABEL_PADDING);
            // Commented, as this makes the row bigger (this padding is added to the cell padding)
            // liveNowLink.setPadding(new Insets(15)); // this is to make it easier to click, especially on mobiles
            Controls.setupTextWrapping(availableUntilLabel, true, false);
            // Initial computation of the status
            StatusElements se = new StatusElements(videoScheduledItem, statusLabel, availableUntilLabel, liveNowLink, watchButton, activity.livestreamAndVideoPlayers);
            updateStatusElements(se, true);
            VBox vBoxStatusAndButtonContainer = new VBox(10,
                watchButton,
                statusLabel,
                liveNowLink,
                availableUntilLabel
            );

            // Push-notification management: we turn the published field into a property
            ModalityEntityMessageReceiver.getFrontOfficeEntityMessageReceiver().listenEntityChanges(videoScheduledItem.getStore());
            BooleanProperty isPublishedProperty = EntityBindings.getBooleanFieldProperty(videoScheduledItem, ScheduledItem.published);

            // Starting a regular update of the status over the time to reflect the video lifecycle
            FXProperties.runOnPropertyChange(() -> Platform.runLater(() -> updateStatusElements(se, false)), isPublishedProperty);

            vBoxStatusAndButtonContainer.setPadding(new Insets(10));
            vBoxStatusAndButtonContainer.setAlignment(Pos.CENTER);
            return vBoxStatusAndButtonContainer;
        });

    }

    // PRIVATE API

    private static ObservableStringValue formatVideoDateOrTimes(ScheduledItem scheduledItem, boolean times) {
        TimeZoneSwitch globalTimeZoneSwitch = TimeZoneSwitch.getGlobal();
        boolean eventLocalTimeSelected = globalTimeZoneSwitch.isEventLocalTimeSelected();
        LocalDate eventLocalDate = scheduledItem.getDate();
        LocalTime eventLocalStartTime = ScheduledItems.getSessionStartTime(scheduledItem);
        if (times) {
            LocalTime displayStartTime = eventLocalTimeSelected ? eventLocalStartTime : globalTimeZoneSwitch.convertEventLocalTimeToUserLocalTime(eventLocalDate, eventLocalStartTime);
            LocalTime eventLocalEndTime = ScheduledItems.getSessionEndTime(scheduledItem);
            LocalTime displayEndTime = eventLocalTimeSelected ? eventLocalEndTime : globalTimeZoneSwitch.convertEventLocalTimeToUserLocalTime(eventLocalDate, eventLocalEndTime);
            return LocalizedTime.formatLocalTimeRangeProperty(displayStartTime, displayEndTime, FrontOfficeTimeFormats.AUDIO_VIDEO_DAY_TIME_FORMAT);
        }
        LocalDateTime eventLocalStartDateTime = LocalDateTime.of(eventLocalDate, eventLocalStartTime);
        LocalDateTime displayLocalStartDateTime = eventLocalTimeSelected ? eventLocalStartDateTime : globalTimeZoneSwitch.convertEventLocalDateTimeToUserLocalDateTime(eventLocalStartDateTime);
        LocalDate displayLocalDate = displayLocalStartDateTime.toLocalDate();
        return LocalizedTime.formatMonthDayProperty(displayLocalDate, FrontOfficeTimeFormats.VOD_TODAY_MONTH_DAY_FORMAT);
    }

    @SuppressWarnings("unusable-by-js")
    private record StatusElements(ScheduledItem videoScheduledItem, Label statusLabel, Label availableUntilLabel, Hyperlink liveNowLink, ButtonBase watchButton, LivestreamAndVideoPlayers livestreamAndVideoPlayers) { }

    private static void updateStatusElements(StatusElements se, boolean initial) {
        // Stopping refreshing labels and button once removed from the scene (due to responsive design)
        if (!initial && se.statusLabel.getScene() == null)
            return;

        Runnable refresher = () -> updateStatusElements(se, false);

        // Setting default visibilities for most cases (to be changed in specific cases)
        hideButton(se.watchButton);
        showLabeled(se.statusLabel);
        hideLabeled(se.liveNowLink);
        hideLabeled(se.availableUntilLabel);

        Object statusI18nKey = VideoState.getVideoStatusI18nKey(se.videoScheduledItem);
        Object statusI18nArg = null;
        VideoLifecycle videoLifecycle = new VideoLifecycle(se.videoScheduledItem);
        boolean hideOrShowWatchButton = false;
        Event event = se.videoScheduledItem.getEvent();

        // TODO: move this to switch(Object) in Java 21
        if (statusI18nKey.equals(VideoStreamingI18nKeys.OnTime)) {
            scheduleRefreshAt(event, videoLifecycle.getCountdownStart(), refresher);
        } else if (statusI18nKey.equals(VideoStreamingI18nKeys.StartingIn1)) {
            statusI18nArg = MediaUtil.formatDuration(videoLifecycle.durationBetweenNowAndSessionStart());
            scheduleRefreshSeconds(1, refresher); // We refresh the countdown every second
            hideOrShowWatchButton = true;
        } else if (statusI18nKey.equals(VideoStreamingI18nKeys.LiveNow)) {
            se.liveNowLink.setOnAction(e -> se.livestreamAndVideoPlayers.setWatchingVideo(videoLifecycle));
            hideLabeled(se.statusLabel);
            showLabeled(se.liveNowLink);
            scheduleRefreshDuration(videoLifecycle.durationBetweenNowAndSessionEnd(), refresher);
            hideOrShowWatchButton = true;
        } else if (statusI18nKey.equals(VideoStreamingI18nKeys.RecordingSoonAvailable) || statusI18nKey.equals(VideoStreamingI18nKeys.VideoDelayed)) {
            scheduleRefreshSeconds(60, refresher); // Maybe different in 1 min due to push notification
        } else if (statusI18nKey.equals(BaseI18nKeys.Available)) {
            hideLabeled(se.statusLabel);
            showButton(se.watchButton, e -> {
                se.livestreamAndVideoPlayers.setWatchingVideo(videoLifecycle);
                transformButtonFromPlayToPlayAgain(se.watchButton);
            });
            LocalDateTime videoSpecificExpirationDate = se.videoScheduledItem.getExpirationDate();
            if (videoSpecificExpirationDate != null) {
                TimeZoneSwitch globalTimeZoneSwitch = TimeZoneSwitch.getGlobal();
                FXProperties.runNowAndOnPropertyChange(eventTimeSelected -> {
                    LocalDateTime userTimezoneExpirationDate = eventTimeSelected ? videoSpecificExpirationDate : globalTimeZoneSwitch.convertEventLocalDateTimeToUserLocalDateTime(videoSpecificExpirationDate);
                    I18nControls.bindI18nProperties(se.availableUntilLabel, MediasI18nKeys.AvailableUntil1, LocalizedTime.formatLocalDateTimeProperty(userTimezoneExpirationDate, "dd MMM '-' HH.mm"));
                }, globalTimeZoneSwitch.eventLocalTimeSelectedProperty());
                showLabeled(se.availableUntilLabel);
                // We schedule a refresh so the UI is updated when the expirationDate is reached
                scheduleRefreshAt(event, videoSpecificExpirationDate, refresher);
            } else {
                LocalDateTime generalEventExpirationDate = event.getVodExpirationDate();
                if (generalEventExpirationDate != null) {
                    scheduleRefreshAt(event, generalEventExpirationDate, refresher);
                }
            }
        }
        if (hideOrShowWatchButton) {
            // In case a user clicked on a previous recorded video, we need to display a button so he can go back to the livestream
            if (se.livestreamAndVideoPlayers.isSameVideoAsAlreadyWatching(videoLifecycle)) {
                hideButton(se.watchButton);
            } else if (!videoLifecycle.isNowBeforeLiveNowStart()) { // Preventing Watch button to appear to early (if published = true wrongly set on ScheduledItem)
                showButton(se.watchButton, e -> se.livestreamAndVideoPlayers.setWatchingVideo(videoLifecycle));
                hideLabeled(se.liveNowLink);
            }
            // We may also need to update the button again when the user changes the watching video
            String arbitraryKey = "watchingVideoItemPropertyListener";
            if (!se.watchButton.getProperties().containsKey(arbitraryKey)) { // we install that listener only once
                se.watchButton.getProperties().put(arbitraryKey,
                    FXProperties.runOnPropertyChange(refresher, se.livestreamAndVideoPlayers.watchingVideoItemProperty()));
            }
        }

        I18nControls.bindI18nProperties(se.statusLabel, I18nKeys.upperCase(statusI18nKey), statusI18nArg);
    }

    private static void hideLabeled(Labeled label) {
        showLabeled(label, false);
    }

    private static void showLabeled(Labeled label) {
        showLabeled(label, true);
    }

    private static void showLabeled(Labeled label, boolean show) {
        Layouts.setManagedAndVisibleProperties(label, show);
        label.setDisable(!show); // to prevent it gaining focus
    }

    private static void hideButton(ButtonBase button) {
        hideLabeled(button);
        button.setOnAction(null);
    }

    private static void showButton(ButtonBase button, EventHandler<ActionEvent> actionEvent) {
        showLabeled(button);
        button.setOnAction(actionEvent);
    }

    private static void transformButtonFromPlayToPlayAgain(ButtonBase actionButton) {
        I18nControls.bindI18nProperties(actionButton, VideoStreamingI18nKeys.WatchAgain);
        actionButton.getStyleClass().clear();
        Bootstrap.secondaryButton(actionButton);
    }

    private static void scheduleRefreshAt(Event event, LocalDateTime scheduleDataTime, Runnable refresher) {
        scheduleRefreshDuration(Duration.between(event.nowInEventTimezone(), scheduleDataTime), refresher);
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

}
