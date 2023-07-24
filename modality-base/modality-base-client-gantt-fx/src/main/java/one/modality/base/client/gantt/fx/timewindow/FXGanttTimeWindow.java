package one.modality.base.client.gantt.fx.timewindow;

import dev.webfx.extras.time.projector.HasTimeProjector;
import dev.webfx.extras.time.projector.PairedTimeProjector;
import dev.webfx.extras.time.projector.TimeProjector;
import dev.webfx.extras.time.window.TimeWindowUtil;
import dev.webfx.extras.time.window.impl.TimeWindowImpl;
import dev.webfx.kit.util.properties.FXProperties;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

/**
 * @author Bruno Salmon
 */
public final class FXGanttTimeWindow extends TimeWindowImpl<LocalDate> {

    private static final ObjectProperty<TimeProjector<LocalDate>> timeProjectorProperty =
            new SimpleObjectProperty<>();
    private static final ObjectProperty<Node> ganttNodeProperty = new SimpleObjectProperty<>();

    @Override
    protected void onTimeWindowChanged() {
        // Console.log("ganttTimeWindow = [" + getTimeWindowStart() + ", " + getTimeWindowEnd() +
        // "]");
    }

    private FXGanttTimeWindow() {
        TimeWindowUtil.setTimeWindowStartAndDuration(
                this,
                LocalDate.now().with(TemporalAdjusters.previous(DayOfWeek.MONDAY)),
                2,
                ChronoUnit.WEEKS);
    }

    private static final FXGanttTimeWindow ganttTimeWindow = new FXGanttTimeWindow();

    public static FXGanttTimeWindow ganttTimeWindow() {
        return ganttTimeWindow;
    }

    public static Node getGanttNode() {
        return ganttNodeProperty.get();
    }

    public static void setGanttNode(Node ganttNode) {
        ganttNodeProperty.set(ganttNode);
    }

    public static TimeProjector<LocalDate> getTimeProjector() {
        return timeProjectorProperty.get();
    }

    public static void setTimeProjector(TimeProjector<LocalDate> timeProjector) {
        timeProjectorProperty.set(timeProjector);
    }

    public static PairedTimeProjector<LocalDate> createPairedTimeProjector(Node node) {
        return new PairedTimeProjector<>(getTimeProjector(), getGanttNode(), node);
    }

    public static void setupPairedTimeProjectorWhenReady(
            HasTimeProjector<LocalDate> hasTimeProjector, Node node) {
        if (getTimeProjector() == null)
            FXProperties.onPropertySet(
                    timeProjectorProperty,
                    p -> setupPairedTimeProjectorWhenReady(hasTimeProjector, node),
                    false);
        else if (getGanttNode() == null)
            FXProperties.onPropertySet(
                    ganttNodeProperty,
                    p -> setupPairedTimeProjectorWhenReady(hasTimeProjector, node),
                    false);
        else hasTimeProjector.setTimeProjector(createPairedTimeProjector(node));
    }
}
