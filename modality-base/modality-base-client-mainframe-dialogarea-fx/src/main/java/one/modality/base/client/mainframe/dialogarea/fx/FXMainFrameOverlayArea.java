package one.modality.base.client.mainframe.dialogarea.fx;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Region;

/**
 * @author Bruno Salmon
 */
public final class FXMainFrameOverlayArea {

    private static final ObjectProperty<Region> overlayAreaProperty = new SimpleObjectProperty<>();
    private static final ObservableList<Node> overlayChildren = FXCollections.observableArrayList();

    public static ObjectProperty<Region> overlayAreaProperty() {
        return overlayAreaProperty;
    }

    public static Region getOverlayArea() {
        return overlayAreaProperty().get();
    }

    public static void setOverlayArea(Region dialogArea) {
        overlayAreaProperty.set(dialogArea);
    }

    public static ObservableList<Node> getOverlayChildren() {
        return overlayChildren;
    }

}
