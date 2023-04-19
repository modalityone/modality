package one.modality.event.backoffice.events.ganttcanvas;

import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.ThemeRegistry;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.timelayout.ChildPosition;
import dev.webfx.extras.timelayout.canvas.TimeCanvasUtil;
import dev.webfx.extras.timelayout.gantt.GanttLayout;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import one.modality.base.client.ganttcanvas.LayeredGanttCanvas;
import one.modality.base.shared.entities.Event;
import one.modality.crm.backoffice.organization.fx.FXOrganization;
import one.modality.event.backoffice.event.fx.FXEvent;
import one.modality.event.backoffice.events.pm.EventsPresentationModel;
import one.modality.event.client.theme.EventTheme;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static dev.webfx.stack.orm.dql.DqlStatement.where;

/**
 * @author Bruno Salmon
 */
public final class EventsGanttCanvas {

    private final EventsPresentationModel pm;
    private final LayeredGanttCanvas layeredGanttCanvas = new LayeredGanttCanvas();
    private final Canvas canvas = layeredGanttCanvas.getCanvas();
    private final GanttLayout<Event, LocalDate> eventsLayout = new GanttLayout<>();
    private Font EVENT_FONT;
    private final double EVENT_HEIGHT = 20;
    private final double RADIUS = 10;
    private final double H_SPACING = 2; // Max value, will be reduced when zooming out
    private final double V_SPACING = 2;

    public EventsGanttCanvas(EventsPresentationModel pm) {
        this.pm = pm;

        eventsLayout.setChildFixedHeight(EVENT_HEIGHT);
        eventsLayout.setVSpacing(V_SPACING);
        eventsLayout.setInclusiveChildStartTimeReader(Event::getStartDate);
        eventsLayout.setInclusiveChildEndTimeReader(Event::getEndDate);

        eventsLayout.getChildren().addListener((ListChangeListener<Event>) c -> layeredGanttCanvas.markLayoutAsDirty());
        eventsLayout.selectedChildProperty().addListener(observable -> layeredGanttCanvas.markCanvasAsDirty());

        layeredGanttCanvas.addLayer(eventsLayout, this::drawEvent);

        // Managing user interaction
        setupMouseHandlers();

        // Initializing the canvas time window from the presentation model (if set)
        setTimeWindow(pm.timeWindowStartProperty().getValue(), pm.timeWindowEndProperty().getValue());
        // But then, binding the presentation model to the canvas time window
        pm.timeWindowStartProperty().bind(layeredGanttCanvas.timeWindowStartProperty());
        pm.timeWindowEndProperty().bind(layeredGanttCanvas.timeWindowEndProperty());

        ThemeRegistry.runNowAndOnModeChange(() ->
                EVENT_FONT = TextTheme.getFont(FontDef.font(FontWeight.BOLD, 13))
        );
    }

    private void setTimeWindow(LocalDate start, LocalDate end) {
        layeredGanttCanvas.setTimeWindow(start, end);
    }

    private void setTimeWindow(LocalDate start, long duration) {
        setTimeWindow(start, start.plus(duration, ChronoUnit.DAYS));
    }

    private double mousePressedX;
    private LocalDate mousePressedStart;
    private long mousePressedDuration;

