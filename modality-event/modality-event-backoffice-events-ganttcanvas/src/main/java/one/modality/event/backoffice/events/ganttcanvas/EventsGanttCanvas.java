package one.modality.event.backoffice.events.ganttcanvas;

import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.ThemeRegistry;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.timelayout.bar.BarDrawer;
import dev.webfx.extras.bounds.Bounds;
import dev.webfx.extras.timelayout.gantt.LocalDateGanttLayout;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import javafx.beans.property.ObjectProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.text.FontWeight;
import one.modality.base.client.gantt.fx.visibility.FXGanttVisibility;
import one.modality.base.client.gantt.fx.visibility.GanttVisibility;
import one.modality.base.client.ganttcanvas.DatedGanttCanvas;
import one.modality.base.shared.entities.Event;
import one.modality.crm.backoffice.organization.fx.FXOrganization;
import one.modality.event.backoffice.event.fx.FXEvent;
import one.modality.event.backoffice.events.pm.EventsPresentationModel;
import one.modality.event.client.theme.EventTheme;

import static dev.webfx.stack.orm.dql.DqlStatement.where;

/**
 * @author Bruno Salmon
 */
public final class EventsGanttCanvas {

    // Style constants used for drawing bars in the canvas:
    private static final double BAR_HEIGHT = 18;
    private static final double BAR_RADIUS = 10;
    private static final double BAR_H_SPACING = 2; // Max value, may be reduced when zooming out
    private static final double BAR_V_SPACING = 2;

    private final EventsPresentationModel pm = new EventsPresentationModel();
    // The dated Gantt canvas that already displays dates, weeks, months & years (depending on zoom level)
    private final DatedGanttCanvas datedGanttCanvas = new DatedGanttCanvas();
    // The additional layer that will display the events
    private final LocalDateGanttLayout<Event> eventsLayer = new LocalDateGanttLayout<>();
    private final BarDrawer eventBarDrawer = new BarDrawer();

    public EventsGanttCanvas() {
        // Binding the presentation model time window with the UI, here the dated Gantt canvas - probably bound itself to global FXGanttTimeWindow by application code through setupFXBindings()
        pm.organizationIdProperty().bind(FXOrganization.organizationProperty());
        pm.bindTimeWindow(datedGanttCanvas);

        // Setting up the events layer
        eventsLayer.setChildFixedHeight(BAR_HEIGHT);
        eventsLayer.setVSpacing(BAR_V_SPACING);
        eventsLayer.setInclusiveChildStartTimeReader(Event::getStartDate);
        eventsLayer.setInclusiveChildEndTimeReader(Event::getEndDate);
        eventsLayer.setTetrisPacking(true);
        eventsLayer.setChildSelectionEnabled(true);

        // Passing it to the gantt canvas as an additional layer, that will be automatically drawn using the drawEvent method
        datedGanttCanvas.addLayer(eventsLayer, this::drawEvent);

        // Activating user interaction (user can move & zoom in/out the time window) and date selection
        datedGanttCanvas.setInteractive(true);
        datedGanttCanvas.setDateSelectionEnabled(true);

        // Setting up eventBarDrawer global properties (properties specific to events are set in drawEvent())
        eventBarDrawer.setRadius(BAR_RADIUS);
        // The following properties depend on the theme mode (light/dark mode, etc...):
        ThemeRegistry.runNowAndOnModeChange(() -> {
            eventBarDrawer.setTextFont(TextTheme.getFont(FontDef.font(FontWeight.BOLD, 13)));
            eventBarDrawer.setTextFill(EventTheme.getEventTextColor());
        });
    }

    private void drawEvent(Event event, Bounds b, GraphicsContext gc) {
        boolean selected = Entities.sameId(event, eventsLayer.getSelectedChild());
        eventBarDrawer.sethPadding(Math.min(b.getWidth() * 0.01, BAR_H_SPACING));
        eventBarDrawer.setBackgroundFill(EventTheme.getEventBackgroundColor(event, selected));
        eventBarDrawer.setMiddleText(event.getPrimaryKey() + " " + event.getName());
        eventBarDrawer.drawBar(b, gc);
    }

    public Pane getCanvasContainer() {
        return datedGanttCanvas.getCanvasContainer();
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
        datedGanttCanvas.setupFXBindings();
    }

    private void bindFXEventToSelection() {
        selectedEventProperty().set(FXEvent.getEvent());
        FXEvent.eventProperty().bindBidirectional(selectedEventProperty());
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
