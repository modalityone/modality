package one.modality.hotel.backoffice.activities.accommodation;

import dev.webfx.extras.canvas.bar.BarDrawer;
import dev.webfx.extras.canvas.pane.VirtualCanvasPane;
import dev.webfx.extras.geometry.Bounds;
import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.ThemeRegistry;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.time.layout.bar.LocalDateBar;
import dev.webfx.extras.time.layout.bar.TimeBarUtil;
import dev.webfx.extras.time.layout.canvas.LocalDateCanvasDrawer;
import dev.webfx.extras.time.layout.canvas.TimeCanvasUtil;
import dev.webfx.extras.time.layout.gantt.LocalDateGanttLayout;
import dev.webfx.extras.time.layout.gantt.canvas.ParentsCanvasDrawer;
import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.kit.launcher.WebFxKitLauncher;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import one.modality.base.client.gantt.fx.timewindow.FXGanttTimeWindow;
import one.modality.base.shared.entities.*;
import one.modality.crm.backoffice.organization.fx.FXOrganization;

import java.util.List;
import java.util.stream.Collectors;

import static dev.webfx.stack.orm.dql.DqlStatement.orderBy;
import static dev.webfx.stack.orm.dql.DqlStatement.where;

public class AccommodationGanttCanvas {

    private static final double BAR_HEIGHT = 20;
    private static final double BAR_RADIUS = 10;

    // The presentation model used by the logic code to query the server (see startLogic() method)
    private final AccommodationPresentationModel pm = new AccommodationPresentationModel();

    // The results returned by the server will be stored in observable lists of Attendance and ScheduledResource entities:
    private final ObservableList<Attendance> entities = FXCollections.observableArrayList();

    private final ObservableList<ScheduledResource> allScheduledResources = FXCollections.observableArrayList();

    /**
     * We will ask TimeBarUtil to automatically convert those Attendance entities first into AttendanceBlock
     * instances which are a reduction of those entities to the strictly minimal set of fields required for the canvas
     * draw, so here fields = resourceConfiguration (= parent from gantt point of view) & available (the number displayed)
     * and nothing more (we forget the entityId and other fields). And because at this stage these instances will form
     * blocks (one instance per day), TimeBarUtil will then identify and group all series of identical blocks, and
     * finally transform them into bars (in this terminology, a bar can cover several days as opposed to a block).
     *
     * For example if room 208 has 2 beds available for 5 days, this series of 5 blocks (ie 5 Attendance
     * instances with identical fields: resourceConfiguration = of 208 & available = 2) will be grouped and transformed
     * into a 5-days bar (holding 1 single instance of Attendance + first & last day of that series).
     *
     * Note that there are 2 conditions for this to work:
     * 1) AttendanceBlock must implement equals(), which is used by TimeBarUtil to identify identical blocks
     * 2) The entities must be sorted so that identical blocks will appear in a consecutive order in that list
     *    => see order by configuration,date in startLogic()
     */

    // As a result, TimeBarUtil generates a list of bars that will be the input of this barsLayout:
    private final LocalDateGanttLayout<LocalDateBar<AttendanceBlock>> barsLayout = new LocalDateGanttLayout<>();

    // Once the position of the bars are computed by barsLayout, they will be automatically drawn in a canvas by this
    // barsDrawer (each bar will be rendered using the drawBar() method provided in this class)
    private final LocalDateCanvasDrawer<LocalDateBar<AttendanceBlock>> barsDrawer = new LocalDateCanvasDrawer<>(barsLayout, this::drawBar);

    // The user has the option to enable/disable the blocks grouping (when disabled, TimeBarUtil will not group the
    // blocks, but simply map each block to a 1-day-long bar, so the user will see all these blocks)
    public final BooleanProperty blocksGroupingProperty = new SimpleBooleanProperty(true);

    // We will use the BarDrawer utility class to draw the bars & rooms names & types
    private final BarDrawer barDrawer = new BarDrawer();  // unique instance to draw all the bars
    private final BarDrawer parentRoomDrawer = new BarDrawer(); // unique instance to draw all the room names
    private final BarDrawer grandparentRoomTypeDrawer = new BarDrawer(); // unique instance to draw all the room types

    private Font font;

