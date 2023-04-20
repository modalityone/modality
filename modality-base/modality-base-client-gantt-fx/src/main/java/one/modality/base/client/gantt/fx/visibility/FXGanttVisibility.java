package one.modality.base.client.gantt.fx.visibility;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * @author Bruno Salmon
 */
public final class FXGanttVisibility {

    private final static ObjectProperty<GanttVisibility> ganttVisibilityProperty = new SimpleObjectProperty<>(GanttVisibility.HIDDEN) {
        @Override
        protected void invalidated() {
            dev.webfx.platform.console.Console.log(get());
        }
    };

    public static ObjectProperty<GanttVisibility> ganttVisibilityProperty() {
        return ganttVisibilityProperty;
    }

    public static GanttVisibility getGanttVisibility() {
        return ganttVisibilityProperty.get();
    }

    public static void setGanttVisibility(GanttVisibility ganttVisibility) {
        ganttVisibilityProperty.set(ganttVisibility);
    }

    public static boolean isHidden() {
        return getGanttVisibility() == GanttVisibility.HIDDEN;
    }

    public static boolean isVisible() {
        return !isHidden();
    }

    public static boolean showMonths() {
        return getGanttVisibility().ordinal() >= GanttVisibility.MONTHS.ordinal();
    }

    public static boolean showDays() {
        return getGanttVisibility().ordinal() >= GanttVisibility.DAYS.ordinal();
    }

    public static boolean showEvents() {
        return getGanttVisibility().ordinal() >= GanttVisibility.EVENTS.ordinal();
    }

}
