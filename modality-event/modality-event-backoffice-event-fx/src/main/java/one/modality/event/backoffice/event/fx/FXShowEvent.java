package one.modality.event.backoffice.event.fx;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * @author Bruno Salmon
 */
public final class FXShowEvent {

    private final static BooleanProperty showEventProperty = new SimpleBooleanProperty(false);

    public static ReadOnlyBooleanProperty showEventProperty() {
        return showEventProperty;
    }

    public static boolean isShowEvent() {
        return showEventProperty.get();
    }

    public static void setShowEvent(boolean showEvent) {
       showEventProperty.set(showEvent);
    }

}
