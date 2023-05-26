package one.modality.hotel.backoffice.activities.household;

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
import dev.webfx.extras.time.layout.gantt.HeaderPosition;
import dev.webfx.extras.time.layout.gantt.LocalDateGanttLayout;
import dev.webfx.extras.time.layout.gantt.canvas.ParentsCanvasDrawer;
import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.kit.launcher.WebFxKitLauncher;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import one.modality.base.client.gantt.fx.highlight.FXGanttHighlight;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.base.shared.entities.ScheduledResource;
import one.modality.hotel.backoffice.accommodation.AccommodationPresentationModel;
import one.modality.hotel.backoffice.accommodation.AccommodationStatusBarUpdater;
import one.modality.hotel.backoffice.accommodation.AttendanceBlock;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static dev.webfx.stack.orm.dql.DqlStatement.orderBy;
import static dev.webfx.stack.orm.dql.DqlStatement.where;
import static one.modality.hotel.backoffice.icons.BedSvgIcon.*;
import static one.modality.hotel.backoffice.icons.RoomSvgIcon.*;

public class HouseholdGanttCanvas {

    private static final double BAR_HEIGHT = 20;
    private static final double BAR_RADIUS = 10;


    // The presentation model used by the logic code to query the server (see startLogic() method)
    private final AccommodationPresentationModel pm;

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

    private Font barsFont;

    // As a result, TimeBarUtil generates a list of bars that will be the input of this barsLayout:
    private final LocalDateGanttLayout<LocalDateBar<AttendanceBlock>> barsLayout =
            new LocalDateGanttLayout<LocalDateBar<AttendanceBlock>>()
                    .setChildFixedHeight(BAR_HEIGHT)
                    .setChildParentReader(bar -> bar.getInstance().getResourceConfiguration())
                    .setChildGrandparentReader(bar -> bar.getInstance().getResourceConfiguration().getItem())
                    .setParentGrandparentReader(ResourceConfiguration::getItem)
                    .setParentHeaderPosition(HeaderPosition.TOP)
                    .setParentHeaderHeight(BAR_HEIGHT)
                    .setTetrisPacking(true)
                    .setChildTetrisMinWidthReader(bar -> WebFxKitLauncher.measureText(bar.getInstance().getPersonName(), barsFont).getWidth())
                    .setVSpacing(2);

    // Once the position of the bars are computed by barsLayout, they will be automatically drawn in a canvas by this
    // barsDrawer (each bar will be rendered using the drawBar() method provided in this class)
    private final LocalDateCanvasDrawer<LocalDateBar<AttendanceBlock>> barsDrawer =
            new LocalDateCanvasDrawer<>(barsLayout, this::drawBar)
                    // Enabling canvas interaction (user can move & zoom in/out the time window)
                    .enableCanvasInteraction();

    // We will use the BarDrawer utility class to draw the bars & rooms names & types
    private final BarDrawer barDrawer = new BarDrawer()  // unique instance to draw all the bars
            .setTextFill(Color.WHITE)
            .setRadius(BAR_RADIUS);

    private final BarDrawer parentRoomDrawer = new BarDrawer() // unique instance to draw all the room names
            .setBackgroundFill(Color.WHITE)
            .setStroke(Color.grayRgb(130))
            .setIcon(ROOM_ICON_SVG_PATH, ROOM_ICON_SVG_FILL, ROOM_ICON_SVG_WIDTH, ROOM_ICON_SVG_HEIGHT, Pos.CENTER_LEFT, HPos.LEFT, VPos.CENTER, 10, 0)
            .setTextFill(Color.BLACK)
            .setTextAlignment(TextAlignment.LEFT);

    private final BarDrawer bedDrawer = new BarDrawer()
            .setBackgroundFill(Color.grayRgb(243))
            .setIcon(BED_ICON_SVG_PATH, BED_ICON_SVG_FILL, BED_ICON_SVG_WIDTH, BED_ICON_SVG_HEIGHT, Pos.CENTER_LEFT, HPos.LEFT, VPos.CENTER, 10, 0)
            .setTextFill(Color.grayRgb(130));

