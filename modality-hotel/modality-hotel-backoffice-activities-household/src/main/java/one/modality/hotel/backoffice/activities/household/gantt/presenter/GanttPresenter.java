package one.modality.hotel.backoffice.activities.household.gantt.presenter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import one.modality.hotel.backoffice.activities.household.gantt.model.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Presenter for the Gantt view.
 * Handles all business logic and coordinates between data and view.
 *
 * @author Claude Code Assistant
 */
public class GanttPresenter {

    private final BookingAggregator bookingAggregator;
    private final ConflictDetector conflictDetector;
    private final GanttFilterManager filterManager;

    private LocalDate timeWindowStart;
    private LocalDate timeWindowEnd;
    private List<DateColumn> dateColumns;

    // Expand/collapse state for multi-bed rooms
    private final ObservableSet<String> expandedRoomIds = FXCollections.observableSet(new HashSet<>());

    public GanttPresenter() {
        this.bookingAggregator = new BookingAggregator();
        this.conflictDetector = new ConflictDetector();
        this.filterManager = new GanttFilterManager();

        // Initialize with default time window (centered on today)
        LocalDate today = LocalDate.now();
        this.timeWindowStart = today.minusDays(2);
        this.timeWindowEnd = today.plusDays(14);
        this.dateColumns = generateDateColumns();
    }

    /**
     * Gets the current time window start date
     */
    public LocalDate getTimeWindowStart() {
        return timeWindowStart;
    }

    /**
     * Gets the current time window end date
     */
    public LocalDate getTimeWindowEnd() {
        return timeWindowEnd;
    }

    /**
     * Gets the list of date columns for the current time window
     */
    public List<DateColumn> getDateColumns() {
        return dateColumns;
    }

    /**
     * Navigates the time window by the specified number of weeks
     */
    public void navigateWeeks(int weeks) {
        timeWindowStart = timeWindowStart.plusWeeks(weeks);
        timeWindowEnd = timeWindowEnd.plusWeeks(weeks);
        dateColumns = generateDateColumns();
    }

    /**
     * Navigates to today (centers today in the view)
     */
    public void navigateToToday() {
        LocalDate today = LocalDate.now();
        timeWindowStart = today.minusDays(2);
        timeWindowEnd = today.plusDays(14);
        dateColumns = generateDateColumns();
    }

    /**
     * Sets a custom date range
     */
    public void setDateRange(LocalDate start, LocalDate end) {
        this.timeWindowStart = start;
        this.timeWindowEnd = end;
        this.dateColumns = generateDateColumns();
    }

    /**
     * Calculates booking bars for a specific room and date.
     * Returns a list of BookingBar objects that need to be rendered.
     */
    public List<BookingBar> calculateBookingBars(GanttRoomData room, LocalDate date) {
        List<BookingBar> bars = new ArrayList<>();

        if (room.getRoomType() == RoomType.SINGLE_BED) {
            // Single room: check if it has beds (overbooking case when collapsed)
            if (!room.getBeds().isEmpty() && !isRoomExpanded(room.getId())) {
                // Single room with overbooking, collapsed: show first booking from beds
                // Collect all bookings from all beds
                List<GanttBookingData> allBookings = new ArrayList<>();
                for (GanttBedData bed : room.getBeds()) {
                    for (GanttBookingData booking : bed.getBookings()) {
                        if (!date.isBefore(booking.getStartDate()) && !date.isAfter(booking.getEndDate())) {
                            allBookings.add(booking);
                        }
                    }
                }

                if (!allBookings.isEmpty()) {
                    // Sort by start date to get the first booking
                    allBookings.sort(Comparator.comparing(GanttBookingData::getStartDate));
                    GanttBookingData firstBooking = allBookings.get(0);

                    // Create bar for the first booking
                    bars.add(bookingAggregator.createSingleRoomBookingBar(firstBooking, date));
                }
            } else {
                // Normal single room (no overbooking) or expanded
                List<? extends GanttBookingData> activeBookings = room.getBookings().stream()
                        .filter(b -> !date.isBefore(b.getStartDate()) && !date.isAfter(b.getEndDate()))
                        .collect(Collectors.toList());

                if (activeBookings.isEmpty()) {
                    return bars;
                }

                boolean hasConflict = conflictDetector.hasConflict(room, date);
                boolean hasTurnover = conflictDetector.hasTurnover(room, date);

                if (hasConflict) {
                    // Multiple overlapping bookings - render all with conflict indicator
                    for (GanttBookingData booking : activeBookings) {
                        BookingBar bar = bookingAggregator.createSingleRoomBookingBar(booking, date);
                        BookingBar conflictBar = new BookingBar(
                                bar.status(),
                                bar.position(),
                                bar.occupancy(),
                                bar.totalCapacity(),
                                true, // Mark as conflict
                                bar.guestInfo(),
                                bar.hasComments(),
                                false
                        );
                        bars.add(conflictBar);
                    }
                } else if (hasTurnover) {
                    // Same-day turnover - render both departure and arrival with turnover flag
                    for (GanttBookingData booking : activeBookings) {
                        BookingBar bar = bookingAggregator.createSingleRoomBookingBar(booking, date);
                        BookingBar turnoverBar = new BookingBar(
                                bar.status(),
                                bar.position(),
                                bar.occupancy(),
                                bar.totalCapacity(),
                                false,
                                bar.guestInfo(),
                                bar.hasComments(),
                                true // Mark as turnover
                        );
                        bars.add(turnoverBar);
                    }
                } else {
                    // Normal single booking
                    bars.add(bookingAggregator.createSingleRoomBookingBar(activeBookings.get(0), date));
                }
            }

        } else {
            // Multi-bed room: aggregate bookings across beds
            BookingBar aggregatedBar = bookingAggregator.aggregateMultiBedBookings(room, date);
            if (aggregatedBar != null) {
                bars.add(aggregatedBar);
            }
        }

        return bars;
    }

    /**
     * Generates date columns based on current time window.
     * Spans from timeWindowStart to timeWindowEnd, with today highlighted.
     */
    private List<DateColumn> generateDateColumns() {
        List<DateColumn> columns = new ArrayList<>();
        LocalDate today = LocalDate.now();

        LocalDate current = timeWindowStart;
        while (!current.isAfter(timeWindowEnd)) {
            boolean isToday = current.equals(today);
            // Last 2 columns are wider (design spec)
            boolean isWider = current.isAfter(timeWindowEnd.minusDays(2));
            columns.add(new DateColumn(current, isToday, isWider));
            current = current.plusDays(1);
        }

        return columns;
    }

    /**
     * Gets the filter manager
     */
    public GanttFilterManager getFilterManager() {
        return filterManager;
    }

    /**
     * Toggles expand/collapse state for a multi-bed room
     */
    public void toggleRoomExpanded(String roomId) {
        if (expandedRoomIds.contains(roomId)) {
            expandedRoomIds.remove(roomId);
        } else {
            expandedRoomIds.add(roomId);
        }
    }

    /**
     * Checks if a room is currently expanded
     */
    public boolean isRoomExpanded(String roomId) {
        return expandedRoomIds.contains(roomId);
    }

    /**
     * Gets the observable set of expanded room IDs (for UI binding)
     */
    public ObservableSet<String> getExpandedRoomIds() {
        return expandedRoomIds;
    }

    /**
     * Applies filters to a list of rooms
     */
    public List<GanttRoomData> applyFilters(List<GanttRoomData> rooms) {
        return filterManager.applyFilters(rooms);
    }
}
