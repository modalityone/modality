package one.modality.base.client.mainframe.dialogarea.fx;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * @author Bruno Salmon
 */
public class FXMainFrameTransiting {

    private static final BooleanProperty transitingProperty = new SimpleBooleanProperty();

    public static BooleanProperty transitingProperty() {
        return transitingProperty;
    }

    public static void setTransiting(boolean transiting) {
        transitingProperty.set(transiting);
    }

    public static boolean isTransiting() {
        return transitingProperty.get();
    }

}
