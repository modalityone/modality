package one.modality.catering.backoffice.activities.kitchen.view;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.theme.luminance.LuminanceTheme;
import dev.webfx.extras.time.pickers.DateField;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import one.modality.catering.backoffice.activities.kitchen.AttendanceCounts;
import one.modality.catering.backoffice.activities.kitchen.KitchenI18nKeys;
import one.modality.catering.backoffice.activities.kitchen.model.KitchenDisplayModel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Main view component for Kitchen activity.
 * Pure JavaFX UI construction without business logic.
 * Composites table view, bottom panel, and meal selection components.
 *
 * @author Claude Code (Refactored from KitchenActivity)
 */
public final class KitchenViewUI {

    private final KitchenTableView tableView;
    private final Label dateRangeDisplay;
    private final DateField startDateField;
    private final DateField endDateField;
    private final Button applyButton;
    private final Button nextWeekButton;
    private final Region loadingSpinner;
    private final StackPane overlayPane;
    private final Label copiedFeedbackLabel;
    private final Label organizationLabel;
    private final VBox emptyStateView;
    private final VBox tableContainer;
    private final HBox headerContent;

    // Store current display model for export
    private KitchenDisplayModel currentDisplayModel;

    public KitchenViewUI() {
        dev.webfx.platform.console.Console.log("KitchenViewUI constructor called");

        // Create overlay pane for date picker popups
        overlayPane = new StackPane();

        // Create sub-components
        tableView = new KitchenTableView();

        // Create Top Panel (Filter Bar)
        dateRangeDisplay = new Label();
        dateRangeDisplay.getStyleClass().add("kitchen-date-range-display");

        Label filterLabel = I18nControls.newLabel(KitchenI18nKeys.Filter);
        filterLabel.getStyleClass().add("kitchen-filter-label");

        // Create WebFX DateFields (text field with calendar icon) - compact width
        startDateField = new DateField(overlayPane);
        styleCalendarIcon(startDateField);
        styleCalendarPicker(startDateField);

        Label arrowLabel = new Label("→");
        arrowLabel.getStyleClass().add("kitchen-filter-label");

        endDateField = new DateField(overlayPane);
        styleCalendarIcon(endDateField);
        styleCalendarPicker(endDateField);

        applyButton = I18nControls.newButton(KitchenI18nKeys.Apply);
        applyButton.getStyleClass().add("btn-primary");

        nextWeekButton = I18nControls.newButton(KitchenI18nKeys.NextWeek);
        nextWeekButton.getStyleClass().add("btn-secondary");

        // Create kitchen icon (chef hat / restaurant icon) - smaller size
        SVGPath kitchenIcon = new SVGPath();
        kitchenIcon.setContent("M18.06 22.99h1.66c.84 0 1.53-.64 1.63-1.46L23 5.05h-5V1h-1v4h-1V1h-1v4h-1V1H13v4H8l1.65 16.48c.1.82.79 1.46 1.63 1.46h1.66v-6.03c0-.77.62-1.39 1.39-1.39h1.34c.77 0 1.39.62 1.39 1.39v6.03zM7 2H2v2h5V2z");
        kitchenIcon.getStyleClass().add("kitchen-icon");
        kitchenIcon.setScaleX(0.75);
        kitchenIcon.setScaleY(0.75);

        // Create title with icon - smaller font
        Label titleLabel = Bootstrap.textPrimary(Bootstrap.h4(I18nControls.newLabel(KitchenI18nKeys.Meals)));
        titleLabel.setGraphic(kitchenIcon);
        titleLabel.setContentDisplay(ContentDisplay.LEFT);
        titleLabel.setGraphicTextGap(6);

        // Create organization label - will be placed in table's top-left cell
        organizationLabel = new Label();
        organizationLabel.getStyleClass().addAll(Bootstrap.TEXT_SECONDARY, Bootstrap.SMALL, "kitchen-organization-label");
        organizationLabel.setWrapText(true);
        organizationLabel.setMaxWidth(200);

        // Create a nice vertical separator
        Region separator = new Region();
        separator.setPrefWidth(1);
        separator.setMinHeight(20);
        separator.setMaxHeight(20);
        separator.getStyleClass().add("kitchen-separator");

        // Compact date range display with "Showing" label
        Label showingLabel = I18nControls.newLabel(KitchenI18nKeys.Showing);
        showingLabel.getStyleClass().addAll(Bootstrap.TEXT_SECONDARY, Bootstrap.SMALL);

        HBox dateDisplayBox = new HBox(6, showingLabel, dateRangeDisplay);
        dateDisplayBox.setAlignment(Pos.CENTER_LEFT);

        // Compact filter controls with reduced date field width
        startDateField.getView().setPrefWidth(120);
        endDateField.getView().setPrefWidth(120);

        HBox filterBox = new HBox(6, filterLabel, startDateField.getView(), arrowLabel, endDateField.getView(),
                applyButton, nextWeekButton);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        filterBox.setPadding(new Insets(3, 8, 3, 8));
        filterBox.getStyleClass().add("kitchen-filter-box");

        // Group showing and filter together
        HBox showingAndFilters = new HBox(12, dateDisplayBox, filterBox);
        showingAndFilters.setAlignment(Pos.CENTER_LEFT);

        // Single-line header: title | separator | showing+filters (no spacer)
        headerContent = new HBox(15, titleLabel, separator, showingAndFilters);
        headerContent.setAlignment(Pos.CENTER_LEFT);
        headerContent.setPadding(new Insets(10, 15, 8, 15));
        headerContent.getStyleClass().add("kitchen-top-header");
        headerContent.setMaxWidth(Region.USE_PREF_SIZE); // Fit to content width

        // Create progress indicator
        loadingSpinner = Controls.createSpinner(80);
        loadingSpinner.setVisible(true); // Show initially during page load

        // Create a stack to overlay progress indicator on table
        StackPane contentStack = new StackPane();
        contentStack.setMinHeight(400); // Ensure minimum height for centered progress indicator
        contentStack.getChildren().addAll(tableView.getNode(), loadingSpinner);
        StackPane.setAlignment(loadingSpinner, Pos.CENTER);
        StackPane.setAlignment(tableView.getNode(), Pos.TOP_LEFT); // Align table to top-left instead of center

        // Hide table initially until data loads
        tableView.getNode().setOpacity(0);

        // Set ScrollPane max width to enable horizontal scrollbar when table is too
        // wide
        javafx.scene.control.ScrollPane scrollPane = (javafx.scene.control.ScrollPane) tableView.getNode();
        scrollPane.setMaxWidth(1500); // Cap width at 1500px
        // No max height - let table grow vertically, outer ScrollPane handles vertical
        // scrolling

        // Create copy button below the table
        Button copyButton = Bootstrap.primaryButton(I18nControls.newButton(KitchenI18nKeys.CopyTable));

        // Add copy icon SVG
        SVGPath copyIcon = new SVGPath();
        copyIcon.setContent("M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z");
        copyIcon.setFill(Color.WHITE);
        copyIcon.setScaleX(0.8);
        copyIcon.setScaleY(0.8);

        copyButton.setGraphic(copyIcon);
        copyButton.setOnAction(e -> {
            copyTableAsHtml();
            showCopiedFeedback();
        });

        // Create "Copied!" feedback label
        copiedFeedbackLabel = I18nControls.newLabel(KitchenI18nKeys.Copied);
        copiedFeedbackLabel.getStyleClass().add("copied-feedback");
        copiedFeedbackLabel.setOpacity(0);
        copiedFeedbackLabel.setVisible(false);

        HBox copyButtonsBox = new HBox(10, copyButton, copiedFeedbackLabel);
        copyButtonsBox.setAlignment(Pos.CENTER_LEFT);
        copyButtonsBox.setPadding(new Insets(10, 0, 0, 0));

        // Wrap table and copy buttons in a VBox
        tableContainer = new VBox(10, contentStack, copyButtonsBox);

        // Create empty state view
        emptyStateView = KitchenEmptyStateView.createEmptyStateView();
        emptyStateView.setVisible(false);
        emptyStateView.setManaged(false);

        // Create a StackPane to hold either table or empty state
        StackPane centerStack = new StackPane();
        centerStack.getChildren().addAll(tableContainer, emptyStateView);
        StackPane.setAlignment(tableContainer, Pos.TOP_LEFT);
        StackPane.setAlignment(emptyStateView, Pos.CENTER);

        // Create main container
        BorderPane container = new BorderPane();
        container.setTop(headerContent);
        container.setCenter(centerStack);
        BorderPane.setMargin(headerContent, new Insets(10, 20, 0, 20)); // Top, Right, Bottom, Left - reduced top margin
        BorderPane.setMargin(centerStack, new Insets(20));

        // Apply styling to show the same background if the scroll pane doesn't cover
        // the whole area
        LuminanceTheme.createPrimaryPanelFacet(container).style();

        // Stack the overlay pane on top of the main container for date picker popups
        overlayPane.getChildren().add(container);
    }

