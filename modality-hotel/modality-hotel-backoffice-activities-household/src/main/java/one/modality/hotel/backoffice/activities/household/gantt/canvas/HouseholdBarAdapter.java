package one.modality.hotel.backoffice.activities.household.gantt.canvas;

import dev.webfx.extras.time.layout.bar.LocalDateBar;
import one.modality.hotel.backoffice.activities.household.gantt.canvas.HouseholdGanttCanvas.HouseholdBookingBlock;
import one.modality.hotel.backoffice.activities.household.gantt.model.*;
import one.modality.hotel.backoffice.activities.household.gantt.presenter.GanttPresenter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter that converts GanttRoomData bookings to Canvas LocalDateBar format.
 *
 * This adapter:
 * - Handles both single rooms (bookings on room) and multi-bed rooms (bookings on beds)
 * - Converts GanttBookingData directly into LocalDateBar objects for Canvas rendering
 * - Maintains proper parent/child relationships for tetris packing layout
 *
 * @author Claude Code Assistant
 */
public class HouseholdBarAdapter {

    private final GanttPresenter presenter;

    public HouseholdBarAdapter(GanttPresenter presenter) {
        this.presenter = presenter;
    }

    /**
     * Converts room bookings into LocalDateBar objects for Canvas rendering.
     *
     * IMPORTANT: Multi-bed rooms have bookings in getBeds(), not getBookings()!
     * - Single rooms: bookings are in room.getBookings()
     * - Multi-bed rooms: room.getBookings() is EMPTY, bookings are in room.getBeds().get(i).getBookings()
     *
     * Multi-bed room behavior:
     * - When COLLAPSED: Create aggregated bar with room as parent
     * - When EXPANDED: Create individual bed bars with each bed as parent (NO aggregated bar)
     *
     * @param room The room to process
     * @return List of LocalDateBar objects ready for Canvas rendering
     */
    public List<LocalDateBar<HouseholdBookingBlock>> adaptRoomBookings(GanttRoomData room) {
        // Check if this is a multi-bed room (bookings are in beds, not directly on room)
        if (!room.getBeds().isEmpty()) {
            boolean isExpanded = presenter.isRoomExpanded(room.getId());
            List<LocalDateBar<HouseholdBookingBlock>> allBars = new ArrayList<>();

            if (isExpanded) {
                // EXPANDED: Create individual bed bars (each bed is a parent row)
                int bedIndex = 0;
                for (GanttBedData bed : room.getBeds()) {
                    GanttParentRow bedParent = GanttParentRow.forBed(room, bed, bedIndex);
                    List<LocalDateBar<HouseholdBookingBlock>> bedBars = adaptBookingsToBars(
                        bed.getBookings(), bedParent);

                    allBars.addAll(bedBars);
                    bedIndex++;
                }
            } else {
                // COLLAPSED: Create aggregated bar (room is the parent)
                GanttParentRow roomParent = GanttParentRow.forRoom(room);
                allBars.addAll(createAggregatedBars(room, roomParent));
            }

            return allBars;
        }

        // Single room - process room's bookings directly (room is the parent)
        GanttParentRow roomParent = GanttParentRow.forRoom(room);
        return adaptBookingsToBars(room.getBookings(), roomParent);
    }

    /**
     * Converts a list of bookings into LocalDateBar objects.
     * This is the core adaptation logic used for both single rooms and individual beds in multi-bed rooms.
     */
    private List<LocalDateBar<HouseholdBookingBlock>> adaptBookingsToBars(
            List<? extends GanttBookingData> bookings, GanttParentRow parentRow) {
        List<LocalDateBar<HouseholdBookingBlock>> bars = new ArrayList<>();

        // Convert each booking directly to a bar
        for (GanttBookingData booking : bookings) {
            LocalDate startDate = booking.getStartDate();
            LocalDate endDate = booking.getEndDate();

            if (startDate == null || endDate == null) {
                continue;
            }

            // Determine position for icon placement (always ARRIVAL for Canvas bars to show person icon)
            BookingPosition position = BookingPosition.ARRIVAL; // Show person icon on first day

            // Create booking block
            HouseholdBookingBlock block = new HouseholdBookingBlock(
                booking.getGuestName(),
                booking.getStatus(),
                position,
                false, // No conflict detection needed for direct conversion
                booking.getComments() != null && !booking.getComments().isEmpty(),
                false, // Turnover detection done by presenter
                !booking.isArrived() && booking.getStartDate().isBefore(LocalDate.now()),
                booking,
                1, // Single booking occupancy
                parentRow.getRoom().getCapacity()
            );
            block.setParentRow(parentRow);

            // Create bar - endDate is already the departure day (day after last night)
            bars.add(new LocalDateBar<>(block, startDate, endDate));
        }

        return bars;
    }

