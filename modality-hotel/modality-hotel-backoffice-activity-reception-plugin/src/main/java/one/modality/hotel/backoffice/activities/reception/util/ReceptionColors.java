package one.modality.hotel.backoffice.activities.reception.util;

import javafx.scene.paint.Color;

/**
 * Color palette for the Reception Dashboard.
 * Based on Bootstrap color scheme for a calm, professional Buddhist center environment.
 *
 * Design Principles:
 * - Calm & Gentle: Soft colors, subtle shading
 * - Professional: Clean, consistent hierarchy
 * - Accessible: WCAG AA compliant contrast ratios
 *
 * @author David Hello
 * @author Claude Code
 */
public final class ReceptionColors {

    private ReceptionColors() {} // Utility class

    // ==========================================
    // Primary Colors (Bootstrap Blue)
    // ==========================================
    public static final Color PRIMARY = Color.web("#0d6efd");
    public static final Color PRIMARY_HOVER = Color.web("#0b5ed7");
    public static final Color PRIMARY_LIGHT = Color.web("#e7f1ff");
    public static final Color PRIMARY_BORDER = Color.web("#b6d4fe");
    public static final Color PRIMARY_TEXT = Color.web("#084298");

    // ==========================================
    // Success Colors (Bootstrap Green)
    // ==========================================
    public static final Color SUCCESS = Color.web("#198754");
    public static final Color SUCCESS_HOVER = Color.web("#157347");
    public static final Color SUCCESS_LIGHT = Color.web("#e8f5e9");
    public static final Color SUCCESS_BORDER = Color.web("#c3e6cb");
    public static final Color SUCCESS_TEXT = Color.web("#0f5132");

    // ==========================================
    // Warning Colors (Bootstrap Orange)
    // ==========================================
    public static final Color WARNING = Color.web("#fd7e14");
    public static final Color WARNING_HOVER = Color.web("#e96b02");
    public static final Color WARNING_LIGHT = Color.web("#fff3cd");
    public static final Color WARNING_BORDER = Color.web("#ffecb5");
    public static final Color WARNING_TEXT = Color.web("#5c4503"); // Darkened for WCAG AA

    // ==========================================
    // Danger Colors (Bootstrap Red)
    // ==========================================
    public static final Color DANGER = Color.web("#dc3545");
    public static final Color DANGER_HOVER = Color.web("#bb2d3b");
    public static final Color DANGER_LIGHT = Color.web("#f8d7da");
    public static final Color DANGER_BORDER = Color.web("#f5c2c7");
    public static final Color DANGER_TEXT = Color.web("#842029");

    // ==========================================
    // Purple Colors (Special states)
    // ==========================================
    public static final Color PURPLE = Color.web("#6f42c1");
    public static final Color PURPLE_HOVER = Color.web("#5a32a3");
    public static final Color PURPLE_LIGHT = Color.web("#f3e8ff");
    public static final Color PURPLE_BORDER = Color.web("#e2d9f3");
    public static final Color PURPLE_TEXT = Color.web("#59359a");

    // ==========================================
    // Info Colors (Bootstrap Cyan)
    // ==========================================
    public static final Color INFO = Color.web("#0dcaf0");
    public static final Color INFO_HOVER = Color.web("#0ab4d6");
    public static final Color INFO_LIGHT = Color.web("#cff4fc");
    public static final Color INFO_BORDER = Color.web("#9eeaf9");
    public static final Color INFO_TEXT = Color.web("#055160");

    // ==========================================
    // Teal Colors (Extended)
    // ==========================================
    public static final Color TEAL = Color.web("#20c997");
    public static final Color TEAL_HOVER = Color.web("#1aa179");
    public static final Color TEAL_LIGHT = Color.web("#d2f4ea");
    public static final Color TEAL_BORDER = Color.web("#b8efe0");
    public static final Color TEAL_TEXT = Color.web("#0d6854");