    /**
     * Returns the root node of this view (includes overlay pane for date pickers).
     */
    public Node getNode() {
        return overlayPane;
    }

    /**
     * Returns the overlay pane (used for dialogs).
     */
    public Pane getOverlayPane() {
        return overlayPane;
    }

    /**
     * Shows the loading indicator and dims the table.
     */
    public void showLoading() {
        UiScheduler.runInUiThread(() -> {
            loadingSpinner.setVisible(true);
            tableView.getNode().setOpacity(0);
        });
    }

    /**
     * Hides the loading indicator and shows the table.
     */
    public void hideLoading() {
        UiScheduler.runInUiThread(() -> {
            loadingSpinner.setVisible(false);
            tableView.getNode().setOpacity(1.0);
        });
    }

    /**
     * Updates the calendar display with new data.
     */
    public void updateData(LocalDate startDate, LocalDate endDate, KitchenDisplayModel displayModel) {
        UiScheduler.runInUiThread(() -> {
            dev.webfx.platform.console.Console.log("KitchenViewUI.updateData called");
            dev.webfx.platform.console.Console
                    .log("KitchenViewUI: displayModel is " + (displayModel != null ? "present" : "null"));

            // Store current display model for export
            this.currentDisplayModel = displayModel;

            // Update Date Range Display
            if (startDate != null && endDate != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d");
                DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
                dateRangeDisplay.setText(startDate.format(formatter) + " - " + endDate.format(yearFormatter));

                // Update date fields if not focused (to avoid interrupting user typing)
                if (!startDateField.getTextField().isFocused())
                    startDateField.setDate(startDate);
                if (!endDateField.getTextField().isFocused())
                    endDateField.setDate(endDate);
            }

            // Update Table
            if (displayModel != null) {
                try {
                    dev.webfx.platform.console.Console.log("KitchenViewUI: Calling tableView.update");

                    // Check if there are any meals to display
                    AttendanceCounts counts = displayModel.attendanceCounts();
                    var allMeals = counts.getUniqueMeals();

                    // Filter to only meals that have data (non-zero Total count) in the date range
                    List<LocalDate> dates = new ArrayList<>();
                    for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                        dates.add(date);
                    }

                    boolean hasMeals = allMeals.stream()
                        .anyMatch(meal -> {
                            // Check if this meal has any non-zero Total count in the date range
                            for (LocalDate date : dates) {
                                int totalCount = counts.getCount(date, meal, "Total");
                                if (totalCount > 0) {
                                    return true; // Has data
                                }
                            }
                            return false; // No data for this meal
                        });

                    if (hasMeals) {
                        // Show table view with header
                        headerContent.setVisible(true);
                        headerContent.setManaged(true);
                        tableContainer.setVisible(true);
                        tableContainer.setManaged(true);
                        emptyStateView.setVisible(false);
                        emptyStateView.setManaged(false);

                        tableView.update(counts, startDate, endDate, organizationLabel);
                        dev.webfx.platform.console.Console.log("KitchenViewUI: tableView.update returned");
                    } else {
                        // Show empty state, hide header and table
                        dev.webfx.platform.console.Console.log("KitchenViewUI: No meals found, showing empty state");
                        headerContent.setVisible(false);
                        headerContent.setManaged(false);
                        tableContainer.setVisible(false);
                        tableContainer.setManaged(false);
                        emptyStateView.setVisible(true);
                        emptyStateView.setManaged(true);
                    }

                    // Hide loading indicator after table is fully rendered
                    // Use runLater to execute after the scene graph has been updated and rendered
                    javafx.application.Platform.runLater(this::hideLoading);
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

    public DateField getStartDateField() {
        return startDateField;
    }

    public DateField getEndDateField() {
        return endDateField;
    }

    public Button getApplyButton() {
        return applyButton;
    }

    public Button getNextWeekButton() {
        return nextWeekButton;
    }

    /**
     * Sets the click handler for table cells.
     */
    public void setCellClickHandler(KitchenTableView.CellClickHandler handler) {
        tableView.setCellClickHandler(handler);
    }

    /**
     * Sets the organization name displayed in the table's top-left cell.
     */
    public void setOrganizationName(String organizationName) {
        UiScheduler.runInUiThread(() -> {
            if (organizationName != null && !organizationName.isEmpty()) {
                organizationLabel.setText(organizationName);
                organizationLabel.setVisible(true);
            } else {
                organizationLabel.setText("");
                organizationLabel.setVisible(false);
            }
        });
    }

    /**
     * Styles the calendar icon (SVG) to be smaller and grey.
     */
    private void styleCalendarIcon(DateField dateField) {
        // The DateField view is an HBox containing the text field and calendar icon
        Region dateFieldView = dateField.getView();
        if (dateFieldView instanceof HBox container) {
            // Align items vertically center
            container.setAlignment(Pos.CENTER_LEFT);

            // The calendar icon is the second child (index 1)
            if (container.getChildren().size() > 1) {
                Node iconNode = container.getChildren().get(1);
                // Scale the icon to 50%
                iconNode.setScaleX(0.5);
                iconNode.setScaleY(0.5);
                // Find and style the SVGPath inside
                styleSVGPath(iconNode);
            }
        }
    }

    /**
     * Recursively finds and styles SVGPath nodes with CSS class.
     */
    private void styleSVGPath(Node node) {
        if (node instanceof SVGPath) {
            node.getStyleClass().add("kitchen-calendar-icon");
        } else if (node instanceof javafx.scene.Parent) {
            for (Node child : ((javafx.scene.Parent) node).getChildrenUnmodifiable()) {
                styleSVGPath(child);
            }
        }
    }

    /**
     * Styles the calendar picker popup (full size, appears under text field).
     */
    private void styleCalendarPicker(DateField dateField) {
        Node datePickerView = dateField.getDatePicker().getView();

        // Listen for when the calendar becomes visible (added to overlay), then force resize
        datePickerView.visibleProperty().addListener((obs, wasVisible, isVisible) -> {
            if (isVisible && datePickerView instanceof Region region) {
                // Use Platform.runLater to ensure this happens after relocateDatePicker
                javafx.application.Platform.runLater(() -> {
                    region.setMinWidth(280);
                    region.setPrefWidth(280);
                    region.setMaxWidth(280);
                    region.resize(280, region.getHeight());
                });
            }
        });

        // Also listen for parent changes (when added to overlay)
        datePickerView.parentProperty().addListener((obs, oldParent, newParent) -> {
            if (newParent != null && datePickerView instanceof Region region) {
                javafx.application.Platform.runLater(() -> {
                    region.setMinWidth(280);
                    region.setPrefWidth(280);
                    region.setMaxWidth(280);
                    region.resize(280, region.getHeight());
                });
            }
        });

        // Apply CSS class for styling
        datePickerView.getStyleClass().add("kitchen-calendar-picker");

        // Apply styling to all children recursively
        styleCalendarChildren(datePickerView);
    }

    /**
     * Recursively applies CSS classes to calendar picker children.
     */
    private void styleCalendarChildren(Node node) {
        if (node instanceof Region) {
            node.getStyleClass().add("kitchen-calendar-picker");
        }
        if (node instanceof javafx.scene.Parent) {
            for (Node child : ((javafx.scene.Parent) node).getChildrenUnmodifiable()) {
                styleCalendarChildren(child);
            }
        }
    }

    /**
     * Copies the table data as HTML to clipboard.
     * Format is compatible with Google Sheets paste.
     */
    private void copyTableAsHtml() {
        if (currentDisplayModel == null) {
            dev.webfx.platform.console.Console.log("No data to copy");
            return;
        }

        StringBuilder html = new StringBuilder();
        html.append("<table border='1' cellpadding='5' cellspacing='0' style='border-collapse: collapse; font-family: Arial, sans-serif;'>\n");

        // Get data from model
        var counts = currentDisplayModel.attendanceCounts();
        var dates = new ArrayList<>(counts.getDates());
        dates.sort(LocalDate::compareTo);

        // Filter to only show meals that have data (non-zero Total count) in the current date range
        var allMeals = counts.getUniqueMeals();
        var meals = allMeals.stream()
                .filter(meal -> {
                    // Check if this meal has any non-zero Total count in the date range
                    for (LocalDate date : dates) {
                        int totalCount = counts.getCount(date, meal, "Total");
                        if (totalCount > 0) {
                            return true; // Has data, include this meal
                        }
                    }
                    return false; // No data for this meal in the date range
                })
                .collect(java.util.stream.Collectors.toList());

        // Filter out special code "Total" to get only actual dietary options (keep "?" for unknown diet)
        var dietaryOptions = counts.getSortedDietaryOptions().stream()
                .filter(code -> !code.equals("Total"))
                .collect(java.util.stream.Collectors.toList());

        // Header row with dates
        html.append("<tr>");
        html.append("<th style='background-color: #d4d4d4; color: #0096D6; font-weight: bold; padding: 8px; text-align: center;'></th>");
        for (LocalDate date : dates) {
            html.append("<th style='background-color: #d4d4d4; color: #0096D6; font-weight: bold; padding: 8px; text-align: center;'>")
                .append(date.format(DateTimeFormatter.ofPattern("EEE MMM d")))
                .append("</th>");
        }
        html.append("<th style='background-color: #0096D6; color: white; font-weight: bold; padding: 8px; text-align: center;'>Total</th>");
        html.append("</tr>\n");

        // Meal count rows
        for (String meal : meals) {
            html.append("<tr>");
            html.append("<td style='background-color: #0096D6; color: white; font-weight: bold; padding: 8px; text-align: left;'>")
                .append(meal).append("</td>");
            int mealTotal = 0;
            for (LocalDate date : dates) {
                int count = 0;
                for (String dietaryOption : dietaryOptions) {
                    count += counts.getCount(date, meal, dietaryOption);
                }
                html.append("<td style='background-color: #0096D6; color: white; font-weight: bold; padding: 8px; text-align: center;'>")
                    .append(count > 0 ? count : "").append("</td>");
                mealTotal += count;
            }
            html.append("<td style='background-color: #007AB8; color: white; font-weight: bold; padding: 8px; text-align: center; border-left: 3px solid #006399;'>")
                .append(mealTotal).append("</td>");
            html.append("</tr>\n");

            // Dietary option rows for this meal - only include options with data
            List<String> visibleDietaryOptions = dietaryOptions.stream()
                    .filter(dietCode -> {
                        // Check if this dietary option has any non-zero count for this meal
                        for (LocalDate date : dates) {
                            if (counts.getCount(date, meal, dietCode) > 0) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .collect(java.util.stream.Collectors.toList());

            int dietIndex = 0;
            for (String dietaryOption : visibleDietaryOptions) {
                String dietaryName = counts.getNameForDietaryOption(dietaryOption);
                boolean isStriped = (dietIndex % 2 == 1);
                String bgColor = isStriped ? "#f8f9fa" : "white";

                html.append("<tr>");
                html.append("<td style='background-color: #f5f5f5; color: #999999; padding: 8px; text-align: left;'>")
                    .append(dietaryName != null ? dietaryName : dietaryOption).append("</td>");
                int dietTotal = 0;
                for (LocalDate date : dates) {
                    int count = counts.getCount(date, meal, dietaryOption);
                    html.append("<td style='background-color: ").append(bgColor)
                        .append("; color: #666666; padding: 8px; text-align: center;'>")
                        .append(count > 0 ? count : "").append("</td>");
                    dietTotal += count;
                }
                html.append("<td style='background-color: #e9ecef; color: #333333; font-weight: bold; padding: 8px; text-align: center; border-left: 3px solid #0096D6;'>")
                    .append(dietTotal).append("</td>");
                html.append("</tr>\n");
                dietIndex++;
            }
        }

        // Total row
        html.append("<tr>");
        html.append("<td style='background-color: #0096D6; color: white; font-weight: bold; padding: 8px; text-align: left;'>Total</td>");
        int grandTotal = 0;
        for (LocalDate date : dates) {
            int dateTotal = 0;
            for (String meal : meals) {
                for (String dietaryOption : dietaryOptions) {
                    dateTotal += counts.getCount(date, meal, dietaryOption);
                }
            }
            html.append("<td style='background-color: #0096D6; color: white; font-weight: bold; padding: 8px; text-align: center;'>")
                .append(dateTotal).append("</td>");
            grandTotal += dateTotal;
        }
        html.append("<td style='background-color: #007AB8; color: white; font-weight: bold; padding: 8px; text-align: center; border-left: 3px solid #006399;'>")
            .append(grandTotal).append("</td>");
        html.append("</tr>\n");

        html.append("</table>");

        ClipboardContent content = new ClipboardContent();
        content.putHtml(html.toString());
        Console.log(html.toString());
        Clipboard.getSystemClipboard().setContent(content);
        dev.webfx.platform.console.Console.log("Table copied as HTML");
    }

    /**
     * Shows a brief "Copied!" feedback animation.
     */
    private void showCopiedFeedback() {
        copiedFeedbackLabel.setVisible(true);
        copiedFeedbackLabel.setOpacity(0);

        // Fade in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), copiedFeedbackLabel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // Fade out animation after a delay
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), copiedFeedbackLabel);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setDelay(Duration.millis(1500));
        fadeOut.setOnFinished(e -> copiedFeedbackLabel.setVisible(false));

        // Play animations in sequence
        fadeIn.setOnFinished(e -> fadeOut.play());
        fadeIn.play();
    }
}
