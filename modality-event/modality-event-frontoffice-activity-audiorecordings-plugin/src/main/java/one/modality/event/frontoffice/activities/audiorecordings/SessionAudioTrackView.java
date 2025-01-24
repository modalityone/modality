package one.modality.event.frontoffice.activities.audiorecordings;

import dev.webfx.extras.media.metadata.MediaMetadataBuilder;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.player.audio.javafxmedia.JavaFXMediaAudioPlayer;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.platform.blob.spi.BlobProvider;
import dev.webfx.platform.shutdown.Shutdown;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.UpdateStore;
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
import one.modality.base.shared.entities.MediaConsumption;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.Timeline;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Bruno Salmon
 */
final class SessionAudioTrackView {

    static final double MAX_WIDTH = 750;
    private static final double BUTTON_WIDTH = 130;

    private final ScheduledItem scheduledAudioItem;
    private final List<Media> publishedMedias;
    private final JavaFXMediaAudioPlayer audioPlayer;

    private final BorderPane containerBorderPane = new BorderPane();
    private final int index;
    private final int totalNumberOfTracks;
    private final UpdateStore updateStore;
    private MediaConsumption mediaConsumption;

    public SessionAudioTrackView(ScheduledItem scheduledAudioItem, List<Media> publishedMedias, JavaFXMediaAudioPlayer audioPlayer, int index, int totalNb) {
        this.scheduledAudioItem = scheduledAudioItem;
        this.audioPlayer = audioPlayer;
        this.index = index;
        this.totalNumberOfTracks = totalNb;
        this.publishedMedias = publishedMedias.stream()
            .filter(media -> media.getScheduledItem() != null && Entities.sameId(scheduledAudioItem, media.getScheduledItem()))
            .collect(Collectors.toList());

        buildUi();
        updateStore = UpdateStore.createAbove(scheduledAudioItem.getEvent().getStore());
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
        //containerBorderPane.setLeft(favoriteMonoPane);
        containerBorderPane.setMaxWidth(MAX_WIDTH);
        String title = scheduledAudioItem.getName();
        if (title == null)
            title = scheduledAudioItem.getProgramScheduledItem().getName();
        String indexToString = formatIndex(index,totalNumberOfTracks);
        Label titleLabel = Bootstrap.h3(new Label(indexToString + ". " + title));
        titleLabel.setWrapText(true);
        Timeline timeline = scheduledAudioItem.getProgramScheduledItem().getTimeline();
        LocalDate date = scheduledAudioItem.getDate();
        String startTime = "";
        if (timeline != null) {
            //Case fo festivals, when null it's a recurring event, and we don't need to display the time
            startTime = " - " + timeline.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        Long durationMillis;

        Label dateLabel = new Label(
            date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")) + startTime);
        dateLabel.getStyleClass().add(ModalityStyle.TEXT_COMMENT);
        if (!publishedMedias.isEmpty()) {
            durationMillis = publishedMedias.get(0).getDurationMillis();
            dateLabel.setText(formatDuration(durationMillis) + " • " + dateLabel.getText());
        } else {
            durationMillis = 0L;
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
            if ((Boolean) scheduledAudioItem.getFieldValue("alreadyPlayed"))
                transformButtonFromPlayToPlayAgain(playButton);

            Media firstMedia = publishedMedias.get(0);
            String finalTitle = title;
            playButton.setOnAction(e -> {
                dev.webfx.extras.player.Media oldMedia = audioPlayer.getMedia();
                if (oldMedia != null) {
                    Button oldPlayButton = (Button) oldMedia.getUserData();
                    oldPlayButton.setDisable(false);
                    transformButtonFromPlayToPlayAgain(oldPlayButton);
                    MediaConsumption mediaConsumption = (MediaConsumption) oldPlayButton.getProperties().get("MediaConsumption");
                    UpdateStore previousUpdateStore = (UpdateStore) oldPlayButton.getProperties().get("UpdateStore");

                    if (mediaConsumption != null) {
                        mediaConsumption.setDurationMillis((long) audioPlayer.getCurrentTime().toMillis());
                        previousUpdateStore.submitChanges();
                    }
                }
                dev.webfx.extras.player.Media media = audioPlayer.acceptMedia(firstMedia.getUrl(), new MediaMetadataBuilder().setTitle(finalTitle).setDurationMillis(durationMillis).build());
                media.setUserData(playButton);
                audioPlayer.resetToInitialState();
                audioPlayer.setMedia(media);
                playButton.setDisable(true);
                audioPlayer.play();
                Object attendanceId = scheduledAudioItem.getFieldValue("attendanceId");
                mediaConsumption = updateStore.insertEntity(MediaConsumption.class);
                mediaConsumption.setAttendance(attendanceId);
                mediaConsumption.setPlayed(true);
                mediaConsumption.setMedia(firstMedia);
                mediaConsumption.setScheduledItem(scheduledAudioItem);

                updateStore.submitChanges()
                    .onSuccess(success -> {
                        //We pass the MediaConsumption and the UpdateStore as properties of the play button to be able to retrieve them when we press the playButton of another instance of this class
                        MediaConsumption updatedMediaConsumption = updateStore.updateEntity(mediaConsumption);
                        playButton.getProperties().remove("MediaConsumption");
                        playButton.getProperties().put("MediaConsumption", updatedMediaConsumption);
                        playButton.getProperties().remove("UpdateStore");
                        playButton.getProperties().put("UpdateStore", updateStore);
                        Shutdown.addShutdownHook(()-> {
                            updatedMediaConsumption.setDurationMillis((long )media.getCurrentTime().toMillis());
                            updateStore.submitChanges();
                        });
                    });
            });
            Button downloadButton = ModalityStyle.blackButton(I18nControls.newButton(AudioRecordingsI18nKeys.Download));
            playButton.setGraphicTextGap(10);
            downloadButton.setGraphicTextGap(10);
            downloadButton.setOnAction(event -> {
                downloadFile(firstMedia.getUrl());
                Object attendanceId = scheduledAudioItem.getFieldValue("attendanceId");
                mediaConsumption = updateStore.insertEntity(MediaConsumption.class);
                mediaConsumption.setAttendance(attendanceId);
                mediaConsumption.setDownloaded(true);
                mediaConsumption.setMedia(firstMedia);
                mediaConsumption.setScheduledItem(scheduledAudioItem);
                updateStore.submitChanges();
            });
            playButton.setMinWidth(BUTTON_WIDTH);
            downloadButton.setMinWidth(BUTTON_WIDTH);
            HBox buttonHBox = new HBox(playButton, downloadButton);
            buttonHBox.setSpacing(10);
            containerBorderPane.setRight(buttonHBox);
        }
    }

    private String formatIndex(int index, int totalNumberOfTracks) {
        if(totalNumberOfTracks < 10) return "" + index;
        if((totalNumberOfTracks < 100) && (index < 10)) return "0" + index;
        return "" + index;
    }

    private void transformButtonFromPlayToPlayAgain(Button playButton) {
        playButton.getStyleClass().remove(Bootstrap.DANGER);
        Bootstrap.secondaryButton(playButton);
        I18nControls.bindI18nProperties(playButton, AudioRecordingsI18nKeys.PlayAgain);
    }

    public static String formatDuration(long durationMillis) {
        // Calculate hours, minutes, and seconds
        long hours = durationMillis / (1000 * 60 * 60); // Calculate hours
        long minutes = (durationMillis / (1000 * 60)) % 60; // Calculate minutes
        long seconds = (durationMillis / 1000) % 60; // Calculate seconds
        // Return formatted string
        return (hours < 10 ? "0" : "") + hours + ":" + (minutes < 10 ? "0" : "") + minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
    }

    private void downloadFile(String fileUrl) {
        BlobProvider.get().downloadUrl(fileUrl);
    }

}
