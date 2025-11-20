package one.modality.catering.backoffice.activities.kitchen.controller;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import one.modality.base.shared.entities.Organization;
import one.modality.catering.backoffice.activities.kitchen.model.KitchenDisplayModel;
import one.modality.catering.backoffice.activities.kitchen.view.KitchenViewUI;
import one.modality.crm.backoffice.organization.fx.FXOrganization;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;

import java.time.LocalDate;

/**
 * Main controller coordinating Kitchen activity lifecycle.
 * Manages state, delegates to sub-controllers, updates view.
 *
 * <p>This controller follows the MVC pattern:
 * <ul>
 *   <li>Listens to property changes (organization, date range selection)</li>
 *   <li>Delegates data loading to KitchenDataController</li>
 *   <li>Updates KitchenViewUI when model changes</li>
 *   <li>Manages date range filtering and navigation</li>
 * </ul>
 *
 * @author Claude Code (Refactored from KitchenActivity)
 */
public final class KitchenController {

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

        // Start the logic immediately after initialization
        Console.log("KitchenController: Calling startLogic from initialize()");
        startLogic();
    }

    private void applyDateFilter() {
        LocalDate start = view.getStartDateField().getDate();
        LocalDate end = view.getEndDateField().getDate();
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

        // Update organization label whenever organization changes
        FXProperties.runOnPropertiesChange(
                this::updateOrganizationLabel,
                FXOrganization.organizationProperty());

        // Also load data and update organization label immediately
        loadData();
        updateOrganizationLabel();
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

    /**
     * Updates the organization label in the view.
     */
    private void updateOrganizationLabel() {
        Organization organization = FXOrganization.getOrganization();
        if (organization != null) {
            String organizationName = organization.getName();
            view.setOrganizationName(organizationName);
        } else {
            view.setOrganizationName(null);
        }
    }
}
