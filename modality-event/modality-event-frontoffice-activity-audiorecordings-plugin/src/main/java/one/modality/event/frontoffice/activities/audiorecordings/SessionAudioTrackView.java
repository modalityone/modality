package one.modality.event.frontoffice.activities.audiorecordings;

import dev.webfx.extras.media.metadata.MediaMetadataBuilder;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.player.audio.javafxmedia.JavaFXMediaAudioPlayer;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.platform.blob.spi.BlobProvider;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.entity.Entities;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Bruno Salmon
 */
final class SessionAudioTrackView {

    private final ScheduledItem scheduledAudioItem;
    private final List<Media> publishedMedias;
    private final JavaFXMediaAudioPlayer audioPlayer;

    private final BorderPane container = new BorderPane();
    public static final int MAX_WIDTH=750;
    private static final int BUTTON_WIDTH=130;
    private final int index;

    public SessionAudioTrackView(ScheduledItem scheduledAudioItem, List<Media> publishedMedias, JavaFXMediaAudioPlayer audioPlayer, int index) {
        this.scheduledAudioItem = scheduledAudioItem;
        this.audioPlayer = audioPlayer;
        this.index = index;
        this.publishedMedias = publishedMedias.stream()
            .filter(media -> media.getScheduledItem() != null && Entities.sameId(scheduledAudioItem, media.getScheduledItem()))
            .collect(Collectors.toList());
        buildUi();
    }

    BorderPane getView() {
        return container;
    }

    private void buildUi() {
        SVGPath favoritePath = SvgIcons.createFavoritePath();
        favoritePath.setScaleX(0.5);
        favoritePath.setScaleY(0.5);
        favoritePath.setFill(Color.TRANSPARENT);
        favoritePath.setStroke(Color.BLACK);

        MonoPane favoriteMonoPane = new MonoPane(favoritePath);
        container.setLeft(favoriteMonoPane);
        container.setMaxWidth(MAX_WIDTH);
        String title = scheduledAudioItem.getProgramScheduledItem().getName();
        Label titleLabel = Bootstrap.h3(new Label(index + ". " + title));
        Timeline timeline = scheduledAudioItem.getProgramScheduledItem().getTimeline();
        LocalDate date = scheduledAudioItem.getDate();
        LocalTime startTime = timeline.getStartTime();
        Long durationMillis;

        Label dateLabel = new Label(
            date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")) + " - " + startTime.format(DateTimeFormatter.ofPattern("HH:mm")));
        dateLabel.getStyleClass().add(ModalityStyle.TEXT_COMMENT);
        if(publishedMedias.size()>0) {
            durationMillis = publishedMedias.get(0).getDurationMillis();
            dateLabel.setText(formatDuration(durationMillis) + " • " + dateLabel.getText());
        } else {
            durationMillis = 0L;
        }

        VBox descriptionVBox = new VBox(titleLabel,dateLabel);
        titleLabel.getStyleClass().add("description");
        container.setCenter(descriptionVBox);
        container.getStyleClass().addAll("audio-library", "bottom-border");
        BorderPane.setMargin(favoriteMonoPane, new Insets(0, 20, 0, 0));
        BorderPane.setMargin(descriptionVBox, new Insets(0, 50, 0, 0));
        BorderPane.setAlignment(descriptionVBox, Pos.CENTER_LEFT);
        //Here we should have only one media for audio
        if (publishedMedias.isEmpty()) {
            Label noMediaLabel = I18nControls.newLabel(AudioRecordingsI18nKeys.AudioRecordingNotYetPublished);
            noMediaLabel.getStyleClass().add(ModalityStyle.TEXT_COMMENT);
            container.setRight(noMediaLabel);
        } else {
            Button playButton = Bootstrap.dangerButton(I18nControls.newButton(AudioRecordingsI18nKeys.Play));
            Media firstMedia = publishedMedias.get(0);
            playButton.setOnAction(e->{
                dev.webfx.extras.player.Media oldMedia = audioPlayer.getMedia();
                if(oldMedia!=null) {
                    ((Button )oldMedia.getUserData()).setDisable(false);
                }
                dev.webfx.extras.player.Media media = audioPlayer.acceptMedia(firstMedia.getUrl(),new MediaMetadataBuilder().setTitle(title).setDurationMillis(durationMillis).build());
                media.setUserData(playButton);
                audioPlayer.resetToInitialState();
                audioPlayer.setMedia(media);
                playButton.setDisable(true);
                audioPlayer.play();
            });
            Button downloadButton = ModalityStyle.blackButton(I18nControls.newButton(AudioRecordingsI18nKeys.Download));
            playButton.setGraphicTextGap(10);
            downloadButton.setGraphicTextGap(10);
            downloadButton.setOnAction(event -> downloadFile(firstMedia.getUrl()));
            playButton.setMinWidth(BUTTON_WIDTH);
            downloadButton.setMinWidth(BUTTON_WIDTH);
            HBox buttonHBox = new HBox(playButton,downloadButton);
            buttonHBox.setSpacing(10);
            container.setRight(buttonHBox);
        }
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
