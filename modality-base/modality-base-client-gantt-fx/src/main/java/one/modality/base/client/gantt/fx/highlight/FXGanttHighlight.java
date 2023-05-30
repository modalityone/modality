package one.modality.base.client.gantt.fx.highlight;

import dev.webfx.extras.canvas.layer.interact.CanvasInteractionHandler;
import dev.webfx.extras.canvas.layer.interact.HasCanvasInteractionManager;
import dev.webfx.extras.time.projector.HasTimeProjector;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;

import java.time.LocalDate;
import java.util.Objects;

/**
 * @author Bruno Salmon
 */
public class FXGanttHighlight {

    private final static ObjectProperty<LocalDate> ganttHighlightedDayProperty = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            //System.out.println(get());
        }
    };


    public static ObjectProperty<LocalDate> ganttHighlightedDayProperty() {
        return ganttHighlightedDayProperty;
    }

    public static LocalDate getGanttHighlightedDay() {
        return ganttHighlightedDayProperty.get();
    }

    public static void setGanttHighlighted(LocalDate day) {
        if (!Objects.equals(day, getGanttHighlightedDay()))
            ganttHighlightedDayProperty.set(day);
    }


    public static void addDayHighlight(HasTimeProjector<LocalDate> hasTimeProjector, HasCanvasInteractionManager hasCanvasInteractionManager) {
        hasCanvasInteractionManager.getCanvasInteractionManager().addHandler(new CanvasInteractionHandler() {
            @Override
            public boolean handleMouseMoved(MouseEvent e, Canvas canvas) {
                LocalDate day = hasTimeProjector.getTimeProjector().xToTime(e.getX());
                FXGanttHighlight.setGanttHighlighted(day);
                return true;
            }
        });
    }

}
