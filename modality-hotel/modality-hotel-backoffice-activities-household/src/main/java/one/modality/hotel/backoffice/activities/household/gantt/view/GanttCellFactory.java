package one.modality.hotel.backoffice.activities.household.gantt.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import one.modality.hotel.backoffice.activities.household.gantt.model.BookingBar;
import one.modality.hotel.backoffice.activities.household.gantt.model.BookingPosition;
import one.modality.hotel.backoffice.activities.household.gantt.model.DateColumn;
import one.modality.hotel.backoffice.activities.household.gantt.model.GanttBedData;
import one.modality.hotel.backoffice.activities.household.gantt.model.GanttBookingData;
import one.modality.hotel.backoffice.activities.household.gantt.model.GanttRoomData;
import one.modality.hotel.backoffice.activities.household.gantt.model.RoomStatus;
import one.modality.hotel.backoffice.activities.household.gantt.model.RoomType;
import one.modality.hotel.backoffice.activities.household.gantt.presenter.GanttPresenter;
import one.modality.hotel.backoffice.activities.household.gantt.renderer.BookingBarRenderer;
import one.modality.hotel.backoffice.activities.household.gantt.renderer.GanttColorScheme;
import one.modality.hotel.backoffice.activities.household.gantt.renderer.MultiRoomBookingBarRenderer;
import one.modality.hotel.backoffice.activities.household.gantt.renderer.SingleRoomBookingBarRenderer;

import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

/**
 * Factory for creating Gantt table cells with consistent styling.
 * Centralizes all cell creation logic to eliminate duplication.
 *
 * @author Claude Code Assistant
 */
public class GanttCellFactory {

    // === DESIGN CONSTANTS ===
    private static final double ROOM_COLUMN_WIDTH = 200;
    private static final double DATE_COLUMN_WIDTH = 50;  // Reduced by 50% from 100
    private static final double DATE_COLUMN_WIDTH_WIDER = 65;  // Reduced by 50% from 130
    private static final double ROW_HEIGHT = 34;  // Unified height for all cells in a row
    private static final double CATEGORY_CELL_HEIGHT = 28;

    private final GanttColorScheme colorScheme;
    private final GanttPresenter presenter;
    private final BookingBarRenderer singleRoomRenderer;
    private final BookingBarRenderer multiRoomRenderer;
    private Runnable onExpandCollapseCallback;

    public GanttCellFactory(GanttColorScheme colorScheme, GanttPresenter presenter) {
        this.colorScheme = colorScheme;
        this.presenter = presenter;
        this.singleRoomRenderer = new SingleRoomBookingBarRenderer(colorScheme);
        this.multiRoomRenderer = new MultiRoomBookingBarRenderer(colorScheme);
    }

    /**
     * Sets callback for expand/collapse events
     */
    public void setOnExpandCollapseCallback(Runnable callback) {
        this.onExpandCollapseCallback = callback;
    }

    /**
     * Creates a header cell with consistent styling
     */
    public StackPane createHeaderCell(Node content, double width, boolean isToday) {
        StackPane cell = new StackPane(content);
        cell.setPrefWidth(width);
        cell.setMinWidth(width);
        cell.setMaxWidth(width);
        cell.setPrefHeight(32);
        cell.setMinHeight(32);
        cell.setMaxHeight(32);

        // Background color
        Color bgColor = isToday ? GanttColorScheme.COLOR_BG_TODAY : Color.web("#D9D9D9");
        cell.setBackground(new Background(new BackgroundFill(bgColor, CornerRadii.EMPTY, Insets.EMPTY)));

        // Border
        Color borderColor = isToday ? GanttColorScheme.COLOR_BLUE : GanttColorScheme.COLOR_BORDER;
        double borderWidth = isToday ? 2 : 1;
        cell.setBorder(new Border(new BorderStroke(borderColor, BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY, new BorderWidths(borderWidth))));

        return cell;
    }

    /**
     * Creates a room column header cell
     */
    public StackPane createRoomHeaderCell() {
        Label roomHeader = new Label("Room");
        return createHeaderCell(roomHeader, ROOM_COLUMN_WIDTH, false);
    }

