package one.modality.catering.backoffice.activities.kitchen.view;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.theme.luminance.LuminanceTheme;
import dev.webfx.extras.time.pickers.DateField;
import dev.webfx.extras.util.animation.Animations;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import one.modality.base.client.i18n.BaseI18nKeys;
import one.modality.base.shared.entities.Item;
import one.modality.catering.backoffice.activities.kitchen.i18n.KitchenI18nKeys;
import one.modality.catering.backoffice.activities.kitchen.model.KitchenDisplayModel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    private final DateField startDateField;
    private final DateField endDateField;
    private final Button applyButton;
    private final Button nextWeekButton;
    private final ProgressIndicator progressIndicator;
    private final StackPane contentStack;
    private final StackPane overlayPane;
    private final Label copiedFeedbackLabel;
    private final Label organizationLabel;

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

        // Create WebFX DateFields (text field with calendar icon)
        startDateField = new DateField(overlayPane);
        startDateField.getView().setPrefWidth(140);
        styleCalendarIcon(startDateField);
        styleCalendarPicker(startDateField);

        Label arrowLabel = new Label("→");
        arrowLabel.getStyleClass().add("kitchen-filter-label");

        endDateField = new DateField(overlayPane);
        endDateField.getView().setPrefWidth(140);
        styleCalendarIcon(endDateField);
        styleCalendarPicker(endDateField);

        applyButton = I18nControls.newButton(KitchenI18nKeys.Apply);
        applyButton.getStyleClass().add("btn-primary");

        nextWeekButton = I18nControls.newButton(KitchenI18nKeys.NextWeek);
        nextWeekButton.getStyleClass().add("btn-secondary");

        // Create kitchen icon (chef hat / restaurant icon)
        SVGPath kitchenIcon = new SVGPath();
        kitchenIcon.setContent("M18.06 22.99h1.66c.84 0 1.53-.64 1.63-1.46L23 5.05h-5V1h-1v4h-1V1h-1v4h-1V1H13v4H8l1.65 16.48c.1.82.79 1.46 1.63 1.46h1.66v-6.03c0-.77.62-1.39 1.39-1.39h1.34c.77 0 1.39.62 1.39 1.39v6.03zM7 2H2v2h5V2z");
        kitchenIcon.setFill(Color.web("#0096D6"));
        kitchenIcon.setScaleX(1.2);
        kitchenIcon.setScaleY(1.2);

        // Create title with icon
        Label titleLabel = Bootstrap.textPrimary(Bootstrap.h2(I18nControls.newLabel(KitchenI18nKeys.Meals)));
        titleLabel.setGraphic(kitchenIcon);
        titleLabel.setContentDisplay(ContentDisplay.LEFT);
        titleLabel.setGraphicTextGap(12);

        // Create organization label (will be updated with actual organization name)
        organizationLabel = new Label();
        organizationLabel.getStyleClass().addAll(Bootstrap.TEXT_SECONDARY, Bootstrap.SMALL);
        organizationLabel.setPadding(new Insets(5, 0, 0, 0));

        // Title area with organization
        VBox titleArea = new VBox(5, titleLabel, organizationLabel);
        titleArea.setAlignment(Pos.CENTER_LEFT);

        HBox filterBox = new HBox(10, filterLabel, startDateField.getView(), arrowLabel, endDateField.getView(),
                applyButton,
                nextWeekButton);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        filterBox.setPadding(new Insets(6, 12, 6, 12));
        filterBox.getStyleClass().add("kitchen-filter-box");

        Label showingLabel = I18nControls.newLabel(KitchenI18nKeys.Showing);
        HBox filterArea = new HBox(15, showingLabel, dateRangeDisplay, filterBox);
        filterArea.setAlignment(Pos.CENTER_LEFT);

        // Combine title and filter areas
        VBox headerContent = new VBox(20, titleArea, filterArea);
        headerContent.setPadding(new Insets(20, 20, 15, 20));
        headerContent.getStyleClass().add("kitchen-top-header");
        headerContent.setMaxWidth(Region.USE_PREF_SIZE);

        // Create progress indicator
        progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(80, 80);
        progressIndicator.setVisible(true); // Show initially during page load

        // Create a stack to overlay progress indicator on table
        contentStack = new StackPane();
        contentStack.setMinHeight(400); // Ensure minimum height for centered progress indicator
        contentStack.getChildren().addAll(tableView.getNode(), progressIndicator);
        StackPane.setAlignment(progressIndicator, Pos.CENTER);
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
        VBox tableContainer = new VBox(10, contentStack, copyButtonsBox);

        // Create main container
        container = new BorderPane();
        container.setTop(headerContent);
        container.setCenter(tableContainer);
        BorderPane.setMargin(headerContent, new Insets(20, 20, 0, 20)); // Top, Right, Bottom, Left
        BorderPane.setMargin(tableContainer, new Insets(20));

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
     * Shows the loading indicator and dims the table.
     */
    public void showLoading() {
        UiScheduler.runInUiThread(() -> {
            progressIndicator.setVisible(true);
            tableView.getNode().setOpacity(0);
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
     * Sets the organization name displayed in the header.
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
        if (dateFieldView instanceof HBox) {
            HBox container = (HBox) dateFieldView;
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
     * Recursively finds and styles SVGPath nodes to be grey.
     */
    private void styleSVGPath(Node node) {
        if (node instanceof SVGPath) {
            // Use Java Color instead of web color - grey color (102, 102, 102 = #666666)
            ((SVGPath) node).setFill(Color.rgb(102, 102, 102));
        } else if (node instanceof javafx.scene.Parent) {
            for (Node child : ((javafx.scene.Parent) node).getChildrenUnmodifiable()) {
                styleSVGPath(child);
            }
        }
    }

    /**
     * Styles the calendar picker popup to be grey (full size, appears under text
     * field).
     */
    private void styleCalendarPicker(DateField dateField) {
        Node datePickerView = dateField.getDatePicker().getView();

        // Use Java colors: light grey background (245, 245, 245 = #f5f5f5) and grey
        // text (102, 102, 102 = #666666)
        String greyBg = toRGBCode(Color.rgb(245, 245, 245));
        String greyText = toRGBCode(Color.rgb(102, 102, 102));

        // Listen for when the calendar becomes visible (added to overlay), then force resize
        datePickerView.visibleProperty().addListener((obs, wasVisible, isVisible) -> {
            if (isVisible && datePickerView instanceof Region) {
                Region region = (Region) datePickerView;
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
            if (newParent != null && datePickerView instanceof Region) {
                Region region = (Region) datePickerView;
                javafx.application.Platform.runLater(() -> {
                    region.setMinWidth(280);
                    region.setPrefWidth(280);
                    region.setMaxWidth(280);
                    region.resize(280, region.getHeight());
                });
            }
        });

        // Remove border and set colors
        datePickerView.setStyle("-fx-background-color: " + greyBg + "; -fx-text-fill: " + greyText
                + "; -fx-border-width: 0; -fx-border-color: transparent;");

        // Apply grey styling to all children recursively
        styleCalendarChildren(datePickerView, Color.rgb(102, 102, 102), Color.rgb(245, 245, 245));
    }

    /**
     * Recursively applies grey styling to calendar picker children.
     */
    private void styleCalendarChildren(Node node, Color textColor, Color bgColor) {
        if (node instanceof Region) {
            Region region = (Region) node;
            String textRgb = toRGBCode(textColor);
            String bgRgb = toRGBCode(bgColor);
            region.setStyle("-fx-text-fill: " + textRgb + "; -fx-background-color: " + bgRgb + ";");
        }
        if (node instanceof javafx.scene.Parent) {
            for (Node child : ((javafx.scene.Parent) node).getChildrenUnmodifiable()) {
                styleCalendarChildren(child, textColor, bgColor);
            }
        }
    }

    /**
     * Converts a JavaFX Color to CSS RGB code.
     */
    private String toRGBCode(Color color) {
        int r = (int) (color.getRed() * 255);
        int g = (int) (color.getGreen() * 255);
        int b = (int) (color.getBlue() * 255);
        return "rgb(" + r + ", " + g + ", " + b + ")";
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
        var counts = currentDisplayModel.getAttendanceCounts();
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
