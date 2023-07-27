package one.modality.base.frontoffice.activities.home.views;

import dev.webfx.extras.imagestore.ImageStore;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.util.Objects;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import one.modality.base.frontoffice.utility.GeneralUtility;
import one.modality.base.frontoffice.utility.StyleUtility;
import one.modality.base.frontoffice.utility.TextUtility;
import one.modality.base.shared.entities.Podcast;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public final class PodcastView {
    private static MediaPlayer PLAYING_MEDIA_PLAYER;
    private MediaPlayer mediaPlayer;
    private Podcast podcast;
    private Duration podcastDuration;
    private final ImageView authorImageView = new ImageView();
    private final Rectangle authorImageClip = new Rectangle();
    private final Text dateText = TextUtility.getSubText(null);
    private final Label titleLabel = GeneralUtility.getMainLabel(null, StyleUtility.MAIN_BLUE);
    private final Label excerptLabel = GeneralUtility.getMainLabel(null, StyleUtility.ELEMENT_GRAY);
    private final Pane playButton = PodcastButtons.createPlayButton();
    private final Pane pauseButton = PodcastButtons.createPauseButton();
    private final Pane forwardButton = PodcastButtons.createForwardButton();
    private final Pane backwardButton = PodcastButtons.createBackwardButton();
    private final ProgressBar progressBar = new ProgressBar();
    private final Text elapsedTimeText = TextUtility.getSubText(null, StyleUtility.ELEMENT_GRAY);

    {
        authorImageView.setPreserveRatio(true);
        authorImageView.setClip(authorImageClip);
        TextUtility.setFontFamily(elapsedTimeText, StyleUtility.CLOCK_FAMILY, 9);
        // Arming buttons
        playButton    .setOnMouseClicked(e -> play());
        pauseButton   .setOnMouseClicked(e -> pause());
        forwardButton .setOnMouseClicked(e -> seekRelative(30));
        backwardButton.setOnMouseClicked(e -> seekRelative(-10));
        progressBar   .setOnMouseClicked(e -> seekX(e.getX()));
        progressBar   .setOnMouseDragged(e -> seekX(e.getX()));
    }

    private final Pane podcastContainer = new Pane(authorImageView, dateText, titleLabel, excerptLabel, backwardButton, pauseButton, playButton, forwardButton, progressBar, elapsedTimeText) {
        private double imageY, imageSize, rightX, rightWidth, dateY, dateHeight, titleY, titleHeight, excerptY, excerptHeight, buttonY, buttonSize;

        @Override
        protected void layoutChildren() {
            computeLayout(getWidth());
            authorImageView.setFitWidth(imageSize);
            authorImageClip.setWidth(imageSize);
            authorImageClip.setHeight(imageSize);
            double arcWidthHeight = imageSize / 4;
            authorImageClip.setArcWidth(arcWidthHeight);
            authorImageClip.setArcHeight(arcWidthHeight);
            layoutInArea(authorImageView, 0, imageY, imageSize, imageSize, 0, HPos.LEFT, VPos.TOP);
            layoutInArea(dateText, rightX, dateY, rightWidth, dateHeight, 0, HPos.LEFT, VPos.TOP);
            layoutInArea(titleLabel, rightX, titleY, rightWidth, titleHeight, 0, HPos.LEFT, VPos.TOP);
            layoutInArea(excerptLabel, rightX, excerptY, rightWidth, excerptHeight, 0, HPos.LEFT, VPos.TOP);
            layoutInArea(backwardButton, rightX, buttonY, buttonSize, buttonSize, 0, HPos.LEFT, VPos.TOP);
            layoutInArea(playButton, rightX + buttonSize + 5, buttonY, buttonSize, buttonSize, 0, HPos.LEFT, VPos.TOP);
            layoutInArea(pauseButton, rightX + buttonSize + 5, buttonY, buttonSize, buttonSize, 0, HPos.LEFT, VPos.TOP);
            layoutInArea(forwardButton, rightX + 2 * (buttonSize + 5), buttonY, buttonSize, buttonSize, 0, HPos.LEFT, VPos.TOP);
            progressBar.setPrefWidth(rightWidth - 6 * (buttonSize + 5));
            layoutInArea(progressBar, rightX + 3 * (buttonSize + 5), buttonY, progressBar.getPrefWidth(), buttonSize, 0, HPos.LEFT, VPos.CENTER);
            layoutInArea(elapsedTimeText, rightX + 3 * (buttonSize + 5), buttonY + buttonSize, rightWidth, buttonSize, 0, HPos.LEFT, VPos.TOP);
        }

        @Override
        protected double computePrefHeight(double width) {
            computeLayout(width);
            return Math.max(imageY + imageSize, buttonY + buttonSize);
        }

        private void computeLayout(double width) {
            if (width == -1)
                width = getWidth();
            /* Image: */      imageY = 0;                               imageSize = width / 4;
            /* Right side: */ rightX = imageSize + 20;                  rightWidth = width - rightX;
            /* Date: */       dateY = 0;                                dateHeight = dateText.prefHeight(rightWidth);
            /* Title: */      titleY = dateY + dateHeight + 10;         titleHeight = titleLabel.prefHeight(rightWidth);
            /* Excerpt: */    excerptY = titleY + titleHeight + 10;     excerptHeight = excerptLabel.prefHeight(rightWidth);
            /* Buttons: */    buttonY = excerptY + excerptHeight + 10;  buttonSize = 32;
        }
    };

    public void setPodcast(Podcast podcast) {
        this.podcast = podcast;
        podcastDuration = Duration.millis(podcast.getDurationMillis());
        authorImageView.setImage(ImageStore.getOrCreateImage(podcast.getImageUrl()));
        updateText(dateText, DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).format(podcast.getDate()));
        updateLabel(titleLabel, podcast.getTitle().toUpperCase());
        updateLabel(excerptLabel, podcast.getExcerpt());
        if (mediaPlayer == null || !mediaPlayer.getMedia().getSource().equals(podcast.getAudioUrl())) {
            disposePlayer();
            updateElapsedTimeAndProgressBar(Duration.ZERO);
        }
    }

    public Node getView() {
        return podcastContainer;
    }

    private void play() {
        if (mediaPlayer == null)
            createMediaPlayer();
        if (PLAYING_MEDIA_PLAYER != null && PLAYING_MEDIA_PLAYER != mediaPlayer)
            PLAYING_MEDIA_PLAYER.pause();
        PLAYING_MEDIA_PLAYER = mediaPlayer;
        mediaPlayer.play();
    }

    private void pause() {
        if (mediaPlayer != null)
            mediaPlayer.pause();
    }

    private void seekRelative(double relativeSeconds) {
        if (mediaPlayer != null)
            mediaPlayer.seek(mediaPlayer.getCurrentTime().add(Duration.seconds(relativeSeconds)));
    }

    private void seekX(double x) {
        if (mediaPlayer != null) {
            double percentage = x / progressBar.getWidth();
            Duration seekTime = podcastDuration.multiply(percentage);
            mediaPlayer.seek(seekTime);
        }
    }

    private void disposePlayer() {
        if (mediaPlayer != null) {
            if (PLAYING_MEDIA_PLAYER == mediaPlayer)
                PLAYING_MEDIA_PLAYER = null;
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }

    private void createMediaPlayer() {
        mediaPlayer = new MediaPlayer(new Media(podcast.getAudioUrl()));

        FXProperties.runNowAndOnPropertiesChange(() -> {
            MediaPlayer.Status status = mediaPlayer.getStatus();
            boolean isPlaying = status == MediaPlayer.Status.PLAYING;
            pauseButton.setVisible(isPlaying);
            playButton.setVisible(!isPlaying);
            if (isPlaying)
                updateElapsedTimeAndProgressBar(mediaPlayer.getCurrentTime());
            else {
                if (status == null || status == MediaPlayer.Status.UNKNOWN)
                    progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            }
        }, mediaPlayer.statusProperty(), mediaPlayer.currentTimeProperty());

        mediaPlayer.setOnEndOfMedia(mediaPlayer::stop); // Forcing stop status (sometimes this doesn't happen automatically for any reason)
    }

    private void updateElapsedTimeAndProgressBar(Duration elapsed) {
        updateText(elapsedTimeText, formatDuration(elapsed) + " / " + formatDuration(podcastDuration));
        progressBar.setProgress(elapsed.toMillis() / podcastDuration.toMillis());
    }

    private static void updateText(Text text, String newContent) {
        if (!Objects.areEquals(newContent, text.getText()))
            text.setText(newContent);
    }

    private static void updateLabel(Label label, String newContent) {
        if (!Objects.areEquals(newContent, label.getText()))
            label.setText(newContent);
    }

    private static String formatDuration(Duration duration) {
        int minutes = (int) duration.toMinutes();
        int seconds = ((int) duration.toSeconds()) % 60;
        return (minutes < 10 ? "0" : "") + minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
    }
}