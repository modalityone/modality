package one.modality.hotel.backoffice.activities.household.gantt.canvas;

import javafx.scene.paint.Color;

/**
 * SVG icon constants for Canvas-based Gantt rendering.
 * These icons are rendered directly in Canvas using BarDrawer.setIcon().
 *
 * @author Claude Code Assistant
 */
public class HouseholdGanttIcons {

    // Person icon SVG path (used for guest occupancy indicators)
    public static final String PERSON_ICON_SVG_PATH =
        "M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z";
    public static final Color PERSON_ICON_SVG_FILL = Color.WHITE;
    public static final double PERSON_ICON_SVG_WIDTH = 24;
    public static final double PERSON_ICON_SVG_HEIGHT = 24;

    // Person icon in red (for late arrivals)
    public static final Color PERSON_ICON_RED_FILL = Color.web("#FF0404");

    // Message/comment icon SVG path
    public static final String MESSAGE_ICON_SVG_PATH =
        "M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H6l-2 2V4h16v12z";
    public static final Color MESSAGE_ICON_SVG_FILL = Color.WHITE;
    public static final double MESSAGE_ICON_SVG_WIDTH = 24;
    public static final double MESSAGE_ICON_SVG_HEIGHT = 24;

    // Warning triangle icon (for turnover indicators)
    public static final String WARNING_ICON_SVG_PATH =
        "M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z";
    public static final Color WARNING_ICON_SVG_FILL = Color.WHITE;
    public static final double WARNING_ICON_SVG_WIDTH = 24;
    public static final double WARNING_ICON_SVG_HEIGHT = 24;
}
