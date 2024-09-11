package one.modality.base.client.gantt.fx.interstice;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * @author Bruno Salmon
 */
public class FXGanttInterstice {

    private final static BooleanProperty ganttIntersticeVisibleProperty = new SimpleBooleanProperty();

    public static BooleanProperty ganttIntersticeRequiredProperty() {
        return ganttIntersticeVisibleProperty;
    }

    public static boolean isGanttIntersticeVisible() {
        return ganttIntersticeVisibleProperty.get();
    }

    public static void setGanttIntersticeVisible(boolean visible) {
         ganttIntersticeVisibleProperty.set(visible);
    }

    public static void showGanttInterstice() {
        setGanttIntersticeVisible(true);
    }

    public static void hideGanttInterstice() {
        setGanttIntersticeVisible(false);
    }

    public static void resetToDefault() {
        setGanttIntersticeVisible(false);
    }

}
