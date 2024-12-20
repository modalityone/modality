package one.modality.base.backoffice.mainframe.fx;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * @author Bruno Salmon
 */
public class FXEventSelector {

    private final static BooleanProperty eventSelectorVisibleProperty = new SimpleBooleanProperty();

    public static ReadOnlyBooleanProperty eventSelectorVisibleProperty() {
        return eventSelectorVisibleProperty;
    }

    public static boolean isEventSelectorVisible() {
        return eventSelectorVisibleProperty.get();
    }

    public static void setEventSelectorVisible(boolean visible) {
        eventSelectorVisibleProperty.set(visible);
    }

    public static void showEventSelector() {
        setEventSelectorVisible(true);
    }

    public static void hideEventSelector() {
        setEventSelectorVisible(false);
    }

    public static void resetToDefault() {
        hideEventSelector();
    }

}
