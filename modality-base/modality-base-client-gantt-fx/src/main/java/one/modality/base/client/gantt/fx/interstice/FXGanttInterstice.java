package one.modality.base.client.gantt.fx.interstice;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * @author Bruno Salmon
 */
public class FXGanttInterstice {

    private static final BooleanProperty ganttIntersticeRequiredProperty =
            new SimpleBooleanProperty();

    public static BooleanProperty ganttIntersticeRequiredProperty() {
        return ganttIntersticeRequiredProperty;
    }

    public static boolean isGanttIntersticeRequired() {
        return ganttIntersticeRequiredProperty.get();
    }

    public static void setGanttIntersticeRequired(boolean breathingSpace) {
        ganttIntersticeRequiredProperty.set(breathingSpace);
    }
}
