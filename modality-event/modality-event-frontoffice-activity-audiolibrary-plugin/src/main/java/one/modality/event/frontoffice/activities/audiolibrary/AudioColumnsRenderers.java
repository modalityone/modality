package one.modality.event.frontoffice.activities.audiolibrary;

import dev.webfx.extras.async.AsyncSpinner;
import dev.webfx.extras.cell.renderer.ValueRendererRegistry;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.I18nKeys;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.media.metadata.MediaMetadataBuilder;
import dev.webfx.extras.player.Player;
import dev.webfx.extras.player.audio.javafxmedia.AudioMediaView;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.time.format.LocalizedTime;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.Unregisterable;
import dev.webfx.platform.blob.spi.BlobProvider;
import dev.webfx.platform.storage.LocalStorage;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.Booleans;
import dev.webfx.platform.util.Objects;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.orm.entity.Entities;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import one.modality.base.client.bootstrap.ModalityStyle;
import one.modality.base.client.time.FrontOfficeTimeFormats;
import one.modality.base.shared.entities.Media;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.markers.EntityHasStartAndEndTime;
import one.modality.event.frontoffice.medias.MediaConsumptionRecorder;
import one.modality.event.frontoffice.medias.MediaUtil;
import one.modality.event.frontoffice.medias.MediasI18nKeys;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * @author Bruno Salmon
 */
final class AudioColumnsRenderers {

    static void registerRenderers() {
        // Actually done (only once) in the static initializer below
    }

    static {
        ValueRendererRegistry.registerValueRenderer("audioName", (value /* expecting ScheduledItem */, context /* expecting AudioColumnsContext */) -> {
            ScheduledItem audioScheduledItem = (ScheduledItem) value; // value = 'this' = audio ScheduledItem
            Label nameLabel = new Label();
            if (audioScheduledItem.getProgramScheduledItem().isCancelled()) {
                I18nControls.bindI18nProperties(nameLabel, MediasI18nKeys.SessionCancelled);
                nameLabel.getStyleClass().add("session-cancelled");
            } else {
                nameLabel.setText(getAudioName(audioScheduledItem));
                nameLabel.getStyleClass().add("name");
            }
            Controls.setupTextWrapping(nameLabel, true, false);
            LocalDate date = audioScheduledItem.getDate();
            EntityHasStartAndEndTime startAndEndTimeHolder = MediaUtil.getStartAndEndTimeHolder(audioScheduledItem);
            LocalTime startTime = startAndEndTimeHolder.getStartTime();
            EventAudioLibraryActivity activity = context.getAppContext();
            Media firstMedia = Collections.findFirst(activity.getPublishedMedias(), media -> Entities.sameId(audioScheduledItem, media.getScheduledItem()));
            long durationMillis = firstMedia == null ? 0 : firstMedia.getDurationMillis();
            Text dateText = I18n.newText("{0}{1} {2}",  durationMillis == 0 ? "" : AudioMediaView.formatDuration(durationMillis) + " • ", LocalizedTime.formatLocalDateProperty(date, FrontOfficeTimeFormats.AUDIO_TRACK_DATE_TIME_FORMAT), LocalizedTime.formatLocalTimeProperty(startTime, FrontOfficeTimeFormats.AUDIO_VIDEO_DAY_TIME_FORMAT));
            dateText.getStyleClass().add(ModalityStyle.TEXT_COMMENT);
            VBox vbox = new VBox(2, nameLabel, dateText);
            vbox.setAlignment(Pos.BOTTOM_LEFT);
            return vbox;
        });
        ValueRendererRegistry.registerValueRenderer("audioButtons", (value /* expecting ScheduledItem */, context /* expecting AudioColumnsContext */) -> {
            ScheduledItem audio = (ScheduledItem) value; // value = 'this' = audio ScheduledItem
            if (audio.getProgramScheduledItem().isCancelled()) {
                Label cancelledLabel = I18nControls.newLabel(I18nKeys.upperCase(AudioLibraryI18nKeys.AudioCancelled));
                cancelledLabel.getStyleClass().add("cancelled");
                return cancelledLabel;
            }
            EventAudioLibraryActivity activity = context.getAppContext();
            Media firstMedia = Collections.findFirst(activity.getPublishedMedias(), media -> Entities.sameId(audio, media.getScheduledItem()));
            if (firstMedia == null) {
                Label notYetPublishedLabel = I18nControls.newLabel(AudioLibraryI18nKeys.AudioLibraryNotYetPublished);
                notYetPublishedLabel.getStyleClass().add(ModalityStyle.TEXT_COMMENT);
                notYetPublishedLabel.setMaxWidth(Double.MAX_VALUE);
                notYetPublishedLabel.setAlignment(Pos.CENTER_RIGHT);
                return notYetPublishedLabel;
            }
            Button playButtonButton = createAudioButton(audio, firstMedia, activity.getAudioPlayer(), false);
            Button downloadButton = createAudioButton(audio, firstMedia, activity.getAudioPlayer(), true);
            // Using a flow pane for the button, so that buttons are laid out vertically if they don't fit in the width
            // (typically on mobiles).
            FlowPane buttonBar = new FlowPane(10, 10, playButtonButton, downloadButton);
            buttonBar.setAlignment(Pos.CENTER);
            buttonBar.setPadding(new Insets(0, 0, 10, 0));
            return buttonBar;
        });
    }

