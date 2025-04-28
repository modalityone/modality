package one.modality.event.frontoffice.activities.audiorecordings;

import dev.webfx.extras.cell.renderer.ValueRendererRegistry;
import dev.webfx.extras.media.metadata.MediaMetadataBuilder;
import dev.webfx.extras.player.Player;
import dev.webfx.extras.player.audio.javafxmedia.AudioMediaView;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.time.format.LocalizedTime;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.Unregisterable;
import dev.webfx.platform.blob.spi.BlobProvider;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.Booleans;
import dev.webfx.platform.util.Objects;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.i18n.I18nKeys;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.ui.operation.OperationUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import one.modality.base.client.bootstrap.ModalityStyle;
import one.modality.base.client.time.FrontOfficeTimeFormats;
import one.modality.base.shared.entities.Media;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.Timeline;
import one.modality.event.frontoffice.medias.MediaConsumptionRecorder;

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
            ScheduledItem audio = (ScheduledItem) value; // value = 'this' = audio ScheduledItem
            Label nameLabel = new Label();
            if (audio.getProgramScheduledItem().isCancelled()) {
                I18nControls.bindI18nProperties(nameLabel, AudioRecordingsI18nKeys.SessionCancelled);
                nameLabel.getStyleClass().add("session-cancelled");
            } else {
                nameLabel.setText(getAudioName(audio));
                nameLabel.getStyleClass().add("name");
            }
            ValueRendererRegistry.renderLabeled(nameLabel, true, false);
            LocalDate date = audio.getDate();
            Timeline timeline = audio.getProgramScheduledItem().getTimeline();
            LocalTime startTime = timeline == null ? null : timeline.getStartTime();
            Label dateLabel = new Label();
            dateLabel.textProperty().bind(LocalizedTime.formatLocalDateTimeProperty(date, startTime, FrontOfficeTimeFormats.AUDIO_TRACK_DATE_TIME_FORMAT));
            dateLabel.getStyleClass().add(ModalityStyle.TEXT_COMMENT);
            long durationMillis;
            AudioColumnsContext audioContext = context.getAppContext();
            if (!audioContext.publishedMedias().isEmpty()) {
                Media firstMedia = Collections.findFirst(audioContext.publishedMedias(), media -> Entities.sameId(audio, media.getScheduledItem()));
                if (firstMedia != null) {
                    durationMillis = firstMedia.getDurationMillis();
                    dateLabel.textProperty().unbind();
                    dateLabel.setText(AudioMediaView.formatDuration(durationMillis) + " • " + dateLabel.getText());
                }
            }
            VBox vbox = new VBox(2, nameLabel, dateLabel);
            vbox.setAlignment(Pos.BOTTOM_LEFT);
            return vbox;
        });
        ValueRendererRegistry.registerValueRenderer("audioButtons", (value /* expecting ScheduledItem */, context /* expecting AudioColumnsContext */) -> {
            ScheduledItem audio = (ScheduledItem) value; // value = 'this' = audio ScheduledItem
            if (audio.getProgramScheduledItem().isCancelled()) {
                Label cancelledLabel = I18nControls.newLabel(I18nKeys.upperCase(AudioRecordingsI18nKeys.AudioCancelled));
                cancelledLabel.getStyleClass().add("cancelled");
                return cancelledLabel;
            }
            AudioColumnsContext audioContext = context.getAppContext();
            Media firstMedia = Collections.findFirst(audioContext.publishedMedias(), media -> Entities.sameId(audio, media.getScheduledItem()));
            if (firstMedia == null) {
                Label notYetPublishedLabel = I18nControls.newLabel(AudioRecordingsI18nKeys.AudioRecordingNotYetPublished);
                notYetPublishedLabel.getStyleClass().add(ModalityStyle.TEXT_COMMENT);
                notYetPublishedLabel.setMaxWidth(Double.MAX_VALUE);
                notYetPublishedLabel.setAlignment(Pos.CENTER_RIGHT);
                return notYetPublishedLabel;
            }
            HBox hBox = new HBox(10, createAudioButton(audio, firstMedia, audioContext.audioPlayer(), false), createAudioButton(audio, firstMedia, audioContext.audioPlayer(),true));
            hBox.setAlignment(Pos.CENTER);
            hBox.setPadding(new Insets(0, 0, 10, 0));
            return hBox;
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
                // 1) We download the file. Note: there is no way to track the progress of the download...
                downloadFile(firstMedia.getUrl());
                // 2) We record this action using MediaConsumptionRecorder
                MediaConsumptionRecorder.recordDownloadMediaConsumption(audio, firstMedia);
                // 3) Sometimes (especially on mobiles) the system can take a few seconds before showing there is a
                // download in progress, giving the impression that nothing happens and making the user pressing
                // the button several times. To prevent this, we disable the download button for 5s.
                OperationUtil.turnOnButtonsWaitMode(button);
                UiScheduler.scheduleDelay(5000, () -> OperationUtil.turnOffButtonsWaitMode(button));
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
        I18nControls.bindI18nProperties(button, AudioRecordingsI18nKeys.Play);
    }

    private static void showButtonAsPlaying(Button button) {
        button.getStyleClass().removeAll(Bootstrap.DANGER, Bootstrap.SECONDARY);
        Bootstrap.successButton(button);
        I18nControls.bindI18nProperties(button, AudioRecordingsI18nKeys.Playing);
    }

    private static void showButtonAsPlayAgain(Button button) {
        button.getStyleClass().removeAll(Bootstrap.DANGER, Bootstrap.SUCCESS);
        Bootstrap.secondaryButton(button);
        I18nControls.bindI18nProperties(button, AudioRecordingsI18nKeys.PlayAgain);
    }

    private static void showButtonAsDownload(Button button) {
        ModalityStyle.blackButton(I18nControls.bindI18nProperties(button, AudioRecordingsI18nKeys.Download));
    }

}
