package one.modality.base.client.gantt.fx.timewindow;

import dev.webfx.extras.time.window.impl.TimeWindowImpl;
import javafx.beans.property.ObjectProperty;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

/**
 * @author Bruno Salmon
 */
public final class FXGanttTimeWindow extends TimeWindowImpl<LocalDate> {

    @Override
    protected void onTimeWindowChanged() {
        //Console.log("ganttTimeWindow = [" + getTimeWindowStart() + ", " + getTimeWindowEnd() + "]");
    }

    private FXGanttTimeWindow() {
        setTimeWindow(LocalDate.now().with(TemporalAdjusters.previous(DayOfWeek.MONDAY)), LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY)).plus(1, ChronoUnit.WEEKS));
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