    private final BarDrawer grandparentRoomTypeDrawer = new BarDrawer() // unique instance to draw all the room types
            .setStroke(Color.grayRgb(130))
            .setBackgroundFill(Color.WHITE)
            .setTextAlignment(TextAlignment.LEFT)
            .setTextFill(Color.rgb(0, 150, 214));

    public HouseholdGanttCanvas(AccommodationStatusBarUpdater controller) {
        this(new AccommodationPresentationModel(), controller);
        pm.doFXBindings();
    }

    public HouseholdGanttCanvas(AccommodationPresentationModel pm, AccommodationStatusBarUpdater controller) {
        this.pm = pm;
        // Binding the presentation model and the barsLayout time window
        barsLayout.bindTimeWindowBidirectional(pm);

        // Asking TimeBarUtil to automatically transform entities into bars that will feed the input of barsLayout
        TimeBarUtil.setupBarsLayout(
                entities, // the observable list of Attendance entities to take as input
                Attendance::getDate, // the entity date reader that will be used to date each block
                AttendanceBlock::new, // the factory that creates blocks, initially 1 instance per entity, but then grouped into bars
                barsLayout); // the layout that will receive the final list of bars as a result of the blocks grouping

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

        FXGanttHighlight.addDayHighlight(barsLayout, barsDrawer);

        // Updating the text font on any theme mode change that may impact it (light/dark mode, etc...)
        ThemeRegistry.runNowAndOnModeChange(() -> {
            parentRoomDrawer.setTextFont(barsFont = TextTheme.getFont(FontDef.font(FontWeight.BOLD,13)));
            grandparentRoomTypeDrawer.setTextFont(barsFont);
            barDrawer.setTextFont(barsFont = TextTheme.getFont(FontDef.font(13)));
            bedDrawer.setTextFont(barsFont);
        });
    }

    BooleanProperty parentsProvidedProperty() {
        return barsLayout.parentsProvidedProperty();
    }

    public Node buildCanvasContainer() {
        // We embed everything in a scrollPane because the rooms probably won't all fit on the screen. This scrollPane
        // will be responsible for the vertical scrolling only, because the horizontal scrolling is actually already
        // managed by the interactive canvas itself (which reacts to user dragging to move the gantt dates).
        ScrollPane scrollPane = new ScrollPane();
        // That scrollPane will contain a splitPane showing the list of rooms on its left side, and the blocks/bars on
        // the right side. To show the list of rooms, we just use a ParentCanvasPane which displays the parents of the
        // barsLayout (the parents are ResourceConfiguration instances as set in barsLayout.setChildParentReader() above)
        ParentsCanvasDrawer.create(barsLayout, barsDrawer, this::drawParentRoom, this::drawGrandparentRoomType)
                .setChildRowHeaderDrawer(this::drawBed)
                .setHorizontalStroke(Color.grayRgb(200))
                .setVerticalStroke(Color.grayRgb(233), false)
                .setTetrisAreaFill(Color.grayRgb(243))
                .setParentWidth(150);
        // We embed the canvas in a VirtualCanvasPane which has 2 functions:
        // 1) As a CanvasPane it is responsible for automatically resizing the canvas when the user resizes the UI, and
        // for calling the canvas refresher (the piece of code that redraws the canvas). TimeCanvasUtil will actually
        // create that refresher using the passed barsLayout & barsDrawer.
        // 2) in addition, VirtualCanvasPane keeps the canvas size as small as possible when used in a scrollPane to
        // prevent memory overflow. Whereas the virtual canvas represents the whole canvas that the user seems to watch
        // and can have a very long height, the real canvas will be only the size of the scrollPane viewport, and when
        // the user scrolls, VirtualCanvasPane is responsible for redrawing the canvas to the scrolled position.
        VirtualCanvasPane virtualCanvasPane = TimeCanvasUtil.createTimeVirtualCanvasPane(barsLayout, barsDrawer,
                scrollPane.viewportBoundsProperty(), scrollPane.vvalueProperty());
        // We finally set up the scrollPane for vertical scrolling only (no horizontal scrollbar, etc...), and return it
        LayoutUtil.setupVerticalScrollPane(scrollPane, virtualCanvasPane);
        return scrollPane;
    }

