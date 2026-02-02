package one.modality.booking.backoffice.activities.registration;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.entity.Entities;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import one.modality.base.shared.domainmodel.formatters.PriceFormatter;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.ItemFamily;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.base.shared.knownitems.KnownItemFamily;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.BiConsumer;

import static one.modality.booking.backoffice.activities.registration.RegistrationStyles.*;

/**
 * Canvas-based timeline for displaying booking options over the event period.
 * <p>
 * Features:
 * - Day header row with day numbers and weekday initials
 * - Event period bar (violet)
 * - Booking period bar (green)
 * - Line item rows with colored cells for covered days
 * <p>
 * Based on RegistrationDashboardFull.jsx Timeline section (lines 4200-4500).
 *
 * @author Claude Code
 */
public class BookingTimelineCanvas extends Region {

    // Layout constants (from JSX ux object) - increased for better visibility
    private static final int ROW_HEADER_WIDTH = 160;   // Left side for option name
    private static final int DAY_COLUMN_WIDTH = 50;    // Wider day columns for better gantt visibility
    private static final int HEADER_HEIGHT = 45;
    private static final int PERIOD_BAR_HEIGHT = 28;
    private static final int ROW_HEIGHT = 36;          // Taller rows for better readability
    private static final int CELL_HEIGHT = 22;         // Taller cells
    private static final int CELL_GAP = 2;             // Gap between day cells (reduced for tighter bars)
    private static final int CELL_PADDING = CELL_GAP / 2; // Half gap on each side of cell (1 pixel)
    private static final int CELL_RADIUS = 6;
    private static final int PRICE_COLUMN_WIDTH = 80;  // Right side for price
    private static final int ACTION_COLUMN_WIDTH = 90; // Right side for action buttons (edit, cancel, delete)
    private static final int ICON_SIZE = 18;           // Category icon size
    private static final int ACTION_ICON_SIZE = 16;    // Action button icon size

    // Color constants for excluded days and hover
    private static final Color EXCLUDED_CELL_BG = Color.WHITE;  // Empty/gap cells are white
    private static final Color HOVER_BG = Color.web("#1e3a5f");
    private static final Color GRID_LINE_COLOR = Color.web("#e8e4df"); // Light vertical grid lines

    // Data properties
    private final ObjectProperty<LocalDate> eventStartProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> eventEndProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> bookingStartProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> bookingEndProperty = new SimpleObjectProperty<>();
    private final StringProperty eventNameProperty = new SimpleStringProperty();
    private final ObjectProperty<Event> eventProperty = new SimpleObjectProperty<>();
    private final ObservableList<DocumentLine> documentLines = FXCollections.observableArrayList();

    // Attendance dates per line (key = DocumentLine ID primary key, value = set of attended dates)
    // This determines which days are "included" (colored) vs "excluded" (grey) in the gantt bar
    private Map<Object, Set<LocalDate>> attendanceDatesMap = new HashMap<>();

    // Original attendance dates from database (used to distinguish gaps from pending removals)
    // A cross (X) is only shown when a date was originally included but is now being removed
    private Map<Object, Set<LocalDate>> originalAttendanceDatesMap = new HashMap<>();

    // Pending status for lines that are being cancelled/deleted but not yet saved
    // Key = DocumentLine ID primary key, Value = "cancelled" or "deleted"
    private Map<Object, String> pendingStatusMap = new HashMap<>();

    // Computed prices for lines (key = DocumentLine ID primary key, value = price in cents)
    // Used to display updated prices after attendance changes (before saving)
    private Map<Object, Integer> computedPricesMap = new HashMap<>();

    // Hover state
    private int hoveredRowIndex = -1;
    private int hoveredDayIndex = -1;
    private String hoveredAction = null; // "cancel" or "delete" or null

    // Callback for toggle events
    private BiConsumer<DocumentLine, LocalDate> onDayToggled;
    // Callbacks for action buttons
    private java.util.function.Consumer<DocumentLine> onCancelClicked;
    private java.util.function.Consumer<DocumentLine> onDeleteClicked;
    private java.util.function.Consumer<DocumentLine> onRestoreClicked;
    private java.util.function.Consumer<DocumentLine> onEditClicked;

    // Cancellation check predicate - checks if a DocumentLine is cancelled using WorkingBooking API
    // This is set by BookingTab to use workingBooking.isDocumentLineCancelled()
    private java.util.function.Predicate<DocumentLine> cancellationChecker;

    // Canvas
    private Canvas canvas;

    public BookingTimelineCanvas() {
        // Create canvas
        canvas = new Canvas();
        getChildren().add(canvas);

        // Set up repaint on changes
        FXProperties.runOnPropertiesChange(this::requestRepaint,
            eventStartProperty, eventEndProperty, bookingStartProperty, bookingEndProperty);

        documentLines.addListener((ListChangeListener<DocumentLine>) c -> {
            requestLayout(); // Request layout to update canvas size for new line count
            requestRepaint();
        });

        // Set up mouse event handlers for click-to-toggle functionality
        canvas.setOnMouseMoved(this::handleMouseMove);
        canvas.setOnMouseClicked(this::handleMouseClick);
        canvas.setOnMouseExited(e -> {
            hoveredRowIndex = -1;
            hoveredDayIndex = -1;
            requestRepaint();
        });

        // Initial sizing (period bars removed)
        setMinHeight(HEADER_HEIGHT + ROW_HEIGHT);
    }

    /**
     * Requests a repaint of the canvas.
     */
    private void requestRepaint() {
        if (canvas != null && canvas.getWidth() > 0 && canvas.getHeight() > 0) {
            draw();
        }
    }

    // ===== Mouse Event Handling =====

    /**
     * Handles mouse movement for hover effects.
     */
    private void handleMouseMove(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();

        // Calculate which row and day the mouse is over
        int newRowIndex = getRowIndexAtY(y);
        int newDayIndex = getDayIndexAtX(x);
        String newAction = getActionAtX(x, newRowIndex);

        if (newRowIndex != hoveredRowIndex || newDayIndex != hoveredDayIndex || !Objects.equals(newAction, hoveredAction)) {
            hoveredRowIndex = newRowIndex;
            hoveredDayIndex = newDayIndex;
            hoveredAction = newAction;

            // Update cursor based on whether something is clickable
            if (newAction != null || isCellClickable(newRowIndex, newDayIndex)) {
                canvas.setCursor(Cursor.HAND);
            } else {
                canvas.setCursor(Cursor.DEFAULT);
            }

            requestRepaint();
        }
    }

    /**
     * Handles mouse click for toggling days or action buttons.
     */
    private void handleMouseClick(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();

        int rowIndex = getRowIndexAtY(y);
        int dayIndex = getDayIndexAtX(x);
        String action = getActionAtX(x, rowIndex);

        // Handle action button clicks
        if (action != null && rowIndex >= 0 && rowIndex < documentLines.size()) {
            DocumentLine line = documentLines.get(rowIndex);

            // If line has pending status, clicking action area triggers restore
            String pendingStatus = getPendingStatus(line);
            if (pendingStatus != null) {
                if (onRestoreClicked != null) {
                    onRestoreClicked.accept(line);
                }
                return;
            }

            // Normal edit/cancel/delete handling
            if ("edit".equals(action) && onEditClicked != null) {
                onEditClicked.accept(line);
            } else if ("cancel".equals(action) && onCancelClicked != null) {
                onCancelClicked.accept(line);
            } else if ("delete".equals(action) && onDeleteClicked != null) {
                onDeleteClicked.accept(line);
            }
            return;
        }

        // Handle day cell clicks
        if (isCellClickable(rowIndex, dayIndex)) {
            DocumentLine line = documentLines.get(rowIndex);
            LocalDate date = getDateForDayIndex(dayIndex);

            toggleDay(line, date);
        }
    }

