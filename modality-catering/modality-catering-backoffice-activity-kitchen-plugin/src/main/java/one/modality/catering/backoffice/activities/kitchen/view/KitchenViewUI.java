package one.modality.catering.backoffice.activities.kitchen.view;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.theme.luminance.LuminanceTheme;
import dev.webfx.platform.uischeduler.UiScheduler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import one.modality.base.shared.entities.Item;
import one.modality.catering.backoffice.activities.kitchen.model.KitchenDisplayModel;
import one.modality.catering.client.i18n.CateringI18nKeys;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Main view component for Kitchen activity.
 * Pure JavaFX UI construction without business logic.
 * Composites table view, bottom panel, and meal selection components.
 *
 * @author Claude Code (Refactored from KitchenActivity)
 */
public final class KitchenViewUI {

    private final BorderPane container;
    private final KitchenTableView tableView;
    private final Label dateRangeDisplay;
    private final DatePicker startDatePicker;
    private final DatePicker endDatePicker;
    private final Button applyButton;
    private final Button nextWeekButton;
    private final Button prevButton;
    private final Button nextButton;
    private final ProgressIndicator progressIndicator;
    private final StackPane contentStack;

    public KitchenViewUI() {
        dev.webfx.platform.console.Console.log("KitchenViewUI constructor called");

        // Create sub-components
        tableView = new KitchenTableView();

        // Create Top Panel (Filter Bar)
        dateRangeDisplay = new Label();
        dateRangeDisplay.getStyleClass().add("kitchen-date-range-display");

        Label filterLabel = I18nControls.newLabel(CateringI18nKeys.Filter);
        filterLabel.getStyleClass().add("kitchen-filter-label");

        startDatePicker = new DatePicker();
        startDatePicker.setPrefWidth(120);

        Label arrowLabel = new Label("→");
        arrowLabel.getStyleClass().add("kitchen-filter-label");

        endDatePicker = new DatePicker();
        endDatePicker.setPrefWidth(120);

        applyButton = I18nControls.newButton(CateringI18nKeys.Apply);
        applyButton.getStyleClass().add("btn-primary");

        nextWeekButton = I18nControls.newButton(CateringI18nKeys.NextWeek);
        nextWeekButton.getStyleClass().add("btn-secondary");

        HBox filterBox = new HBox(10, filterLabel, startDatePicker, arrowLabel, endDatePicker, applyButton,
                nextWeekButton);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        filterBox.setPadding(new Insets(6, 12, 6, 12));
        filterBox.getStyleClass().add("kitchen-filter-box");

        prevButton = new Button("‹");
        prevButton.getStyleClass().add("btn-primary");
        prevButton.setMinSize(45, 45);
        prevButton.setPrefSize(45, 45);

        nextButton = new Button("›");
        nextButton.getStyleClass().add("btn-primary");
        nextButton.setMinSize(45, 45);
        nextButton.setPrefSize(45, 45);

        HBox navControls = new HBox(15, prevButton, nextButton);
        navControls.setAlignment(Pos.CENTER_RIGHT);

        HBox topHeader = new HBox(20);
        topHeader.setAlignment(Pos.CENTER_LEFT);
        topHeader.setPadding(new Insets(15, 20, 15, 20));
        topHeader.getStyleClass().add("kitchen-top-header");

        Label showingLabel = I18nControls.newLabel(CateringI18nKeys.Showing);
        HBox leftHeader = new HBox(15, showingLabel, dateRangeDisplay, filterBox);
        leftHeader.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(leftHeader, javafx.scene.layout.Priority.ALWAYS);

        topHeader.getChildren().addAll(leftHeader, navControls);

        // Create progress indicator
        progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(80, 80);
        progressIndicator.setVisible(false);

        // Create a stack to overlay progress indicator on table
        contentStack = new StackPane();
        contentStack.getChildren().addAll(tableView.getNode(), progressIndicator);
        StackPane.setAlignment(progressIndicator, Pos.CENTER);

        // Create main container
        container = new BorderPane();
        container.setTop(topHeader);
        container.setCenter(contentStack);
        BorderPane.setMargin(contentStack, new Insets(20));

        // Apply styling to show the same background if the scroll pane doesn't cover
        // the whole area
        LuminanceTheme.createPrimaryPanelFacet(container).style();
    }

    /**
     * Returns the root node of this view.
     */
    public Node getNode() {
        return container;
    }

    /**
     * Shows the loading indicator and hides the table.
     */
    public void showLoading() {
        UiScheduler.runInUiThread(() -> {
            progressIndicator.setVisible(true);
            tableView.getNode().setOpacity(0.3);
        });
    }

    /**
     * Hides the loading indicator and shows the table.
     */
    public void hideLoading() {
        UiScheduler.runInUiThread(() -> {
            progressIndicator.setVisible(false);
            tableView.getNode().setOpacity(1.0);
        });
    }

    /**
     * Updates the calendar display with new data.
     */
    public void updateData(LocalDate startDate, LocalDate endDate, KitchenDisplayModel displayModel,
            List<Item> selectedMeals) {
        UiScheduler.runInUiThread(() -> {
            dev.webfx.platform.console.Console.log("KitchenViewUI.updateData called");
            dev.webfx.platform.console.Console
                    .log("KitchenViewUI: displayModel is " + (displayModel != null ? "present" : "null"));

            // Update Date Range Display
            if (startDate != null && endDate != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d");
                DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
                dateRangeDisplay.setText(startDate.format(formatter) + " - " + endDate.format(yearFormatter));

                // Update pickers if not focused (to avoid interrupting user typing)
                if (!startDatePicker.isFocused())
                    startDatePicker.setValue(startDate);
                if (!endDatePicker.isFocused())
                    endDatePicker.setValue(endDate);
            }

            // Update Table
            if (displayModel != null) {
                try {
                    dev.webfx.platform.console.Console.log("KitchenViewUI: Calling tableView.update");
                    tableView.update(displayModel.getAttendanceCounts(), startDate, endDate);
                    dev.webfx.platform.console.Console.log("KitchenViewUI: tableView.update returned");

                    // Hide loading indicator after table is fully rendered
                    // Use runLater to execute after the scene graph has been updated and rendered
                    javafx.application.Platform.runLater(() -> hideLoading());
                } catch (Exception e) {
                    dev.webfx.platform.console.Console.log("KitchenViewUI: Exception in tableView.update: " + e);
                    e.printStackTrace();
                    hideLoading();
                }
            } else {
                dev.webfx.platform.console.Console.log("KitchenViewUI: displayModel is null, skipping update");
                hideLoading();
            }
        });
    }

    public DatePicker getStartDatePicker() {
        return startDatePicker;
    }

    public DatePicker getEndDatePicker() {
        return endDatePicker;
    }

    public Button getApplyButton() {
        return applyButton;
    }

    public Button getNextWeekButton() {
        return nextWeekButton;
    }

    public Button getPrevButton() {
        return prevButton;
    }

    public Button getNextButton() {
        return nextButton;
    }
}