    /**
     * Creates a date header cell
     */
    public StackPane createDateHeaderCell(DateColumn dateCol) {
        VBox dateHeader = new VBox();
        dateHeader.setAlignment(Pos.CENTER);

        // Day of week
        String dayOfWeek = dateCol.getDate().getDayOfWeek()
                .getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase();
        Label dayLabel = new Label(dayOfWeek);
        dayLabel.setTextFill(GanttColorScheme.COLOR_BLUE);
        dayLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 500;");

        // Day of month
        String dayOfMonth = String.format("%02d", dateCol.getDate().getDayOfMonth());
        Label dateLabel = new Label(dayOfMonth);
        dateLabel.setTextFill(GanttColorScheme.COLOR_BLUE);
        dateLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 500;");

        dateHeader.getChildren().addAll(dayLabel, dateLabel);

        if (dateCol.isToday()) {
            Label todayLabel = new Label("(Today)");
            todayLabel.setTextFill(GanttColorScheme.COLOR_BLUE);
            todayLabel.setStyle("-fx-font-size: 10px;");
            dateHeader.getChildren().add(todayLabel);
        }

        double width = dateCol.isWider() ? DATE_COLUMN_WIDTH_WIDER : DATE_COLUMN_WIDTH;
        return createHeaderCell(dateHeader, width, dateCol.isToday());
    }

    /**
     * Creates a category row cell (spans all columns)
     */
    public StackPane createCategoryCell(String categoryName) {
        Label categoryLabel = new Label(categoryName);
        categoryLabel.setTextFill(GanttColorScheme.COLOR_BLUE);
        categoryLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 500;");

        StackPane categoryCell = new StackPane(categoryLabel);
        categoryCell.setAlignment(Pos.CENTER_LEFT);
        categoryCell.setPadding(new Insets(4, 10, 4, 10));
        categoryCell.setPrefHeight(CATEGORY_CELL_HEIGHT);
        categoryCell.setMinHeight(CATEGORY_CELL_HEIGHT);
        categoryCell.setMaxHeight(CATEGORY_CELL_HEIGHT);
        categoryCell.setBackground(new Background(new BackgroundFill(
                GanttColorScheme.COLOR_BG_HEADER, CornerRadii.EMPTY, Insets.EMPTY)));

        // Top border is thicker (2px) to separate categories
        categoryCell.setBorder(new Border(new BorderStroke(
                Color.web("#999999"), GanttColorScheme.COLOR_BORDER,
                GanttColorScheme.COLOR_BORDER, GanttColorScheme.COLOR_BORDER,
                BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID,
                BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY,
                new BorderWidths(2, 1, 1, 1),
                Insets.EMPTY
        )));

        return categoryCell;
    }

    /**
     * Creates a room name cell with status dot, room name, and expand/collapse arrow for multi-bed rooms
     */
    public StackPane createRoomCell(GanttRoomData room) {
        HBox content = new HBox(6);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(0, 8, 0, 8));
        content.setPickOnBounds(false); // Allow clicks to pass through to children

        // Expand/collapse arrow for multi-bed rooms (double rooms, dormitories) OR single rooms with overbooking
        if (!room.getBeds().isEmpty()) {
            boolean isExpanded = presenter.isRoomExpanded(room.getId());
            Label arrow = new Label(isExpanded ? "∨" : "›"); // Down arrow when expanded, right arrow when collapsed
            arrow.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333; -fx-font-weight: bold; -fx-cursor: hand;");
            arrow.setCursor(javafx.scene.Cursor.HAND);

            // Click handler to expand/collapse individual bed rows
            arrow.setOnMouseClicked(e -> {
                e.consume(); // Prevent event from bubbling up
                presenter.toggleRoomExpanded(room.getId());
                // Trigger UI refresh (callback will be handled by GanttTableView)
                if (onExpandCollapseCallback != null) {
                    onExpandCollapseCallback.run();
                }
            });

            content.getChildren().add(arrow);
        }

        // Status dot
        Circle statusDot = new Circle(4);
        statusDot.setFill(colorScheme.getRoomStatusColor(room.getStatus()));

        // Room name
        Label roomName = new Label(room.getName());
        roomName.setTextFill(GanttColorScheme.COLOR_TEXT_GREY);
        roomName.setStyle("-fx-font-size: 13px;");

        content.getChildren().addAll(statusDot, roomName);

        // Add room comments if present
        if (room.getRoomComments() != null && !room.getRoomComments().isEmpty()) {
            Label commentLabel = new Label(truncateText(room.getRoomComments(), 15));
            commentLabel.setTextFill(Color.web("#666666"));
            commentLabel.setStyle("-fx-font-size: 11px; -fx-font-style: italic;");
            content.getChildren().add(commentLabel);
        }

