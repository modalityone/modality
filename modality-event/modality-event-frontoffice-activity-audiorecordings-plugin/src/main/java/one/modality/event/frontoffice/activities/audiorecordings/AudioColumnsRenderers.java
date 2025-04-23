package one.modality.event.frontoffice.activities.audiorecordings;

import dev.webfx.extras.cell.renderer.ValueRendererRegistry;
import dev.webfx.extras.media.metadata.MediaMetadataBuilder;
import dev.webfx.extras.player.Player;
import dev.webfx.extras.player.audio.javafxmedia.AudioMediaView;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.time.format.LocalizedTime;
import dev.webfx.platform.blob.spi.BlobProvider;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.Booleans;
import dev.webfx.platform.util.Objects;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.ui.operation.OperationUtil;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
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

    static {
        ValueRendererRegistry.registerValueRenderer("audioName", (value, context) -> {
            ScheduledItem audio = (ScheduledItem) value;
            Label titleLabel = new Label(getAudioName(audio));
            titleLabel.getStyleClass().add("description");
            titleLabel.setWrapText(true);
            titleLabel.setMinHeight(Region.USE_PREF_SIZE);
            LocalDate date = audio.getDate();
            Timeline timeline = audio.getProgramScheduledItem().getTimeline();
            LocalTime startTime = timeline == null ? null : timeline.getStartTime();
            Label dateLabel = new Label();
            dateLabel.textProperty().bind(LocalizedTime.formatLocalDateTimeProperty(date, startTime, FrontOfficeTimeFormats.AUDIO_TRACK_DATE_TIME_FORMAT));
            dateLabel.getStyleClass().add(ModalityStyle.TEXT_COMMENT);
            long durationMillis;
            AudioColumnsContext audioContext = context.getAppContext();
            if (!audioContext.getPublishedMedias().isEmpty()) {
                Media firstMedia = Collections.findFirst(audioContext.getPublishedMedias(), media -> Entities.sameId(audio, media.getScheduledItem()));
                if (firstMedia != null) {
                    durationMillis = firstMedia.getDurationMillis();
                    dateLabel.textProperty().unbind();
                    dateLabel.setText(AudioMediaView.formatDuration(durationMillis) + " • " + dateLabel.getText());
                }
            }
            VBox vbox = new VBox();
            vbox.getStyleClass().addAll("audio-library");
            vbox.setAlignment(Pos.CENTER_LEFT);

            vbox.getChildren().addAll(titleLabel,dateLabel);
            return vbox;
        });
        ValueRendererRegistry.registerValueRenderer("audioButtons", (value, context) -> {
            ScheduledItem audio = (ScheduledItem) value;
            AudioColumnsContext audioContext = context.getAppContext();
            Media firstMedia = Collections.findFirst(audioContext.getPublishedMedias(), media -> Entities.sameId(audio, media.getScheduledItem()));
            if (firstMedia == null) {
                Label noMediaLabel = I18nControls.newLabel(AudioRecordingsI18nKeys.AudioRecordingNotYetPublished);
                noMediaLabel.getStyleClass().add(ModalityStyle.TEXT_COMMENT);
                HBox hbox = new HBox(noMediaLabel);
                hbox.setAlignment(Pos.CENTER);
                hbox.getStyleClass().addAll("audio-library");
                return hbox;
            }
            HBox hBox = new HBox(10, createAudioButton(audio, firstMedia, audioContext.getAudioPlayer(), false), createAudioButton(audio, firstMedia, audioContext.getAudioPlayer(),true));
            hBox.setAlignment(Pos.CENTER_LEFT);
            return hBox;
        });
    }

    private static Button createAudioButton(ScheduledItem audio, Media firstMedia, Player audioPlayer, boolean download) {
        Button button = download ? ModalityStyle.blackButton(I18nControls.newButton(AudioRecordingsI18nKeys.Download))
                : Bootstrap.dangerButton(I18nControls.newButton(AudioRecordingsI18nKeys.Play));
        if (!download && Booleans.isTrue(audio.getFieldValue("alreadyPlayed")))
            transformButtonFromPlayToPlayAgain(button);
        button.setGraphicTextGap(10);
        button.setMinWidth(130);
        button.setOnAction(event -> {
            if (download) { // Download action
                // 1) We download the file. Note: there is no way to track the progress of the download...
                downloadFile(firstMedia.getUrl());
                // 2) We record this action using MediaConsumptionRecorder
                MediaConsumptionRecorder.recordDownloadMediaConsumption(audio, firstMedia);
                // 3) Sometimes (especially on mobiles) the system can take a few seconds before showing there is a
                // download in progress, giving the impression that nothing happens, and making the user pressing
                // the button several times. To prevent this, we disable the download button for 5s.
                OperationUtil.turnOnButtonsWaitMode(button);
                UiScheduler.scheduleDelay(5000, () -> OperationUtil.turnOffButtonsWaitMode(button));
            } else { // Play action
                var playerMedia = audioPlayer.acceptMedia(firstMedia.getUrl(), new MediaMetadataBuilder()
                        .setTitle(getAudioName(audio)).setDurationMillis(firstMedia.getDurationMillis()).build());
                playerMedia.setUserData(firstMedia); // used later to check which track the audio player is playing (see below)
                audioPlayer.resetToInitialState();
                audioPlayer.setMedia(playerMedia);
                button.disableProperty().bind(audioPlayer.mediaProperty().isEqualTo(playerMedia));
                audioPlayer.play();
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

    static void registerRenderers() {
        // Actually done (only once) in the static initializer above
    }


    private static void transformButtonFromPlayToPlayAgain(Button playButton) {
        playButton.getStyleClass().remove(Bootstrap.DANGER);
        Bootstrap.secondaryButton(playButton);
        I18nControls.bindI18nProperties(playButton, AudioRecordingsI18nKeys.PlayAgain);
    }
}
