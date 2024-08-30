package one.modality.base.frontoffice.mainframe.fx;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;

/**
 * @author Bruno Salmon
 */
public final class FXBackgroundNode {

    private static final ObjectProperty<Node> backgroundNodeProperty = new SimpleObjectProperty<>();

    public static Node getBackgroundNode() {
        return backgroundNodeProperty.get();
    }

    public static ObjectProperty<Node> backgroundNodeProperty() {
        return backgroundNodeProperty;
    }

    public static void setBackgroundNode(Node backgroundNode) {
        backgroundNodeProperty.set(backgroundNode);
    }
}
