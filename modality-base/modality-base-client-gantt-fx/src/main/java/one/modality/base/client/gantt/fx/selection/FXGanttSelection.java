package one.modality.base.client.gantt.fx.selection;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * @author Bruno Salmon
 */
public final class FXGanttSelection {

    private static final ObjectProperty<Object> ganttSelectedObjectProperty =
            new SimpleObjectProperty<>();

    public static ObjectProperty<Object> ganttSelectedObjectProperty() {
        return ganttSelectedObjectProperty;
    }

    public static Object getGanttSelectedObject() {
        return ganttSelectedObjectProperty.get();
    }

    public static void setGanttSelectedObject(Object value) {
        ganttSelectedObjectProperty.set(value);
    }
}
