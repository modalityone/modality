package one.modality.hotel.backoffice.activities.household.gantt.renderer;

import javafx.scene.paint.Color;
import one.modality.hotel.backoffice.activities.household.gantt.model.BookingStatus;
import one.modality.hotel.backoffice.activities.household.gantt.model.RoomStatus;

/**
 * Centralized color scheme for the Gantt view.
 * All colors are defined according to the design specification.
 *
 * @author Claude Code Assistant
 */
public class GanttColorScheme {

    // === BOOKING BAR COLORS (named by booking status) ===
    public static final Color COLOR_BOOKING_CONFIRMED = Color.web("#0096D6");  // Blue - future booking
    public static final Color COLOR_BOOKING_OCCUPIED = Color.web("#DB2777");   // Hot Berry - guest in room
    public static final Color COLOR_BOOKING_DEPARTED = Color.web("#D9D9D9");   // Grey - past booking

    // === ROOM STATUS COLORS (for status dots in room headers) ===
    // Red (to clean - urgent) → Orange (to inspect - warning) → Green (ready) → Pink (occupied)
    public static final Color COLOR_ROOM_TO_CLEAN = Color.web("#E53935");      // Red - urgent action needed
    public static final Color COLOR_ROOM_TO_INSPECT = Color.web("#FF9800");    // Warning orange - needs inspection
    public static final Color COLOR_ROOM_READY = Color.web("#43A047");         // Green - success, ready
    public static final Color COLOR_ROOM_OCCUPIED = Color.web("#DB2777");      // Hot Berry - room in use, can't act

    // === BACKGROUND COLORS ===
    public static final Color COLOR_BG_HEADER = Color.web("#F5F5F5");
    public static final Color COLOR_BG_TODAY = Color.web("#FFF9E6");           // Light yellow/cream - distinct from weekend blue

    // === TEXT AND BORDER COLORS ===
    public static final Color COLOR_TEXT_GREY = Color.web("#333333");
    public static final Color COLOR_BORDER = Color.web("#CCCCCC");

    // === UI ACCENT COLORS ===
    public static final Color COLOR_ACCENT = Color.web("#0096D6");             // Blue - for headers, titles, accents
    public static final Color COLOR_CONFLICT = Color.web("#E53935");           // Red - for conflict highlighting

    /**
     * Gets the color for a booking status (gantt bars)
     */
    public Color getBookingStatusColor(BookingStatus status) {
        return switch (status) {
            case OCCUPIED -> COLOR_BOOKING_OCCUPIED;
            case DEPARTED -> COLOR_BOOKING_DEPARTED;
            default -> COLOR_BOOKING_CONFIRMED;
        };
    }

    /**
     * Gets the color for a room status
     */
    public Color getRoomStatusColor(RoomStatus status) {
        return switch (status) {
            case OCCUPIED -> COLOR_ROOM_OCCUPIED;
            case TO_CLEAN -> COLOR_ROOM_TO_CLEAN;
            case TO_INSPECT -> COLOR_ROOM_TO_INSPECT;
            case READY -> COLOR_ROOM_READY;
            default -> Color.GRAY;
        };
    }
}
