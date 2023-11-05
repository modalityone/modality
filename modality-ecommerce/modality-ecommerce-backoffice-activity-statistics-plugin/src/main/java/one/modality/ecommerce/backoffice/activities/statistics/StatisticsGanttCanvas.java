package one.modality.ecommerce.backoffice.activities.statistics;

import dev.webfx.extras.canvas.bar.BarDrawer;
import dev.webfx.extras.canvas.pane.VirtualCanvasPane;
import dev.webfx.extras.geometry.Bounds;
import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.ThemeRegistry;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.time.layout.canvas.LocalDateCanvasDrawer;
import dev.webfx.extras.time.layout.canvas.TimeCanvasUtil;
import dev.webfx.extras.time.layout.gantt.LocalDateGanttLayout;
import dev.webfx.extras.time.layout.gantt.canvas.ParentsCanvasDrawer;
import dev.webfx.extras.util.control.ControlUtil;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import one.modality.base.client.gantt.fx.highlight.FXGanttHighlight;
import one.modality.base.client.gantt.fx.timewindow.FXGanttTimeWindow;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.ItemFamily;
import one.modality.crm.backoffice.organization.fx.FXOrganization;

import static dev.webfx.stack.orm.dql.DqlStatement.orderBy;
import static dev.webfx.stack.orm.dql.DqlStatement.where;

/**
 * @author Bruno Salmon
 */
final class StatisticsGanttCanvas {

    // Style constants used for drawing bars in the canvas:
    private static final double BAR_HEIGHT = 40;

    // The presentation model used by the logic code to query the server (see startLogic() method)
    private final StatisticsPresentationModel pm = new StatisticsPresentationModel();

    // As a result, TimeBarUtil generates a list of bars that will be the input of this barsLayout:
    private final LocalDateGanttLayout<Attendance> barsLayout = new LocalDateGanttLayout<Attendance>()
                .setInclusiveChildStartTimeReader(Attendance::getDate)
                .setInclusiveChildEndTimeReader(Attendance::getDate)
                .setChildFixedHeight(BAR_HEIGHT)
                .setChildParentReader(     a -> a.getDocumentLine().getItem())
                .setChildGrandparentReader(a -> a.getDocumentLine().getItem().getFamily());

    // Once the position of the bars are computed by barsLayout, they will be automatically drawn in a canvas by this
    // barsDrawer (each bar will be rendered using the drawBar() method provided in this class)
    private final LocalDateCanvasDrawer<Attendance> barsDrawer
            = new LocalDateCanvasDrawer<>(barsLayout, this::drawAttendance);

    // We will use the BarDrawer utility class to draw the bars & rooms names & types
    private final BarDrawer attendanceDrawer = new BarDrawer()  // unique instance to draw all the bars
            // Setting the unchanging properties (remaining changing properties will be set in drawBar())
            .setTextFill(Color.WHITE)
            .setBackgroundFill(Color.rgb(65, 186, 77))
            .setClipText(false) // doesn't need clipping (better perf)
            ;
    private final BarDrawer parentItemDrawer = new BarDrawer() // unique instance to draw all the room names
            // Setting the unchanging properties (remaining changing properties will be set in drawParentItem())
            .setStroke(Color.grayRgb(130))
            .setTextFill(Color.grayRgb(130))
            .setBackgroundFill(Color.ALICEBLUE);

    private final BarDrawer grandparentItemFamilyDrawer = new BarDrawer() // unique instance to draw all the room types
            // Setting the unchanging properties (remaining changing properties will be set in drawGrandParentItemFamily())
            .setTextFill(Color.rgb(0, 150, 214))
            .setBackgroundFill(Color.ALICEBLUE)
            .setClipText(false); // So the text is always visible even when slider is on left