    private void setupMouseHandlers() {
        canvas.setOnMousePressed(e -> {
            mousePressedX = e.getX();
            mousePressedStart = layeredGanttCanvas.getTimeWindowStart();
            mousePressedDuration = ChronoUnit.DAYS.between(mousePressedStart, pm.timeWindowEndProperty().getValue());
            updateCanvasCursor(e, true);
        });
        canvas.setOnMouseDragged(e -> {
            double deltaX = mousePressedX - e.getX();
            double dayWidth = canvas.getWidth() / (mousePressedDuration + 1);
            long deltaDay = (long) (deltaX / dayWidth);
            setTimeWindow(mousePressedStart.plus(deltaDay, ChronoUnit.DAYS), mousePressedDuration);
            updateCanvasCursor(e, true);
        });
        // Selecting the event when clicked
        canvas.setOnMouseClicked(e -> {
            if (!wasDragged(e))
                eventsLayout.selectClickedChild(e.getX(), e.getY());
            updateCanvasCursor(e, false);
            mousePressedStart = null;
        });
        // Changing cursor to hand cursor when hovering an event (to indicate it's clickable)
        canvas.setOnMouseMoved(e -> updateCanvasCursor(e, false));
        canvas.setOnScroll(e -> {
            if (e.isControlDown()) {
                LocalDate start = layeredGanttCanvas.getTimeWindowStart();
                LocalDate end = layeredGanttCanvas.getTimeWindowEnd();
                long duration = ChronoUnit.DAYS.between(start, end);
                LocalDate middle = start.plus(duration / 2, ChronoUnit.DAYS);
                if (e.getDeltaY() > 0) // Mouse wheel up => Zoom in
                    duration = (long) (duration / 1.10);
                else // Mouse wheel down => Zoom out
                    duration = Math.max(duration + 1, (long) (duration * 1.10));
                duration = Math.min(duration, 10_000);
                setTimeWindow(middle.minus(duration / 2, ChronoUnit.DAYS), duration);
            }
        });
    }

    private boolean wasDragged(MouseEvent e) {
        return e.getX() != mousePressedX;
    }

    private void updateCanvasCursor(MouseEvent e, boolean mouseDown) {
        canvas.setCursor(mouseDown && wasDragged(e) ? Cursor.CLOSED_HAND : eventsLayout.pickChild(e.getX(), e.getY()) != null ? Cursor.HAND : Cursor.OPEN_HAND);
    }

    public ObjectProperty<Event> selectedEventProperty() {
        return eventsLayout.selectedChildProperty();
    }

    public void bindFXEventToSelection() {
        selectedEventProperty().set(FXEvent.getEvent());
        FXEvent.eventProperty().bindBidirectional(selectedEventProperty());
    }

    public void bindFXOrganization() {
        pm.organizationIdProperty().bind(FXOrganization.organizationProperty());
    }

    public Pane getCanvasPane() {
        return layeredGanttCanvas.getCanvasPane();
    }

    private void drawEvent(Event event, ChildPosition<LocalDate> p, GraphicsContext gc) {
        boolean selected = Entities.sameId(event, eventsLayout.getSelectedChild());
        double hPadding = Math.min(p.getWidth() * 0.01, H_SPACING);
        TimeCanvasUtil.fillRect(p, hPadding, EventTheme.getEventBackgroundColor(event, selected), RADIUS, gc);
        if (p.getWidth() > 5) { // Unnecessary to draw text when width < 5px (this skip makes a big performance improvement on big zoom out over many events - because the text clip operation is time-consuming)
            gc.setFont(EVENT_FONT);
            TimeCanvasUtil.fillCenterText(p, hPadding, event.getPrimaryKey() + " " + event.getName(), EventTheme.getEventTextColor(), gc);
        }
    }

    public void startLogic(Object mixin) {
        ReactiveEntitiesMapper.<Event>createPushReactiveChain(mixin)
                .always("{class: 'Event', alias: 'e', fields: 'name,startDate,endDate', where: 'active'}")
                // Search box condition
                //.ifTrimNotEmpty(pm.searchTextProperty(), s -> where("lower(name) like ?", "%" + s.toLowerCase() + "%"))
                //.ifNotEquals(FXGanttVisibility.ganttVisibilityProperty(), GanttVisibility.EVENTS, DqlStatement.EMPTY_STATEMENT)
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), o -> where("e.organization=?", o))
                .always(pm.timeWindowStartProperty(), startDate -> where("e.endDate >= ?", startDate))
                .always(pm.timeWindowEndProperty(), endDate -> where("e.startDate <= ?", endDate))
                .always(pm.timeWindowStartProperty(), startDate -> DqlStatement.orderBy("greatest(e.startDate, ?),id", startDate))
                .storeEntitiesInto(eventsLayout.getChildren())
                .start()
        ;
    }

}
