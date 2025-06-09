package one.modality.base.frontoffice.activities.mainframe;

import dev.webfx.extras.aria.AriaToggleGroup;
import dev.webfx.kit.util.aria.Aria;
import dev.webfx.kit.util.aria.AriaRole;
import dev.webfx.kit.util.properties.ObservableLists;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Cursor;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;

/**
 * @author Bruno Salmon
 */
final class SegmentedButton<T> {

    private final AriaToggleGroup<T> ariaToggleGroup = new AriaToggleGroup<>(AriaRole.RADIO);
    private final HBox hBox = new HBox();

    public SegmentedButton() {
        ObservableLists.bind(hBox.getChildren(), ariaToggleGroup.getToggleButtons());
        hBox.getStyleClass().setAll("segmented-button");
        Aria.setAriaRole(hBox, AriaRole.RADIOGROUP);
        Aria.setAriaLabel(hBox, "Language selector");
    }

    public HBox getView() {
        return hBox;
    }

    public T getState() {
        return ariaToggleGroup.getFiredItem();
    }

    public ObjectProperty<T> stateProperty() {
        return ariaToggleGroup.firedItemProperty();
    }

    public void setState(T state) {
        ariaToggleGroup.setFiredItem(state);
    }

    public ToggleButton addButtonSegment(T item, String text) {
        ToggleButton button = ariaToggleGroup.createItemButton(item);
        button.setText(text);
        button.getStyleClass().add("button-segment");
        button.setCursor(Cursor.HAND);
        button.setMaxHeight(Double.MAX_VALUE); // So the buttons can grow and always fit in HBox container bar
        return button;
    }

}
