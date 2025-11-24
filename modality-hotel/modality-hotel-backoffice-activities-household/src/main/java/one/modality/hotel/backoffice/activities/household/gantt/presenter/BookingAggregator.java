package one.modality.hotel.backoffice.activities.household.gantt.presenter;

import one.modality.hotel.backoffice.activities.household.gantt.model.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles aggregation logic for multi-bed room bookings.
 * Aggregates multiple bookings on different beds into a single display representation.
 *
 * @author Claude Code Assistant
 */
public class BookingAggregator {

    /**
     * Aggregates bookings from multiple beds for a given date.
     * This is used for MULTI-BED ROOMS (dormitories, etc.) to display a single aggregated booking bar
     * on the collapsed room row that summarizes all bookings across all beds.
     *
     * AGGREGATION LOGIC:
     * - Spans from earliest start date to latest end date across all active bookings
     * - Status uses highest priority (OCCUPIED > CONFIRMED > UNCONFIRMED > DEPARTED)
     * - Occupancy shows count of active bookings
     * - Capacity uses database max field (excludes overbooking beds)
     *
     * COMMENT ICON RULE:
     * - Icon appears on a date ONLY if at least one booking on that date is:
     *   1. In MIDDLE position (not arrival/departure day)
     *   2. Has special needs (non-empty list)
     * - This matches the date where the icon appears on individual bed rows
     *
     * @param room The multi-bed room to aggregate bookings for
     * @param date The current date to check for active bookings
     * @return BookingBar with aggregated data, or null if no bookings are active on this date
     */
    public BookingBar aggregateMultiBedBookings(GanttRoomData room, LocalDate date) {
        if (room.getBeds().isEmpty()) {
            return null;
        }

        // Collect all active bookings across all beds on this specific date
        // A booking is "active" if the date falls within its start-end range (inclusive)
        List<? extends GanttBookingData> allActiveBookings = room.getBeds().stream()
                .flatMap(bed -> bed.getBookings().stream())
                .filter(b -> !date.isBefore(b.getStartDate()) && !date.isAfter(b.getEndDate()))
                .collect(Collectors.toList());

        if (allActiveBookings.isEmpty()) {
            return null;
        }

        // Find earliest start and latest end across all active bookings (aggregation logic)
        // This creates a "super booking" that spans all individual bookings
        LocalDate earliestStart = allActiveBookings.stream()
                .map(GanttBookingData::getStartDate)
                .min(LocalDate::compareTo)
                .orElse(date);

        LocalDate latestEnd = allActiveBookings.stream()
                .map(GanttBookingData::getEndDate)
                .max(LocalDate::compareTo)
                .orElse(date);

        // Determine booking position based on aggregated dates
        // This controls visual styling (arrival/middle/departure/single)
        BookingPosition position = determinePosition(earliestStart, latestEnd, date);

        // Determine status using highest priority rule
        // Priority: OCCUPIED > CONFIRMED > UNCONFIRMED > DEPARTED
        BookingStatus aggregatedStatus = getAggregatedStatus(allActiveBookings);

        // Calculate occupancy and capacity
        // Occupancy = number of active bookings on this date
        // Capacity = actual room capacity from database (rc.max field, excludes overbooking beds)
        int occupancy = allActiveBookings.size();
        int totalBeds = room.getCapacity(); // Use actual room capacity from database (excludes overbooking beds)

        // Build guest info string for tooltip (comma-separated list of guest names)
        String guestInfo = buildGuestInfo(allActiveBookings);

        // COMMENT ICON LOGIC (matches individual bed row behavior):
        // The icon should appear ONLY on dates where at least one booking meets BOTH conditions:
        // 1. The booking is in MIDDLE position on THIS specific date (not arrival/departure day)
        // 2. The booking has special needs (non-empty list)
        // This ensures the icon appears on the same dates for both collapsed and expanded views
        boolean hasComments = allActiveBookings.stream()
                .anyMatch(b -> {
                    BookingPosition bookingPosition = determinePosition(b.getStartDate(), b.getEndDate(), date);
                    return bookingPosition == BookingPosition.MIDDLE
                        && (b.getSpecialNeeds() != null && !b.getSpecialNeeds().isEmpty());
                });

        // Check for turnover across any beds on this date
        // Turnover = a bed has both a departure and arrival on the same date (requires cleaning)
        boolean hasTurnover = room.getBeds().stream()
                .anyMatch(bed -> hasBedTurnover(bed, date));

        // Use first booking as representative for tooltip data (shows aggregated info in multi-bed)
        GanttBookingData representativeBooking = allActiveBookings.isEmpty() ? null : allActiveBookings.get(0);

        return new BookingBar(aggregatedStatus, position, occupancy, totalBeds, false, guestInfo, hasComments, hasTurnover, representativeBooking);
    }

