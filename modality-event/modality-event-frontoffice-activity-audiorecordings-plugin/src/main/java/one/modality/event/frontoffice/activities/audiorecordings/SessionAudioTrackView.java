package one.modality.event.frontoffice.activities.audiorecordings;

import dev.webfx.extras.media.metadata.MediaMetadataBuilder;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.player.Player;
import dev.webfx.extras.player.audio.javafxmedia.AudioMediaView;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.platform.blob.spi.BlobProvider;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.Booleans;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.ui.operation.OperationUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.bootstrap.ModalityStyle;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.entities.Media;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.Timeline;
import one.modality.event.frontoffice.medias.MediaConsumptionRecorder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author Bruno Salmon
 */
final class SessionAudioTrackView {

    static final double MAX_WIDTH = 750;
    private static final double BUTTON_WIDTH = 130;

    private final ScheduledItem scheduledAudioItem;
    private final List<Media> publishedMedias;
    private final Player audioPlayer;

    private final BorderPane containerBorderPane = new BorderPane();
    private final int index;
    private final int totalNumberOfTracks;
    private MediaConsumptionRecorder playingMediaConsumptionRecorder;

    public SessionAudioTrackView(ScheduledItem scheduledAudioItem, List<Media> publishedMedias, Player audioPlayer, int index, int totalNb) {
        this.scheduledAudioItem = scheduledAudioItem;
        this.audioPlayer = audioPlayer;
        this.index = index;
        this.totalNumberOfTracks = totalNb;
        this.publishedMedias = Collections.filter(publishedMedias, media -> Entities.sameId(scheduledAudioItem, media.getScheduledItem()));
        buildUi();
    }

    BorderPane getView() {
        return containerBorderPane;
    }

