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
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import one.modality.base.client.gantt.fx.highlight.FXGanttHighlight;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.base.shared.entities.ScheduledResource;
import one.modality.hotel.backoffice.accommodation.AccommodationStatusBarUpdater;
import one.modality.hotel.backoffice.accommodation.AccommodationPresentationModel;
import one.modality.hotel.backoffice.accommodation.AttendanceBlock;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static dev.webfx.stack.orm.dql.DqlStatement.orderBy;
import static dev.webfx.stack.orm.dql.DqlStatement.where;

public class HouseholdGanttCanvas {

    private static final double BAR_HEIGHT = 20;
    private static final double BAR_RADIUS = 10;

    private static final String ROOM_ICON_SVG_PATH = "m 17.515408,6.5882517 c 0.711113,-3.19e-5 1.395431,0.2513817 1.913315,0.7029548 0.517963,0.451605 0.830495,1.069333 0.873958,1.7271744 l 0.0051,0.1581403 V 15.29415 c -8e-5,0.178875 -0.07337,0.350971 -0.205112,0.481678 -0.131744,0.130707 -0.312054,0.210136 -0.504566,0.222338 -0.192511,0.0122 -0.38279,-0.04378 -0.532477,-0.156625 -0.149687,-0.112923 -0.247538,-0.274174 -0.273855,-0.451374 l -0.0071,-0.09602 V 13.176522 H 1.5230857 v 2.117628 c -8e-6,0.170581 -0.066653,0.33542 -0.1876152,0.463974 -0.1209538,0.128554 -0.2880418,0.21213 -0.47036172,0.235337 L 0.76154005,16 C 0.57751434,16 0.39971536,15.938275 0.2610248,15.826149 0.12233424,15.714024 0.03213526,15.559153 0.00710794,15.390167 L 0,15.29415 V 9.1765212 C -3.5083179e-5,8.5174039 0.27120224,7.8830882 0.75839,7.4030052 1.2455706,6.9229461 1.9120008,6.6332216 2.6217341,6.5929568 l 0.170581,-0.00471 z M 4.8230902,0 H 15.484705 c 0.711033,-3.2691116e-5 1.395351,0.25137928 1.913235,0.70296035 0.517963,0.45158105 0.830495,1.06930115 0.873958,1.72716645 l 0.0051,0.1581163 V 5.6470737 H 15.230787 L 15.22369,5.5369576 C 15.196974,5.3268695 15.094658,5.1312556 14.933327,4.9816723 14.771917,4.8320889 14.560904,4.7372606 14.33426,4.7124829 l -0.118824,-0.00659 h -2.030783 c -0.248734,2.39e-5 -0.488776,0.084661 -0.674589,0.2378484 -0.185892,0.153188 -0.304557,0.364265 -0.333665,0.5932135 l -0.0071,0.1101161 H 9.1385181 l -0.0071,-0.1101161 C 9.1046251,5.3268695 9.0023882,5.1312556 8.8409782,4.9816723 8.6795683,4.8320889 8.468555,4.7372606 8.2419112,4.7124829 L 8.1230867,4.7058957 H 6.0923276 C 5.8436254,4.7059196 5.6035839,4.7905562 5.4177312,4.9437441 5.2318784,5.0969321 5.1131496,5.3080091 5.0840496,5.5369576 L 5.076944,5.6470737 H 2.0307774 V 2.5882431 C 2.0307376,1.9291498 2.3019765,1.294866 2.7891651,0.81478301 3.2763456,0.33469202 3.9427758,0.0449707 4.6525092,0.00470593 Z";
    private static final Paint ROOM_ICON_SVG_FILL = Color.web("#838788");
    private static final double ROOM_ICON_SVG_WIDTH = 20.3;
    private static final double ROOM_ICON_SVG_HEIGHT = 16;
    private static final String BED_ICON_SVG_PATH = "m 11.264187,5.9995628 c 0,0.7956522 -0.323669,1.5587124 -0.899771,2.1213039 -0.5761032,0.5626666 -1.3575011,0.8786786 -2.1722266,0.8786786 -0.8147256,0 -1.5961085,-0.316012 -2.1722187,-0.8786786 C 5.443868,7.5582752 5.1202146,6.795215 5.1202146,5.9995628 c 0,-0.7956523 0.3236534,-1.5587125 0.8997561,-2.1213265 0.5761102,-0.5626065 1.3574931,-0.8786786 2.1722187,-0.8786786 0.8147255,0 1.5961234,0.3160721 2.1722266,0.8786786 0.576102,0.562614 0.899771,1.3256742 0.899771,2.1213265 z M 0,8.0000165 C 0,5.8782772 0.86307465,3.8434449 2.399366,2.3431534 3.9356499,0.84285438 6.0193026,0 8.1919642,0 c 2.1726018,0 4.2562538,0.84285438 5.7925528,2.3431534 1.536299,1.5002915 2.399365,3.5351238 2.399365,5.6568631 0,2.1217095 -0.863066,4.1565715 -2.399365,5.6568405 C 12.448218,15.157201 10.364566,16 8.1919642,16 6.0193026,16 3.9356499,15.157201 2.399366,13.656857 0.86307465,12.156588 0,10.121726 0,8.0000165 Z M 8.1919642,1.0000047 C 6.8420974,1.0000722 5.5196959,1.3723659 4.3769558,2.0740328 3.2342158,2.7756996 2.3175857,3.7782158 1.7325581,4.9662034 1.1475305,6.154191 0.91789261,7.4793848 1.0700738,8.7891458 1.2222475,10.098982 1.7500551,11.340211 2.5927489,12.36999 c 0.7270378,-1.143949 2.3275374,-2.36994 5.5992153,-2.36994 3.2716628,0 4.8710888,1.224941 5.5991928,2.36994 0.842649,-1.029779 1.370486,-2.271008 1.522638,-3.5808442 C 15.466021,7.4793848 15.23633,6.154191 14.651295,4.9662034 14.066335,3.7782158 13.149675,2.7756996 12.006927,2.0740328 10.86418,1.3723659 9.5418084,1.0000722 8.1919642,1.0000047 Z";
    private static final Paint BED_ICON_SVG_FILL = Color.web("#838788");
    private static final double BED_ICON_SVG_WIDTH = 16.384;
    private static final double BED_ICON_SVG_HEIGHT = 16;


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