    public StatisticsGanttCanvas() {
        // Binding the presentation model and the barsLayout time window
        pm.organizationIdProperty().bind(FXOrganization.organizationProperty());
        pm.bindTimeWindow(barsLayout); // barsLayout will itself be bound to FXGanttTimeWindow (see below)
        barsLayout.bindTimeWindowBidirectional(FXGanttTimeWindow.ganttTimeWindow());

        // That scrollPane will contain a splitPane showing the list of rooms on its left side, and the blocks/bars on
        // the right side. To show the list of rooms, we just use a ParentCanvasPane which displays the parents of the
        // barsLayout (the parents are ResourceConfiguration instances as set in barsLayout.setChildParentReader() above)
        new ParentsCanvasDrawer(barsLayout, barsDrawer)
                .setParentDrawer(this::drawParentItem)
                .setGrandparentDrawer(this::drawGrandParentItemFamily)
                .setParentWidth(150)
                .setHorizontalStroke(Color.BLACK)
                .setVerticalStroke(Color.BLACK)
        ;

        FXGanttHighlight.addDayHighlight(barsLayout, barsDrawer);

        // Enabling canvas interaction (user can move & zoom in/out the time window)
        barsDrawer.enableCanvasInteraction();

        // Updating the text font on any theme mode change that may impact it (light/dark mode, etc...)
        ThemeRegistry.runNowAndOnModeChange(() -> {
            Font font = TextTheme.getFont(FontDef.font(13));
            attendanceDrawer.setTextFont(font);
            parentItemDrawer.setTextFont(font);
            grandparentItemFamilyDrawer.setTextFont(TextTheme.getFont(FontDef.font(FontWeight.BOLD,13)));
        });
    }

    public Node buildCanvasContainer() {
        // We embed everything in a scrollPane because the rooms probably won't all fit on the screen. This scrollPane
        // will be responsible for the vertical scrolling only, because the horizontal scrolling is actually already
        // managed by the interactive canvas itself (which reacts to user dragging to move the gantt dates).
        ScrollPane scrollPane = new ScrollPane();
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
        ControlUtil.setupVerticalScrollPane(scrollPane, roomsAndBarsPane);
        return scrollPane;
    }

    private void drawAttendance(Attendance a, Bounds b, GraphicsContext gc) {
        int count = a.getIntegerFieldValue("count");
        attendanceDrawer
                .setMiddleText(String.valueOf(count))
                .drawBar(b, gc);
    }

    private void drawParentItem(Item item, Bounds b, GraphicsContext gc) {
        // The only remaining property that needs to be set here is the room name that we display in the bar middle
        parentItemDrawer
                .setMiddleText(item.getName())
                .drawBar(b, gc); // This also draws a rectangle stroke - see properties set in constructor
        // But the wireframe doesn't show a stroke on the left, so we erase it to match the UX design
        gc.fillRect(b.getMinX(), b.getMinY(), 2, b.getHeight()); // erasing the left side of the stroke rectangle
    }

    private void drawGrandParentItemFamily(ItemFamily item, Bounds b, GraphicsContext gc) {
        grandparentItemFamilyDrawer
                .setBottomText(item.getName())
                .drawBar(b, gc);
    }

    public void startLogic(Object mixin) {
        ReactiveEntitiesMapper.<Attendance>createPushReactiveChain(mixin)
                .always("{class: 'Attendance', alias: 'a', fields: 'documentLine.item.family,date,count(1) as count', where: 'present and !documentLine.cancelled', groupBy: 'documentLine.item.family,documentLine.item,date'}")
                .always(orderBy("documentLine.item.family.ord,documentLine.item,date")) // Order is important for TimeBarUtil (see comment on barsLayout)
                // Returning events for the selected organization only (or returning an empty set if no organization is selected)
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), o -> where("documentLine.document.event.organization=?", o))
                // Restricting events to those appearing in the time window
                .always(pm.timeWindowStartProperty(), startDate -> where("date >= ?", startDate))
                .always(pm.timeWindowEndProperty(),   endDate   -> where("date <= ?", endDate))
                // Storing the result directly in the events layer
                .storeEntitiesInto(barsLayout.getChildren())
                // We are now ready to start
                .start();
    }
}
