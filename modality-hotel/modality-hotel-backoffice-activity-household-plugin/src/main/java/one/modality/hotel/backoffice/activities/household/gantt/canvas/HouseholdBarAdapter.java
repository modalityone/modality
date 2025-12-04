package one.modality.hotel.backoffice.activities.household.gantt.canvas;

import dev.webfx.extras.time.layout.bar.LocalDateBar;
import one.modality.hotel.backoffice.activities.household.gantt.canvas.HouseholdGanttCanvas.HouseholdBookingBlock;
import one.modality.hotel.backoffice.activities.household.gantt.model.*;
import one.modality.hotel.backoffice.activities.household.gantt.presenter.GanttPresenter;

import java.time.LocalDate;
import java.util.*;

/**
 * Adapter that converts GanttRoomData bookings to Canvas LocalDateBar format.
 * <p>
 * This adapter:
 * - Handles both single rooms (bookings on room) and multi-bed rooms (bookings on beds)
 * - Converts GanttBookingData directly into LocalDateBar objects for Canvas rendering
 * - Maintains proper parent/child relationships for tetris packing layout
 *
 * @author Claude Code Assistant
 */
public record HouseholdBarAdapter(GanttPresenter presenter) {

    /**
     * Converts a list of bookings into LocalDateBar objects.
     * This is the core adaptation logic used for both single rooms and individual beds in multi-bed rooms.
     * <p>
     * For bookings with attendance gaps, creates MULTIPLE bars (one per date segment) to show
     * the interruption visually in the Gantt chart.
     *
     * @param bookings The bookings to convert to bars (may be a subset, e.g., one bed's bookings)
     * @param parentRow The parent row these bars belong to
     * @param allRoomBookings ALL bookings in the room (for turnover detection across beds)
     */
    private List<LocalDateBar<HouseholdBookingBlock>> adaptBookingsToBars(
            List<? extends GanttBookingData> bookings, GanttParentRow parentRow,
            List<GanttBookingData> allRoomBookings) {
        List<LocalDateBar<HouseholdBookingBlock>> bars = new ArrayList<>();

        // Convert each booking to bar(s) - may create multiple bars for gap bookings
        for (GanttBookingData booking : bookings) {
            // Detect turnover: does this booking start when another booking in the ROOM ends?
            // Use allRoomBookings to detect turnovers across different beds in multi-bed rooms
            boolean hasTurnover = detectTurnover(booking, allRoomBookings);

            List<DateSegment> segments = booking.getDateSegments();

            if (segments == null || segments.isEmpty()) {
                // Fallback to simple start/end dates if no segments
                LocalDate startDate = booking.getStartDate();
                LocalDate endDate = booking.getEndDate();
                if (startDate == null || endDate == null) {
                    continue;
                }
                // endDate from booking is last night stayed (inclusive), convert to checkout day (exclusive)
                LocalDate checkoutDate = endDate.plusDays(1);
                bars.add(createBarForSegment(booking, startDate, checkoutDate, parentRow, true, hasTurnover));
            } else {
                // Create a bar for each segment (handles gaps)
                boolean isFirstSegment = true;
                for (DateSegment segment : segments) {
                    // segment.endDate() is last night stayed (inclusive), convert to checkout day (exclusive)
                    LocalDate checkoutDate = segment.endDate().plusDays(1);
                    // Only show turnover on first segment (where guest arrives)
                    bars.add(createBarForSegment(booking, segment.startDate(), checkoutDate, parentRow, isFirstSegment, isFirstSegment && hasTurnover));
                    isFirstSegment = false;
                }
            }
        }

        return bars;
    }

