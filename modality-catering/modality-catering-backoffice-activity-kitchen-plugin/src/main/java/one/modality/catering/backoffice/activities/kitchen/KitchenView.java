package one.modality.catering.backoffice.activities.kitchen;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.luminance.LuminanceTheme;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.time.TimeUtil;
import dev.webfx.extras.time.layout.calendar.CalendarLayout;
import dev.webfx.extras.time.layout.node.TimeGridPane;
import dev.webfx.extras.time.layout.node.TimePane;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.kit.launcher.WebFxKitLauncher;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.uischeduler.UiScheduler;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.FontWeight;
import one.modality.base.client.gantt.fx.selection.FXGanttSelection;
import one.modality.base.shared.entities.Item;
import one.modality.catering.client.i18n.CateringI18nKeys;
import one.modality.crm.backoffice.organization.fx.FXOrganization;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main view for the Kitchen activity.
 * Displays a calendar with meal attendance counts organized by dietary options.
 *
 * @author Bruno Salmon
 */
public class KitchenView {

    private static final FontDef NO_DATA_MSG_FONT = FontDef.font(FontWeight.BOLD, 18);

    private final BorderPane view;
    private final KitchenDataManager dataManager;
    private final CalendarLayout<LocalDate, LocalDate> daysOfMonthLayout;
    private final Pane keyPane = new HBox();
    private final MealsSelectionPane mealsSelectionPane = new MealsSelectionPane();
    private final DietaryOptionKeyPanel dietaryOptionKeyPanel = new DietaryOptionKeyPanel();
    private final Map<LocalDate, AttendanceDayPanel> attendanceDayPanels = new HashMap<>();

    static {
        // Register renderers
        KitchenRenderers.setKitchenView(null); // Will be set in constructor
    }

    public KitchenView(KitchenDataManager dataManager) {
        this.dataManager = dataManager;

        // Set this view instance for renderers
        KitchenRenderers.setKitchenView(this);

        // Building the box (TimePane with just 1 row) that will show the days of the week (horizontally)
        CalendarLayout<DayOfWeek, DayOfWeek> daysOfWeekLayout = new CalendarLayout<>();
        daysOfWeekLayout.getChildren().setAll(TimeUtil.generateDaysOfWeek());
        daysOfWeekLayout.setChildFixedHeight(40);
        daysOfWeekLayout.setHSpacing(2);
        TimePane<DayOfWeek, DayOfWeek> daysOfWeekPane = new TimePane<>(daysOfWeekLayout, KitchenRenderers::createDayOfWeekNode);
        VBox.setMargin(daysOfWeekPane, new Insets(0, WebFxKitLauncher.getVerticalScrollbarExtraWidth(), 0, 0));

        // Building the box (TimeGridPane with several rows) that will show each day of the month (calendar layout)
        daysOfMonthLayout = new CalendarLayout<>();
        TimeGridPane<LocalDate, LocalDate> daysOfMonthPane = new TimeGridPane<>(daysOfMonthLayout, KitchenRenderers::createDateNode);
        LuminanceTheme.createPrimaryPanelFacet(daysOfMonthPane).style();

        // Building the container
        ScrollPane verticalScrollPane = dev.webfx.extras.util.control.Controls.createVerticalScrollPane(daysOfMonthPane);
        verticalScrollPane.setFitToWidth(true);

        view = new BorderPane();
        view.setCenter(verticalScrollPane);

        LuminanceTheme.createTopPanelFacet(daysOfWeekPane).setShadowed(true).style();
        view.setTop(daysOfWeekPane);

        LuminanceTheme.createBottomPanelFacet(keyPane).setShadowed(true).style();
        view.setBottom(keyPane);

        LuminanceTheme.createPrimaryPanelFacet(view).style();

        // Setup data bindings and listeners
        setupDataBindings();
    }

