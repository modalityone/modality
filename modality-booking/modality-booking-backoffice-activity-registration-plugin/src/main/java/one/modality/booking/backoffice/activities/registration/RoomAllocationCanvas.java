package one.modality.booking.backoffice.activities.registration;

import dev.webfx.extras.canvas.bar.BarDrawer;
import dev.webfx.extras.time.layout.bar.LocalDateBar;
import dev.webfx.extras.time.layout.canvas.LocalDateCanvasDrawer;
import dev.webfx.extras.time.layout.gantt.HeaderPosition;
import dev.webfx.extras.time.layout.gantt.LocalDateGanttLayout;
import dev.webfx.extras.time.layout.gantt.canvas.ParentsCanvasDrawer;
import dev.webfx.kit.util.properties.FXProperties;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static one.modality.booking.backoffice.activities.registration.RegistrationStyles.*;

/**
 * Canvas-based Gantt chart for room/bed allocation.
 * <p>
 * Shows:
 * - Rooms/beds on the left as row headers
 * - Timeline showing dates across the top
 * - Occupancy bars for each bed showing existing bookings
 * - Highlighted current booking position
 * - Selection capability for room/bed assignment
 * <p>
 * Based on RegistrationDashboardFull.jsx RoomAllocationModal (lines 3055-3350)
 * and HouseholdGanttCanvas.java canvas rendering pattern.
 *
 * @author Claude Code
 */
public class RoomAllocationCanvas {

    // Layout constants
    private static final double ROW_HEIGHT = 36;
    private static final double BAR_HEIGHT = 24;
    private static final double BAR_RADIUS = 6;
    private static final double ROOM_HEADER_WIDTH = 160;
    private static final double DAY_WIDTH = 50;
    private static final double DATE_HEADER_HEIGHT = 40;

    // Colors
    private static final Color AVAILABLE_COLOR = Color.web("#dcfce7");  // Light green
    private static final Color OCCUPIED_COLOR = Color.web("#fee2e2");   // Light red
    private static final Color CURRENT_BOOKING_COLOR = Color.web("#dbeafe");  // Light blue
    private static final Color SELECTED_COLOR = Color.web("#c7d2fe");   // Light indigo
    private static final Color BOOKING_BAR_COLOR = Color.web("#6366f1"); // Indigo
    private static final Color CURRENT_BAR_COLOR = Color.web("#3b82f6"); // Blue
    private static final Color GRID_COLOR = Color.web("#e5e7eb");        // Light grey
    private static final Color HEADER_BG_COLOR = Color.web("#f9fafb");   // Very light grey

    // Booking dates
    private LocalDate bookingStartDate;
    private LocalDate bookingEndDate;

    // Timeline dates (5 days buffer on each side)
    private LocalDate timelineStart;
    private LocalDate timelineEnd;
    private List<LocalDate> timelineDates = new ArrayList<>();

    // Room/bed data
    private List<RoomRow> rows = new ArrayList<>();

    // Selection state
    private final ObjectProperty<RoomAllocation> selectedAllocationProperty = new SimpleObjectProperty<>();
    private RoomAllocation currentAllocation;

    // Hover state
    private int hoveredRowIndex = -1;

    // Canvas
    private Canvas canvas;
    private double canvasWidth;
    private double canvasHeight;

    // Bar drawer for booking bars
    private final BarDrawer barDrawer = new BarDrawer()
            .setTextFill(Color.WHITE)
            .setRadius(BAR_RADIUS);

    /**
     * Data class representing a room/bed row in the Gantt.
     */
    public static class RoomRow {
        public final String roomId;
        public final String roomName;
        public final int bedIndex;
        public final String bedName;
        public final boolean isHeader;  // True for multi-bed room headers
        public final List<Occupancy> occupancies = new ArrayList<>();

        public RoomRow(String roomId, String roomName, int bedIndex, String bedName, boolean isHeader) {
            this.roomId = roomId;
            this.roomName = roomName;
            this.bedIndex = bedIndex;
            this.bedName = bedName;
            this.isHeader = isHeader;
        }
    }

    /**
     * Data class representing an occupancy (booking bar).
     */
    public static class Occupancy {
        public final String guestName;
        public final LocalDate startDate;
        public final LocalDate endDate;
        public final boolean isCurrentBooking;

        public Occupancy(String guestName, LocalDate startDate, LocalDate endDate, boolean isCurrentBooking) {
            this.guestName = guestName;
            this.startDate = startDate;
            this.endDate = endDate;
            this.isCurrentBooking = isCurrentBooking;
        }
    }