    /**
     * Creates aggregated bars for collapsed multi-bed rooms.
     * Shows occupancy count instead of individual guest names.
     *
     * Creates SEPARATE bars for each contiguous segment of occupied days.
     * When occupancy drops to 0, the bar ends and a new bar starts when occupancy resumes.
     */
    private List<LocalDateBar<HouseholdBookingBlock>> createAggregatedBars(GanttRoomData room, GanttParentRow parentRow) {
        List<LocalDateBar<HouseholdBookingBlock>> bars = new ArrayList<>();

        // Collect all bookings from all beds
        List<GanttBookingData> allBookings = new ArrayList<>();
        for (GanttBedData bed : room.getBeds()) {
            allBookings.addAll(bed.getBookings());
        }

        if (allBookings.isEmpty()) {
            return bars;
        }

        // Use first booking as representative for aggregated bars
        GanttBookingData representativeBooking = allBookings.get(0);

        // Find the overall date range
        LocalDate minStart = null;
        LocalDate maxEnd = null;

        for (GanttBookingData booking : allBookings) {
            if (booking.getStartDate() == null || booking.getEndDate() == null) continue;

            if (minStart == null || booking.getStartDate().isBefore(minStart)) {
                minStart = booking.getStartDate();
            }
            if (maxEnd == null || booking.getEndDate().isAfter(maxEnd)) {
                maxEnd = booking.getEndDate();
            }
        }

        if (minStart == null || maxEnd == null) {
            return bars;
        }

        // Calculate total bed capacity from room configuration
        int totalCapacity = room.getCapacity(); // Use room's max capacity from configuration

        // Calculate daily occupancy for each day in the range
        java.util.Map<LocalDate, Integer> dailyOccupancy = new java.util.HashMap<>();
        LocalDate currentDate = minStart;
        while (!currentDate.isAfter(maxEnd)) {
            int occupancyOnDate = 0;
            for (GanttBookingData booking : allBookings) {
                if (booking.getStartDate() == null || booking.getEndDate() == null) continue;
                // A booking occupies a bed from startDate (inclusive) to endDate (exclusive)
                if (!currentDate.isBefore(booking.getStartDate()) && currentDate.isBefore(booking.getEndDate())) {
                    occupancyOnDate++;
                }
            }
            dailyOccupancy.put(currentDate, occupancyOnDate);
            currentDate = currentDate.plusDays(1);
        }

        // Group consecutive days with occupancy > 0 into segments
        // Each segment becomes a separate bar
        LocalDate segmentStart = null;
        java.util.Map<LocalDate, Integer> segmentOccupancy = new java.util.HashMap<>();
        List<GanttBookingData> segmentBookings = new ArrayList<>();
        int maxOccupancyInSegment = 0;

        currentDate = minStart;
        while (!currentDate.isAfter(maxEnd)) {
            Integer occupancy = dailyOccupancy.get(currentDate);
            if (occupancy == null) occupancy = 0;

            if (occupancy > 0) {
                // Day has occupancy
                if (segmentStart == null) {
                    // Start new segment
                    segmentStart = currentDate;
                    segmentOccupancy.clear();
                    segmentBookings.clear();
                    maxOccupancyInSegment = 0;
                }
                // Add to current segment
                segmentOccupancy.put(currentDate, occupancy);
                if (occupancy > maxOccupancyInSegment) {
                    maxOccupancyInSegment = occupancy;
                }

                // Collect all bookings that overlap with this day
                for (GanttBookingData booking : allBookings) {
                    if (booking.getStartDate() == null || booking.getEndDate() == null) continue;
                    if (!currentDate.isBefore(booking.getStartDate()) && currentDate.isBefore(booking.getEndDate())) {
                        if (!segmentBookings.contains(booking)) {
                            segmentBookings.add(booking);
                        }
                    }
                }
            } else {
                // Day has no occupancy - end current segment if any
                if (segmentStart != null) {
                    // Create bar for the segment
                    LocalDate segmentEnd = currentDate; // Exclusive end
                    HouseholdBookingBlock block = new HouseholdBookingBlock(
                        "", // Guest name not needed for aggregate bars
                        BookingStatus.CONFIRMED, // Default status for aggregated view
                        BookingPosition.MIDDLE,
                        false,
                        false,
                        false,
                        false,
                        representativeBooking,
                        maxOccupancyInSegment,
                        totalCapacity,
                        new java.util.HashMap<>(segmentOccupancy), // Copy of segment occupancy
                        new ArrayList<>(segmentBookings) // Copy of segment bookings
                    );
                    block.setParentRow(parentRow);
                    bars.add(new LocalDateBar<>(block, segmentStart, segmentEnd));

                    // Reset segment
                    segmentStart = null;
                }
            }

            currentDate = currentDate.plusDays(1);
        }

        // Create bar for final segment if it exists
        if (segmentStart != null) {
            LocalDate segmentEnd = maxEnd.plusDays(1); // End is exclusive, so add 1 day
            HouseholdBookingBlock block = new HouseholdBookingBlock(
                "", // Guest name not needed for aggregate bars
                BookingStatus.CONFIRMED, // Default status for aggregated view
                BookingPosition.MIDDLE,
                false,
                false,
                false,
                false,
                representativeBooking,
                maxOccupancyInSegment,
                totalCapacity,
                new java.util.HashMap<>(segmentOccupancy), // Copy of segment occupancy
                new ArrayList<>(segmentBookings) // Copy of segment bookings
            );
            block.setParentRow(parentRow);
            bars.add(new LocalDateBar<>(block, segmentStart, segmentEnd));
        }

        return bars;
    }