    public AccommodationGanttCanvas(AccommodationController controller) {
        // Binding the presentation model and the barsLayout time window
        pm.organizationIdProperty().bind(FXOrganization.organizationProperty());
        pm.bindTimeWindow(barsLayout); // barsLayout will itself be bound to FXGanttTimeWindow (see below)
        barsLayout.bindTimeWindowBidirectional(FXGanttTimeWindow.ganttTimeWindow());

        // Asking TimeBarUtil to automatically transform entities into bars that will feed the input of barsLayout
        TimeBarUtil.setupBarsLayout(
                entities, // the observable list of Attendance entities to take as input
                Attendance::getDate, // the entity date reader that will be used to date each block
                AttendanceBlock::new, // the factory that creates blocks, initially 1 instance per entity, but then grouped into bars
                barsLayout, // the layout that will receive the final list of bars as a result of the blocks grouping
                blocksGroupingProperty); // optional property to eventually disable the blocks grouping (=> 1 bar per block if disabled)

        // Update key with new colours when the entities change
        entities.addListener((ListChangeListener<Attendance>) change -> controller.setEntities(entities));

        // Update summary pane when scheduled resources change
        allScheduledResources.addListener((ListChangeListener<ScheduledResource>) change -> {
            controller.setAllScheduledResource(allScheduledResources);
            List<ResourceConfiguration> parents = allScheduledResources.stream()
                    .map(ScheduledResource::getResourceConfiguration)
                    .distinct()
                    .collect(Collectors.toList());
            barsLayout.getParents().setAll(parents);
        });

        // Finishing setting up barsLayout
        setParentsProvided(true);
        barsLayout.setChildFixedHeight(BAR_HEIGHT);
        barsLayout.setChildParentReader(bar -> bar.getInstance().getResourceConfiguration());
        barsLayout.setTetrisPacking(true);
        barsLayout.setChildTetrisMinWidthReader(bar -> WebFxKitLauncher.measureText(bar.getInstance().getPersonName(), font).getWidth());

        // Enabling canvas interaction (user can move & zoom in/out the time window)
        barsDrawer.enableCanvasInteraction();

        // Setting the properties of barDrawer & roomDrawer (other properties are set in drawBar() & drawRoom())
        barDrawer.setStroke(Color.BLACK);
        barDrawer.setTextFill(Color.WHITE);
        barDrawer.setRadius(BAR_RADIUS);
        parentRoomDrawer.setTextFill(Color.grayRgb(130));
        parentRoomDrawer.setStroke(Color.grayRgb(130));
        parentRoomDrawer.setBackgroundFill(Color.ALICEBLUE);
        grandparentRoomTypeDrawer.setClipText(false); // So the text is always visible even when slider is on left
        grandparentRoomTypeDrawer.setBackgroundFill(Color.ALICEBLUE);
        grandparentRoomTypeDrawer.setTextFill(Color.rgb(0, 150, 214));
        // Updating the text font on any theme mode change that may impact it (light/dark mode, etc...)
        ThemeRegistry.runNowAndOnModeChange(() -> {
            font = TextTheme.getFont(FontDef.font(13));
            barDrawer.setTextFont(font);
            parentRoomDrawer.setTextFont(font);
            grandparentRoomTypeDrawer.setTextFont(TextTheme.getFont(FontDef.font(FontWeight.BOLD,13)));
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
        new ParentsCanvasDrawer(barsLayout, barsDrawer, this::drawParentRoom, this::drawGrandParentRoomType)
                .setHorizontalStroke(Color.GRAY)
                .setParentWidth(150);
        // We embed the canvas in a VirtualCanvasPane which has 2 functions:
        // 1) As a CanvasPane it is responsible for automatically resizing the canvas when the user resizes the UI, and
        // for calling the canvas refresher (the piece of code that redraws the canvas). TimeCanvasUtil will actually
        // create that refresher using the passed barsLayout & barsDrawer.
        // 2) in addition, VirtualCanvasPane keeps the canvas size as small as possible when used in a scrollPane to
        // prevent memory overflow. Whereas the virtual canvas represents the whole canvas that the user seems to watch
        // and can have a very long height, the real canvas will be only the size of the scrollPane viewport, and when
        // the user scrolls, VirtualCanvasPane is responsible for redrawing the canvas to the scrolled position.
        VirtualCanvasPane roomsAndBarsPane = TimeCanvasUtil.createTimeVirtualCanvasPane(barsLayout, barsDrawer,
                scrollPane.viewportBoundsProperty(), scrollPane.vvalueProperty());
        // We finally set up the scrollPane for vertical scrolling only (no horizontal scrollbar, etc...), and return it
        LayoutUtil.setupVerticalScrollPane(scrollPane, roomsAndBarsPane);
        return scrollPane;
    }

    private void drawBar(LocalDateBar<AttendanceBlock> bar, Bounds b, GraphicsContext gc) {
        // The bar wraps a block over 1 or several days (or always 1 day if the user hasn't ticked the grouping block
        // checkbox). So the bar instance is that block that was repeated over that period.
        AttendanceBlock block = bar.getInstance();

        barDrawer.setMiddleText(block.getPersonName());

        Color barColor = block.getAttendeeCategory().getColor();
        barDrawer.setBackgroundFill(barColor);

        // First draw the un-clipped text in a dark colour which contrasts with the background of the chart
        barDrawer.setClipText(false);
        barDrawer.setTextFill(Color.GRAY);
        barDrawer.drawBar(b, gc);

        // Second draw the clipped text in a light colour which contrasts with the background of the bar
        barDrawer.setClipText(true);
        barDrawer.setTextFill(Color.WHITE);
        barDrawer.drawBar(b, gc);
    }

    private void drawParentRoom(ResourceConfiguration rc, Bounds b, GraphicsContext gc) {
        // The only remaining property that needs to be set here is the room name that we display in the bar middle
        parentRoomDrawer.setMiddleText(rc.getName());
        parentRoomDrawer.drawBar(b, gc); // This also draws a rectangle stroke - see properties set in constructor
        // But the wireframe doesn't show a stroke on the left, so we erase it to match the UX design
        gc.fillRect(b.getMinX(), b.getMinY(), 2, b.getHeight()); // erasing the left side of the stroke rectangle
    }

    private void drawGrandParentRoomType(Item item, Bounds b, GraphicsContext gc) {
        grandparentRoomTypeDrawer.setBottomText(item.getName());
        grandparentRoomTypeDrawer.drawBar(b, gc);
    }

    public void setParentsProvided(boolean parentsProvided) {
        if (parentsProvided) {
            barsLayout.setParentsProvided(true)
                    .setParentGrandparentReader(ResourceConfiguration::getItem);
        } else {
            barsLayout.setChildGrandparentReader(bar -> bar.getInstance().getResourceConfiguration().getItem())
                    .setParentsProvided(false);
        }
    }

    public void startLogic(Object mixin) {
        ReactiveEntitiesMapper.<Attendance>createPushReactiveChain(mixin)
                .always("{class: 'Attendance', alias: 'a', fields: 'date,documentLine.document.(person_firstName,person_lastName),scheduledResource.configuration.(name,item.name),documentLine.document.event.name'}")
                .always(where("scheduledResource is not null"))
                .always(orderBy("scheduledResource.configuration.item.ord,scheduledResource.configuration.name,documentLine.document.person_lastName,documentLine.document.person_firstName,date")) // Order is important for TimeBarUtil (see comment on barsLayout)
                // Returning events for the selected organization only (or returning an empty set if no organization is selected)
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), o -> where("documentLine.document.event.organization=?", o))
                // Restricting events to those appearing in the time window
                .always(pm.timeWindowStartProperty(), startDate -> where("a.date >= ?", startDate))
                .always(pm.timeWindowEndProperty(),   endDate   -> where("a.date <= ?", endDate))
                // Storing the result directly in the events layer
                .storeEntitiesInto(entities)
                // We are now ready to start
                .start();

        ReactiveEntitiesMapper.<ScheduledResource>createPushReactiveChain(mixin)
                .always("{class: 'ScheduledResource', alias: 'sr', fields: 'date,available,online,max,configuration.(name,item.name),(select count(1) from Attendance where scheduledResource=sr) as booked'}")
                // Returning events for the selected organization only (or returning an empty set if no organization is selected)
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), o -> where("configuration.resource.site.organization=?", o))
                // Storing the result directly in the events layer
                .storeEntitiesInto(allScheduledResources)
                // We are now ready to start
                .start();
    }
}