    /**
     * Gets the row index at a Y coordinate.
     * @return row index or -1 if not over a row
     */
    private int getRowIndexAtY(double y) {
        double contentY = y - HEADER_HEIGHT; // Period bars removed
        if (contentY < 0) return -1;

        int rowIndex = (int) (contentY / ROW_HEIGHT);
        if (rowIndex >= documentLines.size()) return -1;
        return rowIndex;
    }

    /**
     * Gets the day index at an X coordinate.
     * @return day index or -1 if not over a day column
     */
    private int getDayIndexAtX(double x) {
        double contentX = x - ROW_HEADER_WIDTH;
        if (contentX < 0) return -1;

        LocalDate eventStart = eventStartProperty.get();
        LocalDate eventEnd = eventEndProperty.get();
        if (eventStart == null || eventEnd == null) return -1;

        int dayCount = (int) ChronoUnit.DAYS.between(eventStart, eventEnd) + 1;
        int dayIndex = (int) (contentX / DAY_COLUMN_WIDTH);
        if (dayIndex >= dayCount) return -1;
        return dayIndex;
    }

    /**
     * Gets the date for a day index.
     */
    private LocalDate getDateForDayIndex(int dayIndex) {
        LocalDate eventStart = eventStartProperty.get();
        if (eventStart == null || dayIndex < 0) return null;
        return eventStart.plusDays(dayIndex);
    }

    /**
     * Gets the action at an X coordinate for a given row.
     * @return "edit", "cancel", "delete", or null if not over an action button
     */
    private String getActionAtX(double x, int rowIndex) {
        if (rowIndex < 0 || rowIndex >= documentLines.size()) return null;

        LocalDate eventStart = eventStartProperty.get();
        LocalDate eventEnd = eventEndProperty.get();
        if (eventStart == null || eventEnd == null) return null;

        int dayCount = (int) ChronoUnit.DAYS.between(eventStart, eventEnd) + 1;
        double actionAreaStart = ROW_HEADER_WIDTH + dayCount * DAY_COLUMN_WIDTH + PRICE_COLUMN_WIDTH;
        double actionAreaEnd = actionAreaStart + ACTION_COLUMN_WIDTH;

        if (x >= actionAreaStart && x < actionAreaEnd) {
            // Determine which button (edit on left, cancel in middle, delete on right)
            double buttonWidth = ACTION_COLUMN_WIDTH / 3.0;
            double relativeX = x - actionAreaStart;
            if (relativeX < buttonWidth) {
                return "edit";
            } else if (relativeX < buttonWidth * 2) {
                return "cancel";
            } else {
                return "delete";
            }
        }
        return null;
    }

    /**
     * Checks if a cell is clickable (within the line's date range, not cancelled, and no pending status).
     */
    private boolean isCellClickable(int rowIndex, int dayIndex) {
        if (rowIndex < 0 || dayIndex < 0) return false;
        if (rowIndex >= documentLines.size()) return false;

        DocumentLine line = documentLines.get(rowIndex);
        LocalDate date = getDateForDayIndex(dayIndex);
        if (date == null) return false;

        // Check if line is cancelled or has pending status
        if (isLineCancelled(line)) return false;
        if (getPendingStatus(line) != null) return false;

        // Check if date is within line's range
        LocalDate lineStart = line.getStartDate();
        LocalDate lineEnd = line.getEndDate();
        if (lineStart == null) lineStart = bookingStartProperty.get();
        if (lineEnd == null) lineEnd = bookingEndProperty.get();
        if (lineStart == null || lineEnd == null) return false;

        return !date.isBefore(lineStart) && !date.isAfter(lineEnd);
    }

    /**
     * Toggles a specific day for a line.
     * Just fires the callback - BookingTab manages the actual attendance state.
     */
    private void toggleDay(DocumentLine line, LocalDate date) {
        // Notify callback - BookingTab will handle the actual state change
        // and call setAttendanceDates() to update the canvas
        if (onDayToggled != null) {
            onDayToggled.accept(line, date);
        }
    }

    // ===== Attendance Dates API =====

    /**
     * Sets the attendance dates map for all document lines.
     * This determines which days to draw cells for each line item.
     */
    public void setAttendanceDates(Map<Object, Set<LocalDate>> attendanceDates) {
        this.attendanceDatesMap = attendanceDates != null ? attendanceDates : new HashMap<>();
        requestRepaint();
    }

    /**
     * Gets the attendance dates for a document line.
     */
    public Set<LocalDate> getAttendanceDates(DocumentLine line) {
        Object lineId = line.getId() != null ? line.getId().getPrimaryKey() : null;
        if (lineId != null) {
            return attendanceDatesMap.getOrDefault(lineId, Collections.emptySet());
        }
        return Collections.emptySet();
    }

    /**
     * Checks if a line has any attendance dates.
     */
    public boolean hasAttendanceDates(DocumentLine line) {
        return !getAttendanceDates(line).isEmpty();
    }

    /**
     * Checks if a specific day is included for a line.
     * A day is included if it's in the attendance dates set.
     * If no attendance dates exist, falls back to the line's date range.
     */
    public boolean isDayIncluded(DocumentLine line, LocalDate date) {
        Set<LocalDate> attendanceDates = getAttendanceDates(line);

        // If we have attendance dates, check if this date is included
        if (!attendanceDates.isEmpty()) {
            return attendanceDates.contains(date);
        }

        // Fallback: use line dates or booking dates (for lines without attendance records)
        LocalDate lineStart = line.getStartDate();
        LocalDate lineEnd = line.getEndDate();

        // Default to booking dates if not set
        if (lineStart == null) lineStart = bookingStartProperty.get();
        if (lineEnd == null) lineEnd = bookingEndProperty.get();
        if (lineStart == null || lineEnd == null) return false;

        // Check if date is within line's range
        return !date.isBefore(lineStart) && !date.isAfter(lineEnd);
    }

    /**
     * Sets the original attendance dates from the database.
     * Used to distinguish between gaps (never selected) and pending removals.
     * Creates a deep copy to prevent modifications from affecting the original data.
     */
    public void setOriginalAttendanceDates(Map<Object, Set<LocalDate>> originalDates) {
        // Deep copy to prevent external modifications from affecting original dates
        this.originalAttendanceDatesMap = new HashMap<>();
        if (originalDates != null) {
            for (Map.Entry<Object, Set<LocalDate>> entry : originalDates.entrySet()) {
                this.originalAttendanceDatesMap.put(entry.getKey(), new HashSet<>(entry.getValue()));
            }
        }
        // Also copy to current attendance dates initially
        if (attendanceDatesMap.isEmpty() && !originalAttendanceDatesMap.isEmpty()) {
            this.attendanceDatesMap = new HashMap<>();
            for (Map.Entry<Object, Set<LocalDate>> entry : originalAttendanceDatesMap.entrySet()) {
                this.attendanceDatesMap.put(entry.getKey(), new HashSet<>(entry.getValue()));
            }
        }
        requestRepaint();
    }

    /**
     * Checks if a date was originally included in the saved attendance data.
     * Returns true if the date was in the database, false if it was a gap.
     */
    public boolean wasOriginallyIncluded(DocumentLine line, LocalDate date) {
        Object lineId = line.getId() != null ? line.getId().getPrimaryKey() : null;
        if (lineId != null) {
            Set<LocalDate> originalDates = originalAttendanceDatesMap.get(lineId);
            if (originalDates != null) {
                return originalDates.contains(date);
            }
        }
        return false;
    }

