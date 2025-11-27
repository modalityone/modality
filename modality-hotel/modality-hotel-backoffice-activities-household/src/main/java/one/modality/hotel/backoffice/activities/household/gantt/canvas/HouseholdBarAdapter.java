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
 * - Conve.printStackTrace();erts GanttBookingData directly into LocalDateBar objects for Canvas rendering
 * - Maintains proper parent/child relationships for tetris packing layout
 *
 * @author Claude Code Assistant
 */
public record HouseholdBarAdapter(GanttPresenter presenter) {

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
                    parentRow.room().getCapacity()
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
        List<AbstractMap.SimpleEntry<LocalDate, Integer>> events = new ArrayList<>();
        for (GanttBookingData booking : allBookings) {
            if (booking.getStartDate() == null || booking.getEndDate() == null) continue;
            events.add(new AbstractMap.SimpleEntry<>(booking.getStartDate(), 1));   // Check-in
            events.add(new AbstractMap.SimpleEntry<>(booking.getEndDate(), -1));    // Check-out
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
                    // Exclusive end (currentDate is the first day with 0 occupancy)
                    LocalDate segmentEnd = currentDate;
                    // Determine status based on segment bookings - OCCUPIED only if at least one bed is occupied
                    BookingStatus segmentStatus = determineSegmentStatus(segmentStart, segmentEnd.minusDays(1), today, segmentBookings);

                    HouseholdBookingBlock block = new HouseholdBookingBlock(
                            "", // Guest name not needed for aggregate bars
                            segmentStatus,
                            BookingPosition.MIDDLE,
                            false,
                            false,
                            false,
                            false,
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

            HouseholdBookingBlock block = new HouseholdBookingBlock(
                    "", // Guest name not needed for aggregate bars
                    segmentStatus,
                    BookingPosition.MIDDLE,
                    false,
                    false,
                    false,
                    false,
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

            if (isExpanded) {
                // EXPANDED multi-bed room: Create room parent WITH aggregated bar + bed parents (with individual bars)
                GanttParentRow roomParent = GanttParentRow.forRoom(room, true);
                allParentRows.add(roomParent);

                // Add aggregated bar on room row as summary
                List<LocalDateBar<HouseholdBookingBlock>> roomBars = createAggregatedBars(room, roomParent);
                allBars.addAll(roomBars);

                // Add bed parents with their booking bars (below the room)
                int bedIndex = 0;
                for (GanttBedData bed : room.getBeds()) {
                    GanttParentRow bedParent = GanttParentRow.forBed(room, bed);
                    allParentRows.add(bedParent);

                    List<LocalDateBar<HouseholdBookingBlock>> bedBars = adaptBookingsToBars(
                            bed.getBookings(), bedParent);
                    allBars.addAll(bedBars);

                    bedIndex++;
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

                List<LocalDateBar<HouseholdBookingBlock>> roomBars = adaptBookingsToBars(
                        room.getBookings(), roomParent);
                allBars.addAll(roomBars);
            }
        }

        return new AdaptedRoomData(allParentRows, allBars);
    }
}
