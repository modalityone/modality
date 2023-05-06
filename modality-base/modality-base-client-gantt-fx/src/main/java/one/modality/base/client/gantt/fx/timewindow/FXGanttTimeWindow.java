package one.modality.base.client.gantt.fx.timewindow;

import dev.webfx.extras.time.window.impl.TimeWindowImpl;
import dev.webfx.platform.console.Console;
import javafx.beans.property.ObjectProperty;

import java.time.LocalDate;

/**
 * @author Bruno Salmon
 */
public final class FXGanttTimeWindow extends TimeWindowImpl<LocalDate> {

    @Override
    protected void onTimeWindowChanged() {
        Console.log("ganttTimeWindow = [" + getTimeWindowStart() + ", " + getTimeWindowEnd() + "]");
    }

    private FXGanttTimeWindow() {
        setTimeWindow(LocalDate.now().minusWeeks(1), LocalDate.now().plusWeeks(3));
    }

    private final static FXGanttTimeWindow ganttTimeWindow = new FXGanttTimeWindow();

    public static FXGanttTimeWindow ganttTimeWindow() {
        return ganttTimeWindow;
    }

    public static ObjectProperty<LocalDate> ganttTimeWindowStartProperty() {
        return ganttTimeWindow().timeWindowStartProperty();
    }

    public static LocalDate getGanttTimeWindowStart() {
        return ganttTimeWindow().getTimeWindowStart();
    }

    public static void setGanttTimeWindowStart(LocalDate timeWindowStart) {
        ganttTimeWindow().setTimeWindowStart(timeWindowStart);
    }

    public static ObjectProperty<LocalDate> ganttTimeWindowEndProperty() {
        return ganttTimeWindow().timeWindowEndProperty();
    }

    public static LocalDate getGanttTimeWindowEnd() {
        return ganttTimeWindow().getTimeWindowEnd();
    }

    public static void setGanttTimeWindowEnd(LocalDate timeWindowEnd) {
        ganttTimeWindow().setTimeWindowEnd(timeWindowEnd);
    }
    
}