    /**
     * Creates a booking bar for a SINGLE ROOM (non-aggregated).
     * This is used for both:
     * 1. True single rooms (RoomType.SINGLE) without overbooking
     * 2. Individual bed rows within multi-bed rooms (when expanded)
     * 3. Individual bed rows within single rooms with overbooking (when expanded)
     *
     * COMMENT ICON RULE:
     * - Icon appears ONLY when BOTH conditions are met:
     *   1. Position is MIDDLE (not arrival/departure day)
     *   2. Special needs list is non-empty
     *
     * LATE ARRIVAL DETECTION:
     * - Red person icon appears when ALL conditions are met:
     *   1. Start date is today or in the past
     *   2. Guest has NOT been marked as arrived (isArrived = false)
     *   3. Booking status is NOT departed
     *
     * @param booking The booking data for this specific booking
     * @param date The current date being rendered
     * @return BookingBar with single booking data (occupancy=1, capacity=1)
     */
    public BookingBar createSingleRoomBookingBar(GanttBookingData booking, LocalDate date) {
        // Determine position for visual styling (ARRIVAL/MIDDLE/DEPARTURE/SINGLE)
        BookingPosition position = determinePosition(booking.getStartDate(), booking.getEndDate(), date);

        String guestInfo = booking.getGuestName() != null ? booking.getGuestName() : "Guest";

        // COMMENT ICON LOGIC:
        // Show icon ONLY if:
        // 1. The booking has special needs (non-empty list)
        // 2. AND the current date is in MIDDLE position (not arrival or departure day)
        // This prevents the icon from appearing on arrival/departure days
        boolean hasComments = (booking.getSpecialNeeds() != null && !booking.getSpecialNeeds().isEmpty())
                           && position == BookingPosition.MIDDLE;

        // LATE ARRIVAL DETECTION:
        // Guest is considered "late" if ALL these conditions are true:
        // 1. Start date exists and is today or in the past (guest should have arrived by now)
        // 2. Guest has NOT been marked as arrived (isArrived = false)
        // 3. Booking is NOT in departed status (prevents showing late icon for past bookings)
        // When true, the person icon will be rendered in red instead of blue
        LocalDate today = LocalDate.now();
        boolean hasLateArrival = booking.getStartDate() != null
            && (booking.getStartDate().isBefore(today) || booking.getStartDate().isEqual(today))
            && !booking.isArrived()
            && booking.getStatus() != BookingStatus.DEPARTED;

        // Return booking bar with single occupancy (1/1)
        // hasConflict=false (conflicts are detected at room level, not booking level)
        // hasTurnover=false (turnover is detected at bed level, not booking level)
        return new BookingBar(booking.getStatus(), position, 1, 1, false, guestInfo, hasComments, false, hasLateArrival, booking);
    }

