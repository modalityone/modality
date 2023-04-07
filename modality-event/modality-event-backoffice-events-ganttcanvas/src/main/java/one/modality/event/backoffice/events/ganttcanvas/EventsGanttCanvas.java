package one.modality.event.backoffice.events.ganttcanvas;

import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.timelayout.ChildPosition;
import dev.webfx.extras.timelayout.ChildTimeReader;
import dev.webfx.extras.timelayout.canvas.TimeCanvasUtil;
import dev.webfx.extras.timelayout.gantt.GanttLayout;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import one.modality.base.client.ganttcanvas.LayeredGanttCanvas;
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
public final class EventsGanttCanvas {

    private final EventsPresentationModel pm;
    private final LayeredGanttCanvas layeredGanttCanvas = new LayeredGanttCanvas();
    private final GanttLayout<Event, LocalDate> eventsLayout = new GanttLayout<>();

    private final FontDef EVENT_FONT_DEF = FontDef.font(13);

    public EventsGanttCanvas(EventsPresentationModel pm) {
        this.pm = pm;

        eventsLayout.setChildFixedHeight(20);
        eventsLayout.setVSpacing(2);
        eventsLayout.setChildTimeReader(new ChildTimeReader<>() {
            @Override
            public LocalDate getStartTime(Event event) {
                return event.getStartDate();
            }

            @Override
            public LocalDate getEndTime(Event event) {
                return event.getEndDate();
            }
        });
        eventsLayout.getChildren().addListener((ListChangeListener<Event>) c -> layeredGanttCanvas.markLayoutAsDirty());
        eventsLayout.selectedChildProperty().addListener(observable -> layeredGanttCanvas.markCanvasAsDirty());

        layeredGanttCanvas.addLayer(eventsLayout, this::drawEvent);

        // Binding the time window with the pm properties
        FXProperties.runNowAndOnPropertiesChange(() -> {
            layeredGanttCanvas.setTimeWindow(pm.timeWindowStartProperty().getValue(), pm.timeWindowEndProperty().getValue());
        }, pm.timeWindowStartProperty(), pm.timeWindowEndProperty());

        // Managing user interaction
        Canvas canvas = layeredGanttCanvas.getCanvas();
        // Changing cursor to hand cursor when hovering an event (to indicate it's clickable)
        canvas.setOnMouseMoved(e -> {
            Event event = eventsLayout.pickChild(e.getX(), e.getY());
            canvas.setCursor(event != null ? Cursor.HAND : Cursor.DEFAULT);
        });
        // Selecting the event when clicked
        canvas.setOnMouseClicked(e -> eventsLayout.selectClickedChild(e.getX(), e.getY()));
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
        TimeCanvasUtil.fillStrokeRect(p, EventTheme.getEventBackgroundColor(selected), EventTheme.getEventBorderColor(), gc);
        gc.setFont(TextTheme.getFont(EVENT_FONT_DEF));
        TimeCanvasUtil.fillCenterLeftText(p, event.getPrimaryKey() + " " + event.getName(), EventTheme.getEventTextColor(), gc);
    }

    public void startLogic(Object mixin) {
        ReactiveEntitiesMapper.<Event>createPushReactiveChain(mixin)
                .always("{class: 'Event', alias: 'e', fields: 'name,startDate,endDate', where: 'active'}")
                // Search box condition
                //.ifTrimNotEmpty(pm.searchTextProperty(), s -> where("lower(name) like ?", "%" + s.toLowerCase() + "%"))
                .ifNotNull(pm.organizationIdProperty(), o -> where("e.organization=?", o))
                .always(pm.timeWindowStartProperty(), startDate -> where("e.endDate >= ?", startDate))
                .always(pm.timeWindowEndProperty(), endDate -> where("e.startDate <= ?", endDate))
                .always(pm.timeWindowStartProperty(), startDate -> DqlStatement.orderBy("greatest(e.startDate, ?),id", startDate))
                .storeEntitiesInto(eventsLayout.getChildren())
                .start();
    }

}