    /**
     * Data class representing a room allocation selection.
     */
    public static class RoomAllocation {
        public final String roomId;
        public final int bedIndex;

        public RoomAllocation(String roomId, int bedIndex) {
            this.roomId = roomId;
            this.bedIndex = bedIndex;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof RoomAllocation other) {
                return roomId.equals(other.roomId) && bedIndex == other.bedIndex;
            }
            return false;
        }
    }

    public RoomAllocationCanvas() {
    }

    /**
     * Initializes the canvas with booking dates and builds the timeline.
     */
    public void initialize(LocalDate bookingStart, LocalDate bookingEnd, RoomAllocation currentAllocation) {
        this.bookingStartDate = bookingStart;
        this.bookingEndDate = bookingEnd;
        this.currentAllocation = currentAllocation;

        // Build timeline with 5-day buffer on each side
        this.timelineStart = bookingStart.minusDays(5);
        this.timelineEnd = bookingEnd.plusDays(5);

        this.timelineDates.clear();
        LocalDate current = timelineStart;
        while (!current.isAfter(timelineEnd)) {
            timelineDates.add(current);
            current = current.plusDays(1);
        }

        // Initialize selected allocation to current
        if (currentAllocation != null) {
            selectedAllocationProperty.set(currentAllocation);
        }
    }

    /**
     * Sets the room/bed rows to display.
     */
    public void setRows(List<RoomRow> rows) {
        this.rows = rows;
        updateCanvasSize();
        redraw();
    }

    /**
     * Builds the canvas UI.
     */
    public Node buildUi() {
        VBox container = new VBox();

        // Calculate canvas size
        updateCanvasSize();

        // Create canvas
        canvas = new Canvas(canvasWidth, canvasHeight);
        canvas.setOnMouseMoved(e -> handleMouseMove(e.getX(), e.getY()));
        canvas.setOnMouseClicked(e -> handleMouseClick(e.getX(), e.getY()));
        canvas.setOnMouseExited(e -> {
            hoveredRowIndex = -1;
            redraw();
        });

        // Wrap in ScrollPane for large room lists
        ScrollPane scrollPane = new ScrollPane(canvas);
        scrollPane.setFitToWidth(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefHeight(400);
        scrollPane.setMaxHeight(500);

        container.getChildren().add(scrollPane);

        // Initial draw
        redraw();

        return container;
    }

    /**
     * Updates canvas size based on data.
     */
    private void updateCanvasSize() {
        canvasWidth = ROOM_HEADER_WIDTH + (timelineDates.size() * DAY_WIDTH);
        canvasHeight = DATE_HEADER_HEIGHT + (rows.size() * ROW_HEIGHT);

        if (canvas != null) {
            canvas.setWidth(canvasWidth);
            canvas.setHeight(canvasHeight);
        }
    }

    /**
     * Redraws the entire canvas.
     */
    public void redraw() {
        if (canvas == null) return;

        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Clear canvas
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvasWidth, canvasHeight);

        // Draw components
        drawDateHeaders(gc);
        drawRoomHeaders(gc);
        drawGrid(gc);
        drawBookingPeriodHighlight(gc);
        drawOccupancyBars(gc);
        drawHoveredRowHighlight(gc);
        drawSelectedRowHighlight(gc);
    }

    /**
     * Draws the date header row.
     */
    private void drawDateHeaders(GraphicsContext gc) {
        gc.setFill(HEADER_BG_COLOR);
        gc.fillRect(ROOM_HEADER_WIDTH, 0, canvasWidth - ROOM_HEADER_WIDTH, DATE_HEADER_HEIGHT);

        gc.setStroke(GRID_COLOR);
        gc.setLineWidth(1);

        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("d");
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM");

        for (int i = 0; i < timelineDates.size(); i++) {
            LocalDate date = timelineDates.get(i);
            double x = ROOM_HEADER_WIDTH + (i * DAY_WIDTH);

            // Draw vertical line
            gc.strokeLine(x, 0, x, canvasHeight);

            // Draw date text
            gc.setFill(TEXT);
            gc.setFont(Font.font("System", FontWeight.NORMAL, 11));
            gc.setTextAlign(TextAlignment.CENTER);

            String dayText = date.format(dayFormatter);
            gc.fillText(dayText, x + DAY_WIDTH / 2, DATE_HEADER_HEIGHT / 2);

            // Draw month on first day or when month changes
            if (i == 0 || date.getDayOfMonth() == 1) {
                gc.setFont(Font.font("System", FontWeight.BOLD, 9));
                gc.setFill(TEXT_MUTED);
                gc.fillText(date.format(monthFormatter), x + DAY_WIDTH / 2, DATE_HEADER_HEIGHT / 2 + 14);
            }

            // Highlight weekends
            if (date.getDayOfWeek().getValue() >= 6) {
                gc.setFill(Color.web("#f3f4f6"));
                gc.fillRect(x, DATE_HEADER_HEIGHT, DAY_WIDTH, canvasHeight - DATE_HEADER_HEIGHT);
            }
        }

        // Draw bottom border of header
        gc.strokeLine(ROOM_HEADER_WIDTH, DATE_HEADER_HEIGHT, canvasWidth, DATE_HEADER_HEIGHT);
    }

    /**
     * Draws the room/bed headers on the left.
     */
    private void drawRoomHeaders(GraphicsContext gc) {
        gc.setFill(HEADER_BG_COLOR);
        gc.fillRect(0, 0, ROOM_HEADER_WIDTH, canvasHeight);

        gc.setStroke(GRID_COLOR);
        gc.setLineWidth(1);

        for (int i = 0; i < rows.size(); i++) {
            RoomRow row = rows.get(i);
            double y = DATE_HEADER_HEIGHT + (i * ROW_HEIGHT);

            // Draw horizontal line
            gc.strokeLine(0, y, ROOM_HEADER_WIDTH, y);

            // Draw room/bed name
            gc.setTextAlign(TextAlignment.LEFT);
            if (row.isHeader) {
                // Room header - bold
                gc.setFont(Font.font("System", FontWeight.BOLD, 12));
                gc.setFill(TEXT);
                gc.fillText(row.roomName, 12, y + ROW_HEIGHT / 2 + 4);
            } else {
                // Bed row - indented
                gc.setFont(Font.font("System", FontWeight.NORMAL, 11));
                gc.setFill(TEXT_SECONDARY);
                String label = row.bedName != null ? row.bedName : row.roomName;
                gc.fillText(label, row.bedIndex >= 0 ? 24 : 12, y + ROW_HEIGHT / 2 + 4);
            }
        }

        // Draw right border of room header column
        gc.strokeLine(ROOM_HEADER_WIDTH, 0, ROOM_HEADER_WIDTH, canvasHeight);
    }

    /**
     * Draws the grid lines.
     */
    private void drawGrid(GraphicsContext gc) {
        gc.setStroke(GRID_COLOR);
        gc.setLineWidth(0.5);

        // Horizontal lines for each row
        for (int i = 0; i <= rows.size(); i++) {
            double y = DATE_HEADER_HEIGHT + (i * ROW_HEIGHT);
            gc.strokeLine(ROOM_HEADER_WIDTH, y, canvasWidth, y);
        }
    }

    /**
     * Highlights the booking period columns.
     */
    private void drawBookingPeriodHighlight(GraphicsContext gc) {
        if (bookingStartDate == null || bookingEndDate == null) return;

        int startIndex = getDateIndex(bookingStartDate);
        int endIndex = getDateIndex(bookingEndDate);

        if (startIndex >= 0 && endIndex >= 0) {
            double x = ROOM_HEADER_WIDTH + (startIndex * DAY_WIDTH);
            double width = (endIndex - startIndex) * DAY_WIDTH;

            gc.setFill(Color.web("#fef3c7", 0.4)); // Light amber
            gc.fillRect(x, DATE_HEADER_HEIGHT, width, canvasHeight - DATE_HEADER_HEIGHT);
        }
    }

    /**
     * Draws occupancy bars for all rows.
     */
    private void drawOccupancyBars(GraphicsContext gc) {
        for (int i = 0; i < rows.size(); i++) {
            RoomRow row = rows.get(i);
            if (row.isHeader) continue; // Skip headers

            double y = DATE_HEADER_HEIGHT + (i * ROW_HEIGHT) + (ROW_HEIGHT - BAR_HEIGHT) / 2;

            for (Occupancy occ : row.occupancies) {
                int startIndex = getDateIndex(occ.startDate);
                int endIndex = getDateIndex(occ.endDate);

                if (startIndex < 0) startIndex = 0;
                if (endIndex > timelineDates.size()) endIndex = timelineDates.size();
                if (startIndex >= endIndex) continue;

                double x = ROOM_HEADER_WIDTH + (startIndex * DAY_WIDTH);
                double width = (endIndex - startIndex) * DAY_WIDTH;

                // Draw bar
                Color barColor = occ.isCurrentBooking ? CURRENT_BAR_COLOR : BOOKING_BAR_COLOR;
                gc.setFill(barColor);
                gc.fillRoundRect(x + 2, y, width - 4, BAR_HEIGHT, BAR_RADIUS, BAR_RADIUS);

                // Draw guest name
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font("System", FontWeight.NORMAL, 10));
                gc.setTextAlign(TextAlignment.LEFT);

                // Clip text if too long
                String name = occ.guestName;
                if (name.length() > 12 && width < 100) {
                    name = name.substring(0, 10) + "...";
                }
                gc.fillText(name, x + 6, y + BAR_HEIGHT / 2 + 3);
            }
        }
    }

    /**
     * Draws hover highlight for the current row.
     */
    private void drawHoveredRowHighlight(GraphicsContext gc) {
        if (hoveredRowIndex >= 0 && hoveredRowIndex < rows.size()) {
            RoomRow row = rows.get(hoveredRowIndex);
            if (!row.isHeader) {
                double y = DATE_HEADER_HEIGHT + (hoveredRowIndex * ROW_HEIGHT);
                gc.setFill(Color.web("#f0f9ff", 0.6)); // Very light blue
                gc.fillRect(0, y, canvasWidth, ROW_HEIGHT);
            }
        }
    }

    /**
     * Draws selected row highlight.
     */
    private void drawSelectedRowHighlight(GraphicsContext gc) {
        RoomAllocation selected = selectedAllocationProperty.get();
        if (selected == null) return;

        for (int i = 0; i < rows.size(); i++) {
            RoomRow row = rows.get(i);
            if (row.roomId.equals(selected.roomId) && row.bedIndex == selected.bedIndex) {
                double y = DATE_HEADER_HEIGHT + (i * ROW_HEIGHT);
                gc.setStroke(PRIMARY);
                gc.setLineWidth(2);
                gc.strokeRect(1, y + 1, canvasWidth - 2, ROW_HEIGHT - 2);
                break;
            }
        }
    }

    /**
     * Handles mouse movement for hover effects.
     */
    private void handleMouseMove(double x, double y) {
        int rowIndex = getRowIndexAtY(y);
        if (rowIndex != hoveredRowIndex) {
            hoveredRowIndex = rowIndex;
            redraw();
        }
    }

    /**
     * Handles mouse click for selection.
     */
    private void handleMouseClick(double x, double y) {
        int rowIndex = getRowIndexAtY(y);
        if (rowIndex >= 0 && rowIndex < rows.size()) {
            RoomRow row = rows.get(rowIndex);
            if (!row.isHeader) {
                // Check if this bed is available
                if (isAvailable(row)) {
                    selectedAllocationProperty.set(new RoomAllocation(row.roomId, row.bedIndex));
                    redraw();
                }
            }
        }
    }

    /**
     * Checks if a bed is available for the booking period.
     */
    private boolean isAvailable(RoomRow row) {
        if (bookingStartDate == null || bookingEndDate == null) return true;

        for (Occupancy occ : row.occupancies) {
            if (occ.isCurrentBooking) continue; // Ignore current booking

            // Check for overlap
            boolean overlaps = bookingStartDate.isBefore(occ.endDate) && bookingEndDate.isAfter(occ.startDate);
            if (overlaps) return false;
        }
        return true;
    }

    /**
     * Gets the row index at a Y coordinate.
     */
    private int getRowIndexAtY(double y) {
        if (y < DATE_HEADER_HEIGHT) return -1;
        return (int) ((y - DATE_HEADER_HEIGHT) / ROW_HEIGHT);
    }

    /**
     * Gets the index of a date in the timeline.
     */
    private int getDateIndex(LocalDate date) {
        if (date == null || timelineStart == null) return -1;
        return (int) ChronoUnit.DAYS.between(timelineStart, date);
    }

    /**
     * Gets the selected allocation property.
     */
    public ObjectProperty<RoomAllocation> selectedAllocationProperty() {
        return selectedAllocationProperty;
    }

    /**
     * Gets the currently selected allocation.
     */
    public RoomAllocation getSelectedAllocation() {
        return selectedAllocationProperty.get();
    }
}