    /**
     * Determines the booking position for visual styling.
     *
     * POSITION TYPES:
     * - SINGLE: One-day booking (startDate = endDate)
     *   Visual: Rounded on both sides
     * - ARRIVAL: First day of multi-day booking (currentDate = startDate)
     *   Visual: Rounded on left, straight on right, person icon appears
     * - DEPARTURE: Last day of multi-day booking (currentDate = endDate)
     *   Visual: Straight on left, rounded on right
     * - MIDDLE: Any day between arrival and departure
     *   Visual: Straight on both sides, comment icon appears here (if special needs exist)
     *
     * @param startDate The booking start date
     * @param endDate The booking end date
     * @param currentDate The date being rendered (to determine which position)
     * @return BookingPosition enum value for styling
     */
    public BookingPosition determinePosition(LocalDate startDate, LocalDate endDate, LocalDate currentDate) {
        if (startDate.equals(endDate)) {
            return BookingPosition.SINGLE;
        } else if (currentDate.equals(startDate)) {
            return BookingPosition.ARRIVAL;
        } else if (currentDate.equals(endDate)) {
            return BookingPosition.DEPARTURE;
        } else {
            return BookingPosition.MIDDLE;
        }
    }

    /**
     * Gets the aggregated status from multiple bookings using highest priority rule.
     *
     * PRIORITY ORDER (highest to lowest):
     * 1. OCCUPIED - At least one guest has checked in (isArrived = true)
     * 2. CONFIRMED - At least one booking is confirmed
     * 3. UNCONFIRMED - At least one booking is unconfirmed
     * 4. DEPARTED - All guests have departed (endDate <= today)
     *
     * This ensures the aggregated bar shows the most important status.
     * Example: If 2 guests are confirmed and 1 is occupied, the bar shows OCCUPIED (green).
     *
     * @param bookings List of active bookings to aggregate
     * @return The highest priority status among all bookings
     */
    private BookingStatus getAggregatedStatus(List<? extends GanttBookingData> bookings) {
        if (bookings.stream().anyMatch(b -> b.getStatus() == BookingStatus.OCCUPIED)) {
            return BookingStatus.OCCUPIED;
        } else if (bookings.stream().anyMatch(b -> b.getStatus() == BookingStatus.CONFIRMED)) {
            return BookingStatus.CONFIRMED;
        } else if (bookings.stream().anyMatch(b -> b.getStatus() == BookingStatus.UNCONFIRMED)) {
            return BookingStatus.UNCONFIRMED;
        } else {
            return BookingStatus.DEPARTED;
        }
    }

    /**
     * Builds a comma-separated string of guest names for tooltips.
     *
     * Example: "John Smith, Jane Doe, Bob Johnson"
     * Falls back to "Guest" for bookings without a guest name.
     *
     * @param bookings List of bookings to extract guest names from
     * @return Comma-separated guest names for tooltip display
     */
    private String buildGuestInfo(List<? extends GanttBookingData> bookings) {
        return bookings.stream()
                .map(b -> b.getGuestName() != null ? b.getGuestName() : "Guest")
                .collect(Collectors.joining(", "));
    }

    /**
     * Checks if a bed has a turnover on a specific date.
     *
     * TURNOVER = A bed has BOTH a departure AND an arrival on the same date.
     * This means the bed needs cleaning between guests (same-day checkout/checkin).
     *
     * Visual indicator: Turnover icon appears on the booking bar for this date.
     *
     * @param bed The bed to check for turnover
     * @param date The date to check for turnover
     * @return true if the bed has both a departure and arrival on this date
     */
    private boolean hasBedTurnover(one.modality.hotel.backoffice.activities.household.gantt.model.GanttBedData bed, LocalDate date) {
        // Find all bookings departing on this date (endDate = date)
        List<? extends GanttBookingData> departures = bed.getBookings().stream()
                .filter(b -> b.getEndDate().equals(date))
                .collect(Collectors.toList());

        // Find all bookings arriving on this date (startDate = date)
        List<? extends GanttBookingData> arrivals = bed.getBookings().stream()
                .filter(b -> b.getStartDate().equals(date))
                .collect(Collectors.toList());

        // Turnover exists if there's at least one departure AND one arrival
        return !departures.isEmpty() && !arrivals.isEmpty();
    }
}
