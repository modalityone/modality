package one.modality.catering.backoffice.activities.kitchen.controller;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import one.modality.base.shared.entities.Organization;
import one.modality.catering.backoffice.activities.kitchen.model.KitchenDisplayModel;
import one.modality.catering.backoffice.activities.kitchen.view.AttendeeDetailsDialog;
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

    private final KitchenViewUI view;
    private final KitchenDataController dataController;

    private final ObjectProperty<LocalDate> startDateProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> endDateProperty = new SimpleObjectProperty<>();

    public KitchenController(DataSourceModel dataSourceModel) {
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

        // Wire cell click handler for showing attendee details
        view.setCellClickHandler(this::onCellClicked);

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
            Console.log("KitchenController: displayModel has " + displayModel.attendanceCounts().getDates().size()
                    + " dates");
            Console.log("KitchenController: displayModel meals: " + displayModel.displayedMealNames());
        }

        // Update the view
        // We pass null for selectedMeals as we are no longer using the selection pane
        view.updateData(startDateProperty.get(), endDateProperty.get(), displayModel);
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

    /**
     * Handles cell clicks to show attendee details dialog.
     */
    private void onCellClicked(LocalDate date, String meal, String dietaryOptionCode, int count) {
        Console.log("Cell clicked: " + meal + " / " + dietaryOptionCode + " on " + date + " (" + count + " people)");

        // Check if this is a "Total" cell click (all diets)
        if ("Total".equals(dietaryOptionCode)) {
            handleTotalCellClick(date, meal, count);
        } else {
            handleDietCellClick(date, meal, dietaryOptionCode, count);
        }
    }

    /**
     * Handles click on a specific dietary option cell.
     */
    private void handleDietCellClick(LocalDate date, String meal, String dietaryOptionCode, int count) {
        // Get dietary option name from current display model
        KitchenDisplayModel displayModel = dataController.getCurrentDisplayModel();

        // Double-check the count from the model
        int modelCount = displayModel.attendanceCounts().getCount(date, meal, dietaryOptionCode);
        Console.log("Count from AttendanceCounts model: " + modelCount);
        if (modelCount != count) {
            Console.log("WARNING: Count mismatch! Cell shows " + count + " but model has " + modelCount);
        }

        String dietaryOptionNameFromModel = displayModel.attendanceCounts().getNameForDietaryOption(dietaryOptionCode);
        final String dietaryOptionName = dietaryOptionNameFromModel != null ? dietaryOptionNameFromModel : dietaryOptionCode;

        Console.log("Loading attendee details...");

        // Show loading dialog
        dev.webfx.extras.util.dialog.DialogCallback loadingDialog = showLoadingDialog();

        // Load attendee details
        dataController.loadAttendeeDetails(
                FXOrganizationId.getOrganizationId(),
                date,
                meal,
                dietaryOptionCode)
                .onSuccess(attendees -> {
                    Console.log("Attendees loaded successfully: " + attendees.size() + " people");

                    // Run on UI thread
                    UiScheduler.runInUiThread(() -> {
                        Console.log("Showing dialog...");

                        // Close loading dialog
                        loadingDialog.closeDialog();

                        // Show dialog with attendees
                        try {
                            AttendeeDetailsDialog.showAttendeeDialog(
                                    view.getOverlayPane(),
                                    date,
                                    meal,
                                    dietaryOptionName,
                                    attendees,
                                    count
                            );
                            Console.log("Dialog shown");
                        } catch (Exception e) {
                            Console.log("Error showing dialog: " + e);
                            e.printStackTrace();
                        }
                    });
                })
                .onFailure(error -> {
                    Console.log("Failed to load attendee details: " + error);
                    if (error != null) {
                        error.printStackTrace();
                    }
                    // Close loading dialog on error
                    UiScheduler.runInUiThread(loadingDialog::closeDialog);
                });
    }

    /**
     * Handles click on Total cell (all dietary options).
     */
    private void handleTotalCellClick(LocalDate date, String meal, int count) {
        Console.log("Loading all attendees grouped by diet...");

        // Show loading dialog
        dev.webfx.extras.util.dialog.DialogCallback loadingDialog = showLoadingDialog();

        // Load attendees grouped by dietary option
        dataController.loadAllAttendeesGroupedByDiet(
                FXOrganizationId.getOrganizationId(),
                date,
                meal)
                .onSuccess(result -> {
                    Console.log("Attendees loaded successfully, grouped by diet");

                    // Run on UI thread
                    UiScheduler.runInUiThread(() -> {
                        Console.log("Showing total attendees dialog...");

                        // Close loading dialog
                        loadingDialog.closeDialog();

                        try {
                            AttendeeDetailsDialog.showTotalAttendeeDialog(
                                    view.getOverlayPane(),
                                    date,
                                    meal,
                                    result.attendeesByDiet,
                                    result.dietaryOptionNames,
                                    count
                            );
                            Console.log("Dialog shown");
                        } catch (Exception e) {
                            Console.log("Error showing dialog: " + e);
                            e.printStackTrace();
                        }
                    });
                })
                .onFailure(error -> {
                    Console.log("Failed to load attendees: " + error);
                    if (error != null) {
                        error.printStackTrace();
                    }
                    // Close loading dialog on error
                    UiScheduler.runInUiThread(loadingDialog::closeDialog);
                });
    }

    /**
     * Shows a loading dialog with a progress indicator.
     */
    private dev.webfx.extras.util.dialog.DialogCallback showLoadingDialog() {
        javafx.scene.control.ProgressIndicator progressIndicator = new javafx.scene.control.ProgressIndicator();
        progressIndicator.setMaxSize(60, 60);

        javafx.scene.layout.VBox loadingContent = new javafx.scene.layout.VBox(15);
        loadingContent.setAlignment(javafx.geometry.Pos.CENTER);
        loadingContent.setPadding(new javafx.geometry.Insets(40));
        loadingContent.getChildren().add(progressIndicator);

        javafx.scene.control.Label loadingLabel = new javafx.scene.control.Label("Loading...");
        loadingLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        loadingContent.getChildren().add(loadingLabel);

        return dev.webfx.extras.util.dialog.DialogUtil.showModalNodeInGoldLayout(
                loadingContent,
                view.getOverlayPane(),
                20,
                20
        );
    }
}
