package one.modality.catering.backoffice.activities.kitchen;

import dev.webfx.extras.canvas.pane.VirtualCanvasPane;
import dev.webfx.extras.geometry.Bounds;
import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.time.layout.canvas.LocalDateCanvasDrawer;
import dev.webfx.extras.time.layout.canvas.TimeCanvasUtil;
import dev.webfx.extras.time.layout.gantt.LocalDateGanttLayout;
import dev.webfx.extras.time.layout.gantt.canvas.ParentsCanvasDrawer;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import one.modality.base.client.gantt.fx.highlight.FXGanttHighlight;
import one.modality.base.client.gantt.fx.timewindow.FXGanttTimeWindow;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.impl.ItemImpl;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static dev.webfx.stack.orm.dql.DqlStatement.orderBy;
import static dev.webfx.stack.orm.dql.DqlStatement.where;

/**
 * @author Bruno Salmon
 */
final class KitchenGantt {

    // Style constants used for drawing bars in the canvas:
    private static final double BAR_HEIGHT = 30;
    private static final Item NO_DIET_ITEM = new ItemImpl(null, null); // Arbitrary item in memory representing no diet
    static { NO_DIET_ITEM.setName("No diet"); }

    record DataRow(Item mealItem, Item dietItem) { }
    record DataCell(LocalDate date, int count, DataRow dataRow) { }

    private final ObservableList<Item> organizationDietItems = FXCollections.observableArrayList();
    private final ObservableList<Attendance> mealsAttendancesWithDietCounts = FXCollections.observableArrayList();

    private final LocalDateGanttLayout<DataCell> dataLayout = new LocalDateGanttLayout<DataCell>()
        .setInclusiveChildStartTimeReader(DataCell::date)
        .setInclusiveChildEndTimeReader(DataCell::date)
        .setChildFixedHeight(BAR_HEIGHT)
        .setChildParentReader(DataCell::dataRow);

    // Once the positions of the cells are computed by dataLayout, they will be automatically drawn in a canvas by this
    // dataDrawer (each cell will be rendered using the drawDataCell() method provided in this class)
    private final LocalDateCanvasDrawer<DataCell> dataDrawer
        = new LocalDateCanvasDrawer<>(dataLayout, this::drawDataCell);

    public KitchenGantt() {
        dataLayout.bindTimeWindowBidirectional(FXGanttTimeWindow.ganttTimeWindow());

        ObservableLists.runOnListChange(() -> {
            List<DataCell> dataCells = new ArrayList<>();
            int i = 0;
            int n = mealsAttendancesWithDietCounts.size();
            while (i < n) {
                Item mealItem = mealsAttendancesWithDietCounts.get(i).getDocumentLine().getItem();
                int j = appendRowDataCells(i, n, mealItem, mealItem, "countTotal", dataCells); // Creating meals cell data (ex: Breakfast, Lunch, Dinner)
                for (Item dietItem : organizationDietItems) {
                    String countField = "count" + dietItem.getPrimaryKey();
                    appendRowDataCells(i, j, mealItem, dietItem, countField, dataCells);
                }
                appendRowDataCells(i, j, mealItem, NO_DIET_ITEM, "countNoDiet", dataCells);
                i = j;
            }
            dataLayout.getChildren().setAll(dataCells);
        }, mealsAttendancesWithDietCounts);

        // That scrollPane will contain a splitPane showing the list of rooms on its left side, and the blocks/bars on
        // the right side. To show the list of rooms, we just use a ParentCanvasPane which displays the parents of the
        // barsLayout (the parents are ResourceConfiguration instances as set in barsLayout.setChildParentReader() above)
        new ParentsCanvasDrawer(dataLayout, dataDrawer)
            .setParentDrawer(this::drawDataRow)
            //.setGrandparentDrawer(this::drawGrandParentItemFamily)
            .setParentWidth(150)
            .setHorizontalStroke(Color.BLACK)
            .setVerticalStroke(Color.BLACK)
        ;

        FXGanttHighlight.addDayHighlight(dataLayout, dataDrawer);

        // Enabling canvas interaction (user can move & zoom in/out the time window)
        dataDrawer.enableCanvasInteraction();
    }