        StackPane cell = new StackPane(content);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setPrefWidth(ROOM_COLUMN_WIDTH);
        cell.setMinWidth(ROOM_COLUMN_WIDTH);
        cell.setMaxWidth(ROOM_COLUMN_WIDTH);
        cell.setPrefHeight(ROW_HEIGHT);
        cell.setMinHeight(ROW_HEIGHT);
        cell.setMaxHeight(ROW_HEIGHT);

        // Use grey background if room is expanded (multi-bed room or single room with overbooking showing individual beds)
        boolean isExpanded = !room.getBeds().isEmpty() && presenter.isRoomExpanded(room.getId());
        Color bgColor = isExpanded ? Color.web("#ECECEC") : Color.WHITE;
        cell.setBackground(new Background(new BackgroundFill(bgColor, CornerRadii.EMPTY, Insets.EMPTY)));

        // Right and bottom borders for alignment with date cells
        cell.setBorder(new Border(new BorderStroke(
                null, GanttColorScheme.COLOR_BORDER, GanttColorScheme.COLOR_BORDER, null,
                BorderStrokeStyle.NONE, BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID, BorderStrokeStyle.NONE,
                CornerRadii.EMPTY,
                new BorderWidths(0, 1, 1, 0),
                Insets.EMPTY
        )));

