package one.modality.base.frontoffice.activities.mainframe;

import dev.webfx.extras.panes.MonoPane;
import dev.webfx.kit.util.aria.Aria;
import dev.webfx.kit.util.aria.AriaRole;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.util.collection.Collections;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author Bruno Salmon
 */
final class SegmentedButton<T> {

    private static final Color SEGMENTED_BUTTON_COLOR = Color.web("#0096D6");
    private static final double RADII = 10;

    private final ButtonSegment<T>[] buttonSegments;
    private final MonoPane[] frames;
    private final HBox hBox;
    private final ObjectProperty<T> stateProperty = FXProperties.newObjectProperty(this::updateFrames);

    @SafeVarargs
    public SegmentedButton(ButtonSegment<T>... buttonSegments) {
        this(null, buttonSegments);
    }

    @SafeVarargs
    public SegmentedButton(T initialState, ButtonSegment<T>... buttonSegments) {
        this.buttonSegments = buttonSegments;
        frames = Arrays.stream(buttonSegments).map(this::createSegmentFrame).toArray(MonoPane[]::new);
        hBox = new HBox(frames);
        hBox.getStyleClass().setAll("segmented-button");
        Aria.setAriaRole(hBox, AriaRole.RADIOGROUP);
        Aria.setAriaLabel(hBox, "Language selector");
        setState(initialState);
    }

    public HBox getView() {
        return hBox;
    }

    public Object getState() {
        return stateProperty.get();
    }

    public ObjectProperty<T> stateProperty() {
        return stateProperty;
    }

    public void setState(T state) {
        stateProperty.set(state);
    }

    private MonoPane createSegmentFrame(ButtonSegment<T> buttonSegment) {
        Node graphic = buttonSegment.getGraphic();
        graphic.setMouseTransparent(true);
        MonoPane frame = new MonoPane(graphic);
        frame.getStyleClass().setAll("button-segment");
        Aria.setAriaRole(frame, AriaRole.RADIO);
        boolean first = buttonSegment == buttonSegments[0];
        frame.setBorder(new Border(new BorderStroke(SEGMENTED_BUTTON_COLOR, BorderStrokeStyle.SOLID, segmentRadii(buttonSegment), new BorderWidths(1, 1 , 1, first ? 1 : 0), null)));
        frame.setCursor(Cursor.HAND);
        frame.setMaxHeight(Double.MAX_VALUE); // So the buttons can grow and always fit in HBox container bar
        frame.setOnMouseClicked(e -> setState(buttonSegment.getState()));
        return frame;
    }

    private CornerRadii segmentRadii(ButtonSegment<T> buttonSegment) {
        boolean first = buttonSegment == buttonSegments[0];
        boolean last = buttonSegment == buttonSegments[buttonSegments.length - 1];
        return new CornerRadii(first ? RADII : 0, last ? RADII : 0, last ? RADII : 0, first ? RADII : 0, false);
    }

    private void updateFrames() {
        updateFramesFromState(getState());
    }

    private void updateFramesFromState(Object state) {
        for (int i = 0, n = buttonSegments.length; i < n; i++) {
            ButtonSegment<T> buttonSegment = buttonSegments[i];
            MonoPane frame = frames[i];
            boolean selected = Objects.equals(state, buttonSegment.getState());
            if (selected) {
                Collections.addIfNotContains("selected", frame.getStyleClass());
                frame.setBackground(new Background(new BackgroundFill(SEGMENTED_BUTTON_COLOR, segmentRadii(buttonSegment), null)));
            } else {
                frame.getStyleClass().remove("selected");
                frame.setBackground(null);
            }
            Aria.setAriaSelected(frame, selected);
        }
    }
}
