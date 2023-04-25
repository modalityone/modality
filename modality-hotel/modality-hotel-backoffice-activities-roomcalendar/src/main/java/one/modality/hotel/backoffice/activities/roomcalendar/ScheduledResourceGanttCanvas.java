package one.modality.hotel.backoffice.activities.roomcalendar;

import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.ThemeRegistry;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.timelayout.ChildPosition;
import dev.webfx.extras.timelayout.canvas.CanvasPane;
import dev.webfx.extras.timelayout.canvas.InteractiveCanvasManager;
import dev.webfx.extras.timelayout.canvas.TimeCanvasDrawer;
import dev.webfx.extras.timelayout.canvas.TimeCanvasUtil;
import dev.webfx.extras.timelayout.gantt.GanttLayout;
import dev.webfx.extras.timelayout.util.DirtyMarker;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import javafx.collections.ListChangeListener;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import one.modality.base.client.gantt.fx.timewindow.FXGanttTimeWindow;
import one.modality.base.shared.entities.ScheduledResource;
import one.modality.crm.backoffice.organization.fx.FXOrganization;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static dev.webfx.stack.orm.dql.DqlStatement.where;

/**
 * @author Bruno Salmon
 */
public final class ScheduledResourceGanttCanvas {

    private static final double BLOCK_HEIGHT = 40;
    private static final double RADIUS = 0;
    private static final double H_SPACING = 2; // Max value, may be reduced when zooming out
    private static final double V_SPACING = 2;
    private final static Color AVAILABLE_COLOR = Color.rgb(65, 186, 77);
    private final static Color UNAVAILABLE_COLOR = Color.rgb(255, 3, 5);


    private final RoomCalendarPresentationModel pm;

    private final Canvas canvas = new Canvas();
    private final GanttLayout<ScheduledResource, LocalDate> scheduledResourceLayout = new GanttLayout<>();
    private final TimeCanvasDrawer<ScheduledResource, LocalDate> canvasDrawer = new TimeCanvasDrawer<>(canvas, scheduledResourceLayout, this::drawScheduledResource);
    private final CanvasPane canvasPane = new CanvasPane(canvas, this::layoutAndRedraw, this::redraw);
    private Font font;

    public ScheduledResourceGanttCanvas(RoomCalendarPresentationModel pm) {
        this.pm = pm;

        // Setting up the events layer
        scheduledResourceLayout.setChildFixedHeight(BLOCK_HEIGHT);
        scheduledResourceLayout.setVSpacing(V_SPACING);
        scheduledResourceLayout.setInclusiveChildStartTimeReader(ScheduledResource::getDate);
        scheduledResourceLayout.setInclusiveChildEndTimeReader(ScheduledResource::getDate);

        // Adding it to the layered gantt canvas, and passing the drawEvent method
        //layeredGanttCanvas.addLayer(eventsLayer, this::drawEvent);
        scheduledResourceLayout.getChildren().addListener((ListChangeListener<ScheduledResource>) c -> markLayoutAsDirty());
        scheduledResourceLayout.selectedChildProperty().addListener(observable -> redraw());

        // Binding the presentation model with the canvas time window (first applying pm => timeWindow, then binding timeWindow => pm)
        scheduledResourceLayout.setOnTimeWindowChanged((start, end) -> markLayoutAsDirty());
        scheduledResourceLayout.bindTimeWindow(pm.timeWindowStartProperty(), pm.timeWindowEndProperty(), true, false);

        // Activating user interaction (user can move & zoom in/out the time window)
        //layeredGanttCanvas.setInteractive(true);
        new InteractiveCanvasManager<>(canvas, scheduledResourceLayout, ChronoUnit.DAYS).setInteractive(true);

        // Updating the events font on any theme mode change (light/dark mode, etc...)
        ThemeRegistry.runNowAndOnModeChange(() ->
                font = TextTheme.getFont(FontDef.font(FontWeight.BOLD, 13))
        );
    }

    public Pane getCanvasPane() {
        return canvasPane;
    }

    private final DirtyMarker layoutDirtyMarker = new DirtyMarker(this::layoutAndRedraw);
    private void markLayoutAsDirty() {
        layoutDirtyMarker.markAsDirty();
    }

    private void layoutAndRedraw() {
        scheduledResourceLayout.markLayoutAsDirty();
        scheduledResourceLayout.layout(canvas.getWidth(), canvas.getHeight());
        canvasPane.setCanvasHeight(scheduledResourceLayout.getRowsCount() * scheduledResourceLayout.getChildFixedHeight(), false);
        redraw();
    }

    private void redraw() {
        canvasDrawer.redraw();
    }

    private void drawScheduledResource(ScheduledResource scheduledResource, ChildPosition<LocalDate> p, GraphicsContext gc) {
        double hPadding = Math.min(p.getWidth() * 0.01, H_SPACING);
        Integer booked = scheduledResource.getIntegerFieldValue("booked");
        Integer max = scheduledResource.getIntegerFieldValue("max");
        int available = max - booked;
        TimeCanvasUtil.fillRect(p, hPadding, available > 0 ? AVAILABLE_COLOR : UNAVAILABLE_COLOR, RADIUS, gc);
        if (p.getWidth() > 10) {
            String name = (String) scheduledResource.evaluate("configuration.name");
            String availableText = String.valueOf(available);
            double h = p.getHeight(), h2 = h / 2, vPadding = h / 16;
            Paint textFill = Color.WHITE;
            gc.setFont(font);
            TimeCanvasUtil.fillText(p.getX(), p.getY() + vPadding, p.getWidth(), h2, hPadding, name, textFill, VPos.CENTER, TextAlignment.CENTER, gc);
            TimeCanvasUtil.fillText(p.getX(), p.getY() + h2, p.getWidth(), h2 - vPadding, hPadding, availableText, textFill, VPos.CENTER, TextAlignment.CENTER, gc);
        }
    }

    public void setupFXBindingsAndStartLogic(Object mixin) {
        setupFXBindings();
        startLogic(mixin);
    }

    private void setupFXBindings() {
        bindFXOrganization();
        scheduledResourceLayout.bindTimeWindow(FXGanttTimeWindow.ganttTimeWindowStartProperty(), FXGanttTimeWindow.ganttTimeWindowEndProperty(), false, true);
    }


    private void bindFXOrganization() {
        pm.organizationIdProperty().bind(FXOrganization.organizationProperty());
    }

    private void startLogic(Object mixin) {
        ReactiveEntitiesMapper.<ScheduledResource>createPushReactiveChain(mixin)
                .always("{class: 'ScheduledResource', alias: 'sr', fields: 'date,max,configuration.name,(select count(1) from Attendance where scheduledResource=sr) as booked', orderBy: 'date'}")
                // Returning events for the selected organization only (or returning an empty set if no organization is selected)
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), o -> where("configuration.resource.site.organization=?", o))
                // Restricting events to those appearing in the time window
                .always(pm.timeWindowStartProperty(), startDate -> where("sr.date >= ?", startDate))
                .always(pm.timeWindowEndProperty(), endDate -> where("sr.date <= ?", endDate))
                // Storing the result directly in the events layer
                .storeEntitiesInto(scheduledResourceLayout.getChildren())
                // We are now ready to start
                .start();
    }

}