    private void setupDataBindings() {
        // Update calendar when selected month changes
        FXProperties.runOnPropertyChange(yearMonth -> refreshCalendar(), dataManager.selectedYearMonthProperty());

        // Update organization for meals selection
        FXProperties.runNowAndOnPropertyChange(mealsSelectionPane::setOrganization, FXOrganization.organizationProperty());

        // Rebuild day panels when selected meals change
        mealsSelectionPane.selectedItemsObservableList().addListener((ListChangeListener<Item>) c -> rebuildDayPanels());

        // Rebuild day panels when attendance data changes
        FXProperties.runOnPropertyChange(counts -> rebuildDayPanels(), dataManager.attendanceCountsProperty());

        // Sync organization ID to data manager
        FXProperties.runOnPropertyChange(orgId -> dataManager.setOrganizationId(orgId), FXOrganizationId.organizationIdProperty());

        // Handle gantt selection changes
        FXProperties.runNowAndOnPropertyChange(ganttSelectedObject -> {
            if (ganttSelectedObject instanceof YearMonth)
                dataManager.setSelectedYearMonth((YearMonth) ganttSelectedObject);
        }, FXGanttSelection.ganttSelectedObjectProperty());

        // Set initial selection if not set
        if (dataManager.getSelectedYearMonth() == null)
            dataManager.setSelectedYearMonth(YearMonth.now());
    }

    private void refreshCalendar() {
        YearMonth selectedYearMonth = dataManager.getSelectedYearMonth();
        if (selectedYearMonth == null)
            return;

        UiScheduler.runInUiThread(() ->
                daysOfMonthLayout.getChildren().setAll(TimeUtil.generateMonthDates(selectedYearMonth))
        );
    }

    private void rebuildDayPanels() {
        AttendanceCounts attendanceCounts = dataManager.getAttendanceCounts();
        if (attendanceCounts == null)
            return;

        AbbreviationGenerator abbreviationGenerator = mealsSelectionPane.getAbbreviationGenerator();
        List<Item> displayedMeals = mealsSelectionPane.selectedItemsObservableList();

        attendanceDayPanels.clear();
        for (LocalDate date : attendanceCounts.getDates()) {
            AttendanceDayPanel dayPanel = KitchenRenderers.createAttendanceDayPanel(
                    date,
                    attendanceCounts,
                    abbreviationGenerator
            );
            if (dayPanel != null) {
                attendanceDayPanels.put(date, dayPanel);
            }
        }

        refreshCalendar();
        updateKeyPane();
    }

    private void updateKeyPane() {
        Map<String, String> dietaryOptionSvgs = dataManager.getDietaryOptionSvgs();
        if (dietaryOptionSvgs == null)
            dietaryOptionSvgs = new HashMap<>();

        Map<String, String> finalDietaryOptionSvgs = dietaryOptionSvgs;
        Platform.runLater(() -> {
            if (finalDietaryOptionSvgs.isEmpty()) {
                Label noDataLabel = I18nControls.newLabel(CateringI18nKeys.NoMealsData);
                TextTheme.createPrimaryTextFacet(noDataLabel).requestedFont(NO_DATA_MSG_FONT).style();
                noDataLabel.setWrapText(true);
                keyPane.getChildren().setAll(noDataLabel);
            } else {
                keyPane.getChildren().setAll(
                        Layouts.createHGrowable(),
                        mealsSelectionPane,
                        Layouts.createHGrowable(),
                        dietaryOptionKeyPanel,
                        Layouts.createHGrowable()
                );
                dietaryOptionKeyPanel.populate(finalDietaryOptionSvgs);
            }

            var displayedMealNames = dataManager.getDisplayedMealNames();
            if (displayedMealNames != null) {
                mealsSelectionPane.setDisplayedMealNames(displayedMealNames);
            }
        });
    }

    /**
     * Helper method for renderers to get the attendance day panel for a specific date.
     */
    public AttendanceDayPanel getAttendanceDayPanelForDate(LocalDate date) {
        return attendanceDayPanels.get(date);
    }

    /**
     * Helper method for renderers to get the list of displayed meals.
     */
    public List<Item> getDisplayedMeals() {
        return mealsSelectionPane.selectedItemsObservableList();
    }

    public Node getView() {
        return view;
    }

    public KitchenDataManager getDataManager() {
        return dataManager;
    }
}
