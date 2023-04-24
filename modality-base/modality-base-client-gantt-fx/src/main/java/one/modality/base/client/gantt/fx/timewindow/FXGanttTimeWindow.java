package one.modality.base.client.gantt.fx.timewindow;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.LocalDate;
import java.util.Objects;

/**
 * @author Bruno Salmon
 */
public class FXGanttTimeWindow {

    private final static ObjectProperty<LocalDate> ganttTimeWindowStartProperty = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            dev.webfx.platform.console.Console.log("ganttTimeWindowStart = " + get());
        }
    };

    private final static ObjectProperty<LocalDate> ganttTimeWindowEndProperty = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            dev.webfx.platform.console.Console.log("ganttTimeWindowEnd = " + get());
        }
    };

    public static ObjectProperty<LocalDate> ganttTimeWindowStartProperty() {
        return ganttTimeWindowStartProperty;
    }

    public static LocalDate getGanttTimeWindowStart() {
        return ganttTimeWindowStartProperty().get();
    }

    public static void setGanttTimeWindowStart(LocalDate timeWindowStart) {
        if (!Objects.equals(timeWindowStart, getGanttTimeWindowStart()))
            ganttTimeWindowStartProperty().set(timeWindowStart);
    }

    public static ObjectProperty<LocalDate> ganttTimeWindowEndProperty() {
        return ganttTimeWindowEndProperty;
    }

    public static LocalDate getGanttTimeWindowEnd() {
        return ganttTimeWindowEndProperty().get();
    }

    public static void setGanttTimeWindowEnd(LocalDate timeWindowEnd) {
        if (!Objects.equals(timeWindowEnd, getGanttTimeWindowEnd()))
            ganttTimeWindowEndProperty().set(timeWindowEnd);
    }
    
    
}
