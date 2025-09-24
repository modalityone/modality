package one.modality.base.frontoffice.activities.mainframe;

import dev.webfx.extras.player.FullscreenButton;
import dev.webfx.extras.player.Player;
import dev.webfx.extras.util.animation.Animations;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.Unregisterable;
import dev.webfx.platform.scheduler.Scheduled;
import dev.webfx.platform.uischeduler.UiScheduler;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import one.modality.base.client.mainframe.fx.FXMainFrameOverlayArea;
import one.modality.event.client.mediaview.MediaButtons;

/**
 * @author Bruno Salmon
 */
public class ModalityFullscreenButton {

    private static final Pane FULLSCREEN_BUTTON = MediaButtons.createFullscreenButton();
    private static Timeline FULLSCREEN_BUTTON_TIMELINE;
    private static Unregisterable FULLSCREEN_LAYOUT;
    private static boolean FULLSCREEN_BUTTON_IN_PLAYER;
    private static boolean FULLSCREEN_BUTTON_ANIMATED;
    private static Scheduled FADE_OUT_SCHEDULED;
    private static final EventHandler<MouseEvent> MOUSE_MOVE_OVER_PLAYER_WITH_FULLSCREEN_BUTTON_HANDLER = e -> {
        if (FULLSCREEN_BUTTON_TIMELINE == null || Animations.isTimelineFinished(FULLSCREEN_BUTTON_TIMELINE))
            fadeFullscreenButton(true);
        fadeFullscreenButton(false);
    };

    public static void setupModalityFullscreenButton() {
        FullscreenButton.setupFullscreenButton(
            ModalityFullscreenButton::showFullscreenButton,
            ModalityFullscreenButton::hideFullscreenButton,
            ModalityFullscreenButton::animateFullscreenButton
        );
    }

    private static Pane showFullscreenButton() {
        // If the suitable fullscreen player has an overlay area, we put the fullscreen button in that area, otherwise
        // we use the global main frame overlay area.
        ObservableList<Node> overlayChildren;
        Region overlayArea;
        Player playingPlayer = FullscreenButton.getSuitableFullscreenPlayingPlayer();
        FULLSCREEN_BUTTON_IN_PLAYER = playingPlayer != null && playingPlayer.appRequestedOverlayChildren();
        if (FULLSCREEN_BUTTON_IN_PLAYER) {
            overlayChildren = playingPlayer.getOverlayChildren();
            overlayArea = playingPlayer.getMediaView();
        } else {
            overlayChildren = FXMainFrameOverlayArea.getOverlayChildren();
            overlayArea = FXMainFrameOverlayArea.getOverlayArea();
        }
        // Translating the fullscreen button down and then animate it
        if (!overlayChildren.contains(FULLSCREEN_BUTTON)) {
            overlayChildren.add(FULLSCREEN_BUTTON);
            FULLSCREEN_BUTTON.setManaged(false); // We don't want the player overlay (StackPane) to center and resize
            // the fullscreen button, we manage it ourselves as follows:
            FULLSCREEN_LAYOUT = FXProperties.runNowAndOnPropertiesChange(() -> {
                double width = overlayArea.getWidth();
                FULLSCREEN_BUTTON.resizeRelocate(width - 60, 10, 50, 50);
            }, overlayArea.widthProperty(), overlayArea.heightProperty());
            FULLSCREEN_BUTTON.setTranslateY(-60);
        }
        FULLSCREEN_BUTTON_ANIMATED = false;
        stopPreviousFullscreenButtonAnimation();
        if (FULLSCREEN_BUTTON.getTranslateY() < 0) {
            FULLSCREEN_BUTTON_TIMELINE = Animations.animateProperty(FULLSCREEN_BUTTON.translateYProperty(), 0);
            FULLSCREEN_BUTTON_TIMELINE.setOnFinished(e -> animateFullscreenButton());
        } else {
            animateFullscreenButton();
        }
        return FULLSCREEN_BUTTON;
    }

    private static void hideFullscreenButton(Pane FULLSCREEN_BUTTON) {
        ObservableList<Node> overlayChildren = FXMainFrameOverlayArea.getOverlayChildren();
        if (overlayChildren.contains(FULLSCREEN_BUTTON)) {
            stopPreviousFullscreenButtonAnimation();
            FULLSCREEN_BUTTON_TIMELINE = Animations.animateProperty(FULLSCREEN_BUTTON.translateYProperty(), -70);
            FULLSCREEN_BUTTON_TIMELINE.setOnFinished(e -> overlayChildren.remove(FULLSCREEN_BUTTON));
        }
        if (FULLSCREEN_LAYOUT != null)
            FULLSCREEN_LAYOUT.unregister();
    }

    private static void animateFullscreenButton() {
        animateFullscreenButton(FULLSCREEN_BUTTON);
    }

    private static void animateFullscreenButton(Pane fullscreenButton) {
        if (!FULLSCREEN_BUTTON_IN_PLAYER)
            MediaButtons.animateFullscreenButton(fullscreenButton);
        else if (!FULLSCREEN_BUTTON_ANIMATED) {
            MediaButtons.animateFullscreenButton(fullscreenButton);
            Player player = FullscreenButton.getSuitableFullscreenPlayingPlayer();
            Node mediaView = player.getMediaView();
            mediaView.removeEventFilter(MouseEvent.ANY, MOUSE_MOVE_OVER_PLAYER_WITH_FULLSCREEN_BUTTON_HANDLER);
            mediaView.addEventFilter(MouseEvent.ANY, MOUSE_MOVE_OVER_PLAYER_WITH_FULLSCREEN_BUTTON_HANDLER);
        }
        FULLSCREEN_BUTTON_ANIMATED = true;
    }

    private static void fadeFullscreenButton(boolean fadeIn) {
        if (FADE_OUT_SCHEDULED != null) {
            FADE_OUT_SCHEDULED.cancel();
            FADE_OUT_SCHEDULED = null;
        }
        if (fadeIn) {
            stopPreviousFullscreenButtonAnimation();
            FULLSCREEN_BUTTON_TIMELINE = Animations.fade(FULLSCREEN_BUTTON, fadeIn, false);
        } else {
            FADE_OUT_SCHEDULED = UiScheduler.scheduleDelay(3500, () -> {
                FULLSCREEN_BUTTON_TIMELINE = Animations.fade(FULLSCREEN_BUTTON, fadeIn, false);
            });
        }
    }

    private static void stopPreviousFullscreenButtonAnimation() {
        if (FULLSCREEN_BUTTON_TIMELINE != null) {
            FULLSCREEN_BUTTON_TIMELINE.stop();
            FULLSCREEN_BUTTON_TIMELINE = null;
        }
    }

}
