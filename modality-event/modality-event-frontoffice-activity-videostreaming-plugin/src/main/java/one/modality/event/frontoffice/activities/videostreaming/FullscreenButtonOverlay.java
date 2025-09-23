package one.modality.event.frontoffice.activities.videostreaming;

import dev.webfx.extras.player.Player;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import one.modality.event.client.mediaview.MediaButtons;

/**
 * @author David Hello
 * @author Bruno Salmon
 */
final class FullscreenButtonOverlay {

    private static final Duration INACTIVITY_DURATION = Duration.seconds(3.5);

    static void addFullscreenButtonOverlayToVideoPlayer(Player videoPlayer) {
        Pane fullScreenContainer = MediaButtons.createFullscreenButton();
        fullScreenContainer.setMaxSize(60, 60);
        fullScreenContainer.setCursor(Cursor.HAND);
        fullScreenContainer.setPadding(new Insets(10, 10, 0, 0));
        StackPane.setAlignment(fullScreenContainer, Pos.TOP_RIGHT);

        fullScreenContainer.setOnMouseClicked(e -> videoPlayer.toggleFullscreen());

        // Add fullscreen button to video player overlay
        videoPlayer.getOverlayChildren().add(fullScreenContainer);

        // Initialize timeline for hiding the button
        Timeline hideFullscreenButtonTimeline = new Timeline(
            new KeyFrame(INACTIVITY_DURATION, e -> hideFullscreenButton(fullScreenContainer))
        );
        hideFullscreenButtonTimeline.play();
        // Get the media view to attach mouse/keyboard listeners
        Node mediaView = videoPlayer.getMediaView();
        mediaView.addEventFilter(MouseEvent.ANY, e -> {
            showFullscreenButton(fullScreenContainer);
            resetHideTimer(hideFullscreenButtonTimeline);
        });

        // Start the initial timer
        resetHideTimer(hideFullscreenButtonTimeline);

    }

    // Method to show the fullscreen button with fade-in animation
    private static void showFullscreenButton(Pane fullScreenContainer) {
        if (fullScreenContainer.getOpacity() < 1.0) {
            Timeline fadeIn = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(fullScreenContainer.opacityProperty(), fullScreenContainer.getOpacity())),
                new KeyFrame(Duration.millis(200), new KeyValue(fullScreenContainer.opacityProperty(), 1.0, Interpolator.EASE_OUT))
            );
            fadeIn.play();
        }
    }

    // Method to hide the fullscreen button with fade-out animation
    private static void hideFullscreenButton(Pane fullScreenContainer) {
        Timeline fadeOut = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(fullScreenContainer.opacityProperty(), 1.0)),
            new KeyFrame(Duration.millis(300), new KeyValue(fullScreenContainer.opacityProperty(), 0.0, Interpolator.EASE_IN))
        );
        fadeOut.play();
    }

    // Method to reset the hide timer
    private static void resetHideTimer(Timeline hideFullscreenButtonTimeline) {
        if (hideFullscreenButtonTimeline != null) {
            hideFullscreenButtonTimeline.stop();
            hideFullscreenButtonTimeline.play();
        }
    }

}
