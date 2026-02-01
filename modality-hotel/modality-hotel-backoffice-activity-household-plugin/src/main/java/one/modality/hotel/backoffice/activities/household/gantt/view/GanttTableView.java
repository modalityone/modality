package one.modality.hotel.backoffice.activities.household.gantt.view;

import dev.webfx.extras.util.control.Controls;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import one.modality.hotel.backoffice.activities.household.gantt.model.DateColumn;
import one.modality.hotel.backoffice.activities.household.gantt.model.GanttBedData;
import one.modality.hotel.backoffice.activities.household.gantt.model.GanttRoomData;
import one.modality.hotel.backoffice.activities.household.gantt.presenter.GanttPresenter;
import one.modality.hotel.backoffice.activities.household.gantt.renderer.GanttColorScheme;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static one.modality.hotel.backoffice.activities.household.HouseholdCssSelectors.gantt_title;

/**
 * Main view component for the Gantt table.
 * Handles UI construction and layout without business logic.
 *
 * @author Claude Code Assistant
 */
public class GanttTableView {

    private final GanttPresenter presenter;
    private final GanttCellFactory cellFactory;

    private BorderPane container;
    private GridPane calendarGrid;
    private ScrollPane scrollPane;
    private Label dateRangeLabel;
    private Runnable onNavigationCallback;
    private List<GanttRoomData> currentRooms;  // Store current rooms data

    public GanttTableView(GanttPresenter presenter) {
        this.presenter = presenter;
        GanttColorScheme colorScheme = new GanttColorScheme();
        this.cellFactory = new GanttCellFactory(colorScheme, presenter);
        // Wire up expand/collapse callback to refresh the grid WITHOUT reloading data
        this.cellFactory.setOnExpandCollapseCallback(this::refreshExpandedState);
        buildUI();
    }

    /**
     * Refreshes the grid when expand/collapse state changes.
     * Rebuilds the grid with existing data instead of reloading from the database.
     * Uses fade animation for smooth transition.
     */
    private void refreshExpandedState() {
        if (currentRooms != null) {
            // Store old grid for animation
            GridPane oldGrid = calendarGrid;

            // Create new grid with updated expand/collapse state
            rebuildCalendarGrid(currentRooms);
            GridPane newGrid = calendarGrid;

            // Set initial opacity for fade-in effect
            newGrid.setOpacity(0);

            // Fade out old grid and fade in new grid simultaneously
            FadeTransition fadeOut = getFadeTransition(oldGrid, newGrid);

            fadeOut.play();
        }
    }

    private FadeTransition getFadeTransition(GridPane oldGrid, GridPane newGrid) {
        FadeTransition fadeOut = new FadeTransition(
            javafx.util.Duration.millis(150), oldGrid);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        FadeTransition fadeIn = new FadeTransition(
            javafx.util.Duration.millis(150), newGrid);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        // Start fade out, then switch content and fade in
        fadeOut.setOnFinished(e -> {
            scrollPane.setContent(newGrid);
            fadeIn.play();
        });
        return fadeOut;
    }

    /**
     * Sets a callback to be called when navigation buttons are clicked.
     * This allows the controller to reload data for the new time window.
     */
    public void setOnNavigationCallback(Runnable callback) {
        this.onNavigationCallback = callback;
    }

