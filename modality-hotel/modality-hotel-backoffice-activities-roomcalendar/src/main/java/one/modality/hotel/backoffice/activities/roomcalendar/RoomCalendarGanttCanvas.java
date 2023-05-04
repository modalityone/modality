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
import dev.webfx.extras.timelayout.canvas.TimeCanvasUtil;
import dev.webfx.extras.timelayout.canvas.generic.VirtualCanvasPane;
import dev.webfx.extras.timelayout.gantt.canvas.GanttCanvasUtil;
import dev.webfx.extras.timelayout.gantt.LocalDateGanttLayout;
import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.extras.util.layout.StationarySplitPane;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import one.modality.base.client.gantt.fx.timewindow.FXGanttTimeWindow;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.base.shared.entities.ScheduledResource;
import one.modality.crm.backoffice.organization.fx.FXOrganization;

import static dev.webfx.stack.orm.dql.DqlStatement.orderBy;
import static dev.webfx.stack.orm.dql.DqlStatement.where;

/**
 * @author Bruno Salmon
 */
public final class RoomCalendarGanttCanvas {

    // Style constants used for drawing bars in the canvas:
    private static final double BAR_HEIGHT = 40;
    private final static Color BAR_AVAILABLE_ONLINE_COLOR = Color.rgb(65, 186, 77);
    private final static Color BAR_AVAILABLE_OFFLINE_COLOR = Color.ORANGE;
    private final static Color BAR_SOLDOUT_COLOR = Color.rgb(255, 3, 5);
    private final static Color BAR_UNAVAILABLE_COLOR = Color.rgb(130, 135, 136);

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

    // The user has the option to enable/disable the blocks grouping (when disabled, TimeBarUtil will not group the
    // blocks, but simply map each block to a 1-day-long bar, so the user will see all these blocks)
    final BooleanProperty blocksGroupingProperty = new SimpleBooleanProperty();

    // We will use the BarDrawer utility class to draw the bars and the rooms
    private final BarDrawer barDrawer = new BarDrawer();  // unique reusable instance to draw all the bars
    private final BarDrawer roomDrawer = new BarDrawer(); // unique reusable instance to draw all the rooms

    public RoomCalendarGanttCanvas() {
        // Binding the presentation model and the barsLayout time window
        pm.organizationIdProperty().bind(FXOrganization.organizationProperty());
        pm.bindTimeWindow(barsLayout); // barsLayout will itself be bound to FXGanttTimeWindow (see below)
        barsLayout.bindTimeWindowBidirectional(FXGanttTimeWindow.ganttTimeWindow());

        // Asking TimeBarUtil to automatically transform entities into bars that will feed the input of barsLayout
        TimeBarUtil.setupBarsLayout(
                entities, // the observable list of ScheduledResource entities to take as input
                ScheduledResource::getDate, // the entity date reader that will be used to date each block
                ScheduledResourceBlock::new, // the factory that creates blocks, initially 1 instance per entity, but then grouped into bars
                barsLayout, // the layout that will receive the final list of bars as a result of the blocks grouping
                blocksGroupingProperty); // optional property to eventually disable the blocks grouping (=> 1 bar per block if disabled)

        // Finishing setting up barsLayout
        barsLayout.setChildFixedHeight(BAR_HEIGHT);
        barsLayout.setChildParentReader(bar -> bar.getInstance().getResourceConfiguration());

        // Activating user interaction on canvas (user can move & zoom in/out the time window)
        LocalDateCanvasInteractionManager.makeCanvasInteractive(barsDrawer, barsLayout);

        // Setting the properties of barDrawer & roomDrawer (other properties are set in drawBar() & drawRoom())
        barDrawer.setStroke(Color.BLACK);
        barDrawer.setTextFill(Color.WHITE);
        roomDrawer.setTextFill(Color.grayRgb(130));
        roomDrawer.setStroke(Color.grayRgb(130));
        roomDrawer.setBackgroundFill(Color.ALICEBLUE);
        // Updating the text font on any theme mode change that may impact it (light/dark mode, etc...)
        ThemeRegistry.runNowAndOnModeChange(() -> {
            Font font = TextTheme.getFont(FontDef.font(13));
            barDrawer.setTextFont(font);
            roomDrawer.setTextFont(font);
        });
    }