    /**
     * Detects if this booking has a turnover situation (another booking ends when this one starts).
     * A turnover means the room needs cleaning between guests on the same day.
     *
     * @param booking The booking to check
     * @param allRoomBookings All bookings in the room (including bookings from other beds)
     * @return true if another booking's checkout day equals this booking's checkin day
     */
    private boolean detectTurnover(GanttBookingData booking, List<GanttBookingData> allRoomBookings) {
        LocalDate startDate = booking.getStartDate();
        if (startDate == null) {
            return false;
        }

        for (GanttBookingData other : allRoomBookings) {
            if (other == booking) {
                continue;
            }
            // endDate from database is the last night stayed (inclusive)
            // Checkout day = endDate + 1 day
            // Turnover occurs when checkout day equals checkin day
            LocalDate otherEndDate = other.getEndDate();
            if (otherEndDate != null) {
                LocalDate otherCheckoutDay = otherEndDate.plusDays(1);
                if (otherCheckoutDay.equals(startDate)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Creates a single bar for a date segment of a booking.
     *
     * @param booking The booking data
     * @param startDate Start date of this segment (check-in day, inclusive)
     * @param checkoutDate Checkout date of this segment (exclusive - bar ends at start of this day)
     * @param parentRow The parent row this bar belongs to
     * @param isFirstSegment True if this is the first segment (unused - all segments show icons)
     * @param hasTurnover True if this is a turnover situation (another guest checks out on arrival day)
     * @return LocalDateBar for this segment
     */
    private LocalDateBar<HouseholdBookingBlock> createBarForSegment(
            GanttBookingData booking, LocalDate startDate, LocalDate checkoutDate,
            GanttParentRow parentRow, boolean isFirstSegment, boolean hasTurnover) {

        // All segments show person icon and comments - important for gap bookings
        // so users can identify who the bar belongs to and access actions
        BookingPosition position = BookingPosition.ARRIVAL;

        // Check if we need to show the "room not ready" warning icon
        // Conditions: guest arriving today, hasn't arrived yet, and room is not ready
        LocalDate today = LocalDate.now();
        boolean hasRoomNotReadyWarning = false;
        if (startDate.equals(today) && !booking.isArrived()) {
            // Get room status from the parent row
            RoomStatus roomStatus = parentRow.isBed() && parentRow.bed() != null
                    ? parentRow.bed().getStatus()
                    : parentRow.room().getStatus();
            // Room is not ready if it needs cleaning or inspection
            hasRoomNotReadyWarning = (roomStatus == RoomStatus.TO_CLEAN || roomStatus == RoomStatus.TO_INSPECT);
        }

        // Create booking block
        HouseholdBookingBlock block = new HouseholdBookingBlock(
                booking.getGuestName(),
                booking.getStatus(),
                position,
                false, // No conflict detection needed for direct conversion
                booking.getComments() != null && !booking.getComments().isEmpty(),
                hasTurnover, // Show turnover indicator when room needs cleaning before this guest
                !booking.isArrived() && booking.getStartDate().isBefore(LocalDate.now()),
                hasRoomNotReadyWarning,
                booking,
                1, // Single booking occupancy
                parentRow.room().getCapacity(),
                null, // No daily occupancy for individual bars
                null  // No segment bookings for individual bars
        );
        block.setParentRow(parentRow);

        // Create bar - checkoutDate is the departure day (exclusive end for the layout)
        return new LocalDateBar<>(block, startDate, checkoutDate);
    }

    /**
     * Creates aggregated bars for collapsed multi-bed rooms.
     * Shows occupancy count instead of individual guest names.
     * <p>
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

        // PERFORMANCE OPTIMIZATION: Use event-sweep algorithm O(B log B) instead of O(D × B)
        // Create events for check-in (+1) and check-out (-1)
        // Use date segments to properly handle bookings with attendance gaps
        List<AbstractMap.SimpleEntry<LocalDate, Integer>> events = new ArrayList<>();
        for (GanttBookingData booking : allBookings) {
            List<DateSegment> segments = booking.getDateSegments();
            if (segments != null && !segments.isEmpty()) {
                // Use segments for gap bookings
                // Note: segment.endDate() is inclusive (last night stayed), so checkout is endDate + 1
                for (DateSegment segment : segments) {
                    events.add(new AbstractMap.SimpleEntry<>(segment.startDate(), 1));   // Check-in
                    events.add(new AbstractMap.SimpleEntry<>(segment.endDate().plusDays(1), -1));    // Check-out (day after last night)
                }
            } else {
                // Fallback to simple dates
                // Note: booking.getEndDate() is last night stayed (inclusive), so checkout is endDate + 1
                if (booking.getStartDate() == null || booking.getEndDate() == null) continue;
                events.add(new AbstractMap.SimpleEntry<>(booking.getStartDate(), 1));   // Check-in
                events.add(new AbstractMap.SimpleEntry<>(booking.getEndDate().plusDays(1), -1));    // Check-out (day after last night)
            }
        }

        // Sort events by date, with check-outs before check-ins on same day (for accurate count)
        // -1 (checkout) before +1 (checkin)
        events.sort(Comparator.comparing((AbstractMap.SimpleEntry<LocalDate, Integer> a) -> a.getKey()).thenComparingInt(AbstractMap.SimpleEntry::getValue));

        // Sweep through events to build daily occupancy map
        Map<LocalDate, Integer> dailyOccupancy = getLocalDateIntegerMap(minStart, events, maxEnd);

        // Group consecutive days with occupancy > 0 into segments
        // Each segment becomes a separate bar
        LocalDate segmentStart = null;
        Map<LocalDate, Integer> segmentOccupancy = new HashMap<>();
        List<GanttBookingData> segmentBookings = new ArrayList<>();
        int maxOccupancyInSegment = 0;

        LocalDate today = LocalDate.now();
        LocalDate currentDate = minStart;
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

                // Collect all bookings that overlap with this day (using date segments for gap support)
                for (GanttBookingData booking : allBookings) {
                    if (isBookingActiveOnDate(booking, currentDate)) {
                        if (!segmentBookings.contains(booking)) {
                            segmentBookings.add(booking);
                        }
                    }
                }
            } else {
                // Day has no occupancy - end current segment if any
                if (segmentStart != null) {
                    // Create bar for the segment
                    // Exclusive end (currentDate is the first day with 0 occupancy)
                    LocalDate segmentEnd = currentDate;
                    // Determine status based on segment bookings - OCCUPIED only if at least one bed is occupied
                    BookingStatus segmentStatus = determineSegmentStatus(segmentStart, segmentEnd.minusDays(1), today, segmentBookings);

                    // Detect overbooking: when max occupancy in segment exceeds total capacity
                    boolean hasConflict = maxOccupancyInSegment > totalCapacity;

                    // Check for room not ready warning: someone arriving today, not arrived yet, room not ready
                    boolean hasRoomNotReadyWarning = checkRoomNotReadyForSegment(segmentStart, segmentEnd, today, segmentBookings, parentRow);

                    HouseholdBookingBlock block = new HouseholdBookingBlock(
                            "", // Guest name not needed for aggregate bars
                            segmentStatus,
                            BookingPosition.MIDDLE,
                            hasConflict,
                            false,
                            false,
                            false,
                            hasRoomNotReadyWarning,
                            representativeBooking,
                            maxOccupancyInSegment,
                            totalCapacity,
                            new HashMap<>(segmentOccupancy), // Copy of segment occupancy
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
            // Determine status based on segment bookings - OCCUPIED only if at least one bed is occupied
            BookingStatus segmentStatus = determineSegmentStatus(segmentStart, segmentEnd.minusDays(1), today, segmentBookings);

            // Detect overbooking: when max occupancy in segment exceeds total capacity
            boolean hasConflict = maxOccupancyInSegment > totalCapacity;

            // Check for room not ready warning: someone arriving today, not arrived yet, room not ready
            boolean hasRoomNotReadyWarning = checkRoomNotReadyForSegment(segmentStart, segmentEnd, today, segmentBookings, parentRow);

            HouseholdBookingBlock block = new HouseholdBookingBlock(
                    "", // Guest name not needed for aggregate bars
                    segmentStatus,
                    BookingPosition.MIDDLE,
                    hasConflict,
                    false,
                    false,
                    false,
                    hasRoomNotReadyWarning,
                    representativeBooking,
                    maxOccupancyInSegment,
                    totalCapacity,
                    new HashMap<>(segmentOccupancy), // Copy of segment occupancy
                    new ArrayList<>(segmentBookings) // Copy of segment bookings
            );
            block.setParentRow(parentRow);
            bars.add(new LocalDateBar<>(block, segmentStart, segmentEnd));
        }

        return bars;
    }

    /**
     * Checks if the room not ready warning should be shown for an aggregate bar segment.
     * Returns true if:
     * 1. Today falls within the segment date range
     * 2. At least one booking starts today and the guest hasn't arrived yet
     * 3. The room is not ready (TO_CLEAN or TO_INSPECT status)
     */
    private boolean checkRoomNotReadyForSegment(LocalDate segmentStart, LocalDate segmentEnd, LocalDate today,
                                                 List<GanttBookingData> segmentBookings, GanttParentRow parentRow) {
        // Check if today is within segment range
        if (today.isBefore(segmentStart) || !today.isBefore(segmentEnd)) {
            return false;
        }

        // Check if room is not ready
        RoomStatus roomStatus = parentRow.room().getStatus();
        if (roomStatus != RoomStatus.TO_CLEAN && roomStatus != RoomStatus.TO_INSPECT) {
            return false;
        }

        // Check if any booking starts today and guest hasn't arrived
        for (GanttBookingData booking : segmentBookings) {
            if (booking.getStartDate() != null && booking.getStartDate().equals(today) && !booking.isArrived()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Determines the booking status for an aggregate bar segment.
     * - DEPARTED (grey): segment ended before today
     * - OCCUPIED (red): at least one booking in the segment has OCCUPIED status
     * - CONFIRMED (blue): segment starts after today OR today is within segment but no bed is occupied yet
     */
    private BookingStatus determineSegmentStatus(LocalDate segmentStart, LocalDate lastOccupiedDay, LocalDate today,
                                                  List<GanttBookingData> segmentBookings) {
        if (lastOccupiedDay.isBefore(today)) {
            // Segment ended before today - past booking
            return BookingStatus.DEPARTED;
        } else if (!segmentStart.isAfter(today) && !lastOccupiedDay.isBefore(today)) {
            // Today falls within the segment - check if at least one bed is actually occupied
            boolean hasOccupiedBed = segmentBookings.stream()
                    .anyMatch(booking -> booking.getStatus() == BookingStatus.OCCUPIED);
            if (hasOccupiedBed) {
                return BookingStatus.OCCUPIED;
            } else {
                // Today is within segment dates but no guest has arrived yet - show as confirmed (blue)
                return BookingStatus.CONFIRMED;
            }
        } else {
            // Segment starts after today - future booking
            return BookingStatus.CONFIRMED;
        }
    }

    private static Map<LocalDate, Integer> getLocalDateIntegerMap(LocalDate minStart, List<AbstractMap.SimpleEntry<LocalDate, Integer>> events, LocalDate maxEnd) {
        Map<LocalDate, Integer> dailyOccupancy = new HashMap<>();
        int currentOccupancy = 0;
        LocalDate prevDate = minStart;

        for (AbstractMap.SimpleEntry<LocalDate, Integer> event : events) {
            LocalDate eventDate = event.getKey();

            // Fill in days between previous event and current event with the current occupancy
            LocalDate fillDate = prevDate;
            while (fillDate.isBefore(eventDate)) {
                dailyOccupancy.put(fillDate, currentOccupancy);
                fillDate = fillDate.plusDays(1);
            }

            // Apply the event (check-in or check-out)
            currentOccupancy += event.getValue();
            prevDate = eventDate;
        }

        // Fill remaining days after last event
        LocalDate fillDate = prevDate;
        while (!fillDate.isAfter(maxEnd)) {
            dailyOccupancy.put(fillDate, currentOccupancy);
            fillDate = fillDate.plusDays(1);
        }
        return dailyOccupancy;
    }

    /**
         * Container class that holds both parent rows and their associated bars.
         */
        public record AdaptedRoomData(List<GanttParentRow> parentRows, List<LocalDateBar<HouseholdBookingBlock>> bars) {
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

            // Collect ALL bookings in this room (from all beds) for turnover detection
            // Turnovers happen at room level, not bed level
            List<GanttBookingData> allRoomBookings = collectAllRoomBookings(room);

            if (isExpanded) {
                // EXPANDED multi-bed room: Create room parent WITH aggregated bar + bed parents (with individual bars)
                GanttParentRow roomParent = GanttParentRow.forRoom(room, true);
                allParentRows.add(roomParent);

                // Add aggregated bar on room row as summary
                List<LocalDateBar<HouseholdBookingBlock>> roomBars = createAggregatedBars(room, roomParent);
                allBars.addAll(roomBars);

                // Add bed parents with their booking bars (below the room)
                for (GanttBedData bed : room.getBeds()) {
                    GanttParentRow bedParent = GanttParentRow.forBed(room, bed);
                    allParentRows.add(bedParent);

                    // Pass ALL room bookings for turnover detection (across beds)
                    List<LocalDateBar<HouseholdBookingBlock>> bedBars = adaptBookingsToBars(
                            bed.getBookings(), bedParent, allRoomBookings);
                    allBars.addAll(bedBars);
                }
            } else if (isMultiBed) {
                // COLLAPSED multi-bed room: Create room parent with aggregated bar
                GanttParentRow roomParent = GanttParentRow.forRoom(room, false);
                allParentRows.add(roomParent);

                List<LocalDateBar<HouseholdBookingBlock>> roomBars = createAggregatedBars(room, roomParent);
                allBars.addAll(roomBars);
            } else {
                // Single room: Create room parent with individual booking bars
                GanttParentRow roomParent = GanttParentRow.forRoom(room, false);
                allParentRows.add(roomParent);

                // For single rooms, allRoomBookings is the same as room.getBookings()
                List<LocalDateBar<HouseholdBookingBlock>> roomBars = adaptBookingsToBars(
                        room.getBookings(), roomParent, allRoomBookings);
                allBars.addAll(roomBars);
            }
        }

        return new AdaptedRoomData(allParentRows, allBars);
    }

    /**
     * Collects all bookings from a room (including bookings from all beds).
     * Used for room-level turnover detection.
     */
    private List<GanttBookingData> collectAllRoomBookings(GanttRoomData room) {
        List<GanttBookingData> allBookings = new ArrayList<>();

        // Add direct room bookings (for single rooms)
        allBookings.addAll(room.getBookings());

        // Add bookings from all beds (for multi-bed rooms)
        for (GanttBedData bed : room.getBeds()) {
            allBookings.addAll(bed.getBookings());
        }

        return allBookings;
    }

    /**
     * Checks if a booking is active on a given date, considering date segments for gap bookings.
     * For bookings with gaps, checks if date falls within any of the date segments.
     */
    private boolean isBookingActiveOnDate(GanttBookingData booking, LocalDate date) {
        List<DateSegment> segments = booking.getDateSegments();

        if (segments == null || segments.isEmpty()) {
            // Fallback to simple date range check
            LocalDate start = booking.getStartDate();
            LocalDate end = booking.getEndDate();
            if (start == null || end == null) return false;
            return !date.isBefore(start) && date.isBefore(end);
        }

        // Check if date falls within any segment (endDate is inclusive for gap segments)
        for (DateSegment segment : segments) {
            if (!date.isBefore(segment.startDate()) && !date.isAfter(segment.endDate())) {
                return true;
            }
        }
        return false;
    }
}
