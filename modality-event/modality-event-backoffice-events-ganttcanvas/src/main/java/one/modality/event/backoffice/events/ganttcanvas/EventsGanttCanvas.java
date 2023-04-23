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
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import one.modality.base.client.gantt.fx.visibility.FXGanttVisibility;
import one.modality.base.client.gantt.fx.visibility.GanttVisibility;
import one.modality.base.client.ganttcanvas.LocalDateLayeredGanttCanvas;
import one.modality.base.shared.entities.Event;
import one.modality.crm.backoffice.organization.fx.FXOrganization;
import one.modality.event.backoffice.event.fx.FXEvent;
import one.modality.event.backoffice.events.pm.EventsPresentationModel;
import one.modality.event.client.theme.EventTheme;

import java.time.LocalDate;

import static dev.webfx.stack.orm.dql.DqlStatement.where;

/**
 * @author Bruno Salmon
 */
@SuppressWarnings("FieldCanBeLocal") // To remove IntelliJ IDEA warnings regarding the constants
public final class EventsGanttCanvas {

    // Constants:
    private final double EVENT_HEIGHT = 20;
    private final double RADIUS = 10;
    private final double H_SPACING = 2; // Max value, may be reduced when zooming out
    private final double V_SPACING = 2;

    private final EventsPresentationModel pm;
    // The layered Gantt canvas that already displays dates, weeks, months & years (depending on zoom level)
    private final LocalDateLayeredGanttCanvas layeredGanttCanvas = new LocalDateLayeredGanttCanvas();
    // The additional layer that will display the events
    private final GanttLayout<Event, LocalDate> eventsLayer = new GanttLayout<>();
    private Font eventFont;

    public EventsGanttCanvas(EventsPresentationModel pm) {
        this.pm = pm;

        // Setting up the events layer
        eventsLayer.setChildFixedHeight(EVENT_HEIGHT);
        eventsLayer.setVSpacing(V_SPACING);
        eventsLayer.setInclusiveChildStartTimeReader(Event::getStartDate);
        eventsLayer.setInclusiveChildEndTimeReader(Event::getEndDate);

        // Adding it to the layered gantt canvas, and passing the drawEvent method
        layeredGanttCanvas.addLayer(eventsLayer, this::drawEvent);

        // Binding the presentation model with the canvas time window (first applying pm => timeWindow, then binding timeWindow => pm)
        layeredGanttCanvas.bindTimeWindow(pm.timeWindowStartProperty(), pm.timeWindowEndProperty(), true, false);

        // Activating user interaction (user can move & zoom in/out the time window)
        layeredGanttCanvas.makeInteractive();

        // Updating the events font on any theme mode change (light/dark mode, etc...)
        ThemeRegistry.runNowAndOnModeChange(() ->
                eventFont = TextTheme.getFont(FontDef.font(FontWeight.BOLD, 13))
        );
    }

    private void drawEvent(Event event, ChildPosition<LocalDate> p, GraphicsContext gc) {
        double hPadding = Math.min(p.getWidth() * 0.01, H_SPACING);
        boolean selected = Entities.sameId(event, eventsLayer.getSelectedChild());
        TimeCanvasUtil.fillRect(p, hPadding, EventTheme.getEventBackgroundColor(event, selected), RADIUS, gc);
        if (p.getWidth() > 5) { // Unnecessary to draw text when width < 5px (this skip makes a big performance improvement on big zoom out over many events - because the text clip operation is time-consuming)
            gc.setFont(eventFont);
            TimeCanvasUtil.fillCenterText(p, hPadding, event.getPrimaryKey() + " " + event.getName(), EventTheme.getEventTextColor(), gc);
        }
    }

    public Pane getCanvasPane() {
        return layeredGanttCanvas.getCanvasPane();
    }

    public ObjectProperty<Event> selectedEventProperty() {
        return eventsLayer.selectedChildProperty();
    }

    public void setupFXBindingsAndStartLogic(Object mixin) {
        setupFXBindings();
        startLogic(mixin);
    }

    private void setupFXBindings() {
        bindFXEventToSelection();
        bindFXOrganization();
        layeredGanttCanvas.setupFXBindings();
    }

    private void bindFXEventToSelection() {
        selectedEventProperty().set(FXEvent.getEvent());
        FXEvent.eventProperty().bindBidirectional(selectedEventProperty());
    }

    private void bindFXOrganization() {
        pm.organizationIdProperty().bind(FXOrganization.organizationProperty());
    }

    private void startLogic(Object mixin) {
        ReactiveEntitiesMapper.<Event>createPushReactiveChain(mixin)
                .always("{class: 'Event', alias: 'e', fields: 'name,startDate,endDate', where: 'active'}")
                // Stopping querying the server when then gantt visibility is not set to EVENTS
                .ifNotEquals(FXGanttVisibility.ganttVisibilityProperty(), GanttVisibility.EVENTS, null)
                // Returning events for the selected organization only (or returning an empty set if no organization is selected)
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), o -> where("e.organization=?", o))
                // Restricting events to those appearing in the time window
                .always(pm.timeWindowStartProperty(), startDate -> where("e.endDate >= ?", startDate))
                .always(pm.timeWindowEndProperty(), endDate -> where("e.startDate <= ?", endDate))
                // Ordering events in a way that the widest in the time window will be first (=> the smallest events will appear at the bottom)
                .always(pm.timeWindowStartProperty(), startDate -> DqlStatement.orderBy("greatest(e.startDate, ?),id", startDate))
                // Storing the result directly in the events layer
                .storeEntitiesInto(eventsLayer.getChildren())
                // We are now ready to start
                .start();
    }

}