    private void drawBar(LocalDateBar<AttendanceBlock> bar, Bounds b, GraphicsContext gc) {
        // The bar wraps a block over 1 or several days (or always 1 day if the user hasn't ticked the grouping block
        // checkbox). So the bar instance is that block that was repeated over that period.
        AttendanceBlock block = bar.getInstance();

        barDrawer
                .setBackgroundFill(getBarColor(block))
                .setMiddleText(block.getPersonName())
                // First draw the un-clipped text in a dark colour which contrasts with the background of the chart
                .setClipText(false)
                .setTextFill(Color.GRAY)
                .drawBar(b, gc)
                // Second draw the clipped text in a light colour which contrasts with the background of the bar
                .setClipText(true)
                .setTextFill(Color.WHITE)
                .drawTexts(b, gc);
    }

    private Color getBarColor(AttendanceBlock block) {
        if (block.isCheckedIn()) {
            return Color.GRAY;
        } else {
            return block.getAttendeeCategory().getColor();
        }
    }

    private void drawGrandparentRoomType(Item item, Bounds b, GraphicsContext gc) {
        grandparentRoomTypeDrawer
                .setBottomText(item.getName())
                .drawBar(b, gc);
    }

    private void drawParentRoom(ResourceConfiguration rc, Bounds b, GraphicsContext gc) {
        // The only remaining property that needs to be set here is the room name that we display in the bar middle
        parentRoomDrawer
                .setMiddleText(rc.getName())
                .drawBar(b, gc); // This also draws a rectangle stroke - see properties set in constructor
        // But the wireframe doesn't show a stroke on the left, so we erase it to match the UX design
        gc.fillRect(b.getMinX(), b.getMinY(), 2, b.getHeight()); // erasing the left side of the stroke rectangle
    }

    private void drawBed(Integer rowIndex, Bounds b, GraphicsContext gc) {
        bedDrawer
                .setMiddleText("Bed " + (rowIndex + 1))
                .drawBar(b, gc);
    }

    public void startLogic(Object mixin) {
        // This ReactiveEntitiesMapper will populate the children of the GanttLayout (indirectly from entities observable list)
        ReactiveEntitiesMapper.<Attendance>createPushReactiveChain(mixin)
                .always("{class: 'Attendance', alias: 'a', fields: 'date,documentLine.document.(arrived,person_firstName,person_lastName,event.id),scheduledResource.configuration.(name,item.name),documentLine.document.event.name'}")
                .always(where("scheduledResource is not null"))
                .always(orderBy("scheduledResource.configuration.item.ord,scheduledResource.configuration.name,documentLine.document.person_lastName,documentLine.document.person_firstName,date")) // Order is important for TimeBarUtil (see comment on barsLayout)
                // Returning events for the selected organization only (or returning an empty set if no organization is selected)
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), o -> where("documentLine.document.event.organization=?", o))
                // Restricting events to those appearing in the time window
                .always(where("a.date >= ?", todayDate())) // Exclude data from the past
                .always(pm.timeWindowEndProperty(),   endDate   -> where("a.date -1 <= ?", endDate)) // -1 is to avoid the round corners on right for bookings exceeding the time window
                // Storing the result directly in the events layer
                .storeEntitiesInto(entities)
                // We are now ready to start
                .start();

        // This ReactiveEntitiesMapper will populate the provided parents of the GanttLayout (indirectly from allScheduledResources observable list)
        ReactiveEntitiesMapper.<ScheduledResource>createPushReactiveChain(mixin)
                .always("{class: 'ScheduledResource', alias: 'sr', fields: 'date,available,online,max,configuration.(name,item.name),(select count(1) from Attendance where scheduledResource=sr) as booked'}")
                .always(orderBy("configuration.item.ord,configuration.name")) // How rooms will be ordered when provided ("Show all" checkbox)
                // Returning events for the selected organization only (or returning an empty set if no organization is selected)
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), o -> where("configuration.resource.site.organization=?", o))
                // Restricting events to those appearing in the time window
                .always(where("sr.date >= ?", todayDate())) // Exclude data from the past
                .always(pm.timeWindowEndProperty(),   endDate   -> where("sr.date <= ?", endDate))
                // Storing the result directly in the events layer
                .storeEntitiesInto(allScheduledResources)
                // We are now ready to start
                .start();
    }

    private static LocalDate todayDate() {
        return LocalDate.now().atStartOfDay().toLocalDate();
    }
}