    public Node buildCanvasContainer() {
        // We embed everything in a scrollPane because the rooms probably won't all fit on the screen. This scrollPane
        // will be responsible for the vertical scrolling only, because the horizontal scrolling is actually already
        // managed by the interactive canvas itself (which reacts to user dragging to move the gantt dates).
        ScrollPane scrollPane = new ScrollPane();
        // That scrollPane will contain a splitPane showing the list of rooms on its left side, and the blocks/bars on
        // the right side. To show the list of rooms, we just use a ParentCanvasPane which displays the parents of the
        // barsLayout (the parents are ResourceConfiguration instances as set in barsLayout.setChildParentReader() above)
        VirtualCanvasPane leftRoomsPane = GanttCanvasUtil.createParentVirtualCanvasPane(new Canvas(), barsLayout, this::drawRoom,
                150, scrollPane.viewportBoundsProperty(), scrollPane.vvalueProperty());
        // We embed the canvas in a VirtualCanvasPane which has 2 functions:
        // 1) As a CanvasPane it is responsible for automatically resizing the canvas when the user resizes the UI, and
        // for calling the canvas refresher (the piece of code that redraws the canvas). TimeCanvasUtil will actually
        // create that refresher using the passed barsLayout & barsDrawer.
        // 2) in addition, VirtualCanvasPane keeps the canvas size as small as possible when used in a scrollPane to
        // prevent memory overflow. Whereas the virtual canvas represents the whole canvas that the user seems to watch
        // and can have a very long height, the real canvas will be only the size of the scrollPane viewport, and when
        // the user scrolls, VirtualCanvasPane is responsible for redrawing the canvas to the scrolled position.
        VirtualCanvasPane rightBarsPane = TimeCanvasUtil.createTimeVirtualCanvasPane(barsLayout, barsDrawer,
                scrollPane.viewportBoundsProperty(), scrollPane.vvalueProperty());
        // We use the StationarySplitPane utility class to create the splitPane, because a standard split pane would
        // move the right node when moving the slider, whereas we want the right node to be stationary, so it stays
        // aligned with the dates of the gantt canvas on top. StationarySplitPane will actually create a StackPane and
        // put the right bars behind the splitPane, and will set a transparent node on the right side of the split pane.
        // The right side will therefore reveal the right bars that stay stationary behind the splitPane.
        StackPane sliderContainer = StationarySplitPane.createRightStationarySplitPaneAndReturnStackPaneContainer(
                leftRoomsPane, rightBarsPane);
        // We finally set up the scrollPane for vertical scrolling only (no horizontal scrollbar, etc...), and return it
        LayoutUtil.setupVerticalScrollPane(scrollPane, sliderContainer);
        return scrollPane;
    }

    private void drawBar(LocalDateBar<ScheduledResourceBlock> bar, ChildPosition<?> p, GraphicsContext gc) {
        // The bar wraps a block over 1 or several days (or always 1 day if the user hasn't ticked the grouping block
        // checkbox). So the bar instance is that block that was repeated over that period.
        ScheduledResourceBlock block = bar.getInstance();
        // The main info we display in the bar is a number which represents how many free beds are remaining for booking
        String remaining = String.valueOf(block.getRemaining());
        // The background will be gray if unavailable, red if sold-out, green if online, orange if offline
        barDrawer.setBackgroundFill(!block.isAvailable() ? BAR_UNAVAILABLE_COLOR : block.getRemaining() <= 0 ? BAR_SOLDOUT_COLOR : block.isOnline() ? BAR_AVAILABLE_ONLINE_COLOR : BAR_AVAILABLE_OFFLINE_COLOR);
        // If the bar is wide enough we show "Beds" on top and the number on bottom, but if it is too narrow, we just
        // display the number in the middle. Unavailable gray bars have no text at all by the way.
        boolean isWideBar = p.getWidth() > 40;
        barDrawer.setTopText(   isWideBar && block.isAvailable() ?   "Beds"   :   null    );
        barDrawer.setMiddleText(isWideBar || !block.isAvailable() ?   null    : remaining );
        barDrawer.setBottomText(isWideBar && block.isAvailable() ?  remaining :   null    );
        barDrawer.drawBar(p, gc);
    }

    private void drawRoom(ResourceConfiguration rc, ChildPosition<?> p, GraphicsContext gc) {
        // The only remaining property that needs to be set here is the room name that we display in the bar middle
        roomDrawer.setMiddleText(rc.getName());
        roomDrawer.drawBar(p, gc); // This also draws a rectangle stroke - see properties set in constructor
        // But the wireframe doesn't show a stroke on the left, so we erase it to match the UX design
        gc.fillRect(p.getX(), p.getY(), 2, p.getHeight()); // erasing the left side of the stroke rectangle
    }

    public void startLogic(Object mixin) {
        ReactiveEntitiesMapper.<ScheduledResource>createPushReactiveChain(mixin)
                .always("{class: 'ScheduledResource', alias: 'sr', fields: 'date,available,online,max,configuration.name,(select count(1) from Attendance where scheduledResource=sr) as booked'}")
                .always(orderBy("configuration.name,configuration,date")) // Order is important for TimeBarUtil (see comment on barsLayout)
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