    private static Button createAudioButton(ScheduledItem audio, Media firstMedia, Player audioPlayer, boolean download) {
        Button button = new Button();
        button.setGraphicTextGap(10);
        button.setMinWidth(150);
        var playerMedia = download ? null : audioPlayer.acceptMedia(firstMedia.getUrl(), new MediaMetadataBuilder()
            .setTitle(getAudioName(audio)).setDurationMillis(firstMedia.getDurationMillis()).build());
        updateButton(button, audio, firstMedia, audioPlayer, download);
        button.setOnAction(event -> {
            if (download) { // Download action
                String url = firstMedia.getUrl();
                boolean useProxy = Booleans.isTrue(LocalStorage.getItem("modality-download-proxy"));
                if (useProxy) {
                    url = "/proxy/" + url;
                }
                // 1) We download the file. Note: there is no way to track the progress of the download...
                downloadFile(url);
                // 2) We record this action using MediaConsumptionRecorder
                MediaConsumptionRecorder.recordDownloadMediaConsumption(audio, firstMedia);
                // 3) Sometimes (especially on mobiles) the system can take a few seconds before showing there is a
                // download in progress, giving the impression that nothing happens and making the user pressing
                // the button several times. To prevent this, we disable the download button for 5s.
                AsyncSpinner.displayButtonSpinner(button);
                UiScheduler.scheduleDelay(5000, () -> AsyncSpinner.hideButtonSpinner(button));
            } else if (!Objects.areEquals(audioPlayer.getMedia(), playerMedia)) { // Play action
                audioPlayer.resetToInitialState();
                audioPlayer.setMedia(playerMedia);
                audioPlayer.play();
                updateButton(button, audio, firstMedia, audioPlayer, download);
                playerMedia.setUserData(firstMedia); // used later to check which track the audio player is playing (see below)
                // Playing MediaConsumption management
                new MediaConsumptionRecorder(audioPlayer, false, () -> audio, () -> firstMedia)
                        .start();
            }
        });
        return button;
    }

    private static String getAudioName(ScheduledItem audio) {
        return Objects.coalesce(audio.getName(), audio.getProgramScheduledItem().getName());
    }

    private static void downloadFile(String fileUrl) {
        BlobProvider.get().downloadUrl(fileUrl);
    }

    private static final Unregisterable[] unregisterable = { null };

    private static void updateButton(Button button, ScheduledItem audio, Media firstMedia, Player audioPlayer, boolean download) {
        if (download) {
            showButtonAsDownload(button);
        } else if (isAudioPlayerPlayingMedia(audioPlayer, firstMedia)) {
            showButtonAsPlaying(button);
            if (unregisterable[0] != null)
                unregisterable[0].unregister();
            unregisterable[0] = FXProperties.runNowAndOnPropertiesChange(() -> {
                if (!isAudioPlayerPlayingMedia(audioPlayer, firstMedia)) {
                    showButtonAsPlayAgain(button);
                    unregisterable[0].unregister();
                }
            }, audioPlayer.mediaProperty(), audioPlayer.statusProperty());
        } else if (Booleans.isTrue(audio.getFieldValue("alreadyPlayed")))
            showButtonAsPlayAgain(button);
        else
            showButtonAsPlay(button);
    }

    private static boolean isAudioPlayerPlayingMedia(Player audioPlayer, Media media) {
        if (audioPlayer.getMedia() == null)
            return false;
        if (!Objects.areEquals(audioPlayer.getMedia().getSource(), media.getUrl()))
            return false;
        if (audioPlayer.getStatus() == null)
            return true;
        return switch (audioPlayer.getStatus()) {
            case LOADING, READY, PLAYING, PAUSED -> true;
            default -> false;
        };
    }

    private static void showButtonAsPlay(Button button) {
        button.getStyleClass().removeAll(Bootstrap.SUCCESS, Bootstrap.SECONDARY);
        Bootstrap.dangerButton(button);
        I18nControls.bindI18nProperties(button, AudioLibraryI18nKeys.Play);
    }

    private static void showButtonAsPlaying(Button button) {
        button.getStyleClass().removeAll(Bootstrap.DANGER, Bootstrap.SECONDARY);
        Bootstrap.successButton(button);
        I18nControls.bindI18nProperties(button, AudioLibraryI18nKeys.Playing);
    }

    private static void showButtonAsPlayAgain(Button button) {
        button.getStyleClass().removeAll(Bootstrap.DANGER, Bootstrap.SUCCESS);
        Bootstrap.secondaryButton(button);
        I18nControls.bindI18nProperties(button, AudioLibraryI18nKeys.PlayAgain);
    }

    private static void showButtonAsDownload(Button button) {
        ModalityStyle.blackButton(I18nControls.bindI18nProperties(button, AudioLibraryI18nKeys.Download));
    }

}
