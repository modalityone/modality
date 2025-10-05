package one.modality.base.frontoffice.activities.mainframe;

import dev.webfx.extras.player.FullscreenButton;
import dev.webfx.extras.player.Player;
import dev.webfx.extras.util.animation.Animations;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.Unregisterable;
import dev.webfx.platform.scheduler.Scheduled;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.useragent.UserAgent;
import dev.webfx.platform.util.collection.Collections;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import one.modality.base.client.mainframe.fx.FXMainFrameOverlayArea;
import one.modality.event.client.mediaview.MediaButtons;

/**
 * @author Bruno Salmon
 */
public class ModalityVideoOverlay {

    private static final long FADE_OUT_DELAY_MILLIS = 10000; // 10s
    private static final String USER_ACTIVITY_DETECTION_OVERLAY_ID = "user-activity-overlay";

    private static final Pane FULLSCREEN_BUTTON = MediaButtons.createFullscreenButton();
    private static final Pane RELOAD_BUTTON = MediaButtons.createReloadButton();
    private static Timeline FULLSCREEN_BUTTON_TIMELINE;
    private static Unregisterable FULLSCREEN_LAYOUT;
    private static boolean FULLSCREEN_BUTTON_IN_PLAYER;
    private static boolean FULLSCREEN_BUTTON_ANIMATED;
    private static Scheduled FADE_OUT_SCHEDULED;
    private static final EventHandler<MouseEvent> MOUSE_OVER_PLAYER_WITH_FULLSCREEN_BUTTON_DETECTION_HANDLER = e -> {
        fadeInFullscreenButtonAndScheduleFadeOut();
    };

    private static void fadeInFullscreenButtonAndScheduleFadeOut() {
        if (FULLSCREEN_BUTTON_TIMELINE == null || Animations.isTimelineFinished(FULLSCREEN_BUTTON_TIMELINE))
            fadeFullscreenButton(true);
        fadeFullscreenButton(false);
    }

