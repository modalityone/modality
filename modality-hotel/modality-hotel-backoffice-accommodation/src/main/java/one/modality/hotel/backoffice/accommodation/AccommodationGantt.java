package one.modality.hotel.backoffice.accommodation;

import dev.webfx.extras.canvas.bar.BarDrawer;
import dev.webfx.extras.canvas.pane.VirtualCanvasPane;
import dev.webfx.extras.geometry.Bounds;
import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.ThemeRegistry;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.time.layout.bar.LocalDateBar;
import dev.webfx.extras.time.layout.bar.TimeBarUtil;
import dev.webfx.extras.time.layout.canvas.LocalDateCanvasDrawer;
import dev.webfx.extras.time.layout.canvas.TimeCanvasInteractionHandler;
import dev.webfx.extras.time.layout.canvas.TimeCanvasUtil;
import dev.webfx.extras.time.layout.gantt.HeaderPosition;
import dev.webfx.extras.time.layout.gantt.HeaderRotation;
import dev.webfx.extras.time.layout.gantt.LocalDateGanttLayout;
import dev.webfx.extras.time.layout.gantt.canvas.ParentsCanvasDrawer;
import dev.webfx.extras.util.control.ControlUtil;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import javafx.beans.property.BooleanProperty;
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
import one.modality.base.client.gantt.fx.selection.FXGanttSelection;
import one.modality.base.client.gantt.fx.timewindow.FXGanttTimeWindow;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.ResourceConfiguration;

import static one.modality.hotel.backoffice.icons.BedSvgIcon.*;
import static one.modality.hotel.backoffice.icons.RoomSvgIcon.*;

public abstract class AccommodationGantt<B extends AccommodationBlock> {

    private static final double BAR_HEIGHT = 20;
    private static final double BAR_RADIUS = 10;

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
    protected final LocalDateGanttLayout<LocalDateBar<B>> barsLayout =
            new LocalDateGanttLayout<LocalDateBar<B>>()
                    .setChildFixedHeight(BAR_HEIGHT)
                    .setChildParentReader(     bar -> bar.getInstance().getRoomConfiguration())
                    //.setChildGrandparentReader(bar -> bar.getInstance().getRoomConfiguration().getItem())
                    .setParentGrandparentReader(ResourceConfiguration::getItem)
                    .setParentHeaderHeight(BAR_HEIGHT)
                    .setGrandparentHeaderWidth(20)
                    .setParentHeaderWidth(90)
            ;

    // Once the position of the bars are computed by barsLayout, they will be automatically drawn in a canvas by this
    // barsDrawer (each bar will be rendered using the drawBar() method provided in this class)
    protected final LocalDateCanvasDrawer<LocalDateBar<B>> barsDrawer =
            new LocalDateCanvasDrawer<>(barsLayout, this::drawBar)
                    // Enabling canvas interaction (user can move & zoom in/out the time window)
                    .enableCanvasInteraction();

    protected final ParentsCanvasDrawer parentsCanvasDrawer = ParentsCanvasDrawer.create(barsLayout, barsDrawer)
            .setParentDrawer(this::drawParentRoom)
            .setGrandparentDrawer(this::drawGrandparentRoomType);

    // We will use the BarDrawer utility class to draw the bars & rooms names & types
    protected final BarDrawer barDrawer = new BarDrawer()  // unique instance to draw all the bars
            .setTextFill(Color.WHITE);

    protected final BarDrawer parentRoomDrawer = new BarDrawer() // unique instance to draw all the room names
            .setBackgroundFill(Color.WHITE)
            .setStroke(Color.grayRgb(130))
            .setIcon(ROOM_ICON_SVG_PATH, ROOM_ICON_SVG_FILL, ROOM_ICON_SVG_WIDTH, ROOM_ICON_SVG_HEIGHT, Pos.CENTER_LEFT, HPos.LEFT, VPos.CENTER, 10, 0)
            .setTextFill(Color.BLACK)
            .setTextAlignment(TextAlignment.LEFT);

    protected final BarDrawer bedDrawer = new BarDrawer()
            .setBackgroundFill(Color.grayRgb(243))
            .setIcon(BED_ICON_SVG_PATH, BED_ICON_SVG_FILL, BED_ICON_SVG_WIDTH, BED_ICON_SVG_HEIGHT, Pos.CENTER_LEFT, HPos.LEFT, VPos.CENTER, 10, 0)
            .setTextFill(Color.grayRgb(130));

