package one.modality.event.frontoffice.activities.audiorecordings;

import dev.webfx.platform.blob.spi.BlobProvider;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import one.modality.event.client.mediaview.MediaInfoView;

public class AudioRecordingMediaInfoView extends MediaInfoView {

    @Override
    protected boolean isMarkedAsFavorite() {
        return false;
    }

    @Override
    protected void toggleAsFavorite() {
    }

    public void setMediaInfo(AudioMedia mediaInfo) {
        super.setMediaInfo(mediaInfo);
        int with = 800;
        mediaPane.getChildren().clear();
        mediaPane = new VBox(progressBar);
        mediaPane.setMinWidth(with);
        ((VBox) mediaPane).setSpacing(10);
        VBox.setMargin(progressBar,new Insets(10,0,0,0));
        progressBar.setMinWidth(with);

        HBox buttonLine = new HBox(playButton, pauseButton, backwardButton, forwardButton);
        buttonLine.setMaxWidth(with);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        buttonLine.getChildren().addAll(spacer,elapsedTimeText);
        buttonLine.setSpacing(20);
        buttonLine.setAlignment(Pos.CENTER_LEFT);

        Hyperlink downloadLink = new Hyperlink("Download File");
        downloadLink.setOnAction(event -> downloadFile(mediaInfo.getAudioUrl()));
        buttonLine.getChildren().add(downloadLink);

        mediaPane.getChildren().add(buttonLine);
    }

    private void downloadFile(String fileUrl) {
        BlobProvider.get().downloadUrl(fileUrl);
    }

    protected void updatePlayPauseButtons(boolean isPlaying) {
        super.updatePlayPauseButtons(isPlaying);
        pauseButton.setManaged(isPlaying);
        playButton.setManaged(!isPlaying);
    }

}