    /**
     * Adapts all rooms into LocalDateBar format
     */
    public List<LocalDateBar<HouseholdBookingBlock>> adaptAllRooms(List<GanttRoomData> rooms) {
        List<LocalDateBar<HouseholdBookingBlock>> allBars = new ArrayList<>();

        for (GanttRoomData room : rooms) {
            allBars.addAll(adaptRoomBookings(room));
        }

        return allBars;
    }

    /**
     * Container class that holds both parent rows and their associated bars.
     */
    public static class AdaptedRoomData {
        private final List<GanttParentRow> parentRows;
        private final List<LocalDateBar<HouseholdBookingBlock>> bars;

        public AdaptedRoomData(List<GanttParentRow> parentRows, List<LocalDateBar<HouseholdBookingBlock>> bars) {
            this.parentRows = parentRows;
            this.bars = bars;
        }

        public List<GanttParentRow> getParentRows() {
            return parentRows;
        }

        public List<LocalDateBar<HouseholdBookingBlock>> getBars() {
            return bars;
        }
    }

    /**
     * Adapts all rooms, creating both parent rows and bars together.
     * This ensures bars reference the correct parent row objects.
     */
    public AdaptedRoomData adaptAllRoomsWithParents(List<GanttRoomData> rooms) {
        List<GanttParentRow> allParentRows = new ArrayList<>();
        List<LocalDateBar<HouseholdBookingBlock>> allBars = new ArrayList<>();

        for (GanttRoomData room : rooms) {
            boolean isMultiBed = !room.getBeds().isEmpty();
            boolean isExpanded = isMultiBed && presenter.isRoomExpanded(room.getId());

            if (isExpanded) {
                // EXPANDED multi-bed room: Create room parent WITH aggregated bar + bed parents (with individual bars)
                GanttParentRow roomParent = GanttParentRow.forRoom(room);
                allParentRows.add(roomParent);

                // Add aggregated bar on room row as summary
                List<LocalDateBar<HouseholdBookingBlock>> roomBars = createAggregatedBars(room, roomParent);
                allBars.addAll(roomBars);

                // Add bed parents with their booking bars (below the room)
                int bedIndex = 0;
                for (GanttBedData bed : room.getBeds()) {
                    GanttParentRow bedParent = GanttParentRow.forBed(room, bed, bedIndex);
                    allParentRows.add(bedParent);

                    List<LocalDateBar<HouseholdBookingBlock>> bedBars = adaptBookingsToBars(
                        bed.getBookings(), bedParent);
                    allBars.addAll(bedBars);

                    bedIndex++;
                }
            } else if (isMultiBed) {
                // COLLAPSED multi-bed room: Create room parent with aggregated bar
                GanttParentRow roomParent = GanttParentRow.forRoom(room);
                allParentRows.add(roomParent);

                List<LocalDateBar<HouseholdBookingBlock>> roomBars = createAggregatedBars(room, roomParent);
                allBars.addAll(roomBars);
            } else {
                // Single room: Create room parent with individual booking bars
                GanttParentRow roomParent = GanttParentRow.forRoom(room);
                allParentRows.add(roomParent);

                List<LocalDateBar<HouseholdBookingBlock>> roomBars = adaptBookingsToBars(
                    room.getBookings(), roomParent);
                allBars.addAll(roomBars);
            }
        }

        return new AdaptedRoomData(allParentRows, allBars);
    }
}
