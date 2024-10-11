package one.modality.event.frontoffice.activities.audiorecordings;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import one.modality.event.client.mediaview.MediaInfoView;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

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

        HBox buttonLine = new HBox(playButton,pauseButton,backwardButton,forwardButton);
        buttonLine.setMaxWidth(with);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        buttonLine.getChildren().addAll(spacer,elapsedTimeText);
        buttonLine.setSpacing(20);
        buttonLine.setAlignment(Pos.CENTER_LEFT);

        Hyperlink downloadLink = new Hyperlink("Download File");
        downloadLink.setOnAction(event -> downloadFile(mediaInfo.getAudioUrl(), (Stage) mediaPane.getScene().getWindow()));
        buttonLine.getChildren().add(downloadLink);

        mediaPane.getChildren().add(buttonLine);
    }

    private void downloadFile(String fileUrl, Stage stage) {
        try {
            // Open a file chooser dialog to let the user select where to save the file
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save File");
            fileChooser.setInitialFileName(((AudioMedia) mediaInfo).getDateTimeForFileToString() + " - " + mediaInfo.getTitle() + ".mp3");
            // Show the save dialog
            java.io.File file = fileChooser.showSaveDialog(stage);
            if (file == null) {
                return; // User canceled the save dialog
            }

            // Open a connection to the file URL
            URL url = new URL(fileUrl);
            InputStream inputStream = url.openStream();

            // Save the file to the chosen location
            FileOutputStream outputStream = new FileOutputStream(file);

            // Create a buffer to read and write data
            byte[] buffer = new byte[4096]; // Create a buffer of 4096 bytes
            int bytesRead;

            // Read from URL and write to the file
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead); // Write the actual bytes read
            }

            // Close streams
            inputStream.close();
            outputStream.close();

            System.out.println("File downloaded successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void updatePlayPauseButtons(boolean isPlaying) {
        super.updatePlayPauseButtons(isPlaying);
        pauseButton.setManaged(isPlaying);
        playButton.setManaged(!isPlaying);
    }

}