    private void buildUi() {
        SVGPath favoritePath = SvgIcons.createFavoritePath();
        favoritePath.setScaleX(0.5);
        favoritePath.setScaleY(0.5);
        favoritePath.setFill(Color.TRANSPARENT);
        favoritePath.setStroke(Color.BLACK);

        MonoPane favoriteMonoPane = new MonoPane(favoritePath);
        containerBorderPane.setMaxWidth(MAX_WIDTH);
        String title = scheduledAudioItem.getName();
        if (title == null)
            title = scheduledAudioItem.getProgramScheduledItem().getName();
        String indexToString = formatIndex(index, totalNumberOfTracks);
        Label titleLabel = Bootstrap.h3(new Label(indexToString + ". " + title));
        titleLabel.setWrapText(true);
        LocalDate date = scheduledAudioItem.getDate();
        Timeline timeline = scheduledAudioItem.getProgramScheduledItem().getTimeline();
        String startTime = "";
        if (timeline != null) {
            //Case fo festivals, when null it's a recurring event, and we don't need to display the time
            startTime = " - " + timeline.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        Label dateLabel = new Label(date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")) + startTime);
        dateLabel.getStyleClass().add(ModalityStyle.TEXT_COMMENT);
        long durationMillis;
        if (!publishedMedias.isEmpty()) {
            durationMillis = publishedMedias.get(0).getDurationMillis();
            dateLabel.setText(AudioMediaView.formatDuration(durationMillis) + " • " + dateLabel.getText());
        } else {
            durationMillis = 0;
        }

        VBox descriptionVBox = new VBox(titleLabel, dateLabel);
        titleLabel.getStyleClass().add("description");
        containerBorderPane.setCenter(descriptionVBox);
        containerBorderPane.getStyleClass().addAll("audio-library", "bottom-border");
        BorderPane.setMargin(favoriteMonoPane, new Insets(0, 20, 0, 0));
        BorderPane.setMargin(descriptionVBox, new Insets(0, 30, 0, 0));
        BorderPane.setAlignment(descriptionVBox, Pos.CENTER_LEFT);
        //Here we should have only one media for audio
        if (publishedMedias.isEmpty()) {
            Label noMediaLabel = I18nControls.newLabel(AudioRecordingsI18nKeys.AudioRecordingNotYetPublished);
            noMediaLabel.getStyleClass().add(ModalityStyle.TEXT_COMMENT);
            containerBorderPane.setRight(noMediaLabel);
        } else {
            Button playButton = Bootstrap.dangerButton(I18nControls.newButton(AudioRecordingsI18nKeys.Play));
            // alreadyPlayed is a dynamic field loaded by EventAudioPlaylistActivity
            if (Booleans.isTrue(scheduledAudioItem.getFieldValue("alreadyPlayed")))
                transformButtonFromPlayToPlayAgain(playButton);

            Media firstMedia = publishedMedias.get(0);
            String finalTitle = title;
            playButton.setOnAction(e -> {
                var playerMedia = audioPlayer.acceptMedia(firstMedia.getUrl(), new MediaMetadataBuilder()
                    .setTitle(finalTitle).setDurationMillis(durationMillis).build());
                playerMedia.setUserData(firstMedia); // used later to check which track the audio player is playing (see below)
                audioPlayer.resetToInitialState();
                audioPlayer.setMedia(playerMedia);
                playButton.setDisable(true);
                audioPlayer.play();
                // Playing MediaConsumption management
                if (playingMediaConsumptionRecorder == null) {
                    playingMediaConsumptionRecorder = new MediaConsumptionRecorder(audioPlayer, false, this::getAudioPlayerScheduledItem, this::getAudioPlayerMedia);
                    playingMediaConsumptionRecorder.start();
                }
            });
            Button downloadButton = ModalityStyle.blackButton(I18nControls.newButton(AudioRecordingsI18nKeys.Download));
            playButton.setGraphicTextGap(10);
            downloadButton.setGraphicTextGap(10);
            downloadButton.setOnAction(event -> {
                // 1) We download the file. Note: there is no way to track the progress of the download...
                downloadFile(firstMedia.getUrl());
                // 2) We record this action using MediaConsumptionRecorder
                MediaConsumptionRecorder.recordDownloadMediaConsumption(scheduledAudioItem, firstMedia);
                // 3) Sometimes (especially on mobiles) the system can take a few seconds before showing there is a
                // download in progress, giving the impression that nothing happens, and making the user pressing
                // the button several times. To prevent this, we disable the download button for 5s.
                OperationUtil.turnOnButtonsWaitMode(downloadButton);
                UiScheduler.scheduleDelay(5000, () -> OperationUtil.turnOffButtonsWaitMode(downloadButton));
            });
            playButton.setMinWidth(BUTTON_WIDTH);
            downloadButton.setMinWidth(BUTTON_WIDTH);
            containerBorderPane.setRight(new HBox(10, playButton, downloadButton));
        }
    }

    private ScheduledItem getAudioPlayerScheduledItem() {
        Media media = getAudioPlayerMedia();
        return media == null ? null : media.getScheduledItem();
    }

    private Media getAudioPlayerMedia() {
        var audioPlayerMedia = audioPlayer.getMedia();
        Media media = (Media) audioPlayerMedia.getUserData();
        if (publishedMedias.contains(media))
            return media;
        return null;
    }

    private String formatIndex(int index, int totalNumberOfTracks) {
        if (totalNumberOfTracks < 10) return "" + index;
        if ((totalNumberOfTracks < 100) && (index < 10)) return "0" + index;
        return "" + index;
    }

    private void transformButtonFromPlayToPlayAgain(Button playButton) {
        playButton.getStyleClass().remove(Bootstrap.DANGER);
        Bootstrap.secondaryButton(playButton);
        I18nControls.bindI18nProperties(playButton, AudioRecordingsI18nKeys.PlayAgain);
    }

    private void downloadFile(String fileUrl) {
        BlobProvider.get().downloadUrl(fileUrl);
    }

}