    private int appendRowDataCells(int startIndex, int maxIndex, Item mealItem, Item dietItem, String countField, List<DataCell> dataCells) {
        for (int i = startIndex; i < maxIndex; i++) {
            Attendance a = mealsAttendancesWithDietCounts.get(i);
            if (!Entities.sameId(a.getDocumentLine().getItem(), mealItem))
                return i;
            int count = a.getIntegerFieldValue(countField);
            if (count > 0)
                dataCells.add(new DataCell(a.getDate(), count, new DataRow(mealItem, dietItem)));
        }
        return maxIndex;
    }

    Node buildCanvasContainer() {
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
        // and can have a very long height, the real canvas will be only the size of the scrollPane viewport. When
        // the user scrolls, VirtualCanvasPane is responsible for redrawing the canvas to the scrolled position.
        VirtualCanvasPane roomsAndBarsPane = TimeCanvasUtil.createTimeVirtualCanvasPane(dataLayout, dataDrawer,
            scrollPane.viewportBoundsProperty(), scrollPane.vvalueProperty());
        // We finally set up the scrollPane for vertical scrolling only (no horizontal scrollbar, etc...), and return it
        Controls.setupVerticalScrollPane(scrollPane, roomsAndBarsPane);
        return scrollPane;
    }

    private void drawDataCell(DataCell dc, Bounds b, GraphicsContext gc) {
        drawText(String.valueOf(dc.count), dc.dataRow, b, gc);
    }

    private void drawDataRow(DataRow dr, Bounds b, GraphicsContext gc) {
        drawText(dr.dietItem != null ? dr.dietItem.getName() : dr.mealItem.getName(), dr, b, gc);
    }

    private void drawText(String text, DataRow dr, Bounds b, GraphicsContext gc) {
        boolean isMeals = dr.dietItem == dr.mealItem;
        gc.setFill(isMeals ? Color.web("#0096D6") : Color.web("#f5f5f5"));
        gc.fillRect(b.getMinX(), b.getMinY(), b.getWidth(), b.getHeight());
        gc.setFill(isMeals ? Color.WHITE : Color.web("#999999"));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFont(TextTheme.getFont(FontDef.font(13)));
        gc.fillText(text, b.getCenterX(), b.getCenterY());
    }

    public void startLogic(Object mixin) {
        // Loading the diet options
        ReactiveEntitiesMapper.<Item>createReactiveChain(mixin)
            .always( // language=JSON5
                "{class: 'Item', fields: 'name', where: 'family.code=`diet`', orderBy: 'ord'}")
            // Returning events for the selected organization only (or returning an empty set if no organization is selected)
            .ifNotNullOtherwiseEmpty(FXOrganizationId.organizationIdProperty(), o -> where("organization=$1", o))
            // Storing the result directly in the events layer
            .storeEntitiesInto(organizationDietItems)
            // We are now ready to start
            .start();

        // Loading the meals attendances with diet counts
        ReactiveEntitiesMapper.<Attendance>createReactiveChain(mixin)
            .always( // language=JSON5
                "{class: 'Attendance', alias: 'a', fields: 'documentLine.item.family,date,count(1) as countTotal,count(!exists(select DocumentLine ddl where ddl.document=a.documentLine.document and ddl.item.family.code=`diet`) ? 1 : null) as countNoDiet', where: 'present and !documentLine.cancelled and documentLine.item.family.code=`meals`', groupBy: 'documentLine.item,date'}")
            .always(ObservableLists.versionNumber(organizationDietItems), v -> {
                if (v.intValue() == 0)
                    return DqlStatement.EMPTY_STATEMENT;
                StringBuilder sb = new StringBuilder();
                for (Item dietItem : organizationDietItems) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append("count(exists(select DocumentLine ddl where ddl.document=a.documentLine.document and ddl.item=").append(dietItem.getPrimaryKey()).append(") ? 1 : null) as count").append(dietItem.getPrimaryKey());
                }
                return DqlStatement.fields(sb.toString());
            })
            .always(orderBy("documentLine.item,date")) // Order is important for TimeBarUtil (see comment on barsLayout)
            // Returning events for the selected organization only (or returning an empty set if no organization is selected)
            .ifNotNullOtherwiseEmpty(FXOrganizationId.organizationIdProperty(), o -> where("documentLine.document.event.organization=$1", o))
            // Restricting events to those appearing in the time window
            .always(dataLayout.timeWindowStartProperty(), startDate -> where("date >= $1", startDate))
            .always(dataLayout.timeWindowEndProperty(), endDate -> where("date <= $1", endDate))
            // Storing the result directly in the events layer
            .storeEntitiesInto(mealsAttendancesWithDietCounts)
            // We are now ready to start
            .start();
    }
}