    public static void setupModalityVideoOverlay() {
        FullscreenButton.setupFullscreenButton(
            ModalityVideoOverlay::showFullscreenButton,
            ModalityVideoOverlay::hideFullscreenButton,
            ModalityVideoOverlay::animateFullscreenButton
        );
        RELOAD_BUTTON.setOpacity(0);
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
            if (!overlayChildren.contains(RELOAD_BUTTON)) {
                overlayChildren.add(RELOAD_BUTTON);
            }
            RELOAD_BUTTON.setOnMouseClicked(e -> playingPlayer.reload());
        } else {
            overlayChildren = FXMainFrameOverlayArea.getOverlayChildren();
            overlayArea = FXMainFrameOverlayArea.getOverlayArea();
        }
        // Translating the fullscreen button down and then animate it
        if (!overlayChildren.contains(FULLSCREEN_BUTTON)) {
            overlayChildren.add(FULLSCREEN_BUTTON);
            FULLSCREEN_BUTTON.setManaged(false); // We don't want the player overlay (StackPane) to center and resize
            RELOAD_BUTTON.setManaged(false);
            // the fullscreen button, we manage it ourselves as follows:
            FULLSCREEN_LAYOUT = FXProperties.runNowAndOnPropertiesChange(() -> {
                double width = overlayArea.getWidth();
                FULLSCREEN_BUTTON.resizeRelocate(width - 60, 10, 50, 50);
                RELOAD_BUTTON.resizeRelocate(width - 60, overlayArea.getHeight() / 2 - 25, 50, 50);
            }, overlayArea.widthProperty(), overlayArea.heightProperty());
            FULLSCREEN_BUTTON.setTranslateY(-60);
        }
        FULLSCREEN_BUTTON_ANIMATED = false;
        stopPreviousFullscreenButtonAnimation();
        if (FULLSCREEN_BUTTON.getTranslateY() < 0) {
            FXProperties.setEvenIfBound(FULLSCREEN_BUTTON.opacityProperty(), 0);
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
            MediaButtons.animateFullscreenButton(fullscreenButton, () -> {
                Animations.fadeIn(RELOAD_BUTTON).setOnFinished(e -> RELOAD_BUTTON.opacityProperty().bind(FULLSCREEN_BUTTON.opacityProperty()));
            });
            // When the fullscreen button is displayed in the player, we can't keep it shown all the time because it
            // covers the video. So we display it only for a short time and then fade it out until we detect some
            // mouse movement on the player, then we fade it back in.
            fadeInFullscreenButtonAndScheduleFadeOut();
            // Now we select the node that we will listen to in order to detect when the mouse is over the player
            Player player = FullscreenButton.getSuitableFullscreenPlayingPlayer();
            Node mouseOverPlayerDetectionNode;
            // If it's not in the browser (OpenJFX), we can simply take the media view of the player
            if (!UserAgent.isBrowser()) {
                mouseOverPlayerDetectionNode = player.getMediaView();
            } else { // But if it's in the browser, it's a bit more challenging.
                // The issue is that the player is likely an iFrame, which is like a black hole regarding events.
                // So what we do is to add a transparent overlay that covers the main player area, and then we listen
                // to mouse events on that overlay. This trick solves this black hole issue, and it works in all browsers.
                // However, the downside is that it prevents the user from interacting with the player controls!
                // To solve this issue, we make the overlay mouse transparent as soon as a mouse event is detected, for
                // the time the fullscreen button is displayed (i.e., 10 seconds). When the fullscreen button is faded
                // out, we restore the overlay mouse transparency to detect again mouse events to fade it back in.

                // First, we check if that overlay has been already added from a previous pass.
                Node userActivityDetectionOverlay = Collections.findFirst(player.getOverlayChildren(), ModalityVideoOverlay::isUserActivityDetectionOverlay);
                // If not, we create it and add it to the player overlay area.
                if (userActivityDetectionOverlay == null) {
                    userActivityDetectionOverlay = new Region();
                    userActivityDetectionOverlay.setId(USER_ACTIVITY_DETECTION_OVERLAY_ID);
                    // Actually not covering the bottom (player controls) and left (unmute button on Castr) to minimize
                    // the risk of dropping the first event interaction with these controls.
                    StackPane.setMargin(userActivityDetectionOverlay, new javafx.geometry.Insets(0, 0, 70, 70));
                    player.getOverlayChildren().add(userActivityDetectionOverlay);
                    // This is the important part: we make the overlay mouse transparent when the fullscreen button is
                    // displayed (i.e., visible and not faded out), because there is no need anymore to cause this button
                    // to appear (it's already there) for the next 5s it is displayed. And this is what allows the user
                    // to interact with the player controls.
                    userActivityDetectionOverlay.mouseTransparentProperty().bind(
                        FULLSCREEN_BUTTON.visibleProperty()
                            .and(FULLSCREEN_BUTTON.opacityProperty().greaterThan(0))
                    );
                }
                mouseOverPlayerDetectionNode = userActivityDetectionOverlay;
                // FOR DEBUG ONLY TO SEE WHEN THE userActivityDetectionOverlay IS MOUSE TRANSPARENT OR NOT
                /*FXProperties.runOnPropertyChange(mouseTransparent -> {
                    ((Region) mouseOverPlayerDetectionNode).setBackground(mouseTransparent ? null : Background.fill(Color.GREEN));
                    mouseOverPlayerDetectionNode.setOpacity(0.5);
                }, mouseOverPlayerDetectionNode.mouseTransparentProperty());*/
            }
            // Installing the mouse over player detection handler (first removing it in case it was already installed)
            mouseOverPlayerDetectionNode.removeEventHandler(MouseEvent.ANY, MOUSE_OVER_PLAYER_WITH_FULLSCREEN_BUTTON_DETECTION_HANDLER);
            mouseOverPlayerDetectionNode.addEventFilter(MouseEvent.ANY, MOUSE_OVER_PLAYER_WITH_FULLSCREEN_BUTTON_DETECTION_HANDLER);
        }
        FULLSCREEN_BUTTON_ANIMATED = true;
    }

    private static boolean isUserActivityDetectionOverlay(Node node) {
        return USER_ACTIVITY_DETECTION_OVERLAY_ID.equals(node.getId());
    }

    private static void fadeFullscreenButton(boolean fadeIn) {
        if (FADE_OUT_SCHEDULED != null) {
            FADE_OUT_SCHEDULED.cancel();
            FADE_OUT_SCHEDULED = null;
        }
        if (fadeIn) {
            stopPreviousFullscreenButtonAnimation();
            FULLSCREEN_BUTTON_TIMELINE = Animations.fade(FULLSCREEN_BUTTON, true, false);
        } else {
            FADE_OUT_SCHEDULED = UiScheduler.scheduleDelay(FADE_OUT_DELAY_MILLIS, () -> {
                FULLSCREEN_BUTTON_TIMELINE = Animations.fade(FULLSCREEN_BUTTON, false, false);
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
