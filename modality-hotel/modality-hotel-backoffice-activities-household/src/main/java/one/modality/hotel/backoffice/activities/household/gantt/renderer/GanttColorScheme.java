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

    // === DESIGN COLORS ===
    public static final Color COLOR_BLUE = Color.web("#0096D6");
    public static final Color COLOR_RED = Color.web("#FF0404");
    public static final Color COLOR_GREY = Color.web("#D9D9D9");
    public static final Color COLOR_BG_HEADER = Color.web("#F5F5F5");
    public static final Color COLOR_BG_TODAY = Color.web("#FFF9E6"); // Light yellow/cream - distinct from weekend blue
    public static final Color COLOR_TEXT_GREY = Color.web("#333333");
    public static final Color COLOR_BORDER = Color.web("#CCCCCC");

    // Room status colors
    public static final Color COLOR_STATUS_OCCUPIED = Color.web("#FF0404");
    public static final Color COLOR_STATUS_TO_CLEAN = Color.web("#FFA500");
    public static final Color COLOR_STATUS_TO_INSPECT = Color.web("#FFD700");
    public static final Color COLOR_STATUS_READY = Color.web("#41BA4D");

    /**
     * Gets the color for a booking status
     */
    public Color getBookingStatusColor(BookingStatus status) {
        switch (status) {
            case OCCUPIED:
                return COLOR_RED;
            case DEPARTED:
                return COLOR_GREY;
            case CONFIRMED:
            case UNCONFIRMED:
            default:
                return COLOR_BLUE;
        }
    }

    /**
     * Gets the color for a room status
     */
    public Color getRoomStatusColor(RoomStatus status) {
        switch (status) {
            case OCCUPIED:
                return COLOR_STATUS_OCCUPIED;
            case TO_CLEAN:
                return COLOR_STATUS_TO_CLEAN;
            case TO_INSPECT:
                return COLOR_STATUS_TO_INSPECT;
            case READY:
                return COLOR_STATUS_READY;
            default:
                return Color.GRAY;
        }
    }
}
