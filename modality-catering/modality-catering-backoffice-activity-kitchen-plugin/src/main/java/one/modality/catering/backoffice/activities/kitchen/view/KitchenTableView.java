package one.modality.catering.backoffice.activities.kitchen.view;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.util.control.Controls;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import one.modality.catering.backoffice.activities.kitchen.AttendanceCounts;
import one.modality.catering.backoffice.activities.kitchen.KitchenI18nKeys;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static one.modality.catering.backoffice.activities.kitchen.KitchenCssSelectors.*;

/**
 * Table view for Kitchen Activity.
 * Displays meals and dietary options in a grid for a range of dates.
 */
public class KitchenTableView {

    private final ScrollPane scrollPane;
    private final GridPane gridPane;
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("EEE");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd");

    // Click handler for cells: (date, meal, dietaryOption, count) -> void
    private CellClickHandler cellClickHandler;

    /**
     * Interface for handling cell clicks.
     */
    @FunctionalInterface
    public interface CellClickHandler {
        void onCellClick(LocalDate date, String meal, String dietaryOption, int count);
    }

    public KitchenTableView() {
        gridPane = new GridPane();
        gridPane.setHgap(0);
        gridPane.setVgap(0);
        gridPane.getStyleClass().add(kitchen_grid);

        scrollPane = Controls.createScrollPane(gridPane);
        scrollPane.setFitToWidth(false);  // Don't force width - let it size to content
        scrollPane.setFitToHeight(true);
        // Fit height to content - no vertical scrollbar

        // Disable vertical scrollbar, only allow horizontal scrolling
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Size to content - max width will be bound by parent to enable horizontal scrollbar
        scrollPane.setPrefWidth(javafx.scene.layout.Region.USE_COMPUTED_SIZE);
        scrollPane.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        scrollPane.setPrefHeight(javafx.scene.layout.Region.USE_COMPUTED_SIZE);

        // Make ScrollPane background transparent so parent background shows through
        scrollPane.setStyle("-fx-background-color: transparent;");
    }

    public Node getNode() {
        return scrollPane;
    }

    /**
     * Sets the click handler for diet option cells.
     */
    public void setCellClickHandler(CellClickHandler handler) {
        this.cellClickHandler = handler;
    }

    public void update(AttendanceCounts counts, LocalDate startDate, LocalDate endDate, Label organizationLabel) {
        gridPane.getChildren().clear();
        gridPane.getColumnConstraints().clear();
        gridPane.getRowConstraints().clear();

        if (counts == null || startDate == null || endDate == null) {
            return;
        }

        List<LocalDate> dates = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            dates.add(date);
        }

        // --- Column Constraints ---
        // Column 0: Row Headers (Meal/Diet names)
        javafx.scene.layout.ColumnConstraints headerCol = new javafx.scene.layout.ColumnConstraints();
        headerCol.setMinWidth(220); // Increased slightly for better fit
        headerCol.setPrefWidth(220);
        gridPane.getColumnConstraints().add(headerCol);

        // Date Columns
        for (int i = 0; i < dates.size(); i++) {
            javafx.scene.layout.ColumnConstraints dateCol = new javafx.scene.layout.ColumnConstraints();
            dateCol.setMinWidth(42);
            dateCol.setPrefWidth(42);
            gridPane.getColumnConstraints().add(dateCol);
        }
        // Total Column
        javafx.scene.layout.ColumnConstraints totalCol = new javafx.scene.layout.ColumnConstraints();
        totalCol.setMinWidth(60);
        totalCol.setPrefWidth(60);
        gridPane.getColumnConstraints().add(totalCol);

        // --- Header Row (Dates) ---
        // Top-left cell with organization name
        StackPane topLeft = new StackPane();
        if (organizationLabel != null) {
            topLeft.getChildren().add(organizationLabel);
            topLeft.setAlignment(Pos.CENTER_LEFT);
            topLeft.setPadding(new Insets(8, 10, 8, 10));
        }
        topLeft.getStyleClass().addAll(kitchen_date_header, kitchen_cell);
        gridPane.add(topLeft, 0, 0);

        int colIndex = 1;
        for (LocalDate date : dates) {
            VBox dateHeader = createDateHeader(date);
            gridPane.add(dateHeader, colIndex++, 0);
        }
        // Total Column Header
        Label totalHeader = I18nControls.newLabel(KitchenI18nKeys.Total);
        StackPane totalHeaderPane = new StackPane(totalHeader);
        totalHeaderPane.setPadding(new Insets(8, 6, 8, 6));
        totalHeaderPane.getStyleClass().addAll(kitchen_total_header, kitchen_cell);
        gridPane.add(totalHeaderPane, colIndex, 0);

        // --- Data Rows ---
        int rowIndex = 1;

        // Get all dietary options (codes) sorted by their order from database
        List<String> dietaryCodes = counts.getSortedDietaryOptions();
        // Filter out special code "Total" to get only actual dietary options (keep "?" for unknown diet)
        List<String> actualDietaryCodes = dietaryCodes.stream()
                .filter(code -> !code.equals("Total"))
                .collect(java.util.stream.Collectors.toList());

        // Get unique meal names from the data
        List<String> allMeals = counts.getUniqueMeals();

        // Filter to only show meals that have data (non-zero Total count) in the current date range
        List<String> meals = allMeals.stream()
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

