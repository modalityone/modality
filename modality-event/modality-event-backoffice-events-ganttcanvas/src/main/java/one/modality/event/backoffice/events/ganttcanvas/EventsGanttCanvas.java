package one.modality.event.backoffice.events.ganttcanvas;

import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.timelayout.ChildPosition;
import dev.webfx.extras.timelayout.ChildTimeReader;
import dev.webfx.extras.timelayout.canvas.TimeCanvasUtil;
import dev.webfx.extras.timelayout.gantt.GanttLayout;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import javafx.collections.ListChangeListener;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import one.modality.base.client.ganttcanvas.LayeredGanttCanvas;
import one.modality.base.shared.entities.Event;
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

        eventsLayout.setTopY(42);
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
        eventsLayout.getChildren().addListener((ListChangeListener<Event>) c -> layeredGanttCanvas.redrawCanvas());

        layeredGanttCanvas.addLayer(eventsLayout, this::drawEvent);

        FXProperties.runNowAndOnPropertiesChange(() -> {
            layeredGanttCanvas.setTimeWindow(pm.timeWindowStartProperty().getValue(), pm.timeWindowEndProperty().getValue());
        }, pm.timeWindowStartProperty(), pm.timeWindowEndProperty());
    }

    public Pane createCanvasPane() {
        return layeredGanttCanvas.createCanvasPane();
    }

    private void drawEvent(Event event, ChildPosition<LocalDate> p, GraphicsContext gc) {
        TimeCanvasUtil.fillStrokeRect(p, EventTheme.getEventBackgroundColor(), EventTheme.getEventBorderColor(), gc);
        gc.setFont(TextTheme.getFont(EVENT_FONT_DEF));
        TimeCanvasUtil.fillCenterLeftText(p, event.getPrimaryKey() + " " + event.getName(), EventTheme.getEventTextColor(), gc);
    }

    public void startLogic(Object mixin) {
        ReactiveEntitiesMapper.<Event>createPushReactiveChain(mixin)
                .always("{class: 'Event', alias: 'e', fields: 'name,startDate,endDate', where: 'active', orderBy: 'startDate,id'}")
                // Search box condition
                //.ifTrimNotEmpty(pm.searchTextProperty(), s -> where("lower(name) like ?", "%" + s.toLowerCase() + "%"))
                .ifNotNull(pm.organizationIdProperty(), o -> where("organization=?", o))
                .always(pm.timeWindowStartProperty(), startDate -> where("e.startDate >= ?", startDate))
                .always(pm.timeWindowEndProperty(), endDate -> where("e.endDate <= ?", endDate))
                .storeEntitiesInto(eventsLayout.getChildren())
                .start();
    }

}
