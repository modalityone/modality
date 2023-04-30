package one.modality.hotel.backoffice.activities.roomcalendar;

import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.ThemeRegistry;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.timelayout.ChildPosition;
import dev.webfx.extras.timelayout.bar.BarDrawer;
import dev.webfx.extras.timelayout.bar.LocalDateBar;
import dev.webfx.extras.timelayout.bar.TimeBarUtil;
import dev.webfx.extras.timelayout.canvas.LocalDateCanvasDrawer;
import dev.webfx.extras.timelayout.canvas.LocalDateCanvasInteractionManager;
import dev.webfx.extras.timelayout.canvas.TimeCanvasPane;
import dev.webfx.extras.timelayout.gantt.LocalDateGanttLayout;
import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import one.modality.base.client.gantt.fx.timewindow.FXGanttTimeWindow;
import one.modality.base.shared.entities.ScheduledResource;
import one.modality.crm.backoffice.organization.fx.FXOrganization;

import java.time.LocalDate;

import static dev.webfx.stack.orm.dql.DqlStatement.orderBy;
import static dev.webfx.stack.orm.dql.DqlStatement.where;

/**
 * @author Bruno Salmon
 */
public final class RoomCalendarGanttCanvas {

    // Style constants used for drawing bars in the canvas:
    private static final double BAR_HEIGHT = 40;
    private static final double BAR_RADIUS = 0;
    private static final double BAR_H_SPACING = 0.5; // Max value, may be reduced when zooming out
    private static final double BAR_V_SPACING = 1;
    private final static Color BAR_AVAILABLE_COLOR = Color.rgb(65, 186, 77);
    private final static Color BAR_UNAVAILABLE_COLOR = Color.rgb(255, 3, 5);

    // The presentation model used by the logic code to query the server (see startLogic() method)
    private final RoomCalendarPresentationModel pm = new RoomCalendarPresentationModel();

    // The result returned by the server will be stored in that observable list of ScheduledResource entities:
    private final ObservableList<ScheduledResource> entities = FXCollections.observableArrayList();

    /**
     * We will ask TimeBarUtil to automatically convert those ScheduledResource entities first into ScheduledResourceBlock
     * instances which are a reduction of those entities to the strictly minimal set of fields required for the canvas
     * draw, so here fields = resourceConfiguration (= parent from gantt point of view) & available (the number displayed)
     * and nothing more (we forget the entityId and other fields). And because at this stage these instances will form
     * blocks (one instance per day), TimeBarUtil will then identify and group all series of identical blocks, and
     * finally transform them into bars (in this terminology, a bar can cover several days as opposed to a block).
     *
     * For example if room 208 has 2 beds available for 5 days, this series of 5 blocks (ie 5 ScheduledResourceBlock
     * instances with identical fields: resourceConfiguration = of 208 & available = 2) will be grouped and transformed
     * into a 5-days bar (holding 1 single instance of ScheduledResourceBlock + first & last day of that series).
     *
     * Note that there are 2 conditions for this to work:
     * 1) ScheduledResourceBlock must implement equals(), which is used by TimeBarUtil to identify identical blocks
     * 2) The entities must be sorted so that identical blocks will appear in a consecutive order in that list
     *    => see order by configuration,date in startLogic()
     */

    // As a result, TimeBarUtil generates a list of bars that will be the input of this barsLayout:
    private final LocalDateGanttLayout<LocalDateBar<ScheduledResourceBlock>> barsLayout = new LocalDateGanttLayout<>();

    // Once the position of the bars are computed by barsLayout, they will be automatically drawn in a canvas by this
    // barsDrawer (each bar will be rendered using the drawBar() method provided in this class)
    private final LocalDateCanvasDrawer<LocalDateBar<ScheduledResourceBlock>> barsDrawer
            = new LocalDateCanvasDrawer<>(barsLayout, this::drawBar);

    // The user has the option to enable/disable the blocks grouping (when disabled, TimeBarUtil will stop trying to
    // transform all series of blocks into bars, but will simply map each block to a 1-day-long bar instead)
    final BooleanProperty blocksGroupingProperty = new SimpleBooleanProperty();

    private final BarDrawer barDrawer = new BarDrawer();

