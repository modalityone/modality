package one.modality.base.client.mainframe.fx;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.Pane;

/**
 * @author Bruno Salmon
 */
public final class FXMainFrameDialogArea {

    private static final ObjectProperty<Pane> dialogAreaProperty = new SimpleObjectProperty<>();

    public static ObjectProperty<Pane> dialogAreaProperty() {
        return dialogAreaProperty;
    }

    public static Pane getDialogArea() {
        return dialogAreaProperty().get();
    }

    public static void setDialogArea(Pane dialogArea) {
        dialogAreaProperty.set(dialogArea);
    }

}