    /**
     * Builds the complete UI structure
     */
    private void buildUI() {
        container = new BorderPane();

        // Build header
        HBox header = buildHeader();
        container.setTop(header);

        // Build calendar grid
        rebuildCalendarGrid(List.of());

        // Wrap in ScrollPane
        scrollPane = new ScrollPane();
        scrollPane.setContent(calendarGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        Controls.setupVerticalScrollPane(scrollPane, calendarGrid);

        container.setCenter(scrollPane);
    }

    /**
     * Builds the header with title, date range, and navigation buttons
     */
    private HBox buildHeader() {
        HBox header = new HBox(20);
        header.setPadding(new Insets(10, 20, 10, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        // Title
        Label title = new Label("Housekeeping Calendar");
        title.setTextFill(GanttColorScheme.COLOR_ACCENT);
        title.getStyleClass().add(gantt_title);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Date controls
        HBox dateControls = new HBox(10);
        dateControls.setAlignment(Pos.CENTER_LEFT);

        Button prevBtn = new Button("<");
        prevBtn.setOnAction(e -> {
            presenter.navigateWeeks(-1);
            refresh();
            if (onNavigationCallback != null) {
                onNavigationCallback.run();
            }
        });

        Button todayBtn = new Button("Today");
        todayBtn.setOnAction(e -> {
            presenter.navigateToToday();
            refresh();
            if (onNavigationCallback != null) {
                onNavigationCallback.run();
            }
        });

        Button nextBtn = new Button(">");
        nextBtn.setOnAction(e -> {
            presenter.navigateWeeks(1);
            refresh();
            if (onNavigationCallback != null) {
                onNavigationCallback.run();
            }
        });

        dateRangeLabel = new Label();
        updateDateRangeLabel();

        dateControls.getChildren().addAll(prevBtn, todayBtn, nextBtn, dateRangeLabel);

        header.getChildren().addAll(title, spacer, dateControls);

        return header;
    }

    /**
     * Displays rooms in the Gantt table
     */
    public void displayRooms(List<GanttRoomData> rooms) {
        this.currentRooms = rooms;  // Store for expand/collapse refresh
        rebuildCalendarGrid(rooms);
        scrollPane.setContent(calendarGrid);
    }

    /**
     * Rebuilds the entire calendar grid
     */
    private void rebuildCalendarGrid(List<GanttRoomData> rooms) {
        calendarGrid = new GridPane();
        calendarGrid.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        // Get date columns from presenter
        List<DateColumn> dateColumns = presenter.getDateColumns();

        // Build header row
        buildHeaderRow(dateColumns);

        // Handle null or empty rooms list
        if (rooms == null || rooms.isEmpty()) {
            return;
        }

        // Build category and room rows
        int rowIndex = 1; // Start after header

        // Group rooms by category (filter out any rooms with null category)
        Map<String, List<GanttRoomData>> roomsByCategory = rooms.stream()
                .filter(room -> room != null && room.getCategory() != null)
                .collect(Collectors.groupingBy(GanttRoomData::getCategory, LinkedHashMap::new, Collectors.toList()));

        for (Map.Entry<String, List<GanttRoomData>> entry : roomsByCategory.entrySet()) {
            // Skip null or empty categories
            if (entry.getKey() == null || entry.getValue() == null || entry.getValue().isEmpty()) {
                continue;
            }

            // Add category header row
            rowIndex = buildCategoryRow(entry.getKey(), rowIndex, dateColumns.size());

            // Add room rows
            for (GanttRoomData room : entry.getValue()) {
                if (room != null) {
                    rowIndex = buildRoomRow(room, rowIndex, dateColumns);
                }
            }
        }
    }

    /**
     * Builds the header row with room column label and date headers
     */
    private void buildHeaderRow(List<DateColumn> dateColumns) {
        int colIndex = 0;

        // Room header cell
        calendarGrid.add(cellFactory.createRoomHeaderCell(), colIndex++, 0);

        // Date header cells
        for (DateColumn dateCol : dateColumns) {
            calendarGrid.add(cellFactory.createDateHeaderCell(dateCol), colIndex++, 0);
        }
    }

    /**
     * Builds a category row
     */
    private int buildCategoryRow(String categoryName, int rowIndex, int dateColumnCount) {
        StackPane categoryCell = cellFactory.createCategoryCell(categoryName);

        // Span across all columns
        int colSpan = 1 + dateColumnCount;
        calendarGrid.add(categoryCell, 0, rowIndex, colSpan, 1);
        GridPane.setHgrow(categoryCell, Priority.ALWAYS);

        return rowIndex + 1;
    }

    /**
     * Builds a room row with room name cell and date cells.
     * If the room is expanded and has beds, also builds bed rows.
     */
    private int buildRoomRow(GanttRoomData room, int rowIndex, List<DateColumn> dateColumns) {
        int colIndex = 0;

        // Room name cell
        calendarGrid.add(cellFactory.createRoomCell(room), colIndex++, rowIndex);

        // Date cells with booking bars
        for (DateColumn dateCol : dateColumns) {
            calendarGrid.add(cellFactory.createDateCell(room, dateCol), colIndex++, rowIndex);
        }

        rowIndex++;

        // If room is expanded and has beds, add individual bed rows
        if (presenter.isRoomExpanded(room.getId()) && !room.getBeds().isEmpty()) {
            for (GanttBedData bed : room.getBeds()) {
                rowIndex = buildBedRow(bed, rowIndex, dateColumns);
            }
        }

        return rowIndex;
    }

    /**
     * Builds an individual bed row (shown when room is expanded)
     */
    private int buildBedRow(GanttBedData bed, int rowIndex, List<DateColumn> dateColumns) {
        int colIndex = 0;

        // Bed name cell (indented to show hierarchy)
        calendarGrid.add(cellFactory.createBedCell(bed), colIndex++, rowIndex);

        // Date cells for this bed
        for (DateColumn dateCol : dateColumns) {
            calendarGrid.add(cellFactory.createBedDateCell(bed, dateCol), colIndex++, rowIndex);
        }

        return rowIndex + 1;
    }

    /**
     * Updates the date range label in the header
     */
    private void updateDateRangeLabel() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
        String rangeText = presenter.getTimeWindowStart().format(formatter) + " - " +
                presenter.getTimeWindowEnd().format(formatter);
        dateRangeLabel.setText(rangeText);
    }

    /**
     * Refreshes the view (called after navigation)
     */
    private void refresh() {
        updateDateRangeLabel();
        // Note: displayRooms() needs to be called externally with updated data
    }

    /**
     * Returns the root container node
     */
    public Node getNode() {
        return container;
    }

    /**
     * Gets the presenter (for external access)
     */
    public GanttPresenter getPresenter() {
        return presenter;
    }
}