    /**
     * Sets the callback for when a day is toggled.
     * The callback receives the DocumentLine and the toggled LocalDate.
     */
    public void setOnDayToggled(BiConsumer<DocumentLine, LocalDate> callback) {
        this.onDayToggled = callback;
    }

    /**
     * Sets the callback for when the cancel button is clicked on a line.
     */
    public void setOnCancelClicked(java.util.function.Consumer<DocumentLine> callback) {
        this.onCancelClicked = callback;
    }

    /**
     * Sets the callback for when the delete button is clicked on a line.
     */
    public void setOnDeleteClicked(java.util.function.Consumer<DocumentLine> callback) {
        this.onDeleteClicked = callback;
    }

    /**
     * Sets the callback for when the restore button is clicked on a line with pending status.
     */
    public void setOnRestoreClicked(java.util.function.Consumer<DocumentLine> callback) {
        this.onRestoreClicked = callback;
    }

    /**
     * Sets the callback for when the edit button is clicked on a line.
     */
    public void setOnEditClicked(java.util.function.Consumer<DocumentLine> callback) {
        this.onEditClicked = callback;
    }

    /**
     * Sets the predicate for checking if a DocumentLine is cancelled.
     * This should be set to use workingBooking.isDocumentLineCancelled() for accurate cancellation detection.
     */
    public void setCancellationChecker(java.util.function.Predicate<DocumentLine> checker) {
        this.cancellationChecker = checker;
        requestRepaint(); // Repaint to reflect any changes in cancellation status
    }

    /**
     * Checks if a DocumentLine is cancelled using the cancellation checker predicate.
     * Falls back to checking line.isCancelled() if no checker is set.
     */
    private boolean isLineCancelled(DocumentLine line) {
        if (cancellationChecker != null) {
            return cancellationChecker.test(line);
        }
        // Fallback to entity property (may not be reliable if not loaded from DB)
        return Boolean.TRUE.equals(line.isCancelled());
    }

    // ===== Pending Status API =====

    /**
     * Sets the pending status for a document line (before saving).
     * @param line the document line
     * @param status "cancelled", "deleted", or null to clear
     */
    public void setPendingStatus(DocumentLine line, String status) {
        Object lineId = line.getId() != null ? line.getId().getPrimaryKey() : null;
        if (lineId != null) {
            if (status != null) {
                pendingStatusMap.put(lineId, status);
            } else {
                pendingStatusMap.remove(lineId);
            }
            requestRepaint();
        }
    }

    /**
     * Gets the pending status for a document line.
     * @return "cancelled", "deleted", or null if no pending status
     */
    public String getPendingStatus(DocumentLine line) {
        Object lineId = line.getId() != null ? line.getId().getPrimaryKey() : null;
        if (lineId != null) {
            return pendingStatusMap.get(lineId);
        }
        return null;
    }

    /**
     * Checks if a line has pending cancellation.
     */
    public boolean isPendingCancelled(DocumentLine line) {
        return "cancelled".equals(getPendingStatus(line));
    }

    /**
     * Checks if a line has pending deletion.
     */
    public boolean isPendingDeleted(DocumentLine line) {
        return "deleted".equals(getPendingStatus(line));
    }

    /**
     * Clears all pending statuses (e.g., after save or cancel).
     */
    public void clearPendingStatuses() {
        pendingStatusMap.clear();
        requestRepaint();
    }

    /**
     * Gets all pending statuses.
     */
    public Map<Object, String> getPendingStatuses() {
        return new HashMap<>(pendingStatusMap);
    }

    // ===== Computed Prices API =====

    /**
     * Sets the computed prices map for document lines.
     * Used to display updated prices after attendance changes.
     * @param prices map of line ID to price in cents
     */
    public void setComputedPrices(Map<Object, Integer> prices) {
        this.computedPricesMap = prices != null ? new HashMap<>(prices) : new HashMap<>();
        requestRepaint();
    }

    /**
     * Gets the computed price for a line, or null if not set.
     */
    public Integer getComputedPrice(DocumentLine line) {
        Object lineId = line.getId() != null ? line.getId().getPrimaryKey() : null;
        if (lineId != null) {
            return computedPricesMap.get(lineId);
        }
        return null;
    }

    /**
     * Sets the computed price for a single line.
     */
    public void setComputedPrice(DocumentLine line, Integer price) {
        Object lineId = line.getId() != null ? line.getId().getPrimaryKey() : null;
        if (lineId != null) {
            if (price != null) {
                computedPricesMap.put(lineId, price);
            } else {
                computedPricesMap.remove(lineId);
            }
            requestRepaint();
        }
    }

    @Override
    protected void layoutChildren() {
        double width = Math.max(getWidth(), computePrefWidth(-1));
        double height = calculateRequiredHeight();

        canvas.setWidth(width);
        canvas.setHeight(height);

        // Set the region's min size to match canvas
        setMinWidth(width);
        setMinHeight(height);

        draw();
    }

    @Override
    protected double computePrefWidth(double height) {
        // Calculate width based on event duration
        LocalDate eventStart = eventStartProperty.get();
        LocalDate eventEnd = eventEndProperty.get();
        if (eventStart != null && eventEnd != null) {
            int dayCount = (int) ChronoUnit.DAYS.between(eventStart, eventEnd) + 1;
            return ROW_HEADER_WIDTH + dayCount * DAY_COLUMN_WIDTH + PRICE_COLUMN_WIDTH + ACTION_COLUMN_WIDTH;
        }
        return ROW_HEADER_WIDTH + 7 * DAY_COLUMN_WIDTH + PRICE_COLUMN_WIDTH + ACTION_COLUMN_WIDTH; // Default 7 days
    }

    @Override
    protected double computePrefHeight(double width) {
        return calculateRequiredHeight();
    }

    @Override
    protected double computeMinWidth(double height) {
        return computePrefWidth(height);
    }

    @Override
    protected double computeMinHeight(double width) {
        return calculateRequiredHeight();
    }

    /**
     * Calculates the required height based on content.
     */
    private double calculateRequiredHeight() {
        int lineCount = documentLines.size();
        return HEADER_HEIGHT + (lineCount > 0 ? lineCount * ROW_HEIGHT : ROW_HEIGHT);
    }

    /**
     * Draws the entire timeline.
     */
    private void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        // Clear canvas
        gc.clearRect(0, 0, width, height);

        // Get dates
        LocalDate eventStart = eventStartProperty.get();
        LocalDate eventEnd = eventEndProperty.get();

        if (eventStart == null || eventEnd == null) {
            // Draw placeholder
            gc.setFill(TEXT_MUTED);
            gc.setFont(Font.font("System", 12));
            gc.fillText("No event dates set", width / 2 - 50, height / 2);
            return;
        }

        // Calculate number of days
        int dayCount = (int) ChronoUnit.DAYS.between(eventStart, eventEnd) + 1;

        // Draw vertical grid lines first (as background)
        drawVerticalGridLines(gc, dayCount, height);

        // Draw components
        double y = 0;

        // 1. Day headers
        drawDayHeaders(gc, eventStart, dayCount, y);
        y += HEADER_HEIGHT;

        // 2. Document line rows (event/booking period bars removed for cleaner UI)
        int rowIndex = 0;
        for (DocumentLine line : documentLines) {
            drawLineRow(gc, line, eventStart, dayCount, y, rowIndex);
            y += ROW_HEIGHT;
            rowIndex++;
        }

