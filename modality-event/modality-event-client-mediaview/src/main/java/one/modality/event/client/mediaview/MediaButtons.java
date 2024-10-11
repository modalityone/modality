package one.modality.event.client.mediaview;

import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.util.animation.Animations;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.util.Duration;
import one.modality.base.frontoffice.utility.StyleUtility;
import one.modality.base.frontoffice.utility.TextUtility;

/**
 * @author Bruno Salmon
 */
public final class MediaButtons {

    private static final String BACKWARD_BUTTON_PATH_32 = "M 4.5868653,27.245536 C 7.6224398,30.16588 11.90083,31.770106 16.433304,31.273183 24.494208,30.389354 30.306212,23.193922 29.414735,15.201985 28.523257,7.2098701 21.265869,1.447545 13.204965,2.3313863 9.3264072,2.7566642 5.9684961,4.6430958 3.6274403,7.376399 M 2.637407,0.63740701 3.2538306,8.1506829 10.827947,8.8079259";
    private static final String FORWARD_BUTTON_PATH_32 = "M 27.555364,27.245536 C 24.51979,30.16588 20.241399,31.770106 15.708925,31.273183 7.6480214,30.389354 1.8360174,23.193922 2.7274944,15.201985 3.6189724,7.2098701 10.87636,1.447545 18.937264,2.3313863 c 3.878558,0.4252779 7.236469,2.3117095 9.577525,5.0450127 m 0.990033,-6.73899199 -0.616423,7.51327589 -7.574117,0.657243";

    private static final String PLAY_TRIANGLE_PATH_15 = "M13.7444 5.51497C14.1236 5.70652 14.4408 5.99248 14.662 6.3422C14.8831 6.69192 15 7.09222 15 7.50021C15 7.9082 14.8831 8.3085 14.662 8.65822C14.4408 9.00794 14.1236 9.2939 13.7444 9.48545L3.62933 14.7107C2.00059 15.5522 0 14.4572 0 12.7262V2.27497C0 0.542479 2.00059 -0.551768 3.62933 0.28898L13.7444 5.51497Z";
    private static final String PAUSE_SIGN_PATH_15 = "M9.5 23H13.5V9H9.5V23ZM17.5 9V23H21.5V9H17.5Z";

    private static final String FULLSCREEN_PATH_16 = "M 2.2857143,10.285714 H 0 V 16 H 5.7142857 V 13.714286 H 2.2857143 Z M 0,5.7142857 H 2.2857143 V 2.2857143 H 5.7142857 V 0 H 0 Z M 13.714286,13.714286 H 10.285714 V 16 H 16 V 10.285714 H 13.714286 Z M 10.285714,0 v 2.2857143 h 3.428572 V 5.7142857 H 16 V 0 Z";

    public static Pane createBackwardButton() {
        return embedButton(new StackPane(
            createSVGButton(BACKWARD_BUTTON_PATH_32, Color.BLACK, null),
            TextUtility.createText("10", Color.GRAY, 8)));
    }

    public static Pane createForwardButton() {
        return embedButton(new StackPane(
            createSVGButton(FORWARD_BUTTON_PATH_32, Color.BLACK, null),
            translateX(TextUtility.createText("30", Color.GRAY, 8), 2) // Needs to be translated a bit to look centered with SVG
        ));
    }

    public static Pane createPlayButton() {
        return embedButton(new StackPane(
            new Circle(16, StyleUtility.MAIN_ORANGE_COLOR),
            translateX(createSVGButton(PLAY_TRIANGLE_PATH_15, null, Color.WHITE), 1) // Needs to be translated a bit to look centered with SVG
        ));
    }

    public static Pane createPauseButton() {
        return embedButton(new StackPane(
            new Circle(16, StyleUtility.MAIN_ORANGE_COLOR),
            createSVGButton(PAUSE_SIGN_PATH_15, null, Color.WHITE)));
    }

    public static Pane createFullscreenButton() {
        return embedButton(new StackPane(
            new Circle(16, StyleUtility.MAIN_ORANGE_COLOR),
            createSVGButton(FULLSCREEN_PATH_16, null, Color.WHITE)));
    }

    public static void animateFullscreenButton(Pane fullscreenButton) {
        StackPane stackPane = (StackPane) ((ScalePane) fullscreenButton).getContent();
        Circle fadingCircle = new Circle(16, 16, 16, StyleUtility.MAIN_ORANGE_COLOR);
        fadingCircle.setManaged(false);
        stackPane.getChildren().add(0, fadingCircle);
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().addAll(
            new KeyFrame(Duration.millis(300),
                new KeyValue(fullscreenButton.scaleXProperty(), 1.2),
                new KeyValue(fullscreenButton.scaleYProperty(), 1.2)
            ),
            new KeyFrame(Duration.millis(500),
                new KeyValue(fullscreenButton.scaleXProperty(), 0.8, Animations.EASE_BOTH_INTERPOLATOR),
                new KeyValue(fullscreenButton.scaleYProperty(), 0.8, Animations.EASE_BOTH_INTERPOLATOR),
                new KeyValue(fadingCircle.radiusProperty(), 32, Animations.EASE_BOTH_INTERPOLATOR),
                new KeyValue(fadingCircle.opacityProperty(), 0, Animations.EASE_BOTH_INTERPOLATOR)
            ),
            new KeyFrame(Duration.millis(900),
                new KeyValue(fullscreenButton.scaleXProperty(), 1, Animations.EASE_BOTH_INTERPOLATOR),
                new KeyValue(fullscreenButton.scaleYProperty(), 1, Animations.EASE_BOTH_INTERPOLATOR)
            )
        );
        timeline.play();
        timeline.setOnFinished(e -> {
            stackPane.getChildren().remove(fadingCircle);
        });
    }

    private static SVGPath createSVGButton(String content, Paint stroke, Paint fill) {
        SVGPath path = new SVGPath();
        path.setContent(content);
        path.setStroke(stroke);
        path.setStrokeLineCap(StrokeLineCap.ROUND);
        path.setStrokeLineJoin(StrokeLineJoin.ROUND);
        path.setFill(fill);
        path.setStrokeWidth(1.5);
        return path;
    }

    private static Pane embedButton(Pane container) {
        container.setMaxSize(32, 32); // Setting a max size, so it can scale
        Pane pane = new ScalePane(container);
        pane.setCursor(Cursor.HAND);
        return pane;
    }

    private static <N extends Node> N translateX(N node, double translateX) {
        node.setTranslateX(translateX);
        return node;
    }

}
