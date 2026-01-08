package one.modality.booking.backoffice.activities.registration;

import dev.webfx.kit.util.properties.FXProperties;
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
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.ItemFamily;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;

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
    private static final int CELL_GAP = 6;             // Gap between day cells (white space)
    private static final int CELL_PADDING = CELL_GAP / 2; // Half gap on each side of cell (3 pixels)
    private static final int CELL_RADIUS = 6;
    private static final int PRICE_COLUMN_WIDTH = 80;  // Right side for price
    private static final int ACTION_COLUMN_WIDTH = 60; // Right side for action buttons (cancel, delete)
    private static final int ICON_SIZE = 18;           // Category icon size
    private static final int ACTION_ICON_SIZE = 16;    // Action button icon size

    // Color constants for excluded days and hover
    private static final Color EXCLUDED_CELL_BG = SAND;
    private static final Color HOVER_BG = Color.web("#1e3a5f");

    // Data properties
    private final ObjectProperty<LocalDate> eventStartProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> eventEndProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> bookingStartProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> bookingEndProperty = new SimpleObjectProperty<>();
    private final StringProperty eventNameProperty = new SimpleStringProperty();
    private final ObjectProperty<Event> eventProperty = new SimpleObjectProperty<>();
    private final ObservableList<DocumentLine> documentLines = FXCollections.observableArrayList();

    // Excluded days tracking (key = DocumentLine ID primary key, value = set of excluded dates)
    private final Map<Object, Set<LocalDate>> excludedDaysMap = new HashMap<>();

    // Attendance dates per line (key = DocumentLine ID primary key, value = set of attended dates)
    // This determines which days to draw cells for each line item
    private Map<Object, Set<LocalDate>> attendanceDatesMap = new HashMap<>();

    // Hover state
    private int hoveredRowIndex = -1;
    private int hoveredDayIndex = -1;
    private String hoveredAction = null; // "cancel" or "delete" or null

    // Callback for toggle events
    private BiConsumer<DocumentLine, LocalDate> onDayToggled;
    // Callbacks for action buttons
    private java.util.function.Consumer<DocumentLine> onCancelClicked;
    private java.util.function.Consumer<DocumentLine> onDeleteClicked;

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

        // Initial sizing
        setMinHeight(HEADER_HEIGHT + PERIOD_BAR_HEIGHT * 2 + ROW_HEIGHT);
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
            if ("cancel".equals(action) && onCancelClicked != null) {
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
        double contentY = y - HEADER_HEIGHT - PERIOD_BAR_HEIGHT * 2;
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
     * @return "cancel", "delete", or null if not over an action button
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
            // Determine which button (cancel on left, delete on right)
            double midPoint = actionAreaStart + ACTION_COLUMN_WIDTH / 2.0;
            if (x < midPoint) {
                return "cancel";
            } else {
                return "delete";
            }
        }
        return null;
    }

    /**
     * Checks if a cell is clickable (within the line's date range and not cancelled).
     */
    private boolean isCellClickable(int rowIndex, int dayIndex) {
        if (rowIndex < 0 || dayIndex < 0) return false;
        if (rowIndex >= documentLines.size()) return false;

        DocumentLine line = documentLines.get(rowIndex);
        LocalDate date = getDateForDayIndex(dayIndex);
        if (date == null) return false;

        // Check if line is cancelled
        if (Boolean.TRUE.equals(line.isCancelled())) return false;

        // Check if date is within line's range
        LocalDate lineStart = line.getStartDate();
        LocalDate lineEnd = line.getEndDate();
        if (lineStart == null) lineStart = bookingStartProperty.get();
        if (lineEnd == null) lineEnd = bookingEndProperty.get();
        if (lineStart == null || lineEnd == null) return false;

        return !date.isBefore(lineStart) && !date.isAfter(lineEnd);
    }

    /**
     * Toggles a specific day for a line (add/remove from excluded days).
     */
    private void toggleDay(DocumentLine line, LocalDate date) {
        Object lineId = line.getId() != null ? line.getId().getPrimaryKey() : null;
        if (lineId == null) return;

        Set<LocalDate> excluded = new HashSet<>(excludedDaysMap.getOrDefault(lineId, Collections.emptySet()));

        if (excluded.contains(date)) {
            excluded.remove(date);
        } else {
            excluded.add(date);
        }

        if (excluded.isEmpty()) {
            excludedDaysMap.remove(lineId);
        } else {
            excludedDaysMap.put(lineId, excluded);
        }

        requestRepaint();

        // Notify callback
        if (onDayToggled != null) {
            onDayToggled.accept(line, date);
        }
    }

    // ===== Excluded Days API =====

    /**
     * Gets the set of excluded days for a document line.
     * @return set of excluded dates (empty set if no exclusions)
     */
    public Set<LocalDate> getExcludedDays(DocumentLine line) {
        Object lineId = line.getId() != null ? line.getId().getPrimaryKey() : null;
        if (lineId == null) return Collections.emptySet();
        return excludedDaysMap.getOrDefault(lineId, Collections.emptySet());
    }

    /**
     * Sets excluded days for a document line.
     */
    public void setExcludedDays(DocumentLine line, Set<LocalDate> excludedDays) {
        Object lineId = line.getId() != null ? line.getId().getPrimaryKey() : null;
        if (lineId != null) {
            if (excludedDays == null || excludedDays.isEmpty()) {
                excludedDaysMap.remove(lineId);
            } else {
                excludedDaysMap.put(lineId, new HashSet<>(excludedDays));
            }
            requestRepaint();
        }
    }

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
     * A day is included if it's in the attendance dates (or falls within booking period if no attendances)
     * and is not in the excluded days list.
     */
    public boolean isDayIncluded(DocumentLine line, LocalDate date) {
        Set<LocalDate> attendanceDates = getAttendanceDates(line);

        // If we have attendance dates, use them to determine inclusion
        if (!attendanceDates.isEmpty()) {
            // Day must be in attendance dates AND not excluded
            return attendanceDates.contains(date) && !getExcludedDays(line).contains(date);
        }

        // Fallback: use line dates or booking dates
        LocalDate lineStart = line.getStartDate();
        LocalDate lineEnd = line.getEndDate();

        // Default to booking dates if not set
        if (lineStart == null) lineStart = bookingStartProperty.get();
        if (lineEnd == null) lineEnd = bookingEndProperty.get();
        if (lineStart == null || lineEnd == null) return false;

        // Check if date is within line's range
        if (date.isBefore(lineStart) || date.isAfter(lineEnd)) return false;

        // Check if date is excluded
        return !getExcludedDays(line).contains(date);
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
        return HEADER_HEIGHT + PERIOD_BAR_HEIGHT * 2 + (lineCount > 0 ? lineCount * ROW_HEIGHT : ROW_HEIGHT);
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

        // Draw components
        double y = 0;

        // 1. Day headers
        drawDayHeaders(gc, eventStart, dayCount, y);
        y += HEADER_HEIGHT;

        // 2. Event period bar
        drawPeriodBar(gc, eventStart, eventEnd, eventStart, dayCount, y,
            EVENT_PERIOD_BG, EVENT_PERIOD_TEXT, "Event: " + (eventNameProperty.get() != null ? eventNameProperty.get() : ""));
        y += PERIOD_BAR_HEIGHT;

        // 3. Booking period bar
        LocalDate bookingStart = bookingStartProperty.get();
        LocalDate bookingEnd = bookingEndProperty.get();
        if (bookingStart != null && bookingEnd != null) {
            drawPeriodBar(gc, bookingStart, bookingEnd, eventStart, dayCount, y,
                BOOKING_PERIOD_BG, BOOKING_PERIOD_TEXT, "Booking period");
        }
        y += PERIOD_BAR_HEIGHT;

        // 4. Document line rows
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

        boolean isCancelled = Boolean.TRUE.equals(line.isCancelled());
        boolean isLineHovered = (rowIndex == hoveredRowIndex);

        // Get attendance dates for this line (these are the actual booked days)
        Set<LocalDate> attendanceDates = getAttendanceDates(line);

        // Determine the date range to draw cells for
        LocalDate lineStart = null;
        LocalDate lineEnd = null;

        if (!attendanceDates.isEmpty()) {
            // Use min/max of attendance dates
            lineStart = attendanceDates.stream().min(LocalDate::compareTo).orElse(null);
            lineEnd = attendanceDates.stream().max(LocalDate::compareTo).orElse(null);
        } else {
            // Fallback to line dates or booking dates
            lineStart = line.getStartDate();
            lineEnd = line.getEndDate();
            if (lineStart == null) lineStart = bookingStartProperty.get();
            if (lineEnd == null) lineEnd = bookingEndProperty.get();
        }

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

        // Check if this is a night-based (accommodation) item for half-cell offset
        boolean isNightBased = isAccommodationLine(line);
        double halfCellOffset = isNightBased ? DAY_COLUMN_WIDTH / 2.0 : 0;

        // Apply reduced opacity for cancelled lines
        if (isCancelled) {
            gc.setGlobalAlpha(0.5);
        }

        // ===== 1. Draw Row Header (Category Icon + Option Name) =====
        drawRowHeader(gc, line, category, categoryFgColor, y);

        // ===== 2. Draw Gantt Cells =====
        if (startDayOffset <= endDayOffset) {
            double cellY = y + (ROW_HEIGHT - CELL_HEIGHT) / 2.0;

            for (int day = startDayOffset; day <= endDayOffset; day++) {
                LocalDate cellDate = eventStart.plusDays(day);
                boolean isIncluded = isDayIncluded(line, cellDate);
                boolean isCellHovered = isLineHovered && (day == hoveredDayIndex);

                double cellX = ROW_HEADER_WIDTH + day * DAY_COLUMN_WIDTH + CELL_PADDING;
                double cellWidth = DAY_COLUMN_WIDTH - CELL_PADDING * 2;

                // Apply half-cell offset for night-based items
                if (isNightBased) {
                    cellX += halfCellOffset;
                    // Adjust width for first and last cells
                    if (day == startDayOffset) {
                        cellX -= halfCellOffset;
                    }
                    if (day == endDayOffset) {
                        cellWidth -= halfCellOffset;
                    }
                }

                // Determine cell color
                Color bgColor;
                if (isCellHovered && !isCancelled) {
                    bgColor = HOVER_BG;
                } else if (isIncluded) {
                    bgColor = cellColor;
                } else {
                    bgColor = EXCLUDED_CELL_BG;
                }

                // Determine corner radii
                boolean isFirst = (day == startDayOffset);
                boolean isLast = (day == endDayOffset);
                boolean isSingle = isFirst && isLast;

                gc.setFill(bgColor);

                if (isSingle) {
                    gc.fillRoundRect(cellX, cellY, cellWidth, CELL_HEIGHT, CELL_RADIUS, CELL_RADIUS);
                } else if (isFirst) {
                    drawCellWithCorners(gc, cellX, cellY, cellWidth, CELL_HEIGHT, true, false);
                } else if (isLast) {
                    drawCellWithCorners(gc, cellX, cellY, cellWidth, CELL_HEIGHT, false, true);
                } else {
                    gc.fillRect(cellX, cellY, cellWidth, CELL_HEIGHT);
                }

                // Draw X icon for excluded days
                if (!isIncluded && !isCancelled) {
                    drawExcludedIcon(gc, cellX, cellY, cellWidth, isCellHovered);
                }

                // Draw checkmark on hover for included days
                if (isIncluded && isCellHovered && !isCancelled) {
                    drawIncludedHoverIcon(gc, cellX, cellY, cellWidth);
                }
            }
        }

        // ===== 3. Draw Price Column =====
        drawPriceColumn(gc, line, dayCount, y, isCancelled);

        // ===== 4. Draw Action Buttons =====
        drawActionButtons(gc, line, dayCount, y, rowIndex);

        // Reset alpha
        gc.setGlobalAlpha(1.0);
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
     */
    private void drawPriceColumn(GraphicsContext gc, DocumentLine line, int dayCount, double y, boolean isCancelled) {
        double priceAreaStart = ROW_HEADER_WIDTH + dayCount * DAY_COLUMN_WIDTH;
        double priceX = priceAreaStart + PRICE_COLUMN_WIDTH - 8; // Right-aligned within price column

        // Get price
        Integer price = line.getPriceNet();
        String priceText = price != null ? formatPrice(price) : "-";

        // Price text (right-aligned)
        gc.setFill(isCancelled ? TEXT_MUTED : WARM_BROWN);
        gc.setFont(Font.font("System", FontWeight.SEMI_BOLD, 11));
        gc.setTextAlign(TextAlignment.RIGHT);

        if (isCancelled) {
            // Draw strikethrough for cancelled
            gc.fillText(priceText, priceX, y + ROW_HEIGHT / 2.0 + 4);
            // Draw strikethrough line
            double textWidth = priceText.length() * 6; // Approximate
            gc.setStroke(TEXT_MUTED);
            gc.setLineWidth(1);
            gc.strokeLine(priceX - textWidth, y + ROW_HEIGHT / 2.0 + 1, priceX, y + ROW_HEIGHT / 2.0 + 1);
        } else {
            gc.fillText(priceText, priceX, y + ROW_HEIGHT / 2.0 + 4);
        }
        gc.setTextAlign(TextAlignment.LEFT); // Reset
    }

    /**
     * Draws the action buttons (cancel, delete) on the right side of each row.
     */
    private void drawActionButtons(GraphicsContext gc, DocumentLine line, int dayCount, double y, int rowIndex) {
        double actionX = ROW_HEADER_WIDTH + dayCount * DAY_COLUMN_WIDTH + PRICE_COLUMN_WIDTH;
        double centerY = y + ROW_HEIGHT / 2.0;
        boolean isCancelled = Boolean.TRUE.equals(line.isCancelled());
        boolean isHovered = (rowIndex == hoveredRowIndex);

        // Button positions
        double cancelBtnX = actionX + ACTION_COLUMN_WIDTH / 4.0;
        double deleteBtnX = actionX + 3 * ACTION_COLUMN_WIDTH / 4.0;

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
     */
    private String formatPrice(int priceInCents) {
        Event event = eventProperty.get();
        if (event != null) {
            return EventPriceFormatter.formatWithCurrency(priceInCents, event);
        }
        // Fallback: manual conversion (divide by 100)
        return "£" + String.format("%.2f", priceInCents / 100.0);
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
     * Gets the category from a document line based on its item family.
     */
    private String getCategoryFromLine(DocumentLine line) {
        if (line.getItem() == null) return "program";

        one.modality.base.shared.entities.ItemFamily family = line.getItem().getFamily();
        if (family == null) return "program";

        String familyName = family.getName();
        String familyCode = family.getCode();
        String search = (familyName != null ? familyName : familyCode);
        if (search == null) return "program";

        search = search.toLowerCase();

        if (search.contains("accommodation") || search.contains("room") || search.contains("bed")) {
            return "accommodation";
        } else if (search.contains("meal") || search.contains("breakfast") || search.contains("lunch") || search.contains("dinner")) {
            return "meals";
        } else if (search.contains("diet") || search.contains("vegetarian") || search.contains("vegan")) {
            return "diet";
        } else if (search.contains("transport") || search.contains("shuttle") || search.contains("bus")) {
            return "transport";
        } else if (search.contains("parking") || search.contains("car")) {
            return "parking";
        }

        return "program";
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
