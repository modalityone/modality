package one.modality.base.client.gantt.fx.visibility;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * @author Bruno Salmon
 */
public final class FXGanttVisibility {

    private final static ObjectProperty<GanttVisibility> ganttVisibilityProperty = new SimpleObjectProperty<>(GanttVisibility.HIDDEN);

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

    public static boolean isShowingMonths() {
        return getGanttVisibility().ordinal() >= GanttVisibility.MONTHS.ordinal();
    }

    public static boolean isShowingDays() {
        return getGanttVisibility().ordinal() >= GanttVisibility.DAYS.ordinal();
    }

    public static boolean isShowingEvents() {
        return getGanttVisibility().ordinal() >= GanttVisibility.ALL_EVENTS.ordinal();
    }

    public static void showMonths() {
        setGanttVisibility(GanttVisibility.MONTHS);
    }

    public static void showDays() {
        setGanttVisibility(GanttVisibility.DAYS);
    }

    public static void showAllEvents() {
        setGanttVisibility(GanttVisibility.ALL_EVENTS);
    }

    public static void showOnsiteAccommodationEvents() {
        setGanttVisibility(GanttVisibility.ONSITE_ACCOMMODATION_EVENTS);
    }

    public static void showRecurringEvents() {
        setGanttVisibility(GanttVisibility.RECURRING_EVENTS);
    }

    public static void resetToDefault() {
        setGanttVisibility(GanttVisibility.HIDDEN);
    }

}