    protected final BarDrawer grandparentRoomTypeDrawer = new BarDrawer() // unique instance to draw all the room types
            .setStroke(Color.grayRgb(130))
            .setBackgroundFill(Color.WHITE)
            .setTextAlignment(TextAlignment.CENTER)
            .setTextFill(Color.rgb(0, 150, 214));

    public AccommodationGantt(AccommodationPresentationModel pm, ObservableList<LocalDateBar<B>> children, ObservableList<ResourceConfiguration> providedParentRooms, double barsFontSize) {
        // Binding the presentation model and the barsLayout time window
        barsLayout.bindTimeWindowBidirectional(pm);

        // Pairing this Gantt canvas with the referent one (ie the event Gantt canvas on top), so it always stays
        // horizontally aligned with the event Gantt dates, even when this canvas is horizontally shifted (ex: when
        // showing the legend on the left, which shifts this canvas to the right).
        FXGanttTimeWindow.setupPairedTimeProjectorWhenReady(barsLayout, barsDrawer.getCanvas());

        // Telling the bars layout how to read start & end times of bars
        TimeBarUtil.setBarsLayoutTimeReaders(barsLayout);
        if (children != null)
            ObservableLists.bind(barsLayout.getChildren(), children);
        if (providedParentRooms != null) {
            ObservableLists.bind(barsLayout.getParents(), providedParentRooms);
            barsLayout.setParentsProvided(true);
        }

        FXGanttHighlight.addDayHighlight(barsLayout, barsDrawer);

        // Updating the text font on any theme mode change that may impact it (light/dark mode, etc...)
        ThemeRegistry.runNowAndOnModeChange(() -> {
            parentRoomDrawer.setTextFont(barsFont = TextTheme.getFont(FontDef.font(FontWeight.BOLD, barsFontSize)));
            grandparentRoomTypeDrawer.setTextFont(barsFont);
            barDrawer.setTextFont(barsFont = TextTheme.getFont(FontDef.font(barsFontSize)));
            bedDrawer.setTextFont(barsFont);
        });

        // Redrawing the canvas when Gantt selected object changes because the guest color may depend on selected event
        FXProperties.runOnPropertyChange(barsDrawer::markDrawAreaAsDirty, FXGanttSelection.ganttSelectedObjectProperty());

        // We disable the time window horizontal scroll on mouse wheel over this canvas, because we want the mouse wheel
        // to control the vertical scroll (via ScrollPane) instead.
        TimeCanvasInteractionHandler.disableScrollTimeWindowOnCanvas(barsDrawer.getCanvas());
    }

    public BooleanProperty parentsProvidedProperty() {
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
        ControlUtil.setupVerticalScrollPane(scrollPane, virtualCanvasPane);
        return scrollPane;
    }

    protected void drawGrandparentRoomType(Item item, Bounds b, GraphicsContext gc) {
        grandparentRoomTypeDrawer
                .setMiddleText(item.getName())
                .drawBar(b, gc);
    }

    protected void drawParentRoom(ResourceConfiguration rc, Bounds b, GraphicsContext gc) {
        // The only remaining property that needs to be set here is the room name that we display in the bar middle
        parentRoomDrawer
                .setMiddleText(rc.getName())
                .drawBar(b, gc); // This also draws a rectangle stroke - see properties set in constructor
        // But the wireframe doesn't show a stroke on the left, so we erase it to match the UX design
        gc.fillRect(b.getMinX(), b.getMinY(), 2, b.getHeight()); // erasing the left side of the stroke rectangle
    }

    protected abstract void drawBar(LocalDateBar<B> bar, Bounds b, GraphicsContext gc);

    protected void showBeds() {
        barsLayout
                .setGrandparentHeaderPosition(HeaderPosition.LEFT)
                .setParentHeaderPosition(HeaderPosition.LEFT)
                .setTetrisPacking(true)
                //.setChildTetrisMinWidthReader(bar -> WebFxKitLauncher.measureText(bar.getInstance().getPersonName(), barsFont).getWidth())
                .setHSpacing(2)
                .setVSpacing(2);
        parentsCanvasDrawer
                .setChildRowHeaderDrawer(this::drawBed)
                .setHorizontalStroke(Color.grayRgb(200))
                .setVerticalStroke(Color.grayRgb(233), false)
                .setTetrisAreaFill(Color.grayRgb(243))
                .setGrandparentHeaderRotation(HeaderRotation.DEG_90_ANTICLOCKWISE);
        barDrawer
            .setRadius(BAR_RADIUS);
    }

    protected void drawBed(Integer rowIndex, Bounds b, GraphicsContext gc) {
        bedDrawer
                .setMiddleText("Bed " + (rowIndex + 1))
                .drawBar(b, gc);
    }

}
