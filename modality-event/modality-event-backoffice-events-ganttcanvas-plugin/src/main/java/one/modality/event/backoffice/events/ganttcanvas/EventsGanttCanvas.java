package one.modality.event.backoffice.events.ganttcanvas;

import dev.webfx.extras.canvas.bar.BarDrawer;
import dev.webfx.extras.geometry.Bounds;
import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.ThemeRegistry;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.time.layout.gantt.LocalDateGanttLayout;
import dev.webfx.extras.time.layout.impl.ChildBounds;
import dev.webfx.extras.time.window.TimeWindowUtil;
import dev.webfx.stack.cache.client.LocalStorageCache;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.text.FontWeight;
import one.modality.base.backoffice.ganttcanvas.DatedGanttCanvas;
import one.modality.base.client.gantt.fx.timewindow.FXGanttTimeWindow;
import one.modality.base.client.gantt.fx.visibility.FXGanttVisibility;
import one.modality.base.client.gantt.fx.visibility.GanttVisibility;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.crm.backoffice.organization.fx.FXOrganization;
import one.modality.event.backoffice.event.fx.FXEvent;
import one.modality.event.backoffice.events.pm.EventsPresentationModel;
import one.modality.event.client.theme.EventTheme;

import java.time.LocalDate;

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
    private final DatedGanttCanvas datedGanttCanvas = new DatedGanttCanvas()
            // Activating user interaction (user can move & zoom in/out the time window) and date selection
            .setInteractive(true)
            .setDateSelectionEnabled(true);

    // The additional layer that will display the events
    private final LocalDateGanttLayout<Event> eventsLayer = new LocalDateGanttLayout<Event>()
            .setChildFixedHeight(BAR_HEIGHT)
            .setVSpacing(BAR_V_SPACING)
            .setInclusiveChildStartTimeReader(Event::getStartDate)
            .setInclusiveChildEndTimeReader(Event::getEndDate)
            .setTetrisPacking(true)
            .setSelectionEnabled(true)
            ;
    private final BarDrawer eventBarDrawer = new BarDrawer()
            // Setting up eventBarDrawer global properties (properties specific to events are set in drawEvent())
            .setRadius(BAR_RADIUS)
            .setTextAlignment(null); // auto

    // And a final layer that will display the dates (actually ScheduledItem) inside recurring events
    private final LocalDateGanttLayout<ScheduledItem> recurringEventDatesLayer = new LocalDateGanttLayout<ScheduledItem>()
            .setChildFixedHeight(BAR_HEIGHT)
            .setVSpacing(BAR_V_SPACING)
            .setInclusiveChildStartTimeReader(ScheduledItem::getDate)
            .setInclusiveChildEndTimeReader(ScheduledItem::getDate)
            .setChildYPositionGetter(this::getRecurringEventDateY) // we force the dates Y position to be the same as the recurring event
            .setSelectionEnabled(true);
    private final BarDrawer recurringEventDateBarDrawer = new BarDrawer()
            // Setting up recurringEventDateBarDrawer global properties (properties specific to events are set in drawRecurringEventDate())
            .setRadius(BAR_RADIUS);

    public EventsGanttCanvas() {
        // datedGanttCanvas is registered as the referent in FXGanttTimeWindow for pairing other gantt canvas, so their
        // horizontal time axis stay aligned with this datedGanttCanvas time axis even if their horizontal origin differs
        // (like in the accommodation canvas when we show the legend on the left). To allow this pairing, we need to
        // register its time projector and its canvas as the referent ones.
        FXGanttTimeWindow.setTimeProjector(datedGanttCanvas.getTimeProjector());
        FXGanttTimeWindow.setGanttNode(datedGanttCanvas.getCanvas());

        // Binding the presentation model time window with the UI, here the dated Gantt canvas - probably bound itself to global FXGanttTimeWindow by application code through setupFXBindings()
        pm.organizationIdProperty().bind(FXOrganization.organizationProperty());
        pm.bindTimeWindow(datedGanttCanvas);

        // Adding the events layer to the dated gantt canvas. Each event will be drawn via a call to drawEvent()
        datedGanttCanvas.addLayer(eventsLayer, this::drawEvent);
        // Adding the recurring dates layer on top of that. Each date will be drawn via a call to drawRecurringEventDate()
        datedGanttCanvas.addLayer(recurringEventDatesLayer, this::drawRecurringEventDate);

        // We may have an async issue with the Y positioning of the recurring dates, because getRecurringEventDateY()
        // works only if the recurring event has been loaded and positioned in the gantt canvas before (otherwise we
        // can't tell yet the date Y position, and we hide it by setting it a negative Y). So we intercept the arrival
        // of events from the server, and invalidate at this point the vertical layout of all recurring dates.
        eventsLayer.getChildren().addListener(new InvalidationListener() { // means we receive events from the server
            @Override
            public void invalidated(Observable observable) {
                recurringEventDatesLayer.invalidateVerticalLayout(); // will force a new computation of recurring dates Y
            }
        });

        // We ensure that the selected event is always visible in the gantt canvas. This eventually requires shifting
        // the time window to make this happen.
        selectedEventProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                Event event = selectedEventProperty().get();
                if (event != null) {
                    LocalDate startDate = event.getStartDate();
                    LocalDate endDate = event.getEndDate();
                    if (startDate != null && endDate != null) {
                        TimeWindowUtil.ensureTimeRangeVisible(FXGanttTimeWindow.ganttTimeWindow(), startDate, endDate, datedGanttCanvas.getTimeProjector().getTemporalUnit());
                    }
                }
            }
        });

        // The following properties depend on the theme mode (light/dark mode, etc...):
        ThemeRegistry.runNowAndOnModeChange(() -> eventBarDrawer
                .setTextFont(TextTheme.getFont(FontDef.font(FontWeight.BOLD, 10)))
                .setTextFill(EventTheme.getEventTextColor()));
    }

    private void drawEvent(Event event, Bounds b, GraphicsContext gc) {
        drawEvent(event, b, gc, false);
    }

    private void drawEvent(Event event, Bounds b, GraphicsContext gc, boolean transparentBackground) {
        boolean selected = Entities.sameId(event, eventsLayer.getSelectedChild());
        eventBarDrawer
                .sethPadding(Math.min(b.getWidth() * 0.01, BAR_H_SPACING))
                .setBackgroundFill(transparentBackground ? null : EventTheme.getEventBackgroundColor(event, selected))
                .setMiddleText(event.getPrimaryKey() + " " + event.getName())
                .drawBar(b, gc);
    }

    private void drawRecurringEventDate(ScheduledItem scheduledItem, Bounds b, GraphicsContext gc) {
        // We first draw the date bar. As it's on top of the event, this date bar may erase the event name.
        boolean selected = Entities.sameId(scheduledItem, recurringEventDatesLayer.getSelectedChild());
        recurringEventDateBarDrawer
                .sethPadding(Math.min(b.getWidth() * 0.01, BAR_H_SPACING))
                .setBackgroundFill(EventTheme.getRecurringEventDateBackgroundColor(scheduledItem, selected))
                .drawBar(b, gc);

        // To correct the possible event name erasure, we draw the event again on top of the date bar, but with a
        // transparent background (to not erase the date bar just drew!).
        Event event = scheduledItem.getEvent();
        int eventIndex = eventsLayer.getChildren().indexOf(event);
        if (eventIndex != -1)
            drawEvent(event, eventsLayer.getChildBounds(eventIndex), gc, true);
    }

    private double getRecurringEventDateY(ScheduledItem scheduledItem) {
        // We want to set the recurring date the same vertical position as the event, so we first get that event
        Event recurringEvent = scheduledItem.getEvent();
        // And we check its index in the events layer
        int recurringEventIndex = eventsLayer.getChildren().indexOf(recurringEvent);
        // If it's not present, it's because we received the date before the event from the server, so we can't position it yet
        if (recurringEventIndex == -1)
            return -100; // so we hide it for now, until the event arrives
        // If it's present, we get its layout bounds computed by the events layer
        ChildBounds<Event, LocalDate> eb = eventsLayer.getChildBounds(recurringEventIndex);
        // And we return the same Y position as the event
        return eb.getMinY();
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
        // Bidirectional binding between selectEventProperty and FXEvent.eventProperty() <- taking this inital value
        selectedEventProperty().bindBidirectional(FXEvent.eventProperty());
    }

    private void startLogic(Object mixin) {
        // Loading events to be displayed in the gantt canvas
        ReactiveEntitiesMapper<Event> eventReactiveEntitiesMapper = ReactiveEntitiesMapper.<Event>createPushReactiveChain(mixin)
                .always("{class: 'Event', alias: 'e', fields: 'name,startDate,endDate,type.recurringItem', where: 'active'}")
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
                .setResultCacheEntry(LocalStorageCache.get().getCacheEntry("cache-eventsGanttCanvas"))
                // We are now ready to start
                .start();

        // Also loading the dates inside the recurring events to be displayed in the gantt canvas
        ReactiveEntitiesMapper.<ScheduledItem>createPushReactiveChain(mixin)
                .always("{class: 'ScheduledItem', alias: 'si', fields: 'date,event'}")
                // Stopping querying the server when then gantt visibility is not set to EVENTS
                .ifNotEquals(FXGanttVisibility.ganttVisibilityProperty(), GanttVisibility.EVENTS, null)
                // Returning events for the selected organization only (or returning an empty set if no organization is selected)
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), o -> where("si.event.(organization=? and type.recurringItem != null)", o))
                // Restricting events to those appearing in the time window
                .always(pm.timeWindowStartProperty(), startDate -> where("si.date >= ?", startDate))
                .always(pm.timeWindowEndProperty(), endDate -> where("si.date <= ?", endDate))
                // Storing the result directly in the events layer
                .storeEntitiesInto(recurringEventDatesLayer.getChildren())
                //.setResultCacheEntry(LocalStorageCache.get().getCacheEntry("cache-eventsGanttCanvas"))
                .setStore(eventReactiveEntitiesMapper.getStore())
                // We are now ready to start
                .start();
    }

}
