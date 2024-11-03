package one.modality.base.frontoffice.activities.mainframe;

import dev.webfx.extras.player.FullscreenButton;
import dev.webfx.extras.util.animation.Animations;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.Unregisterable;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.scene.Node;
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

    public static void setupModalityFullscreenButton() {
        FullscreenButton.setupFullscreenButton(
            ModalityFullscreenButton::showFullscreenButton,
            ModalityFullscreenButton::hideFullscreenButton,
            MediaButtons::animateFullscreenButton
            );
    }

    private static Pane showFullscreenButton() {
        // Translating the fullscreen button down and then animate it
        ObservableList<Node> overlayChildren = FXMainFrameOverlayArea.getOverlayChildren();
        if (!overlayChildren.contains(FULLSCREEN_BUTTON)) {
            overlayChildren.add(FULLSCREEN_BUTTON);
            Region overlayArea = FXMainFrameOverlayArea.getOverlayArea();
            FULLSCREEN_LAYOUT = FXProperties.runNowAndOnPropertiesChange(() -> {
                double width = overlayArea.getWidth();
                FULLSCREEN_BUTTON.resizeRelocate(width - 70, 10, 50, 50);
            }, overlayArea.widthProperty(), overlayArea.heightProperty());
            FULLSCREEN_BUTTON.setTranslateY(-70);
        }
        if (FULLSCREEN_BUTTON_TIMELINE != null)
            FULLSCREEN_BUTTON_TIMELINE.stop();
        if (FULLSCREEN_BUTTON.getTranslateY() < 0) {
            FULLSCREEN_BUTTON_TIMELINE = Animations.animateProperty(FULLSCREEN_BUTTON.translateYProperty(), 0);
            FULLSCREEN_BUTTON_TIMELINE.setOnFinished(e -> MediaButtons.animateFullscreenButton(FULLSCREEN_BUTTON));
        } else {
            MediaButtons.animateFullscreenButton(FULLSCREEN_BUTTON);
        }
        return FULLSCREEN_BUTTON;
    }

    private static void hideFullscreenButton(Pane FULLSCREEN_BUTTON) {
        ObservableList<Node> overlayChildren = FXMainFrameOverlayArea.getOverlayChildren();
        if (overlayChildren.contains(FULLSCREEN_BUTTON)) {
            if (FULLSCREEN_BUTTON_TIMELINE != null)
                FULLSCREEN_BUTTON_TIMELINE.stop();
            FULLSCREEN_BUTTON_TIMELINE = Animations.animateProperty(FULLSCREEN_BUTTON.translateYProperty(), -70);
            FULLSCREEN_BUTTON_TIMELINE.setOnFinished(e -> overlayChildren.remove(FULLSCREEN_BUTTON));
        }
        if (FULLSCREEN_LAYOUT != null)
            FULLSCREEN_LAYOUT.unregister();
    }

}
