package one.modality.catering.backoffice.activities.kitchen;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.luminance.LuminanceTheme;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.time.TimeUtil;
import dev.webfx.extras.time.layout.calendar.CalendarLayout;
import dev.webfx.extras.time.layout.node.TimeGridPane;
import dev.webfx.extras.time.layout.node.TimePane;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.kit.launcher.WebFxKitLauncher;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.db.query.QueryArgument;
import dev.webfx.stack.db.query.QueryArgumentBuilder;
import dev.webfx.stack.db.submit.SubmitArgument;
import dev.webfx.stack.db.submit.SubmitArgumentBuilder;
import dev.webfx.stack.db.submit.SubmitService;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityList;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.EntityStoreQuery;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.reactive.call.query.ReactiveQueryCall;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContextMixin;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.FontWeight;
import one.modality.base.client.activity.ModalityButtonFactoryMixin;
import one.modality.base.client.gantt.fx.selection.FXGanttSelection;
import one.modality.base.client.gantt.fx.visibility.FXGanttVisibility;
import one.modality.base.client.time.theme.TimeFacet;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.Site;
import one.modality.base.shared.entities.Timeline;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.catering.client.i18n.CateringI18nKeys;
import one.modality.crm.backoffice.organization.fx.FXOrganization;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private final Button generateScheduledItemsButton = new Button("Generate Scheduled Items");
    private final Button generateScheduledItemsSqlButton = new Button("Generate Scheduled Items (SQL)");

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
        ScrollPane verticalScrollPane = Controls.createVerticalScrollPane(daysOfMonthPane);
        verticalScrollPane.setFitToWidth(true);

        BorderPane container = new BorderPane();
        container.setCenter(verticalScrollPane);

        //VBox top = new VBox(2, monthsBox, daysOfWeekPane);
        LuminanceTheme.createTopPanelFacet(daysOfWeekPane).setShadowed(true).style();
        container.setTop(daysOfWeekPane);

        // Create bottom panel with buttons and key pane
        VBox bottomPanel = new VBox(5);
        HBox buttonContainer = new HBox(10);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setPadding(new Insets(10));
        buttonContainer.getChildren().addAll(generateScheduledItemsButton, generateScheduledItemsSqlButton);
        bottomPanel.getChildren().addAll(buttonContainer, keyPane);

        // Wire buttons to generation methods
        generateScheduledItemsButton.setOnAction(event -> generateMissingScheduledItems());
        generateScheduledItemsSqlButton.setOnAction(event -> generateScheduledItemsFromTimelines());

        LuminanceTheme.createBottomPanelFacet(bottomPanel).setShadowed(true).style();
        container.setBottom(bottomPanel);

        LuminanceTheme.createPrimaryPanelFacet(container).style(); // To show the same background if the scroll pane doesn't cover the whole area

        // Updating the query each time the selected month or organization change (this will make the reactive call sending the query to the server)
        FXProperties.runOnPropertiesChange(this::updateQueryArgument, selectedYearMonthProperty, FXOrganizationId.organizationIdProperty());
        FXProperties.runNowAndOnPropertyChange(mealsSelectionPane::setOrganization, FXOrganization.organizationProperty());
        mealsSelectionPane.selectedItemsObservableList().addListener((ListChangeListener<Item>) c -> rebuildDayPanels());

        // Setting the initial selection = this month
        FXProperties.runNowAndOnPropertyChange(ganttSelectedObject -> {
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
                keyPane.getChildren().setAll(Layouts.createHGrowable(), mealsSelectionPane, Layouts.createHGrowable(), dietaryOptionKeyPanel, Layouts.createHGrowable());
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

    private static final String MEALS_COUNT_SQL = // language=SQL
        """
            select si.date, i.name, di.code, di.name, count(*), di.ord, di.graphic
             from attendance a
              join scheduled_item si on si.id = a.scheduled_item_id
              join document_line dl on dl.id=a.document_line_id
              join site s on s.id=si.site_id
              join item i on i.id=si.item_id
              join item_family f on f.id=i.family_id
              , ( select i.id,i.code,i.name,i.ord,i.graphic from item i join item_family f on f.id=i.family_id where i.organization_id = $1 and f.code='diet'
                union
                select * from (values (-1, 'Total', null, 10001, null), (-2, '?', null, 10000, null)) vitem(id, code, ord)
                ) di
             where not dl.cancelled
              and s.organization_id = $1
              and f.code = 'meals'
              and si.date between $2 and $3
              and case when di.id=-1 then true
                   when di.id=-2 then not exists(select * from document_line dl2 join item i2 on i2.id=dl2.item_id join item_family f2 on f2.id=i2.family_id where dl2.document_id=dl.document_id and not dl2.cancelled and f2.code='diet')
                   else exists(select * from document_line dl2 where dl2.document_id=dl.document_id and not dl2.cancelled and dl2.item_id=di.id)
                    end
             group by si.date, i.name, di.code, di.name, i.ord, di.ord, di.graphic
             order by si.date, i.ord, di.ord;""";

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
        FXProperties.runOnPropertyChange(result -> {
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
        }, reactiveQueryCall.resultProperty());
        reactiveQueryCall.setResultCacheEntry("modality/catering/kitchen/year-month-meals-count");
        reactiveQueryCall.start();
    }

    /**
     * Generates missing scheduledItems for all meal items in the selected month.
     * This method:
     * 1. Queries for existing scheduledItems in the selected month
     * 2. Queries for all meal items (family.code = 'meals')
     * 3. Queries for the main site for the organization
     * 4. Creates scheduledItems for any missing (item, date) combinations
     * 5. Submits changes to the database
     */
    private void generateMissingScheduledItems() {
        YearMonth selectedYearMonth = selectedYearMonthProperty.get();
        EntityId organizationId = FXOrganizationId.getOrganizationId();

        if (selectedYearMonth == null || organizationId == null) {
            Console.log("Cannot generate scheduled items: month or organization not selected");
            return;
        }

        LocalDate startDate = selectedYearMonth.atDay(1);
        LocalDate endDate = selectedYearMonth.atEndOfMonth();

        // Disable button during generation
        generateScheduledItemsButton.setDisable(true);

        // Create entity store for queries
        EntityStore entityStore = EntityStore.create(getDataSourceModel());

        // Query for existing scheduled items, meal items, and main site
        entityStore.executeQueryBatch(
                // Query 0: Existing scheduled items for the month
                new EntityStoreQuery("select id, item, date from ScheduledItem si where site.organization=? and si.date between ? and ? and item.family.code=?",
                        organizationId.getPrimaryKey(), startDate, endDate, KnownItemFamily.MEALS.getCode()),

                // Query 1: All meal items for the organization
                new EntityStoreQuery("select id, name from Item where organization=? and family.code=? order by name",
                        organizationId.getPrimaryKey(), KnownItemFamily.MEALS.getCode()),

                // Query 2: Main site for the organization
                new EntityStoreQuery("select id, name from Site where organization=? and main=true limit 1",
                        organizationId.getPrimaryKey())
        ).onFailure(error -> {
            Console.log("Error querying data: " + error.getMessage());
            generateScheduledItemsButton.setDisable(false);
        }).onSuccess(results -> {
            EntityList<ScheduledItem> existingScheduledItems = results[0];
            EntityList<Item> mealItems = results[1];
            EntityList<Site> sites = results[2];

            if (sites.isEmpty()) {
                Console.log("No main site found for organization");
                generateScheduledItemsButton.setDisable(false);
                return;
            }

            if (mealItems.isEmpty()) {
                Console.log("No meal items found for organization");
                generateScheduledItemsButton.setDisable(false);
                return;
            }

            Site mainSite = sites.get(0);

            // Build a set of existing (item, date) combinations
            Set<String> existingCombinations = new HashSet<>();
            for (ScheduledItem si : existingScheduledItems) {
                String key = si.getItem().getPrimaryKey() + "|" + si.getDate();
                existingCombinations.add(key);
            }

            // Create update store for insertions
            UpdateStore updateStore = UpdateStore.create(getDataSourceModel());
            int insertCount = 0;

            // For each meal item and date in the month, create if missing
            for (Item mealItem : mealItems) {
                for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                    String key = mealItem.getPrimaryKey() + "|" + date;
                    if (!existingCombinations.contains(key)) {
                        ScheduledItem newScheduledItem = updateStore.insertEntity(ScheduledItem.class);
                        newScheduledItem.setItem(mealItem);
                        newScheduledItem.setDate(date);
                        newScheduledItem.setSite(mainSite);
                        insertCount++;
                    }
                }
            }

            final int finalInsertCount = insertCount;
            if (insertCount > 0) {
                Console.log("Creating " + insertCount + " scheduled items...");
                updateStore.submitChanges()
                        .onFailure(error -> {
                            Console.log("Error creating scheduled items: " + error.getMessage());
                            generateScheduledItemsButton.setDisable(false);
                        })
                        .onSuccess(result -> {
                            Console.log("Successfully created " + finalInsertCount + " scheduled items");
                            generateScheduledItemsButton.setDisable(false);
                            // Refresh the data by re-triggering the query
                            updateQueryArgument();
                        });
            } else {
                Console.log("All scheduled items already exist for the selected month");
                generateScheduledItemsButton.setDisable(false);
            }
        });
    }

    /**
     * Generates scheduledItems for the selected month using SQL INSERT statements based on timelines.
     * This method:
     * 1. Queries for the global site (event_id is null) for the organization
     * 2. Queries for timelines associated with that site
     * 3. For each daily timeline, executes SQL INSERT to create scheduled_items
     * 4. Executes SQL INSERT to create scheduled_resources for accommodation
     */
    private void generateScheduledItemsFromTimelines() {
        YearMonth selectedYearMonth = selectedYearMonthProperty.get();
        EntityId organizationId = FXOrganizationId.getOrganizationId();

        if (selectedYearMonth == null || organizationId == null) {
            Console.log("Cannot generate scheduled items: month or organization not selected");
            return;
        }

        LocalDate startDate = selectedYearMonth.atDay(1);
        LocalDate endDate = selectedYearMonth.atEndOfMonth();

        // Disable button during generation
        generateScheduledItemsSqlButton.setDisable(true);

        // Create entity store for queries
        EntityStore entityStore = EntityStore.create(getDataSourceModel());

        Console.log("Querying global site and timelines for organization " + organizationId.getPrimaryKey());

        // Query for global site and timelines
        entityStore.executeQueryBatch(
                // Query 0: Main global site (event is null and main is true) for the organization
                new EntityStoreQuery("select id, name from Site where event=null and main and organization=?",
                        organizationId),
                // Query 1: Timelines for the main global site
                new EntityStoreQuery("select id, site, item, startTime, endTime, itemFamily from Timeline where site in (select id from Site where event is null and main=true and organization=?)",
                        organizationId)
        ).onFailure(error -> {
            Console.log("Error querying global site and timelines: " + error.getMessage());
            generateScheduledItemsSqlButton.setDisable(false);
        }).onSuccess(results -> {
            EntityList<Site> globalSites = results[0];
            EntityList<Timeline> timelines = results[1];

            if (globalSites.isEmpty()) {
                Console.log("No global site found for organization");
                generateScheduledItemsSqlButton.setDisable(false);
                return;
            }

            if (timelines.isEmpty()) {
                Console.log("No timelines found for global site");
                generateScheduledItemsSqlButton.setDisable(false);
                return;
            }

            Site globalSite = globalSites.get(0);
            Console.log("Found global site: " + globalSite.getName() + " (id: " + globalSite.getPrimaryKey() + ")");
            Console.log("Found " + timelines.size() + " timelines");

            // Process timelines - for now, focus on daily items with item_id specified (meals)
            Timeline accommodationTimeline = null;
            List<Timeline> mealTimelines = new ArrayList<>();

            for (Timeline timeline : timelines) {
                if (timeline.getItem() != null) {
                    // Timeline has a specific item - likely a meal
                    mealTimelines.add(timeline);
                    Console.log("  Meal timeline: item_id=" + timeline.getItem().getPrimaryKey() +
                               ", start=" + timeline.getStartTime() + ", end=" + timeline.getEndTime());
                } else if (timeline.getItemFamily() != null) {
                    // Timeline has only item_family - likely accommodation
                    accommodationTimeline = timeline;
                    Console.log("  Accommodation timeline: family_id=" + timeline.getItemFamily().getPrimaryKey() +
                               ", start=" + timeline.getStartTime() + ", end=" + timeline.getEndTime());
                }
            }

            // Generate scheduled_items for meal timelines
            if (!mealTimelines.isEmpty()) {
                generateScheduledItemsForMeals(mealTimelines, startDate, endDate, globalSite);
            } else {
                Console.log("No meal timelines found");
                generateScheduledItemsSqlButton.setDisable(false);
            }
        });
    }

    /**
     * Generates scheduled_items for meal timelines using SQL INSERT.
     */
    private void generateScheduledItemsForMeals(List<Timeline> mealTimelines, LocalDate startDate, LocalDate endDate, Site globalSite) {
        Console.log("Generating scheduled items for " + mealTimelines.size() + " meal timelines from " + startDate + " to " + endDate);

        // Build SQL INSERT for each timeline
        // insert into scheduled_item (date, timeline_id, event_id, site_id, item_id, start_time, end_time, resource)
        //     select generate_series(:first_date, :last_date, '1 day')::date as day, id, null, site_id, item_id, start_time, end_time, false
        //     from timeline
        //     where id = :timeline_id;

        Timeline firstTimeline = mealTimelines.get(0);

        SubmitArgument submitArgument = new SubmitArgumentBuilder()
                .setDataSourceId(getDataSourceModel().getDataSourceId())
                .setStatement(
                    "insert into scheduled_item (date, timeline_id, event_id, site_id, item_id, start_time, end_time, resource) " +
                    "select generate_series(?::date, ?::date, '1 day')::date as day, id, null, site_id, item_id, start_time, end_time, false " +
                    "from timeline " +
                    "where id = ?"
                )
                .setParameters(startDate, endDate, firstTimeline.getPrimaryKey())
                .build();

        SubmitService.executeSubmit(submitArgument)
                .onFailure(error -> {
                    Console.log("Error creating scheduled items for timeline " + firstTimeline.getPrimaryKey() + ": " + error.getMessage());
                    // Continue with next timeline even if this one fails
                    processNextMealTimeline(mealTimelines, 1, startDate, endDate, globalSite);
                })
                .onSuccess(result -> {
                    Console.log("Successfully created scheduled items for timeline " + firstTimeline.getPrimaryKey());
                    // Process next timeline
                    processNextMealTimeline(mealTimelines, 1, startDate, endDate, globalSite);
                });
    }

    /**
     * Recursively process meal timelines one by one.
     */
    private void processNextMealTimeline(List<Timeline> mealTimelines, int index, LocalDate startDate, LocalDate endDate, Site globalSite) {
        if (index >= mealTimelines.size()) {
            // All meal timelines processed, now generate scheduled_resources
            Console.log("All meal timelines processed, generating scheduled resources...");
            generateScheduledResources(startDate, endDate, globalSite);
            return;
        }

        Timeline timeline = mealTimelines.get(index);

        SubmitArgument submitArgument = new SubmitArgumentBuilder()
                .setDataSourceId(getDataSourceModel().getDataSourceId())
                .setStatement(
                    "insert into scheduled_item (date, timeline_id, event_id, site_id, item_id, start_time, end_time, resource) " +
                    "select generate_series(?::date, ?::date, '1 day')::date as day, id, null, site_id, item_id, start_time, end_time, false " +
                    "from timeline " +
                    "where id = ?"
                )
                .setParameters(startDate, endDate, timeline.getPrimaryKey())
                .build();

        SubmitService.executeSubmit(submitArgument)
                .onFailure(error -> {
                    Console.log("Error creating scheduled items for timeline " + timeline.getPrimaryKey() + ": " + error.getMessage());
                    // Continue with next timeline even if this one fails
                    processNextMealTimeline(mealTimelines, index + 1, startDate, endDate, globalSite);
                })
                .onSuccess(result -> {
                    Console.log("Successfully created scheduled items for timeline " + timeline.getPrimaryKey());
                    // Process next timeline
                    processNextMealTimeline(mealTimelines, index + 1, startDate, endDate, globalSite);
                });
    }

    /**
     * Generates scheduled_resources using SQL INSERT.
     */
    private void generateScheduledResources(LocalDate startDate, LocalDate endDate, Site globalSite) {
        Console.log("Generating scheduled resources for global site " + globalSite.getPrimaryKey());

        // insert into scheduled_resource (date, configuration_id, scheduled_item_id, max, online)
        //     select generate_series(:first_date, :last_date, '1 day')::date as day, rc.id,
        //            (select si.id from scheduled_item si where si.date=day and si.site_id=r.site_id and si.item_id=rc.item_id),
        //            rc.max, rc.online
        //     from resource_configuration rc
        //     join resource r on r.id=rc.resource_id
        //     join item i on i.id=rc.item_id
        //     join site s on s.id=r.site_id
        //     where s.id = :global_site_id and (rc.end_date is null or rc.end_date > :first_date)

        SubmitArgument submitArgument = new SubmitArgumentBuilder()
                .setDataSourceId(getDataSourceModel().getDataSourceId())
                .setStatement(
                    "insert into scheduled_resource (date, configuration_id, scheduled_item_id, max, online) " +
                    "select generate_series(?::date, ?::date, '1 day')::date as day, rc.id, " +
                    "       (select si.id from scheduled_item si where si.date=day and si.site_id=r.site_id and si.item_id=rc.item_id), " +
                    "       rc.max, rc.online " +
                    "from resource_configuration rc " +
                    "join resource r on r.id=rc.resource_id " +
                    "join item i on i.id=rc.item_id " +
                    "join site s on s.id=r.site_id " +
                    "where s.id = ? and (rc.end_date is null or rc.end_date > ?)"
                )
                .setParameters(startDate, endDate, globalSite.getPrimaryKey(), startDate)
                .build();

        SubmitService.executeSubmit(submitArgument)
                .onFailure(error -> {
                    Console.log("Error creating scheduled resources: " + error.getMessage());
                    generateScheduledItemsSqlButton.setDisable(false);
                })
                .onSuccess(result -> {
                    Console.log("Successfully created scheduled resources");
                    generateScheduledItemsSqlButton.setDisable(false);
                    // Refresh the data by re-triggering the query
                    updateQueryArgument();
                });
    }

}
