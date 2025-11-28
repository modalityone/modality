package one.modality.hotel.backoffice.activities.household.gantt.presenter;

import one.modality.hotel.backoffice.activities.household.gantt.model.GanttBookingData;
import one.modality.hotel.backoffice.activities.household.gantt.model.GanttRoomData;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Detects booking conflicts (overlapping bookings in single rooms).
 * Also identifies turnovers (same-day departure and arrival).
 *
 * @author Claude Code Assistant
 */
public class ConflictDetector {

    /**
     * Detects if there's a conflict on a specific date for a single room.
     * A conflict occurs when multiple bookings overlap on the same date.
     * NOTE: A turnover (one guest departs, another arrives) is NOT a conflict.
     */
    public boolean hasConflict(GanttRoomData room, LocalDate date) {
        List<? extends GanttBookingData> activeBookings = getActiveBookings(room.getBookings(), date);

        // If 2 or fewer bookings, check if it's a turnover (not a conflict)
        if (activeBookings.size() <= 1) {
            return false;
        }

        // If it's a turnover (one departs, one arrives on same day), it's NOT a conflict
        if (hasTurnover(room, date)) {
            return false;
        }

        // Otherwise, multiple overlapping bookings = conflict
        return true;
    }

    /**
     * Detects if there's a turnover on a specific date.
     * A turnover occurs when one guest departs and another arrives on the same day.
     */
    public boolean hasTurnover(GanttRoomData room, LocalDate date) {
        List<? extends GanttBookingData> departures = room.getBookings().stream()
                .filter(b -> b.getEndDate().equals(date))
                .collect(Collectors.toList());

        List<? extends GanttBookingData> arrivals = room.getBookings().stream()
                .filter(b -> b.getStartDate().equals(date))
                .collect(Collectors.toList());

        return !departures.isEmpty() && !arrivals.isEmpty();
    }

    /**
     * Gets all bookings active on a specific date
     */
    private List<? extends GanttBookingData> getActiveBookings(List<? extends GanttBookingData> bookings, LocalDate date) {
        return bookings.stream()
                .filter(b -> !date.isBefore(b.getStartDate()) && !date.isAfter(b.getEndDate()))
                .collect(Collectors.toList());
    }

}