    // ==========================================
    // Text Colors (Neutral hierarchy)
    // ==========================================
    public static final Color TEXT_PRIMARY = Color.web("#212529");
    public static final Color TEXT_SECONDARY = Color.web("#495057");
    public static final Color TEXT_MUTED = Color.web("#5c636a"); // Darkened for better contrast
    public static final Color TEXT_LIGHT = Color.web("#8c959d");

    // ==========================================
    // Border Colors
    // ==========================================
    public static final Color BORDER_DEFAULT = Color.web("#dee2e6");
    public static final Color BORDER_LIGHT = Color.web("#e9ecef");

    // ==========================================
    // Background Colors
    // ==========================================
    public static final Color BG_WHITE = Color.web("#ffffff");
    public static final Color BG_LIGHT = Color.web("#f8f9fa");
    public static final Color BG_LIGHTER = Color.web("#e9ecef");

    // ==========================================
    // Hex String Constants (for CSS or string-based usage)
    // ==========================================
    public static final String PRIMARY_HEX = "#0d6efd";
    public static final String SUCCESS_HEX = "#198754";
    public static final String WARNING_HEX = "#fd7e14";
    public static final String DANGER_HEX = "#dc3545";
    public static final String PURPLE_HEX = "#6f42c1";
    public static final String INFO_HEX = "#0dcaf0";
    public static final String TEAL_HEX = "#20c997";

    public static final String TEXT_PRIMARY_HEX = "#212529";
    public static final String TEXT_SECONDARY_HEX = "#495057";
    public static final String TEXT_MUTED_HEX = "#5c636a";

    public static final String BORDER_DEFAULT_HEX = "#dee2e6";
    public static final String BG_WHITE_HEX = "#ffffff";
    public static final String BG_LIGHT_HEX = "#f8f9fa";

    // ==========================================
    // Status-specific colors (mapped to guest statuses)
    // ==========================================
    /** Expected guests - Blue (Primary) */
    public static final Color STATUS_EXPECTED = PRIMARY;
    public static final Color STATUS_EXPECTED_LIGHT = PRIMARY_LIGHT;

    /** Checked-in guests - Green (Success) */
    public static final Color STATUS_CHECKED_IN = SUCCESS;
    public static final Color STATUS_CHECKED_IN_LIGHT = SUCCESS_LIGHT;

    /** Checked-out guests - Purple */
    public static final Color STATUS_CHECKED_OUT = PURPLE;
    public static final Color STATUS_CHECKED_OUT_LIGHT = PURPLE_LIGHT;

    /** No-show guests - Red (Danger) */
    public static final Color STATUS_NO_SHOW = DANGER;
    public static final Color STATUS_NO_SHOW_LIGHT = DANGER_LIGHT;

    /** Pre-booked guests - Orange (Warning) */
    public static final Color STATUS_PRE_BOOKED = WARNING;
    public static final Color STATUS_PRE_BOOKED_LIGHT = WARNING_LIGHT;

    /** Cancelled bookings - Gray */
    public static final Color STATUS_CANCELLED = TEXT_MUTED;
    public static final Color STATUS_CANCELLED_LIGHT = BG_LIGHTER;

    // ==========================================
    // Utility methods
    // ==========================================

    /**
     * Converts a JavaFX Color to CSS hex string format.
     */
    public static String toHex(Color color) {
        int r = (int) (color.getRed() * 255);
        int g = (int) (color.getGreen() * 255);
        int b = (int) (color.getBlue() * 255);
        return "#" + toHexDigits(r) + toHexDigits(g) + toHexDigits(b);
    }

    /**
     * Converts an integer (0-255) to a two-digit hex string.
     */
    private static String toHexDigits(int value) {
        String hex = Integer.toHexString(value);
        return hex.length() == 1 ? "0" + hex : hex;
    }

    /**
     * Creates a color with reduced opacity.
     */
    public static Color withOpacity(Color color, double opacity) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), opacity);
    }
}
