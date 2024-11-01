package one.modality.catering.backoffice.activities.kitchen;

import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.luminance.LuminanceTheme;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.time.TimeUtil;
import dev.webfx.extras.time.layout.calendar.CalendarLayout;
import dev.webfx.extras.time.layout.node.TimeGridPane;
import dev.webfx.extras.time.layout.node.TimePane;
import dev.webfx.extras.util.control.ControlUtil;
import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.kit.launcher.WebFxKitLauncher;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.cache.client.LocalStorageCache;
import dev.webfx.stack.db.query.QueryArgument;
import dev.webfx.stack.db.query.QueryArgumentBuilder;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.reactive.call.query.ReactiveQueryCall;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContextMixin;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.FontWeight;
import one.modality.base.client.activity.ModalityButtonFactoryMixin;
import one.modality.base.client.gantt.fx.selection.FXGanttSelection;
import one.modality.base.client.gantt.fx.visibility.FXGanttVisibility;
import one.modality.base.client.time.theme.TimeFacet;
import one.modality.base.shared.entities.Item;
import one.modality.catering.client.i18n.CateringI18nKeys;
import one.modality.crm.backoffice.organization.fx.FXOrganization;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

/**
 * @author Bruno Salmon
 */
final class KitchenActivity extends ViewDomainActivityBase
        implements UiRouteActivityContextMixin<ViewDomainActivityContextFinal>,
        ModalityButtonFactoryMixin  {

    private static final FontDef NO_DATA_MSG_FONT = FontDef.font(FontWeight.BOLD, 18);
    private AttendanceCounts attendanceCounts;
    private CalendarLayout<LocalDate, LocalDate> daysOfMonthLayout;
    private final Pane keyPane = new HBox();
    private final MealsSelectionPane mealsSelectionPane = new MealsSelectionPane();
    private final DietaryOptionKeyPanel dietaryOptionKeyPanel = new DietaryOptionKeyPanel();

    private final Map<LocalDate, AttendanceDayPanel> attendanceDayPanels = new HashMap<>();
    private final ObjectProperty<YearMonth> selectedYearMonthProperty = FXProperties.newObjectProperty(this::refreshCalendar);

    @Override
    public Node buildUi() {
        // Building the box (TimePane with just 1 row) that will show the days of the week (horizontally)
        CalendarLayout<DayOfWeek, DayOfWeek> daysOfWeekLayout = new CalendarLayout<>();
        daysOfWeekLayout.getChildren().setAll(TimeUtil.generateDaysOfWeek());
        daysOfWeekLayout.setChildFixedHeight(40);
        daysOfWeekLayout.setHSpacing(2);
        TimePane<DayOfWeek, DayOfWeek> daysOfWeekPane = new TimePane<>(daysOfWeekLayout, this::createDayOfWeekNode);
        VBox.setMargin(daysOfWeekPane, new Insets(0, WebFxKitLauncher.getVerticalScrollbarExtraWidth(), 0, 0));

        // Building the box (TimeGridPane with several rows) that will show each day of the month (calendar layout)
        daysOfMonthLayout = new CalendarLayout<>();
        //daysOfMonthLayout.setFillHeight(true);
        TimeGridPane<LocalDate, LocalDate> daysOfMonthPane = new TimeGridPane<>(daysOfMonthLayout, this::createDateNode);
        LuminanceTheme.createPrimaryPanelFacet(daysOfMonthPane).style();

        // Building the container
        ScrollPane verticalScrollPane = ControlUtil.createVerticalScrollPane(daysOfMonthPane);
        verticalScrollPane.setFitToWidth(true);

        BorderPane container = new BorderPane();
        container.setCenter(verticalScrollPane);

        //VBox top = new VBox(2, monthsBox, daysOfWeekPane);
        LuminanceTheme.createTopPanelFacet(daysOfWeekPane).setShadowed(true).style();
        container.setTop(daysOfWeekPane);

        LuminanceTheme.createBottomPanelFacet(keyPane).setShadowed(true).style();
        container.setBottom(keyPane);

        LuminanceTheme.createPrimaryPanelFacet(container).style(); // To show the same background if the scroll pane doesn't cover the whole area

        // Updating the query each time the selected month or organization change (this will make the reactive call sending the query to the server)
        FXProperties.runOnPropertiesChange(this::updateQueryArgument, selectedYearMonthProperty, FXOrganizationId.organizationIdProperty());
        FXProperties.runNowAndOnPropertiesChange(() -> mealsSelectionPane.setOrganization(FXOrganization.getOrganization()), FXOrganization.organizationProperty());
        mealsSelectionPane.selectedItemsObservableList().addListener((ListChangeListener<Item>) c -> rebuildDayPanels());

        // Setting the initial selection = this month
        FXProperties.runNowAndOnPropertiesChange(() -> {
            Object ganttSelectedObject = FXGanttSelection.getGanttSelectedObject();
            if (ganttSelectedObject instanceof YearMonth)
                selectedYearMonthProperty.set((YearMonth) ganttSelectedObject);
        }, FXGanttSelection.ganttSelectedObjectProperty());
        if (selectedYearMonthProperty.get() == null)
            setSelectedYearMonth(YearMonth.now());

        return container;
    }

    private void setSelectedYearMonth(YearMonth yearMonth) {
        selectedYearMonthProperty.set(yearMonth);
    }

    private void refreshCalendar() {
        UiScheduler.runInUiThread(() ->
                daysOfMonthLayout.getChildren().setAll(TimeUtil.generateMonthDates(selectedYearMonthProperty.get()))
        );
    }

    private Node createDateNode(LocalDate date) {
        AttendanceDayPanel attendanceDayPanel = attendanceDayPanels.get(date);
        if (attendanceDayPanel != null)
            return attendanceDayPanel;
        return TimeFacet.createDatePanelFacet(date).getContainerNode();
    }

    private Node createDayOfWeekNode(DayOfWeek dayOfWeek) {
        return TimeFacet.createDayOfWeekFacet(dayOfWeek).getContainerNode();
    }

    private void rebuildDayPanels() {
        if (attendanceCounts == null)
            return;

        AbbreviationGenerator abbreviationGenerator = mealsSelectionPane.getAbbreviationGenerator();
        List<Item> displayedMeals = mealsSelectionPane.selectedItemsObservableList();
        attendanceDayPanels.clear();
        for (LocalDate date : attendanceCounts.getDates()) {
            AttendanceDayPanel dayPanel = new AttendanceDayPanel(attendanceCounts, date, displayedMeals, abbreviationGenerator);
            GridPane.setMargin(dayPanel, new Insets(5));
            TimeFacet.createDatePanelFacet(date, null, dayPanel).style();
            attendanceDayPanels.put(date, dayPanel);
        }
        refreshCalendar();

        Platform.runLater(() -> {
            if (dietaryOptionSvgs.isEmpty()) {
                Label noDataLabel = I18nControls.newLabel(CateringI18nKeys.NoMealsData);
                TextTheme.createPrimaryTextFacet(noDataLabel).requestedFont(NO_DATA_MSG_FONT).style();
                noDataLabel.setWrapText(true);
                keyPane.getChildren().setAll(noDataLabel);
            } else {
                keyPane.getChildren().setAll(LayoutUtil.createHGrowable(), mealsSelectionPane, LayoutUtil.createHGrowable(), dietaryOptionKeyPanel, LayoutUtil.createHGrowable());
                dietaryOptionKeyPanel.populate(dietaryOptionSvgs);
            }
            mealsSelectionPane.setDisplayedMealNames(displayedMealNames);
        });
    }

    @Override
    public void onResume() {
        FXGanttVisibility.showMonths();
        super.onResume();
    }

    @Override
    public void onPause() {
        FXGanttVisibility.resetToDefault();
        super.onPause();
    }

    // LOGIC

    private static final String MEALS_COUNT_SQL = "select si.date, i.name, di.code, di.name, count(*), di.ord, di.graphic\n" +
            "from attendance a\n" +
            "  join scheduled_item si on si.id = a.scheduled_item_id\n" +
            "  join document_line dl on dl.id=a.document_line_id\n" +
            "  join site s on s.id=si.site_id\n" +
            "  join item i on i.id=si.item_id\n" +
            "  join item_family f on f.id=i.family_id  \n" +
            "  , ( select i.id,i.code,i.name,i.ord,i.graphic from item i join item_family f on f.id=i.family_id where i.organization_id = $1 and f.code='diet'\n" +
            "    union\n" +
            "    select * from (values (-1, 'Total', null, 10001, null), (-2, '?', null, 10000, null)) vitem(id, code, ord)\n" +
            "    ) di\n" +
            "where not dl.cancelled\n" +
            "  and s.organization_id = $1\n" +
            "  and f.code = 'meals'\n" +
            "  and si.date between $2 and $3\n" +
            "  and case when di.id=-1 then true\n" +
            "       when di.id=-2 then not exists(select * from document_line dl2 join item i2 on i2.id=dl2.item_id join item_family f2 on f2.id=i2.family_id where dl2.document_id=dl.document_id and not dl2.cancelled and f2.code='diet')\n" +
            "       else exists(select * from document_line dl2 where dl2.document_id=dl.document_id and not dl2.cancelled and dl2.item_id=di.id)\n" +
            "        end\n" +
            "group by si.date, i.name, di.code, di.name, i.ord, di.ord, di.graphic\n" +
            "order by si.date, i.ord, di.ord;";

    private final ReactiveQueryCall reactiveQueryCall = new ReactiveQueryCall();

    private void updateQueryArgument() {
        YearMonth selectedYearMonth = selectedYearMonthProperty.get();
        EntityId organizationId = FXOrganizationId.getOrganizationId();
        if (selectedYearMonth == null || organizationId == null)
            return;

        QueryArgument queryArgument = new QueryArgumentBuilder()
                .setStatement(MEALS_COUNT_SQL)
                .setDataSourceId(getDataSourceModel().getDataSourceId())
                .setParameters(
                        organizationId.getPrimaryKey(),        // $1
                        selectedYearMonth.atDay(1), // $2
                        selectedYearMonth.atEndOfMonth()       // $3
                )
                .build();

        reactiveQueryCall.setArgument(queryArgument);
    }

    private Set<String> displayedMealNames;
    private Map<String, String> dietaryOptionSvgs;
    @Override
    protected void startLogic() {
        reactiveQueryCall.bindActivePropertyTo(activeProperty());
        reactiveQueryCall.resultProperty().addListener((observable, oldValue, result) -> {
            attendanceCounts = new AttendanceCounts();
            attendanceCounts.storeDietaryOptionSvg("Total", "{fill: '#828788', svgPath: 'm 0.971924,10.7805 c 0,-2.85307 1.133386,-5.5893 3.150816,-7.60673 2.01743,-2.01744 4.75366,-3.1508208 7.60676,-3.1508208 2.8531,0 5.5893,1.1333808 7.6067,3.1508208 2.0175,2.01743 3.1508,4.75366 3.1508,7.60673 0,2.8531 -1.1333,5.5893 -3.1508,7.6068 -2.0174,2.0174 -4.7536,3.1508 -7.6067,3.1508 -2.8531,0 -5.58933,-1.1334 -7.60676,-3.1508 C 2.10531,16.3698 0.971924,13.6336 0.971924,10.7805 Z M 11.7295,1.36764 C 9.95688,1.36774 8.22032,1.86836 6.71969,2.81188 5.21906,3.75541 4.01535,5.10349 3.2471,6.70096 2.47885,8.29844 2.17729,10.0804 2.37713,11.8417 c 0.19984,1.7613 0.89295,3.4304 1.99956,4.8151 0.95473,-1.5383 3.05649,-3.1869 7.35281,-3.1869 4.2963,0 6.3967,1.6472 7.3528,3.1869 1.1066,-1.3847 1.7997,-3.0538 1.9995,-4.8151 C 21.2817,10.0804 20.9801,8.29844 20.2119,6.70096 19.4436,5.10349 18.2399,3.75541 16.7393,2.81188 15.2386,1.86836 13.5021,1.36774 11.7295,1.36764 Z m 4.034,6.72357 c 0,1.06991 -0.425,2.09599 -1.1816,2.85249 -0.7565,0.7566 -1.7826,1.1816 -2.8525,1.1816 -1.0699,0 -2.09599,-0.425 -2.85253,-1.1816 C 8.12033,10.1872 7.69531,9.16112 7.69531,8.09121 c 0,-1.0699 0.42502,-2.09599 1.18156,-2.85253 0.75654,-0.75653 1.78263,-1.18155 2.85253,-1.18155 1.0699,0 2.096,0.42502 2.8525,1.18155 0.7566,0.75654 1.1816,1.78263 1.1816,2.85253 z'}");
            displayedMealNames = new HashSet<>();
            dietaryOptionSvgs = new HashMap<>();
            for (int row = 0; row < result.getRowCount(); row++) {
                LocalDate date = result.getValue(row, 0);
                String meal = result.getValue(row, 1);
                displayedMealNames.add(meal);
                String dietaryOptionCode = result.getValue(row, 2);
                String dietaryOptionName = result.getValue(row, 3);
                int count = result.getInt(row, 4, 0);
                attendanceCounts.add(date, meal, dietaryOptionCode, count);
                int dietaryOptionOrdinal = result.getInt(row, 5, 0);
                attendanceCounts.storeDietaryOptionOrder(dietaryOptionCode, dietaryOptionOrdinal);
                String svg = result.getValue(row, 6);
                if (svg != null)
                    attendanceCounts.storeDietaryOptionSvg(dietaryOptionCode, svg);
                if (dietaryOptionCode != null && dietaryOptionName != null && svg != null) {
                    String dietaryOptionKeyText = dietaryOptionName + " (" + dietaryOptionCode + ")";
                    dietaryOptionSvgs.put(dietaryOptionKeyText, svg);
                }
            }

            rebuildDayPanels();
        });
        reactiveQueryCall.setResultCacheEntry(LocalStorageCache.get().getCacheEntry("cache-kitchen-mealsCount"));
        reactiveQueryCall.start();
    }

}
