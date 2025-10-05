package one.modality.event.client.mediaview;

import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
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
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * @author Bruno Salmon
 */
public final class MediaButtons {

    private static final String BACKWARD_BUTTON_PATH_32 = "M4.6 27.2C7.6 30.2 11.9 31.8 16.4 31.3 24.5 30.4 30.3 23.2 29.4 15.2 28.5 7.2 21.3 1.4 13.2 2.3 9.3 2.8 6 4.6 3.6 7.4M2.6.6 3.3 8.2 10.8 8.8";
    private static final String FORWARD_BUTTON_PATH_32 = "M27.6 27.2C24.5 30.2 20.2 31.8 15.7 31.3 7.6 30.4 1.8 23.2 2.7 15.2 3.6 7.2 10.9 1.4 18.9 2.3c3.9.4 7.2 2.3 9.6 5m1-6.7-.6 7.5-7.6.7";

    private static final String PLAY_TRIANGLE_PATH_15 = "M13.7 5.5C14.1 5.7 14.4 6 14.7 6.3 14.9 6.7 15 7.1 15 7.5 15 7.9 14.9 8.3 14.7 8.7 14.4 9 14.1 9.3 13.7 9.5L3.6 14.7C2 15.6 0 14.5 0 12.7V2.3C0 .5 2-.6 3.6.3L13.7 5.5Z";
    private static final String PAUSE_SIGN_PATH_15 = "M9.5 23H13.5V9H9.5V23ZM17.5 9V23H21.5V9H17.5Z";

    private static final String FULLSCREEN_PATH_16 = "M2.3 10.3H0V16H5.7V13.7H2.3ZM0 5.7H2.3V2.3H5.7V0H0ZM13.7 13.7H10.3V16H16V10.3H13.7ZM10.3 0v2.3h3.4V5.7H16V0Z";
    private static final String RELOAD_PATH_16 = "M16 6.2a.6.6 90 01-.6.6H11.5a.6.6 90 01-.4-1l1.5-1.5-.4-.4a5.8 5.8 90 10-4.2 9.9 5.8 5.8 90 005.5-3.9A1.1 1.1 90 1115.5 10.6 8 8 90 118 0a7.9 7.9 90 015.8 2.5l0 0 .3.3 1-1a.6.6 90 01.4-.2.6.6 90 01.6.6Z";

    public static Pane createBackwardButton() {
        Text text = Bootstrap.textSecondary(new Text("10"));
        text.setFont(Font.font(8));
        return embedButton(new StackPane(
            createSVGButton(BACKWARD_BUTTON_PATH_32, Color.BLACK, null),
            text
        ));
    }

    public static Pane createForwardButton() {
        Text text = Bootstrap.textSecondary(new Text("30"));
        text.setFont(Font.font(8));
        return embedButton(new StackPane(
            createSVGButton(FORWARD_BUTTON_PATH_32, Color.BLACK, null),
            translateX(text, 2) // Needs to be translated a bit to look centered with SVG
        ));
    }

    public static Pane createPlayButton() {
        return embedButton(new StackPane(
            createMediaButtonStyledBackgroundCircle(),
            translateX(createSVGButton(PLAY_TRIANGLE_PATH_15, null, Color.WHITE), 1) // Needs to be translated a bit to look centered with SVG
        ));
    }

    public static Pane createPauseButton() {
        return embedButton(new StackPane(
            createMediaButtonStyledBackgroundCircle(),
            createSVGButton(PAUSE_SIGN_PATH_15, null, Color.WHITE)));
    }

    public static Pane createFullscreenButton() {
        return embedButton(new StackPane(
            createMediaButtonStyledBackgroundCircle(),
            createSVGButton(FULLSCREEN_PATH_16, null, Color.WHITE)));
    }

    public static Pane createReloadButton() {
        return embedButton(new StackPane(
            createMediaButtonStyledBackgroundCircle(),
            createSVGButton(RELOAD_PATH_16, null, Color.WHITE)));
    }

    private static Circle createMediaButtonStyledBackgroundCircle() {
        Circle circle = new Circle(16);
        circle.getStyleClass().setAll("media-button"); // background color is defined in CSS
        return circle;
    }

    public static void animateFullscreenButton(Pane fullscreenButton) {
        animateFullscreenButton(fullscreenButton, null);
    }

    public static void animateFullscreenButton(Pane fullscreenButton, Runnable onFinished) {
        StackPane stackPane = (StackPane) ((ScalePane) fullscreenButton).getContent();
        Circle fadingCircle = createMediaButtonStyledBackgroundCircle();
        fadingCircle.setCenterX(16);
        fadingCircle.setCenterY(16);
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
            if (onFinished != null)
                onFinished.run();
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
