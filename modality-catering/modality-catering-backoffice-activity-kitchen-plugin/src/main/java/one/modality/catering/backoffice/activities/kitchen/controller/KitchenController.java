package one.modality.catering.backoffice.activities.kitchen.controller;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import one.modality.catering.backoffice.activities.kitchen.model.KitchenDisplayModel;
import one.modality.catering.backoffice.activities.kitchen.view.KitchenViewUI;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Main controller coordinating Kitchen activity lifecycle.
 * Manages state, delegates to sub-controllers, updates view.
 *
 * This controller follows the MVC pattern:
 * - Listens to property changes (organization, date range selection)
 * - Delegates data loading to KitchenDataController
 * - Delegates scheduled item operations to ScheduledItemController
 * - Updates KitchenView when model changes
 *
 * @author Claude Code (Refactored from KitchenActivity)
 */
public final class KitchenController {

    private static final java.util.Set<String> ALLOWED_MEALS = java.util.Set.of("Breakfast", "Lunch", "Dinner");

    private final DataSourceModel dataSourceModel;
    private final KitchenViewUI view;
    private final KitchenDataController dataController;

    private final ObjectProperty<LocalDate> startDateProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> endDateProperty = new SimpleObjectProperty<>();

    public KitchenController(DataSourceModel dataSourceModel) {
        this.dataSourceModel = dataSourceModel;
        this.view = new KitchenViewUI();
        this.dataController = new KitchenDataController(dataSourceModel);
    }

    /**
     * Initializes the controller by wiring event listeners and setting initial
     * state.
     */
    public void initialize() {
        Console.log("KitchenController initialize called");

        // Initialize Date Range (Today + 20 days)
        LocalDate today = LocalDate.now();
        startDateProperty.set(today);
        endDateProperty.set(today.plusDays(19)); // Total 20 days

        // Wire UI Controls
        view.getApplyButton().setOnAction(e -> applyDateFilter());
        view.getNextWeekButton().setOnAction(e -> setNextWeek());
        view.getPrevButton().setOnAction(e -> navigate(-20));
        view.getNextButton().setOnAction(e -> navigate(20));

        // Start the logic immediately after initialization
        Console.log("KitchenController: Calling startLogic from initialize()");
        startLogic();
    }

    private void applyDateFilter() {
        LocalDate start = view.getStartDatePicker().getValue();
        LocalDate end = view.getEndDatePicker().getValue();
        if (start != null && end != null && !start.isAfter(end)) {
            startDateProperty.set(start);
            endDateProperty.set(end);
        }
    }

    private void setNextWeek() {
        LocalDate today = LocalDate.now();
        LocalDate endOfWeek = today.plusDays(6);

        startDateProperty.set(today);
        endDateProperty.set(endOfWeek);
    }

    private void navigate(int days) {
        LocalDate currentStart = startDateProperty.get();
        LocalDate currentEnd = endDateProperty.get();
        long duration = ChronoUnit.DAYS.between(currentStart, currentEnd);

        // If navigating by "page", we might want to shift by the duration + 1
        // But the requirement says "Next days" button.
        // Let's assume it shifts by the current duration.

        long shift = (days > 0) ? (duration + 1) : -(duration + 1);

        startDateProperty.set(currentStart.plusDays(shift));
        endDateProperty.set(currentEnd.plusDays(shift));
    }

    private boolean logicStarted = false;

    /**
     * Starts the business logic (data loading).
     * Should be called after UI is built.
     * Idempotent - can be called multiple times safely.
     */
    public void startLogic() {
        if (logicStarted) {
            Console.log("KitchenController startLogic already called, skipping");
            return;
        }
        logicStarted = true;

        Console.log("KitchenController startLogic called");

        // Load data whenever the selected dates or organization changes
        FXProperties.runOnPropertiesChange(
                this::loadData,
                startDateProperty,
                endDateProperty,
                FXOrganizationId.organizationIdProperty());

        // Also load data immediately
        loadData();
    }

    /**
     * Called when activity resumes (comes into view).
     */
    public void onResume() {
        // No specific resume logic needed for now
    }

    /**
     * Called when activity pauses (goes out of view).
     */
    public void onPause() {
        // No specific pause logic needed for now
    }

    /**
     * Returns the root view node for the activity to display.
     */
    public Node getViewNode() {
        return view.getNode();
    }

    /**
     * Loads kitchen data and updates the view.
     */
    private void loadData() {
        // Show loading indicator
        view.showLoading();

        dataController.loadKitchenData(
                FXOrganizationId.getOrganizationId(),
                startDateProperty.get(),
                endDateProperty.get())
                .onSuccess(this::onDataLoaded)
                .onFailure(error -> {
                    Console.log("Failed to load kitchen data: " + error);
                    // Hide loading indicator on error
                    view.hideLoading();
                });
    }

    /**
     * Called when data loading completes successfully.
     * Updates the view with the new data.
     */
    private void onDataLoaded(KitchenDisplayModel displayModel) {
        updateView(displayModel);
    }

    /**
     * Reloads data (called after successful scheduled item generation).
     */
    private void reloadData() {
        loadData();
    }

    /**
     * Updates the view with the display model and current meal selection.
     */
    private void updateView(KitchenDisplayModel displayModel) {
        Console.log(
                "KitchenController.updateView called with displayModel=" + (displayModel != null ? "present" : "null"));

        if (displayModel != null) {
            Console.log("KitchenController: displayModel has " + displayModel.getAttendanceCounts().getDates().size()
                    + " dates");
            Console.log("KitchenController: displayModel meals: " + displayModel.getDisplayedMealNames());
        }

        // Update the view
        // We pass null for selectedMeals as we are no longer using the selection pane
        view.updateData(startDateProperty.get(), endDateProperty.get(), displayModel, null);
    }
}