    public RoomCalendarGanttCanvas() {
        // Binding the presentation model and the barsLayout time window
        pm.organizationIdProperty().bind(FXOrganization.organizationProperty());
        pm.bindTimeWindow(barsLayout); // barsLayout will itself be bound to FXGanttTimeWindow (see below)
        barsLayout.bindTimeWindowBidirectional(FXGanttTimeWindow.ganttTimeWindow());

        // Asking TimeBarUtil to automatically transform entities into bars that will feed the input of barsLayout
        TimeBarUtil.setupBarsLayout(
                entities, // the observable list of ScheduledResource entities to transform
                ScheduledResource::getDate, // the entity date reader that will be used to date each block
                ScheduledResourceBlock::new, // the factory that creates instances, initially one per block
                barsLayout, // the barsLayout that will receive the final list of bars as a result of the blocks grouping
                blocksGroupingProperty); // optional property to eventually disable that blocks grouping

        // Finishing setting up barsLayout
        barsLayout.setChildFixedHeight(BAR_HEIGHT);
        barsLayout.setVSpacing(BAR_V_SPACING);
        barsLayout.setChildParentReader(bar -> bar.getInstance().getResourceConfiguration());

        // Activating user interaction on canvas (user can move & zoom in/out the time window)
        LocalDateCanvasInteractionManager.makeCanvasInteractive(barsDrawer, barsLayout);

        barDrawer.setRadius(BAR_RADIUS);
        barDrawer.setTextFill(Color.WHITE);
        // Updating the blocks font on any theme mode change (light/dark mode, etc...)
        ThemeRegistry.runNowAndOnModeChange(() ->
                barDrawer.setTextFont(TextTheme.getFont(FontDef.font(FontWeight.BOLD, 13)))
        );
    }

    public Node buildCanvasContainer() {
        // We embed the canvas in a TimeCanvasPane that takes care of resizing the canvas when the user resizes the UI
        TimeCanvasPane timeCanvasPane = new TimeCanvasPane(barsLayout, barsDrawer);
        // We embed it in a ScrollPane to allow scrolling because the height of this canvas probably won't fit in the screen
        ScrollPane sp = LayoutUtil.createVerticalScrollPane(timeCanvasPane);
        // And we activate the virtual canvas mode to prevent any memory problems (the canvas will just keep the size of
        // the ScrollPane viewport and simulate the scroll by automatically redrawing the canvas to the matching location).
        timeCanvasPane.activateVirtualCanvasMode(sp.viewportBoundsProperty(), sp.vvalueProperty());
        // We return the ScrollPane as the canvas container
        return sp;
    }

    private void drawBar(LocalDateBar<ScheduledResourceBlock> bar, ChildPosition<LocalDate> p, GraphicsContext gc) {
        ScheduledResourceBlock block = bar.getInstance();
        barDrawer.sethPadding(Math.min(p.getWidth() * 0.01, BAR_H_SPACING));
        barDrawer.setBackgroundFill(block.getAvailable() > 0 ? BAR_AVAILABLE_COLOR : BAR_UNAVAILABLE_COLOR);
        barDrawer.setTopText(block.getResourceName());
        barDrawer.setBottomText(String.valueOf(block.getAvailable()));
        barDrawer.drawBar(p, gc);

    }

    public void startLogic(Object mixin) {
        ReactiveEntitiesMapper.<ScheduledResource>createPushReactiveChain(mixin)
                .always("{class: 'ScheduledResource', alias: 'sr', fields: 'date,max,configuration.name,(select count(1) from Attendance where scheduledResource=sr) as booked'}")
                .always(orderBy("configuration,date")) // Order is important for TimeBarUtil (see comment on barsLayout)
                // Returning events for the selected organization only (or returning an empty set if no organization is selected)
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), o -> where("configuration.resource.site.organization=?", o))
                // Restricting events to those appearing in the time window
                .always(pm.timeWindowStartProperty(), startDate -> where("sr.date >= ?", startDate))
                .always(pm.timeWindowEndProperty(),   endDate   -> where("sr.date <= ?", endDate))
                // Storing the result directly in the events layer
                .storeEntitiesInto(entities)
                // We are now ready to start
                .start();
    }
}