        return cell;
    }

    /**
     * Creates a date cell with booking bars for the given room and date
     */
    public StackPane createDateCell(GanttRoomData room, DateColumn dateCol) {
        StackPane cell = new StackPane();

        // Cell dimensions
        double width = dateCol.isWider() ? DATE_COLUMN_WIDTH_WIDER : DATE_COLUMN_WIDTH;
        cell.setPrefWidth(width);
        cell.setMinWidth(width);
        cell.setMaxWidth(width);
        cell.setPrefHeight(ROW_HEIGHT);
        cell.setMinHeight(ROW_HEIGHT);
        cell.setMaxHeight(ROW_HEIGHT);

        // Background color - grey if room is expanded, white otherwise (unless today or weekend)
        boolean isExpanded = !room.getBeds().isEmpty() && presenter.isRoomExpanded(room.getId());
        boolean isWeekend = isWeekend(dateCol.getDate());

        // Check for overbooking on this specific date (when collapsed)
        boolean hasOverbookingOnDate = false;
        if (!room.getBeds().isEmpty() && !isExpanded) {
            // For both single and multi-bed rooms: check if there's overbooking on this date
            hasOverbookingOnDate = hasOverbookingOnDate(room, dateCol.getDate());
        }

        Color bgColor;
        if (hasOverbookingOnDate) {
            // Danger red background for dates with overbooking (both single and multi-bed rooms when collapsed)
            bgColor = Color.web("#FFCDD2");
        } else if (dateCol.isToday()) {
            bgColor = GanttColorScheme.COLOR_BG_TODAY;
        } else if (isWeekend) {
            bgColor = isExpanded ? Color.web("#E0E8F0") : Color.web("#F0F4F8"); // Light blue for weekends
        } else if (isExpanded) {
            bgColor = Color.web("#ECECEC");
        } else {
            bgColor = Color.WHITE;
        }
        cell.setBackground(new Background(new BackgroundFill(bgColor, CornerRadii.EMPTY, Insets.EMPTY)));

        // Border - use right and bottom borders only for grid alignment
        cell.setBorder(new Border(new BorderStroke(
                null, GanttColorScheme.COLOR_BORDER, GanttColorScheme.COLOR_BORDER, null,
                BorderStrokeStyle.NONE, BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID, BorderStrokeStyle.NONE,
                CornerRadii.EMPTY,
                new BorderWidths(0, 1, 1, 0),
                Insets.EMPTY
        )));

        // Add booking bars
        List<BookingBar> bookingBars = presenter.calculateBookingBars(room, dateCol.getDate());
        boolean hasTurnover = false;
        for (BookingBar bar : bookingBars) {
            Node barNode = renderBookingBar(bar, room.getRoomType(), width, ROW_HEIGHT);
            cell.getChildren().add(barNode);
            if (bar.hasTurnover()) {
                hasTurnover = true;
            }
        }

        // Add turnover indicator if this cell has a same-day checkout/checkin
        if (hasTurnover) {
            StackPane turnoverIndicator = SvgIconFactory.createTurnoverIndicator();
            cell.getChildren().add(turnoverIndicator);
            StackPane.setAlignment(turnoverIndicator, Pos.TOP_CENTER);
            StackPane.setMargin(turnoverIndicator, new Insets(2, 0, 0, 0));
        }

        return cell;
    }

    /**
     * Renders a booking bar using the appropriate renderer
     */
    private Node renderBookingBar(BookingBar bar, RoomType roomType, double cellWidth, double cellHeight) {
        BookingBarRenderer renderer = (roomType == RoomType.SINGLE)
                ? singleRoomRenderer
                : multiRoomRenderer;
        return renderer.render(bar, cellWidth, cellHeight);
    }

    /**
     * Creates a bed name cell (indented to show hierarchy)
     */
    public StackPane createBedCell(GanttBedData bed, boolean isFirstBed) {
        HBox content = new HBox(6);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(0, 8, 0, 24)); // Extra left padding for indentation

        // Status dot
        Circle statusDot = new Circle(3); // Smaller dot for beds
        statusDot.setFill(colorScheme.getRoomStatusColor(bed.getStatus()));

        // Bed name
        Label bedName = new Label(bed.getName());
        // Use white text for overbooking beds (on red background), grey for normal beds
        bedName.setTextFill(bed.isOverbooking() ? Color.WHITE : Color.web("#666666"));
        bedName.setStyle("-fx-font-size: 12px; -fx-font-weight: " + (bed.isOverbooking() ? "bold" : "normal") + "; -fx-font-style: italic;");

        content.getChildren().addAll(statusDot, bedName);

        StackPane cell = new StackPane(content);
        cell.setAlignment(Pos.CENTER_LEFT);
        cell.setPrefWidth(ROOM_COLUMN_WIDTH);
        cell.setMinWidth(ROOM_COLUMN_WIDTH);
        cell.setMaxWidth(ROOM_COLUMN_WIDTH);
        cell.setPrefHeight(ROW_HEIGHT);
        cell.setMinHeight(ROW_HEIGHT);
        cell.setMaxHeight(ROW_HEIGHT);

        // Use danger red background for overbooking beds, grey for normal beds
        Color bgColor = bed.isOverbooking() ? Color.web("#FFCDD2") : Color.web("#ECECEC");
        cell.setBackground(new Background(new BackgroundFill(bgColor, CornerRadii.EMPTY, Insets.EMPTY)));

        // Right and bottom borders for alignment with date cells
        cell.setBorder(new Border(new BorderStroke(
                null, GanttColorScheme.COLOR_BORDER, GanttColorScheme.COLOR_BORDER, null,
                BorderStrokeStyle.NONE, BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID, BorderStrokeStyle.NONE,
                CornerRadii.EMPTY,
                new BorderWidths(0, 1, 1, 0),
                Insets.EMPTY
        )));

        return cell;
    }

    /**
     * Creates a date cell for an individual bed
     */
    public StackPane createBedDateCell(GanttBedData bed, DateColumn dateCol, boolean isFirstBed) {
        StackPane cell = new StackPane();

        // Cell dimensions
        double width = dateCol.isWider() ? DATE_COLUMN_WIDTH_WIDER : DATE_COLUMN_WIDTH;
        cell.setPrefWidth(width);
        cell.setMinWidth(width);
        cell.setMaxWidth(width);
        cell.setPrefHeight(ROW_HEIGHT);
        cell.setMinHeight(ROW_HEIGHT);
        cell.setMaxHeight(ROW_HEIGHT);

        // Background color - danger red for overbooking, otherwise grey/blue for bed rows
        boolean isWeekend = isWeekend(dateCol.getDate());
        Color bgColor;
        if (bed.isOverbooking()) {
            // Overbooking beds get danger red background
            bgColor = Color.web("#FFCDD2");
        } else if (dateCol.isToday()) {
            bgColor = GanttColorScheme.COLOR_BG_TODAY;
        } else if (isWeekend) {
            bgColor = Color.web("#E0E8F0"); // Light blue for weekends (darker shade for bed rows)
        } else {
            bgColor = Color.web("#ECECEC"); // Standard grey for bed rows
        }
        cell.setBackground(new Background(new BackgroundFill(bgColor, CornerRadii.EMPTY, Insets.EMPTY)));

        // Border - use right and bottom borders only for grid alignment
        cell.setBorder(new Border(new BorderStroke(
                null, GanttColorScheme.COLOR_BORDER, GanttColorScheme.COLOR_BORDER, null,
                BorderStrokeStyle.NONE, BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID, BorderStrokeStyle.NONE,
                CornerRadii.EMPTY,
                new BorderWidths(0, 1, 1, 0),
                Insets.EMPTY
        )));

        // Add booking bars for this bed
        boolean hasTurnover = false;
        for (GanttBookingData booking : bed.getBookings()) {
            // Check if booking is active on this date
            if (!dateCol.getDate().isBefore(booking.getStartDate()) && !dateCol.getDate().isAfter(booking.getEndDate())) {
                BookingPosition position = determinePosition(booking.getStartDate(), booking.getEndDate(), dateCol.getDate());
                // Show comment icon if booking has special needs (on MIDDLE position)
                boolean hasComments = (booking.getSpecialNeeds() != null && !booking.getSpecialNeeds().isEmpty())
                                   && position == BookingPosition.MIDDLE;

                // Check for turnover: another booking ends on this date while this one starts
                boolean bookingHasTurnover = false;
                if (dateCol.getDate().equals(booking.getStartDate())) {
                    bookingHasTurnover = bed.getBookings().stream()
                            .anyMatch(other -> other != booking &&
                                     dateCol.getDate().equals(other.getEndDate()) &&
                                     other.getStartDate().isBefore(dateCol.getDate()));
                }

                // Pass booking data for icon click handlers
                BookingBar bar = new BookingBar(booking.getStatus(), position, 1, 1, false, booking.getGuestName(), hasComments, bookingHasTurnover, booking);
                Node barNode = singleRoomRenderer.render(bar, width, ROW_HEIGHT);
                cell.getChildren().add(barNode);

                if (bookingHasTurnover) {
                    hasTurnover = true;
                }
            }
        }

        // Add turnover indicator if this cell has a same-day checkout/checkin
        if (hasTurnover) {
            StackPane turnoverIndicator = SvgIconFactory.createTurnoverIndicator();
            cell.getChildren().add(turnoverIndicator);
            StackPane.setAlignment(turnoverIndicator, Pos.TOP_CENTER);
            StackPane.setMargin(turnoverIndicator, new Insets(2, 0, 0, 0));
        }

        return cell;
    }

    /**
     * Checks if a date falls on a weekend (Saturday or Sunday)
     */
    private boolean isWeekend(java.time.LocalDate date) {
        java.time.DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == java.time.DayOfWeek.SATURDAY || dayOfWeek == java.time.DayOfWeek.SUNDAY;
    }

    /**
     * Checks if there is overbooking on a specific date.
     * For single rooms: overbooking occurs when 2+ bookings overlap on the same date.
     * For multi-bed rooms: overbooking occurs when active bookings exceed the number of regular beds.
     */
    private boolean hasOverbookingOnDate(GanttRoomData room, java.time.LocalDate date) {
        // Count how many bookings are active on this date across all beds
        int activeBookingsCount = 0;
        int regularBedCount = 0;

        for (GanttBedData bed : room.getBeds()) {
            // Count regular beds (non-overbooking beds)
            if (!bed.isOverbooking()) {
                regularBedCount++;
            }

            // Count active bookings on this date
            for (GanttBookingData booking : bed.getBookings()) {
                if (!date.isBefore(booking.getStartDate()) && !date.isAfter(booking.getEndDate())) {
                    activeBookingsCount++;
                }
            }
        }

        if (room.getRoomType() == RoomType.SINGLE) {
            // For single rooms: 2 or more active bookings = overbooking
            return activeBookingsCount >= 2;
        } else {
            // For multi-bed rooms: active bookings exceeding regular bed capacity = overbooking
            return activeBookingsCount > regularBedCount;
        }
    }

    /**
     * Determines the booking position for a bed
     */
    private BookingPosition determinePosition(java.time.LocalDate startDate, java.time.LocalDate endDate, java.time.LocalDate currentDate) {
        boolean isStart = currentDate.equals(startDate);
        boolean isEnd = currentDate.equals(endDate);

        if (isStart && isEnd) {
            return BookingPosition.SINGLE;
        } else if (isStart) {
            return BookingPosition.ARRIVAL;
        } else if (isEnd) {
            return BookingPosition.DEPARTURE;
        } else {
            return BookingPosition.MIDDLE;
        }
    }

    /**
     * Truncates text to specified length with ellipsis
     */
    private String truncateText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    /**
     * Gets the room column width
     */
    public double getRoomColumnWidth() {
        return ROOM_COLUMN_WIDTH;
    }

    /**
     * Gets the date column width (normal)
     */
    public double getDateColumnWidth() {
        return DATE_COLUMN_WIDTH;
    }

    /**
     * Gets the date column width (wider)
     */
    public double getDateColumnWidthWider() {
        return DATE_COLUMN_WIDTH_WIDER;
    }
}