        for (String meal : meals) {
            // Meal Header Row (e.g., "Breakfast")
            HBox mealHeader = createMealHeader(meal);
            gridPane.add(mealHeader, 0, rowIndex);

            int mealTotal = 0;
            int dateCol = 1;
            for (LocalDate date : dates) {
                // Get the total count for this meal (using "Total" dietary option code)
                int count = counts.getCount(date, meal, "Total");

                Label countLabel = new Label(count > 0 ? String.valueOf(count) : "");

                StackPane cell = new StackPane(countLabel);
                cell.setPadding(new Insets(8, 6, 8, 6));
                cell.getStyleClass().addAll(kitchen_meal_count_cell, kitchen_cell);

                // Make cell clickable if count > 0 and handler is set
                if (count > 0 && cellClickHandler != null) {
                    cell.setCursor(Cursor.HAND);
                    cell.getStyleClass().add(kitchen_cell_clickable);
                    final LocalDate clickedDate = date;
                    final String clickedMeal = meal;
                    final int clickedCount = count;

                    cell.setOnMouseClicked(event -> {
                        // Pass "Total" as the dietary option code to indicate all diets
                        cellClickHandler.onCellClick(clickedDate, clickedMeal, "Total", clickedCount);
                    });
                }

                gridPane.add(cell, dateCol++, rowIndex);
                mealTotal += count;
            }
            // Meal Row Total
            Label mealTotalLabel = new Label(String.valueOf(mealTotal));

            StackPane mealTotalCell = new StackPane(mealTotalLabel);
            mealTotalCell.setPadding(new Insets(8, 6, 8, 6));
            mealTotalCell.getStyleClass().add(kitchen_meal_total_cell);

            gridPane.add(mealTotalCell, dateCol, rowIndex);

            rowIndex++;

            // Diet Rows - iterate through dynamic dietary options from database
            // First, filter out dietary options that have no data for this meal in the date range
            List<String> visibleDietaryCodes = actualDietaryCodes.stream()
                    .filter(dietCode -> {
                        // Check if this dietary option has any non-zero count for this meal in the date range
                        for (LocalDate date : dates) {
                            int count = counts.getCount(date, meal, dietCode);
                            if (count > 0) {
                                return true; // Has data, include this dietary option
                            }
                        }
                        return false; // No data for this dietary option
                    })
                    .collect(java.util.stream.Collectors.toList());

            int dietIndex = 0;
            for (String dietCode : visibleDietaryCodes) {
                // Get the dietary option name from AttendanceCounts
                String dietName = counts.getNameForDietaryOption(dietCode);
                if (dietName == null) {
                    dietName = dietCode; // Fallback to code if name not available
                }

                Label dietLabel = new Label(dietName);

                HBox dietHeaderPane = new HBox(dietLabel);
                dietHeaderPane.setAlignment(Pos.CENTER_LEFT);
                dietHeaderPane.setPadding(new Insets(8, 6, 8, 12));
                dietHeaderPane.getStyleClass().addAll(kitchen_diet_label_cell, kitchen_cell);

                gridPane.add(dietHeaderPane, 0, rowIndex);

                int dietTotal = 0;
                dateCol = 1;

                // Zebra striping: alternate rows are striped
                boolean isStriped = (dietIndex % 2 == 1);

                for (LocalDate date : dates) {
                    int count = counts.getCount(date, meal, dietCode);
                    Label countLabel = new Label(count > 0 ? String.valueOf(count) : "");

                    StackPane cell = new StackPane(countLabel);
                    cell.setPadding(new Insets(8, 6, 8, 6));
                    cell.getStyleClass().add(kitchen_diet_value_cell);
                    if (isStriped) {
                        cell.getStyleClass().add(striped);
                    }
                    cell.getStyleClass().add(kitchen_cell);

                    // Make cell clickable if count > 0
                    if (count > 0 && cellClickHandler != null) {
                        cell.setCursor(Cursor.HAND);
                        cell.getStyleClass().add(kitchen_diet_cell_clickable);
                        final LocalDate clickedDate = date;
                        final String clickedMeal = meal;
                        final String clickedDietCode = dietCode;
                        final int clickedCount = count;

                        cell.setOnMouseClicked(event -> cellClickHandler.onCellClick(clickedDate, clickedMeal, clickedDietCode, clickedCount));
                    }

                    gridPane.add(cell, dateCol++, rowIndex);
                    dietTotal += count;
                }
                // Diet Row Total
                Label dietTotalLabel = new Label(String.valueOf(dietTotal));

                StackPane dietTotalCell = new StackPane(dietTotalLabel);
                dietTotalCell.setPadding(new Insets(8, 6, 8, 6));
                dietTotalCell.getStyleClass().add(kitchen_diet_total_cell);

                gridPane.add(dietTotalCell, dateCol, rowIndex);

                rowIndex++;
                dietIndex++;
            }
        }
    }

    private VBox createDateHeader(LocalDate date) {
        Label dayLabel = new Label(date.format(DAY_FORMATTER).toUpperCase());
        dayLabel.getStyleClass().add(kitchen_day_label);

        Label dateLabel = new Label(date.format(DATE_FORMATTER));
        dateLabel.getStyleClass().add(kitchen_date_label);

        VBox vbox = new VBox(1, dayLabel, dateLabel);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(6));
        vbox.getStyleClass().addAll(kitchen_date_header, kitchen_cell);
        return vbox;
    }

    private HBox createMealHeader(String mealName) {
        Label label = new Label(mealName);

        HBox hbox = new HBox(label);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setPadding(new Insets(8, 6, 8, 12));
        hbox.getStyleClass().addAll(kitchen_meal_header, kitchen_cell);
        return hbox;
    }
}
