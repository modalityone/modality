package one.modality.catering.backoffice.activities.kitchen.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import one.modality.catering.backoffice.activities.kitchen.AttendanceCounts;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Table view for Kitchen Activity.
 * Displays meals and dietary options in a grid for a range of dates.
 */
public class KitchenTableView {

    private final ScrollPane scrollPane;
    private final GridPane gridPane;
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("EEE");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd");

    public KitchenTableView() {
        gridPane = new GridPane();
        gridPane.setHgap(0);
        gridPane.setVgap(0);
        gridPane.getStyleClass().add("kitchen-grid");

        scrollPane = new ScrollPane(gridPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
    }

    public Node getNode() {
        return scrollPane;
    }

    public void update(AttendanceCounts counts, LocalDate startDate, LocalDate endDate) {
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
        // Empty top-left cell
        StackPane topLeft = new StackPane();
        topLeft.getStyleClass().addAll("kitchen-date-header", "kitchen-cell");
        gridPane.add(topLeft, 0, 0);

        int colIndex = 1;
        for (LocalDate date : dates) {
            VBox dateHeader = createDateHeader(date);
            gridPane.add(dateHeader, colIndex++, 0);
        }
        // Total Column Header
        Label totalHeader = new Label("TOTAL");
        StackPane totalHeaderPane = new StackPane(totalHeader);
        totalHeaderPane.setPadding(new Insets(8, 6, 8, 6));
        totalHeaderPane.getStyleClass().addAll("kitchen-total-header", "kitchen-cell");
        gridPane.add(totalHeaderPane, colIndex, 0);

        // --- Data Rows ---
        int rowIndex = 1;
        // Define diet codes mapping to display names
        Map<String, String> dietMap = new LinkedHashMap<>();
        dietMap.put("Vegetarian", "V");
        dietMap.put("Vegetarian wheat-free", "WF");
        dietMap.put("Vegan", "VG");
        dietMap.put("Vegan wheat-free", "VGWF");

        List<String> meals = List.of("Breakfast", "Lunch", "Dinner");

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
                cell.getStyleClass().addAll("kitchen-meal-count-cell", "kitchen-cell");
                gridPane.add(cell, dateCol++, rowIndex);
                mealTotal += count;
            }
            // Meal Row Total
            Label mealTotalLabel = new Label(String.valueOf(mealTotal));

            StackPane mealTotalCell = new StackPane(mealTotalLabel);
            mealTotalCell.setPadding(new Insets(8, 6, 8, 6));
            mealTotalCell.getStyleClass().add("kitchen-meal-total-cell");

            gridPane.add(mealTotalCell, dateCol, rowIndex);

            rowIndex++;

            // Diet Rows
            int dietIndex = 0;
            for (Map.Entry<String, String> dietEntry : dietMap.entrySet()) {
                String dietName = dietEntry.getKey();
                String dietCode = dietEntry.getValue();

                Label dietLabel = new Label(dietName);

                HBox dietHeaderPane = new HBox(dietLabel);
                dietHeaderPane.setAlignment(Pos.CENTER_LEFT);
                dietHeaderPane.setPadding(new Insets(8, 6, 8, 12));
                dietHeaderPane.getStyleClass().addAll("kitchen-diet-label-cell", "kitchen-cell");

                gridPane.add(dietHeaderPane, 0, rowIndex);

                int dietTotal = 0;
                dateCol = 1;

                // Zebra striping: 2nd and 4th rows (indices 1 and 3) are striped
                boolean isStriped = (dietIndex == 1 || dietIndex == 3);

                for (LocalDate date : dates) {
                    int count = counts.getCount(date, meal, dietCode);
                    Label countLabel = new Label(count > 0 ? String.valueOf(count) : "");

                    StackPane cell = new StackPane(countLabel);
                    cell.setPadding(new Insets(8, 6, 8, 6));
                    cell.getStyleClass().add("kitchen-diet-value-cell");
                    if (isStriped) {
                        cell.getStyleClass().add("striped");
                    }
                    cell.getStyleClass().add("kitchen-cell");
                    gridPane.add(cell, dateCol++, rowIndex);
                    dietTotal += count;
                }
                // Diet Row Total
                Label dietTotalLabel = new Label(String.valueOf(dietTotal));

                StackPane dietTotalCell = new StackPane(dietTotalLabel);
                dietTotalCell.setPadding(new Insets(8, 6, 8, 6));
                dietTotalCell.getStyleClass().add("kitchen-diet-total-cell");

                gridPane.add(dietTotalCell, dateCol, rowIndex);

                rowIndex++;
                dietIndex++;
            }
        }
    }

    private VBox createDateHeader(LocalDate date) {
        Label dayLabel = new Label(date.format(DAY_FORMATTER).toUpperCase());
        dayLabel.getStyleClass().add("kitchen-day-label");

        Label dateLabel = new Label(date.format(DATE_FORMATTER));
        dateLabel.getStyleClass().add("kitchen-date-label");

        VBox vbox = new VBox(1, dayLabel, dateLabel);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(6));
        vbox.getStyleClass().addAll("kitchen-date-header", "kitchen-cell");
        return vbox;
    }

    private HBox createMealHeader(String mealName) {
        Label label = new Label(mealName);

        HBox hbox = new HBox(label);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setPadding(new Insets(8, 6, 8, 12));
        hbox.getStyleClass().addAll("kitchen-meal-header", "kitchen-cell");
        return hbox;
    }
}