        // If no lines, draw empty state
        if (documentLines.isEmpty()) {
            gc.setFill(TEXT_MUTED);
            gc.setFont(Font.font("System", 12));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("No booking options selected", ROW_HEADER_WIDTH + (dayCount * DAY_COLUMN_WIDTH) / 2.0, y + ROW_HEIGHT / 2.0);
            gc.setTextAlign(TextAlignment.LEFT);
        }
    }

    /**
     * Draws the day header row with day numbers and weekday initials.
     */
    private void drawDayHeaders(GraphicsContext gc, LocalDate startDate, int dayCount, double y) {
        double x = ROW_HEADER_WIDTH;

        for (int i = 0; i < dayCount; i++) {
            LocalDate date = startDate.plusDays(i);

            // Day number
            gc.setFill(TEXT);
            gc.setFont(Font.font("System", FontWeight.SEMI_BOLD, 11));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(String.valueOf(date.getDayOfMonth()), x + DAY_COLUMN_WIDTH / 2.0, y + 16);

            // Weekday initial
            gc.setFill(TEXT_MUTED);
            gc.setFont(Font.font("System", 9));
            String weekdayInitial = date.getDayOfWeek().getDisplayName(TextStyle.NARROW, Locale.ENGLISH);
            gc.fillText(weekdayInitial, x + DAY_COLUMN_WIDTH / 2.0, y + 30);

            x += DAY_COLUMN_WIDTH;
        }

        gc.setTextAlign(TextAlignment.LEFT);
    }

    /**
     * Draws light vertical grid lines between day columns for easier visual separation.
     */
    private void drawVerticalGridLines(GraphicsContext gc, int dayCount, double height) {
        gc.setStroke(GRID_LINE_COLOR);
        gc.setLineWidth(1);

        // Draw vertical lines at the boundary between each day column
        for (int i = 1; i <= dayCount; i++) {
            double x = ROW_HEADER_WIDTH + i * DAY_COLUMN_WIDTH;
            gc.strokeLine(x, HEADER_HEIGHT, x, height);
        }
    }

    /**
     * Draws a period bar (event or booking).
     */
    private void drawPeriodBar(GraphicsContext gc, LocalDate periodStart, LocalDate periodEnd,
                               LocalDate eventStart, int dayCount, double y,
                               Color bgColor, Color textColor, String label) {

        // Calculate X positions
        int startDayOffset = (int) ChronoUnit.DAYS.between(eventStart, periodStart);
        int endDayOffset = (int) ChronoUnit.DAYS.between(eventStart, periodEnd);

        // Clamp to visible range
        startDayOffset = Math.max(0, startDayOffset);
        endDayOffset = Math.min(dayCount - 1, endDayOffset);

        if (startDayOffset > endDayOffset) return;

        double startX = ROW_HEADER_WIDTH + startDayOffset * DAY_COLUMN_WIDTH;
        double endX = ROW_HEADER_WIDTH + (endDayOffset + 1) * DAY_COLUMN_WIDTH;
        double barY = y + (PERIOD_BAR_HEIGHT - 8) / 2.0;

        // Draw bar background
        gc.setFill(bgColor);
        gc.fillRoundRect(startX, barY, endX - startX, 8, 4, 4);

        // Draw label
        gc.setFill(textColor);
        gc.setFont(Font.font("System", FontWeight.SEMI_BOLD, 10));
        gc.fillText(label, startX + 8, barY + 7);
    }

    /**
     * Draws a line item row with cells for each day.
     * Layout: [Category Icon + Option Name] [Gantt Cells...] [Price] [Actions]
     */
    private void drawLineRow(GraphicsContext gc, DocumentLine line, LocalDate eventStart, int dayCount, double y, int rowIndex) {
        // Determine category for coloring
        String category = getCategoryFromLine(line);
        Color cellColor = getCategoryBgColor(category);
        Color categoryFgColor = getCategoryFgColor(category);

        boolean isCancelled = isLineCancelled(line);
        boolean isLineHovered = (rowIndex == hoveredRowIndex);

        // Check for pending status (marked for cancel/delete but not yet saved)
        String pendingStatus = getPendingStatus(line);
        boolean isPendingCancelled = "cancelled".equals(pendingStatus);
        boolean isPendingDeleted = "deleted".equals(pendingStatus);
        boolean hasPendingStatus = isPendingCancelled || isPendingDeleted;

        // Combined inactive state: either already cancelled or pending cancel/delete
        boolean isInactive = isCancelled || hasPendingStatus;

        // Determine the date range to draw cells for.
        // Use line dates if available, otherwise fall back to booking dates.
        // The bar range stays constant - clicking toggles individual cells between
        // included (colored) and excluded (grey with X) without shrinking the bar.
        LocalDate lineStart = line.getStartDate();
        LocalDate lineEnd = line.getEndDate();

        // Fallback to booking dates (arrival/departure) to keep bar range constant
        if (lineStart == null) lineStart = bookingStartProperty.get();
        if (lineEnd == null) lineEnd = bookingEndProperty.get();

        // Determine if we have valid dates for drawing cells
        boolean hasDates = (lineStart != null && lineEnd != null);

        // Calculate cell positions (only if we have dates)
        int startDayOffset = 0;
        int endDayOffset = -1; // Will cause no cells to be drawn if hasDates is false
        if (hasDates) {
            startDayOffset = (int) ChronoUnit.DAYS.between(eventStart, lineStart);
            endDayOffset = (int) ChronoUnit.DAYS.between(eventStart, lineEnd);
            // Clamp to visible range
            startDayOffset = Math.max(0, startDayOffset);
            endDayOffset = Math.min(dayCount - 1, endDayOffset);
        }

        // Check if this is a night-based (accommodation) item
        boolean isNightBased = isAccommodationLine(line);

        // Apply reduced opacity for cancelled/pending lines
        if (isInactive) {
            gc.setGlobalAlpha(hasPendingStatus ? 0.6 : 0.5);
        }

        // ===== 1. Draw Row Header (Category Icon + Option Name) =====
        drawRowHeader(gc, line, category, categoryFgColor, y);

        // ===== 2. Draw Gantt Cells =====
        if (startDayOffset <= endDayOffset) {
            double cellY = y + (ROW_HEIGHT - CELL_HEIGHT) / 2.0;

            if (isNightBased) {
                // For accommodation: draw continuous bar from middle of arrival to middle of departure
                // This represents nights (check-in afternoon, check-out morning)
                drawAccommodationBar(gc, line, eventStart, startDayOffset, endDayOffset, cellY, cellColor, isLineHovered, isInactive);
            } else {
                // For non-accommodation: draw individual day cells
                for (int day = startDayOffset; day <= endDayOffset; day++) {
                    LocalDate cellDate = eventStart.plusDays(day);
                    boolean isIncluded = isDayIncluded(line, cellDate);
                    boolean isCellHovered = isLineHovered && (day == hoveredDayIndex);

                    double cellX = ROW_HEADER_WIDTH + day * DAY_COLUMN_WIDTH + CELL_PADDING;
                    double cellWidth = DAY_COLUMN_WIDTH - CELL_PADDING * 2;

                    // Determine cell color
                    Color bgColor;
                    if (isCellHovered && !isInactive) {
                        bgColor = HOVER_BG;
                    } else if (isIncluded) {
                        bgColor = cellColor;
                    } else {
                        bgColor = EXCLUDED_CELL_BG;
                    }

                    // Determine corner radii based on neighbors
                    // Round left corner if: first day OR previous day is a gap (not included)
                    // Round right corner if: last day OR next day is a gap (not included)
                    boolean isFirst = (day == startDayOffset);
                    boolean isLast = (day == endDayOffset);

                    // Check if neighbors are gaps (for rounding corners at gap boundaries)
                    boolean prevIsGap = !isFirst && !isDayIncluded(line, eventStart.plusDays(day - 1));
                    boolean nextIsGap = !isLast && !isDayIncluded(line, eventStart.plusDays(day + 1));

                    // For included cells: round corners at boundaries and gaps
                    // For empty cells: don't draw them at all (they're white/transparent)
                    boolean roundLeft = isFirst || (isIncluded && prevIsGap);
                    boolean roundRight = isLast || (isIncluded && nextIsGap);
                    boolean isSingle = roundLeft && roundRight;

                    gc.setFill(bgColor);

                    if (isSingle) {
                        gc.fillRoundRect(cellX, cellY, cellWidth, CELL_HEIGHT, CELL_RADIUS, CELL_RADIUS);
                    } else if (roundLeft && !roundRight) {
                        drawCellWithCorners(gc, cellX, cellY, cellWidth, CELL_HEIGHT, true, false);
                    } else if (roundRight && !roundLeft) {
                        drawCellWithCorners(gc, cellX, cellY, cellWidth, CELL_HEIGHT, false, true);
                    } else {
                        gc.fillRect(cellX, cellY, cellWidth, CELL_HEIGHT);
                    }

                    // Draw striped pattern overlay for cancelled/pending items
                    if ((isCancelled || hasPendingStatus) && isIncluded) {
                        drawStripedOverlay(gc, cellX, cellY, cellWidth, CELL_HEIGHT, isPendingDeleted);
                    }

                    // Draw X icon only for dates being removed (was originally selected, now unselected)
                    // Gaps that existed in the original data should show as empty, not with X
                    boolean wasOriginallySelected = wasOriginallyIncluded(line, cellDate);
                    if (!isIncluded && wasOriginallySelected && !isInactive) {
                        drawExcludedIcon(gc, cellX, cellY, cellWidth, isCellHovered);
                    }

                    // Draw checkmark on hover for included days
                    if (isIncluded && isCellHovered && !isInactive) {
                        drawIncludedHoverIcon(gc, cellX, cellY, cellWidth);
                    }
                }
            }

            // Reset alpha before drawing badge so it's fully visible
            gc.setGlobalAlpha(1.0);

            // Draw status badge for cancelled or pending items
            if (isCancelled || hasPendingStatus) {
                drawStatusBadge(gc, startDayOffset, y, isCancelled, isPendingCancelled, isPendingDeleted);
            }
        }

        // Ensure alpha is reset for price and action columns
        gc.setGlobalAlpha(1.0);

        // ===== 3. Draw Price Column =====
        drawPriceColumn(gc, line, dayCount, y, isInactive);

        // ===== 4. Draw Action Buttons =====
        drawActionButtons(gc, line, dayCount, y, rowIndex, hasPendingStatus);
    }

    /**
     * Draws an accommodation bar that spans from middle of arrival day to middle of departure day.
     * This represents nights (check-in in afternoon, check-out in morning).
     */
    private void drawAccommodationBar(GraphicsContext gc, DocumentLine line, LocalDate eventStart,
                                       int startDayOffset, int endDayOffset, double cellY,
                                       Color cellColor, boolean isLineHovered, boolean isInactive) {
        // For accommodation, we draw individual "night" segments
        // Each night starts at mid-day and ends at mid-day of the next day
        // If last night is the 29th, checkout is morning of 30th, so bar extends to mid-30th
        // For days 0-4 (nights of 24,25,26,27,28,29), bars are: 0.5-1.5, 1.5-2.5, 2.5-3.5, 3.5-4.5, 4.5-5.5

        double halfDay = DAY_COLUMN_WIDTH / 2.0;

        // Check if this line is cancelled or has pending status
        boolean isCancelled = isLineCancelled(line);
        String pendingStatus = getPendingStatus(line);
        boolean hasPendingStatus = pendingStatus != null;
        boolean isPendingDeleted = "deleted".equals(pendingStatus);

        for (int day = startDayOffset; day <= endDayOffset; day++) {
            // Each iteration draws one night: from middle of 'day' to middle of 'day+1'
            LocalDate nightDate = eventStart.plusDays(day);
            boolean isIncluded = isDayIncluded(line, nightDate);
            boolean isCellHovered = isLineHovered && (day == hoveredDayIndex);

            // Night bar starts at middle of current day
            double barX = ROW_HEADER_WIDTH + day * DAY_COLUMN_WIDTH + halfDay + CELL_PADDING;
            // Night bar width is one full day column (minus padding on each end)
            double barWidth = DAY_COLUMN_WIDTH - CELL_PADDING * 2;

            // Determine cell color
            Color bgColor;
            if (isCellHovered && !isInactive) {
                bgColor = HOVER_BG;
            } else if (isIncluded) {
                bgColor = cellColor;
            } else {
                bgColor = EXCLUDED_CELL_BG;
            }

            // Determine corner radii based on neighbors
            boolean isFirst = (day == startDayOffset);
            boolean isLast = (day == endDayOffset);

            // Check if neighbors are gaps (for rounding corners at gap boundaries)
            boolean prevIsGap = !isFirst && !isDayIncluded(line, eventStart.plusDays(day - 1));
            boolean nextIsGap = !isLast && !isDayIncluded(line, eventStart.plusDays(day + 1));

            // For included cells: round corners at boundaries and gaps
            boolean roundLeft = isFirst || (isIncluded && prevIsGap);
            boolean roundRight = isLast || (isIncluded && nextIsGap);
            boolean isSingle = roundLeft && roundRight;

            gc.setFill(bgColor);

            if (isSingle) {
                gc.fillRoundRect(barX, cellY, barWidth, CELL_HEIGHT, CELL_RADIUS, CELL_RADIUS);
            } else if (roundLeft && !roundRight) {
                drawCellWithCorners(gc, barX, cellY, barWidth, CELL_HEIGHT, true, false);
            } else if (roundRight && !roundLeft) {
                drawCellWithCorners(gc, barX, cellY, barWidth, CELL_HEIGHT, false, true);
            } else {
                gc.fillRect(barX, cellY, barWidth, CELL_HEIGHT);
            }

            // Draw striped pattern overlay for cancelled/pending items
            if ((isCancelled || hasPendingStatus) && isIncluded) {
                drawStripedOverlay(gc, barX, cellY, barWidth, CELL_HEIGHT, isPendingDeleted);
            }

            // Draw X icon only for nights being removed (was originally selected, now unselected)
            // Gaps that existed in the original data should show as empty, not with X
            boolean wasOriginallySelected = wasOriginallyIncluded(line, nightDate);
            if (!isIncluded && wasOriginallySelected && !isInactive) {
                drawExcludedIcon(gc, barX, cellY, barWidth, isCellHovered);
            }

            // Draw checkmark on hover for included nights
            if (isIncluded && isCellHovered && !isInactive) {
                drawIncludedHoverIcon(gc, barX, cellY, barWidth);
            }
        }
    }

    /**
     * Draws the row header with category icon and option name.
     */
    private void drawRowHeader(GraphicsContext gc, DocumentLine line, String category, Color categoryColor, double y) {
        double iconX = 8;
        double iconY = y + (ROW_HEIGHT - ICON_SIZE) / 2.0;

        // Draw category icon background (rounded square)
        gc.setFill(getCategoryBgColor(category));
        gc.fillRoundRect(iconX, iconY, ICON_SIZE, ICON_SIZE, 4, 4);

        // Draw category icon symbol
        gc.setFill(categoryColor);
        gc.setFont(Font.font("System", 10));
        gc.setTextAlign(TextAlignment.CENTER);
        String iconSymbol = getCategoryIcon(category);
        gc.fillText(iconSymbol, iconX + ICON_SIZE / 2.0, iconY + ICON_SIZE / 2.0 + 3);

        // Draw option name (truncated if too long)
        String itemName = line.getItem() != null ? line.getItem().getName() : "Unknown";
        gc.setFill(TEXT);
        gc.setFont(Font.font("System", FontWeight.NORMAL, 11));
        gc.setTextAlign(TextAlignment.LEFT);

        // Truncate name if too long (leave space for icon and padding)
        double maxNameWidth = ROW_HEADER_WIDTH - ICON_SIZE - 20;
        String displayName = truncateText(itemName, maxNameWidth, gc);

        gc.fillText(displayName, iconX + ICON_SIZE + 6, y + ROW_HEIGHT / 2.0 + 4);
    }

    /**
     * Draws the price column on the right side with right alignment.
     * For cancelled items: shows deposit as active price, original price struck through below.
     */
    private void drawPriceColumn(GraphicsContext gc, DocumentLine line, int dayCount, double y, boolean isCancelled) {
        double priceAreaStart = ROW_HEADER_WIDTH + dayCount * DAY_COLUMN_WIDTH;
        double priceX = priceAreaStart + PRICE_COLUMN_WIDTH - 8; // Right-aligned within price column

        // Get prices
        Integer computedPrice = getComputedPrice(line);
        Integer originalPrice = computedPrice != null ? computedPrice : line.getPriceNet();
        Integer minDeposit = line.getPriceMinDeposit();
        int deposit = minDeposit != null ? minDeposit : 0;

        gc.setTextAlign(TextAlignment.RIGHT);

        if (isCancelled) {
            // For cancelled items: show deposit as active price, original struck through
            if (deposit > 0) {
                // Show deposit amount as the kept amount (active price)
                String depositText = formatPrice(deposit);
                gc.setFill(WARM_ORANGE); // Orange to indicate deposit/cancellation fee
                gc.setFont(Font.font("System", FontWeight.SEMI_BOLD, 11));
                gc.fillText(depositText, priceX, y + ROW_HEIGHT / 2.0 - 2);

                // Show original price struck through below
                String originalText = formatPrice(originalPrice != null ? originalPrice : 0);
                gc.setFill(TEXT_MUTED);
                gc.setFont(Font.font("System", FontWeight.NORMAL, 9));
                double originalY = y + ROW_HEIGHT / 2.0 + 10;
                gc.fillText(originalText, priceX, originalY);
                // Draw strikethrough line
                double textWidth = originalText.length() * 5; // Approximate for smaller font
                gc.setStroke(TEXT_MUTED);
                gc.setLineWidth(1);
                gc.strokeLine(priceX - textWidth, originalY - 3, priceX, originalY - 3);
            } else {
                // No deposit - show original price struck through
                String priceText = formatPrice(originalPrice != null ? originalPrice : 0);
                gc.setFill(TEXT_MUTED);
                gc.setFont(Font.font("System", FontWeight.SEMI_BOLD, 11));
                gc.fillText(priceText, priceX, y + ROW_HEIGHT / 2.0 + 4);
                // Draw strikethrough line
                double textWidth = priceText.length() * 6; // Approximate
                gc.setStroke(TEXT_MUTED);
                gc.setLineWidth(1);
                gc.strokeLine(priceX - textWidth, y + ROW_HEIGHT / 2.0 + 1, priceX, y + ROW_HEIGHT / 2.0 + 1);
            }
        } else {
            // Normal price display
            String priceText = originalPrice != null ? formatPrice(originalPrice) : "-";
            gc.setFill(WARM_BROWN);
            gc.setFont(Font.font("System", FontWeight.SEMI_BOLD, 11));
            gc.fillText(priceText, priceX, y + ROW_HEIGHT / 2.0 + 4);
        }
        gc.setTextAlign(TextAlignment.LEFT); // Reset
    }

    /**
     * Draws the action buttons (edit, cancel, delete) on the right side of each row.
     * When item has pending cancelled status, shows a restore button.
     * When item has pending deleted status, no buttons shown (deletion cannot be undone).
     * When item is newly added (not yet saved), no buttons shown.
     */
    private void drawActionButtons(GraphicsContext gc, DocumentLine line, int dayCount, double y, int rowIndex, boolean hasPendingStatus) {
        // Don't show buttons for newly added lines that haven't been saved yet
        if (Entities.isNew(line)) {
            return;
        }

        double actionX = ROW_HEADER_WIDTH + dayCount * DAY_COLUMN_WIDTH + PRICE_COLUMN_WIDTH;
        double centerY = y + ROW_HEIGHT / 2.0;
        boolean isCancelled = isLineCancelled(line);
        boolean isHovered = (rowIndex == hoveredRowIndex);

        // Button positions (3 buttons: edit, cancel, delete)
        double buttonWidth = ACTION_COLUMN_WIDTH / 3.0;
        double editBtnX = actionX + buttonWidth / 2.0;
        double cancelBtnX = actionX + buttonWidth + buttonWidth / 2.0;
        double deleteBtnX = actionX + buttonWidth * 2 + buttonWidth / 2.0;

        // If pending status, check which type
        if (hasPendingStatus) {
            String pendingStatus = getPendingStatus(line);
            if ("cancelled".equals(pendingStatus)) {
                // Show restore button for cancelled items (can be restored)
                boolean restoreHovered = isHovered && ("cancel".equals(hoveredAction) || "delete".equals(hoveredAction) || "edit".equals(hoveredAction));
                drawRestoreIcon(gc, actionX + ACTION_COLUMN_WIDTH / 2.0, centerY, restoreHovered);
            }
            // For deleted items, don't show any buttons (deletion cannot be undone)
            return;
        }

        // Edit button (pencil icon) - always show
        boolean editHovered = isHovered && "edit".equals(hoveredAction);
        drawEditIcon(gc, editBtnX, centerY, editHovered);

        // Cancel button (X icon) - only show if not already cancelled
        if (!isCancelled) {
            boolean cancelHovered = isHovered && "cancel".equals(hoveredAction);
            drawCancelIcon(gc, cancelBtnX, centerY, cancelHovered);
        }

        // Delete button (trash icon)
        boolean deleteHovered = isHovered && "delete".equals(hoveredAction);
        drawDeleteIcon(gc, deleteBtnX, centerY, deleteHovered, isCancelled);
    }

    /**
     * Draws an edit (pencil) icon.
     */
    private void drawEditIcon(GraphicsContext gc, double x, double y, boolean hovered) {
        double size = ACTION_ICON_SIZE / 2.0;

        // Background circle on hover
        if (hovered) {
            gc.setFill(Color.web("#dbeafe")); // Light blue background
            gc.fillOval(x - size - 2, y - size - 2, size * 2 + 4, size * 2 + 4);
        }

        // Pencil icon (simplified)
        Color iconColor = hovered ? Color.web("#1d4ed8") : TEXT_MUTED;
        gc.setStroke(iconColor);
        gc.setFill(iconColor);
        gc.setLineWidth(1.2);

        // Pencil body (diagonal rectangle)
        double pencilLength = size * 1.4;
        double pencilWidth = 3;

        // Draw as rotated rectangle (45 degrees)
        gc.save();
        gc.translate(x, y);
        gc.rotate(-45);

        // Pencil body
        gc.strokeRect(-pencilLength / 2, -pencilWidth / 2, pencilLength, pencilWidth);

        // Pencil tip (triangle)
        double tipX = pencilLength / 2;
        gc.beginPath();
        gc.moveTo(tipX, -pencilWidth / 2);
        gc.lineTo(tipX + 3, 0);
        gc.lineTo(tipX, pencilWidth / 2);
        gc.closePath();
        gc.fill();

        gc.restore();
    }

    /**
     * Draws a cancel (X) icon.
     */
    private void drawCancelIcon(GraphicsContext gc, double x, double y, boolean hovered) {
        double size = ACTION_ICON_SIZE / 2.0;

        // Background circle on hover
        if (hovered) {
            gc.setFill(Color.web("#fef3cd")); // Light yellow background
            gc.fillOval(x - size - 2, y - size - 2, size * 2 + 4, size * 2 + 4);
        }

        // X icon
        gc.setStroke(hovered ? Color.web("#856404") : TEXT_MUTED);
        gc.setLineWidth(1.5);
        gc.strokeLine(x - size + 2, y - size + 2, x + size - 2, y + size - 2);
        gc.strokeLine(x - size + 2, y + size - 2, x + size - 2, y - size + 2);
    }

    /**
     * Draws a delete (trash) icon.
     */
    private void drawDeleteIcon(GraphicsContext gc, double x, double y, boolean hovered, boolean isCancelled) {
        double size = ACTION_ICON_SIZE / 2.0;

        // Background circle on hover
        if (hovered) {
            gc.setFill(Color.web("#f8d7da")); // Light red background
            gc.fillOval(x - size - 2, y - size - 2, size * 2 + 4, size * 2 + 4);
        }

        // Trash can icon (simplified)
        Color iconColor = hovered ? Color.web("#721c24") : (isCancelled ? TEXT_MUTED : TEXT_MUTED);
        gc.setStroke(iconColor);
        gc.setFill(iconColor);
        gc.setLineWidth(1.2);

        // Trash can body (rectangle)
        double bodyTop = y - size + 3;
        double bodyBottom = y + size - 1;
        double bodyLeft = x - size + 3;
        double bodyRight = x + size - 3;
        gc.strokeRect(bodyLeft, bodyTop, bodyRight - bodyLeft, bodyBottom - bodyTop);

        // Trash can lid
        gc.strokeLine(x - size + 1, bodyTop, x + size - 1, bodyTop);

        // Vertical lines inside
        double lineY1 = bodyTop + 2;
        double lineY2 = bodyBottom - 2;
        gc.strokeLine(x, lineY1, x, lineY2);
    }

    /**
     * Draws a restore (undo) icon for items with pending status.
     */
    private void drawRestoreIcon(GraphicsContext gc, double x, double y, boolean hovered) {
        double size = ACTION_ICON_SIZE / 2.0;

        // Background circle on hover
        if (hovered) {
            gc.setFill(Color.web("#d1fae5")); // Light green background
            gc.fillOval(x - size - 2, y - size - 2, size * 2 + 4, size * 2 + 4);
        }

        // Curved arrow icon (restore/undo)
        Color iconColor = hovered ? Color.web("#065f46") : Color.web("#10b981");
        gc.setStroke(iconColor);
        gc.setLineWidth(1.5);

        // Draw a simple curved arrow
        double startX = x + size - 2;
        double startY = y - size + 4;
        double endX = x - size + 2;
        double endY = y - size + 4;
        double controlX = x;
        double controlY = y + size - 2;

        gc.beginPath();
        gc.moveTo(startX, startY);
        gc.quadraticCurveTo(controlX, controlY, endX, endY);
        gc.stroke();

        // Arrow head
        gc.strokeLine(endX, endY, endX + 3, endY - 3);
        gc.strokeLine(endX, endY, endX + 3, endY + 3);
    }

    /**
     * Draws a diagonal striped overlay pattern on a cell for cancelled/deleted items.
     * Based on JSX cancelledPattern/deletedPattern styles.
     */
    private void drawStripedOverlay(GraphicsContext gc, double x, double y, double width, double height, boolean isDeleted) {
        // Save current alpha and reset to full opacity for visible stripes
        double currentAlpha = gc.getGlobalAlpha();
        gc.setGlobalAlpha(1.0);

        gc.save();

        // Clip to the cell bounds
        gc.beginPath();
        gc.rect(x, y, width, height);
        gc.clip();

        // Set stripe color - use semi-transparent darker colors for visibility
        Color stripeColor = isDeleted
            ? Color.rgb(220, 38, 38, 0.4)   // Red-tinted for delete
            : Color.rgb(128, 128, 128, 0.3); // Gray for cancel (visible on any background)

        gc.setStroke(stripeColor);
        gc.setLineWidth(2);

        // Draw diagonal lines across the cell
        double spacing = isDeleted ? 5 : 6; // Tighter spacing for deleted
        int lineCount = (int) Math.ceil((width + height) / spacing);

        for (int i = 0; i < lineCount; i++) {
            double startX = x + i * spacing;
            double startY = y;
            double endX = x;
            double endY = y + i * spacing;

            // Clamp line endpoints to cell bounds
            if (startX > x + width) {
                startY = startY + (startX - x - width);
                startX = x + width;
            }
            if (endY > y + height) {
                endX = endX + (endY - y - height);
                endY = y + height;
            }

            if (startY <= y + height && endX <= x + width) {
                gc.strokeLine(startX, startY, endX, endY);
            }
        }

        gc.restore();
        // Restore the original alpha
        gc.setGlobalAlpha(currentAlpha);
    }

    /**
     * Draws a status badge above the gantt bar for cancelled/pending items.
     * Shows "CANCELLED" for already-cancelled, "CANCELLING" for pending cancel, "REMOVING" for pending delete.
     */
    private void drawStatusBadge(GraphicsContext gc, int startDayOffset, double y, boolean isCancelled, boolean isPendingCancelled, boolean isPendingDeleted) {
        // Badge position - above the row, aligned to the start of the gantt bar
        double badgeX = ROW_HEADER_WIDTH + startDayOffset * DAY_COLUMN_WIDTH + CELL_PADDING;
        double badgeY = y + 2;  // Near the top of the row

        // Badge text and colors based on status
        String text;
        Color bgColor;
        Color textColor;

        if (isPendingDeleted) {
            text = "REMOVING";
            bgColor = Color.web("#fee2e2"); // Light red
            textColor = Color.web("#dc2626"); // Red
        } else if (isPendingCancelled) {
            text = "CANCELLING";
            bgColor = Color.web("#fef3cd"); // Light yellow
            textColor = Color.web("#856404"); // Dark yellow
        } else if (isCancelled) {
            text = "CANCELLED";
            bgColor = Color.web("#f8d7da"); // Light pink/red (from JSX)
            textColor = Color.web("#dc3545"); // Red (from JSX)
        } else {
            return; // No badge needed
        }

        // Calculate badge dimensions
        gc.setFont(Font.font("System", FontWeight.BOLD, 8));
        double textWidth = text.length() * 5; // Approximate
        double badgeWidth = textWidth + 8;
        double badgeHeight = 12;

        // Draw badge background
        gc.setFill(bgColor);
        gc.fillRoundRect(badgeX, badgeY, badgeWidth, badgeHeight, 4, 4);

        // Draw badge border
        gc.setStroke(textColor);
        gc.setLineWidth(0.5);
        gc.strokeRoundRect(badgeX, badgeY, badgeWidth, badgeHeight, 4, 4);

        // Draw badge text
        gc.setFill(textColor);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(text, badgeX + 4, badgeY + 9);
    }

    /**
     * Gets the icon symbol for a category.
     */
    private String getCategoryIcon(String category) {
        switch (category) {
            case "accommodation": return "\uD83D\uDECF"; // Bed emoji
            case "meals": return "\uD83C\uDF7D"; // Fork and knife
            case "diet": return "\uD83E\uDD57"; // Salad
            case "transport": return "\uD83D\uDE8C"; // Bus
            case "parking": return "\uD83C\uDD7F"; // P button
            default: return "\uD83D\uDCDA"; // Books (program)
        }
    }

    /**
     * Gets the foreground color for a category.
     */
    private Color getCategoryFgColor(String category) {
        switch (category) {
            case "accommodation": return ACCOMMODATION_FG;
            case "meals": return MEALS_FG;
            case "diet": return DIET_FG;
            case "transport": return TRANSPORT_FG;
            case "parking": return PARKING_FG;
            default: return PROGRAM_FG;
        }
    }

    /**
     * Truncates text to fit within a given width.
     */
    private String truncateText(String text, double maxWidth, GraphicsContext gc) {
        if (text == null) return "";
        // Approximate character width (rough estimate)
        double charWidth = 6;
        int maxChars = (int) (maxWidth / charWidth);
        if (text.length() <= maxChars) return text;
        return text.substring(0, Math.max(0, maxChars - 2)) + "...";
    }

    /**
     * Formats a price value using EventPriceFormatter (handles cents to currency conversion).
     * Always shows two decimal places (e.g., £0.00, £48.00).
     */
    private String formatPrice(int priceInCents) {
        Event event = eventProperty.get();
        String currencySymbol = EventPriceFormatter.getEventCurrencySymbol(event);
        // Use PriceFormatter with show00cents=true to always show 2 decimal places
        return PriceFormatter.formatWithCurrency(priceInCents, currencySymbol, true);
    }

    /**
     * Draws an X icon for excluded days.
     */
    private void drawExcludedIcon(GraphicsContext gc, double x, double y, double width, boolean isHovered) {
        double iconSize = 8;
        double centerX = x + width / 2;
        double centerY = y + CELL_HEIGHT / 2;

        gc.setStroke(isHovered ? Color.WHITE : TEXT_MUTED);
        gc.setLineWidth(1.5);

        double half = iconSize / 2;
        gc.strokeLine(centerX - half, centerY - half, centerX + half, centerY + half);
        gc.strokeLine(centerX - half, centerY + half, centerX + half, centerY - half);
    }

    /**
     * Draws a checkmark icon on hover for included days.
     */
    private void drawIncludedHoverIcon(GraphicsContext gc, double x, double y, double width) {
        double centerX = x + width / 2;
        double centerY = y + CELL_HEIGHT / 2;

        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.5);

        // Draw checkmark
        gc.strokeLine(centerX - 3, centerY, centerX - 1, centerY + 2);
        gc.strokeLine(centerX - 1, centerY + 2, centerX + 3, centerY - 2);
    }

    /**
     * Checks if a line is an accommodation (night-based) item.
     */
    private boolean isAccommodationLine(DocumentLine line) {
        if (line.getItem() == null) return false;
        ItemFamily family = line.getItem().getFamily();
        if (family == null) return false;

        String familyCode = family.getCode();
        String familyName = family.getName();
        String search = (familyCode != null ? familyCode : familyName);
        if (search == null) return false;

        search = search.toLowerCase();
        return search.contains("acco") || search.contains("accommodation") || search.contains("room");
    }

    /**
     * Draws a cell with selective corner rounding using path-based drawing.
     * This ensures the cell stays within its specified bounds.
     */
    private void drawCellWithCorners(GraphicsContext gc, double x, double y, double w, double h,
                                     boolean roundLeft, boolean roundRight) {
        double r = CELL_RADIUS;

        gc.beginPath();

        if (roundLeft && roundRight) {
            // Both corners rounded (same as fillRoundRect)
            gc.moveTo(x + r, y);
            gc.lineTo(x + w - r, y);
            gc.arcTo(x + w, y, x + w, y + r, r);
            gc.lineTo(x + w, y + h - r);
            gc.arcTo(x + w, y + h, x + w - r, y + h, r);
            gc.lineTo(x + r, y + h);
            gc.arcTo(x, y + h, x, y + h - r, r);
            gc.lineTo(x, y + r);
            gc.arcTo(x, y, x + r, y, r);
        } else if (roundLeft) {
            // Only left corners rounded, right side square
            gc.moveTo(x + r, y);
            gc.lineTo(x + w, y);
            gc.lineTo(x + w, y + h);
            gc.lineTo(x + r, y + h);
            gc.arcTo(x, y + h, x, y + h - r, r);
            gc.lineTo(x, y + r);
            gc.arcTo(x, y, x + r, y, r);
        } else if (roundRight) {
            // Only right corners rounded, left side square
            gc.moveTo(x, y);
            gc.lineTo(x + w - r, y);
            gc.arcTo(x + w, y, x + w, y + r, r);
            gc.lineTo(x + w, y + h - r);
            gc.arcTo(x + w, y + h, x + w - r, y + h, r);
            gc.lineTo(x, y + h);
            gc.lineTo(x, y);
        } else {
            // No rounding (plain rectangle)
            gc.rect(x, y, w, h);
        }

        gc.closePath();
        gc.fill();
    }

    /**
     * Gets the category from a document line based on its item's KnownItemFamily.
     * Uses the proper KnownItemFamily enum instead of string matching.
     */
    private String getCategoryFromLine(DocumentLine line) {
        if (line.getItem() == null) {
            return hasTemporalDates(line) ? "program" : "other";
        }

        ItemFamily family = line.getItem().getFamily();
        if (family == null) {
            return hasTemporalDates(line) ? "program" : "other";
        }

        // Use KnownItemFamily enum for proper categorization
        KnownItemFamily knownFamily = family.getItemFamilyType();
        if (knownFamily == null) {
            knownFamily = KnownItemFamily.UNKNOWN;
        }

        switch (knownFamily) {
            // Temporal categories (date-based)
            case ACCOMMODATION:
                return "accommodation";
            case MEALS:
                return "meals";
            case DIET:
                return "diet";
            case TEACHING:
            case TRANSLATION:
            case VIDEO:
                return "program";

            // Non-temporal categories
            case TRANSPORT:
                return "transport";
            case PARKING:
                return "parking";
            case TAX:
                return "tax";
            case AUDIO_RECORDING:
                return "recording";

            // Unknown family - use dates to determine
            case UNKNOWN:
            default:
                return hasTemporalDates(line) ? "other_temporal" : "other";
        }
    }

    /**
     * Checks if a line has temporal dates (start and/or end date).
     */
    private boolean hasTemporalDates(DocumentLine line) {
        return line.getStartDate() != null || line.getEndDate() != null;
    }

    // Property getters and setters

    public ObjectProperty<LocalDate> eventStartProperty() {
        return eventStartProperty;
    }

    public void setEventStart(LocalDate date) {
        eventStartProperty.set(date);
    }

    public ObjectProperty<LocalDate> eventEndProperty() {
        return eventEndProperty;
    }

    public void setEventEnd(LocalDate date) {
        eventEndProperty.set(date);
    }

    public ObjectProperty<LocalDate> bookingStartProperty() {
        return bookingStartProperty;
    }

    public void setBookingStart(LocalDate date) {
        bookingStartProperty.set(date);
    }

    public ObjectProperty<LocalDate> bookingEndProperty() {
        return bookingEndProperty;
    }

    public void setBookingEnd(LocalDate date) {
        bookingEndProperty.set(date);
    }

    public StringProperty eventNameProperty() {
        return eventNameProperty;
    }

    public void setEventName(String name) {
        eventNameProperty.set(name);
    }

    public ObservableList<DocumentLine> getDocumentLines() {
        return documentLines;
    }

    /**
     * Sets the event for the timeline.
     */
    public void setEvent(Event event) {
        eventProperty.set(event);
        if (event != null) {
            setEventStart(event.getStartDate());
            setEventEnd(event.getEndDate());
            setEventName(event.getName());
        } else {
            setEventStart(null);
            setEventEnd(null);
            setEventName(null);
        }
    }

    /**
     * Gets the total width needed for the timeline.
     */
    public double getTimelineWidth(int dayCount) {
        return ROW_HEADER_WIDTH + dayCount * DAY_COLUMN_WIDTH + PRICE_COLUMN_WIDTH + ACTION_COLUMN_WIDTH;
    }

    /**
     * Gets the Event property for external binding.
     */
    public ObjectProperty<Event> eventProperty() {
        return eventProperty;
    }
}
