package one.modality.booking.backoffice.activities.registration;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Centralized style constants and helper methods for the Registration Dashboard.
 * Based on the RegistrationDashboardFull.jsx mockup color system.
 * <p>
 * This class provides:
 * - Color constants matching the JSX design
 * - Helper methods to apply consistent styles across components
 * - Reusable style patterns for cards, badges, buttons, etc.
 *
 * @author Claude Code
 */
public final class RegistrationStyles {

    private RegistrationStyles() {} // Utility class

    // ═══════════════════════════════════════════════════════════════════════════════
    // COLOR PALETTE - From JSX colors object
    // ═══════════════════════════════════════════════════════════════════════════════

    // Primary
    public static final Color PRIMARY = Color.web("#0d6efd");
    public static final Color PRIMARY_LIGHT = Color.web("#e7f1ff");
    public static final Color PRIMARY_BORDER = Color.web("#b6d4fe");
    public static final Color PRIMARY_TEXT = Color.web("#084298");

    // Success
    public static final Color SUCCESS = Color.web("#198754");
    public static final Color SUCCESS_LIGHT = Color.web("#e8f5e9");
    public static final Color SUCCESS_BORDER = Color.web("#c3e6cb");
    public static final Color SUCCESS_TEXT = Color.web("#0f5132");

    // Warning
    public static final Color WARNING = Color.web("#fd7e14");
    public static final Color WARNING_LIGHT = Color.web("#fff3cd");
    public static final Color WARNING_BG = Color.web("#fef3c7");
    public static final Color WARNING_BORDER = Color.web("#ffecb5");
    public static final Color WARNING_TEXT = Color.web("#856404");

    // Danger
    public static final Color DANGER = Color.web("#dc3545");
    public static final Color DANGER_LIGHT = Color.web("#f8d7da");
    public static final Color DANGER_BORDER = Color.web("#f5c2c7");
    public static final Color DANGER_TEXT = Color.web("#842029");

    // Teal
    public static final Color TEAL = Color.web("#20c997");
    public static final Color TEAL_LIGHT = Color.web("#d2f4ea");

    // Purple
    public static final Color PURPLE = Color.web("#6f42c1");
    public static final Color PURPLE_LIGHT = Color.web("#f3e8ff");
    public static final Color PURPLE_BORDER = Color.web("#e2d9f3");

    // Warm colors (for modal)
    public static final Color WARM = Color.web("#92400e");
    public static final Color WARM_LIGHT = Color.web("#fef3c7");
    public static final Color WARM_BORDER = Color.web("#e8dcc8");
    public static final Color WARM_TEXT = Color.web("#5c4033");
    public static final Color WARM_BROWN = Color.web("#7c5a3c");
    public static final Color WARM_BROWN_LIGHT = Color.web("#f7f3ee");
    public static final Color WARM_ORANGE = Color.web("#c76f2d");
    public static final Color WARM_ORANGE_LIGHT = Color.web("#fef4eb");
    public static final Color WARM_ROSE = Color.web("#be185d");
    public static final Color WARM_ROSE_LIGHT = Color.web("#fce7f3");

    // Neutral tones
    public static final Color CREAM = Color.web("#fdfbf7");
    public static final Color CREAM_BORDER = Color.web("#f5e6d3");
    public static final Color SAND = Color.web("#f5f1ea");
    public static final Color WARM_WHITE = Color.web("#fefdfb");

    // Semantic backgrounds (for badges/status)
    public static final Color SUCCESS_BG = Color.web("#dcfce7");

    // Text colors
    public static final Color TEXT = Color.web("#3d3530");
    public static final Color TEXT_SECONDARY = Color.web("#5c5550");
    public static final Color TEXT_MUTED = Color.web("#8a857f");
    public static final Color TEXT_LIGHT = Color.web("#b5b0aa");

    // Borders and backgrounds
    public static final Color BORDER = Color.web("#e0dbd4");
    public static final Color BORDER_LIGHT = Color.web("#ebe7e1");
    public static final Color BG = Color.web("#faf9f5");
    public static final Color BG_CARD = Color.WHITE;

    // Modal specific
    public static final Color MODAL_BG = Color.web("#faf9f5");
    public static final Color MODAL_CARD = Color.web("#fefdfb");
    public static final Color MODAL_BORDER = Color.web("#e8e4dd");

    // ═══════════════════════════════════════════════════════════════════════════════
    // UX PALETTE - From JSX ux object (TimelineOptionsEditor)
    // ═══════════════════════════════════════════════════════════════════════════════

    // Category colors
    public static final Color ACCOMMODATION_BG = Color.web("#fef3c7");
    public static final Color ACCOMMODATION_TEXT = Color.web("#92400e");
    public static final Color ACCOMMODATION_ICON = Color.web("#d97706");
    public static final Color ACCOMMODATION_FG = Color.web("#d97706");

    public static final Color MEALS_BG = Color.web("#dbeafe");
    public static final Color MEALS_TEXT = Color.web("#1e40af");
    public static final Color MEALS_ICON = Color.web("#3b82f6");
    public static final Color MEALS_FG = Color.web("#3b82f6");

    public static final Color DIET_BG = Color.web("#dcfce7");
    public static final Color DIET_TEXT = Color.web("#166534");
    public static final Color DIET_ICON = Color.web("#22c55e");
    public static final Color DIET_FG = Color.web("#22c55e");

    public static final Color PROGRAM_BG = Color.web("#fce7f3");
    public static final Color PROGRAM_TEXT = Color.web("#9d174d");
    public static final Color PROGRAM_ICON = Color.web("#ec4899");
    public static final Color PROGRAM_FG = Color.web("#ec4899");

    public static final Color TRANSPORT_BG = Color.web("#d1fae5");
    public static final Color TRANSPORT_TEXT = Color.web("#065f46");
    public static final Color TRANSPORT_ICON = Color.web("#10b981");
    public static final Color TRANSPORT_FG = Color.web("#10b981");

    public static final Color TEACHING_BG = Color.web("#fce7f3");
    public static final Color TEACHING_TEXT = Color.web("#9d174d");
    public static final Color TEACHING_FG = Color.web("#ec4899");

    public static final Color COURSE_BG = Color.web("#f3e8ff");
    public static final Color COURSE_TEXT = Color.web("#6b21a8");
    public static final Color COURSE_ICON = Color.web("#8b5cf6");

    public static final Color SERVICES_BG = Color.web("#e0e7ff");
    public static final Color SERVICES_TEXT = Color.web("#3730a3");
    public static final Color SERVICES_ICON = Color.web("#6366f1");

    // Parking category
    public static final Color PARKING_BG = Color.web("#f3e8ff");
    public static final Color PARKING_TEXT = Color.web("#7c3aed");
    public static final Color PARKING_ICON = Color.web("#8b5cf6");
    public static final Color PARKING_FG = Color.web("#8b5cf6");

    // Tax category (financial adjustments like rounding)
    public static final Color TAX_BG = Color.web("#fef2f2");
    public static final Color TAX_TEXT = Color.web("#991b1b");
    public static final Color TAX_ICON = Color.web("#dc2626");
    public static final Color TAX_FG = Color.web("#dc2626");

    // Recording category (audio recordings)
    public static final Color RECORDING_BG = Color.web("#eef2ff");
    public static final Color RECORDING_TEXT = Color.web("#4338ca");
    public static final Color RECORDING_ICON = Color.web("#6366f1");
    public static final Color RECORDING_FG = Color.web("#6366f1");

    // Other/Unknown category (items not matching known families)
    public static final Color OTHER_BG = Color.web("#f3f4f6");
    public static final Color OTHER_TEXT = Color.web("#4b5563");
    public static final Color OTHER_ICON = Color.web("#6b7280");
    public static final Color OTHER_FG = Color.web("#6b7280");

    // Timeline Period Bars
    public static final Color EVENT_PERIOD_BG = Color.web("#c4b5fd");    // violetLight
    public static final Color EVENT_PERIOD_TEXT = Color.web("#6d28d9");  // violet
    public static final Color BOOKING_PERIOD_BG = Color.web("#86efac");  // successLight
    public static final Color BOOKING_PERIOD_TEXT = Color.web("#16a34a"); // green

    // Date Stepper Colors
    public static final Color ARRIVAL_BG = Color.web("#dcfce7");         // greenLight
    public static final Color ARRIVAL_TEXT = Color.web("#16a34a");       // green
    public static final Color ARRIVAL_BORDER = Color.web("#bbf7d0");     // greenBorder
    public static final Color DEPARTURE_BG = Color.web("#fee2e2");       // redLight
    public static final Color DEPARTURE_TEXT = Color.web("#dc2626");     // red
    public static final Color DEPARTURE_BORDER = Color.web("#fecaca");   // redBorder

    // Additional green shades
    public static final Color GREEN_LIGHT = Color.web("#dcfce7");
    public static final Color GREEN = Color.web("#16a34a");
    public static final Color GREEN_BORDER = Color.web("#bbf7d0");

    // Additional red shades
    public static final Color RED_LIGHT = Color.web("#fee2e2");
    public static final Color RED = Color.web("#dc2626");
    public static final Color RED_BORDER = Color.web("#fecaca");

    // ═══════════════════════════════════════════════════════════════════════════════
    // TIMELINE DIMENSIONS (from JSX TimelineOptionsEditor)
    // ═══════════════════════════════════════════════════════════════════════════════

    public static final int TIMELINE_ROW_HEADER_WIDTH = 132;
    public static final int TIMELINE_DAY_COLUMN_WIDTH = 22;
    public static final int TIMELINE_ROW_HEIGHT = 28;
    public static final int TIMELINE_CELL_HEIGHT = 16;
    public static final int TIMELINE_CELL_RADIUS = 4;
    public static final int TIMELINE_HEADER_HEIGHT = 40;
    public static final int TIMELINE_PERIOD_BAR_HEIGHT = 8;

    // ═══════════════════════════════════════════════════════════════════════════════
    // DIMENSIONS
    // ═══════════════════════════════════════════════════════════════════════════════

    public static final double BORDER_RADIUS = 8;
    public static final double BORDER_RADIUS_SMALL = 4;
    public static final double BORDER_RADIUS_MEDIUM = 8;
    public static final double BORDER_RADIUS_LARGE = 12;
    public static final double BORDER_RADIUS_PILL = 16;

    public static final Insets PADDING_SMALL = new Insets(4);
    public static final Insets PADDING_MEDIUM = new Insets(8);
    public static final Insets PADDING_LARGE = new Insets(16);
    public static final Insets PADDING_XLARGE = new Insets(24);

    public static final double SPACING_SMALL = 4;
    public static final double SPACING_MEDIUM = 8;
    public static final double SPACING_LARGE = 16;
    public static final double SPACING_XLARGE = 24;

    // ═══════════════════════════════════════════════════════════════════════════════
    // FONTS
    // ═══════════════════════════════════════════════════════════════════════════════

    public static final Font FONT_TITLE = Font.font("System", FontWeight.BOLD, 18);
    public static final Font FONT_SUBTITLE = Font.font("System", FontWeight.SEMI_BOLD, 14);
    public static final Font FONT_BODY = Font.font("System", FontWeight.NORMAL, 13);
    public static final Font FONT_SMALL = Font.font("System", FontWeight.NORMAL, 12);
    public static final Font FONT_TINY = Font.font("System", FontWeight.NORMAL, 11);
    public static final Font FONT_LABEL = Font.font("System", FontWeight.MEDIUM, 12);
    public static final Font FONT_BADGE = Font.font("System", FontWeight.BOLD, 10);

    // ═══════════════════════════════════════════════════════════════════════════════
    // HELPER METHODS - Apply consistent styles to components
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Creates a Background with the specified fill color and corner radius.
     */
    public static Background createBackground(Color color, double radius) {
        return new Background(new BackgroundFill(color, new CornerRadii(radius), null));
    }

    /**
     * Creates a Background with individual corner radii (topLeft, topRight, bottomRight, bottomLeft).
     */
    public static Background createBackground(Color color, double topLeft, double topRight, double bottomRight, double bottomLeft) {
        return new Background(new BackgroundFill(color, new CornerRadii(topLeft, topRight, bottomRight, bottomLeft, false), null));
    }

    /**
     * Creates a Border with the specified color and corner radius.
     */
    public static Border createBorder(Color color, double radius) {
        return new Border(new BorderStroke(color, BorderStrokeStyle.SOLID, new CornerRadii(radius), BorderWidths.DEFAULT));
    }

    /**
     * Creates a Border with the specified color, width, and corner radius.
     */
    public static Border createBorder(Color color, double width, double radius) {
        return new Border(new BorderStroke(color, BorderStrokeStyle.SOLID, new CornerRadii(radius), new BorderWidths(width)));
    }

    /**
     * Applies card styling to a region (white background, border, shadow-like appearance).
     */
    public static void applyCardStyle(Region region) {
        region.setBackground(createBackground(BG_CARD, BORDER_RADIUS));
        region.setBorder(createBorder(BORDER, BORDER_RADIUS));
        region.setPadding(PADDING_LARGE);
    }

    /**
     * Applies modal card styling (warm white background).
     */
    public static void applyModalCardStyle(Region region) {
        region.setBackground(createBackground(MODAL_CARD, BORDER_RADIUS_LARGE));
        region.setBorder(createBorder(MODAL_BORDER, BORDER_RADIUS_LARGE));
        region.setPadding(PADDING_XLARGE);
    }

    /**
     * Applies primary button styling.
     */
    public static void applyPrimaryButtonStyle(Button button) {
        button.setBackground(createBackground(PRIMARY, BORDER_RADIUS));
        button.setTextFill(Color.WHITE);
        button.setFont(FONT_LABEL);
        button.setPadding(new Insets(10, 20, 10, 20));
        button.setCursor(Cursor.HAND);
    }

    /**
     * Applies secondary (outline) button styling.
     */
    public static void applySecondaryButtonStyle(Button button) {
        button.setBackground(createBackground(Color.WHITE, BORDER_RADIUS));
        button.setBorder(createBorder(BORDER, BORDER_RADIUS));
        button.setTextFill(TEXT_SECONDARY);
        button.setFont(FONT_LABEL);
        button.setPadding(new Insets(10, 20, 10, 20));
        button.setCursor(Cursor.HAND);
    }

    /**
     * Applies danger button styling.
     */
    public static void applyDangerButtonStyle(Button button) {
        button.setBackground(createBackground(DANGER, BORDER_RADIUS));
        button.setTextFill(Color.WHITE);
        button.setFont(FONT_LABEL);
        button.setPadding(new Insets(10, 20, 10, 20));
        button.setCursor(Cursor.HAND);
    }

    /**
     * Applies success button styling.
     */
    public static void applySuccessButtonStyle(Button button) {
        button.setBackground(createBackground(SUCCESS, BORDER_RADIUS));
        button.setTextFill(Color.WHITE);
        button.setFont(FONT_LABEL);
        button.setPadding(new Insets(10, 20, 10, 20));
        button.setCursor(Cursor.HAND);
    }

    /**
     * Applies search text field styling.
     */
    public static void applySearchFieldStyle(TextField field) {
        field.setBackground(createBackground(Color.WHITE, BORDER_RADIUS));
        field.setBorder(createBorder(BORDER, BORDER_RADIUS));
        field.setPadding(new Insets(10, 12, 10, 36)); // Extra left padding for search icon
        field.setFont(FONT_BODY);
    }

    /**
     * Applies standard text field styling.
     */
    public static void applyTextFieldStyle(TextField field) {
        field.setBackground(createBackground(Color.WHITE, BORDER_RADIUS));
        field.setBorder(createBorder(BORDER, BORDER_RADIUS));
        field.setPadding(new Insets(10, 12, 10, 12));
        field.setFont(FONT_BODY);
    }

    /**
     * Creates a styled badge/pill label.
     */
    public static Label createBadge(String text, Color bgColor, Color textColor) {
        Label badge = new Label(text);
        badge.setBackground(createBackground(bgColor, BORDER_RADIUS_PILL));
        badge.setTextFill(textColor);
        badge.setFont(FONT_BADGE);
        badge.setPadding(new Insets(3, 8, 3, 8));
        return badge;
    }

    /**
     * Creates a status badge based on status type.
     */
    public static Label createStatusBadge(String status) {
        return switch (status.toLowerCase()) {
            case "confirmed" -> createBadge("CONFIRMED", SUCCESS_LIGHT, SUCCESS_TEXT);
            case "pending" -> createBadge("PENDING", WARNING_LIGHT, WARNING_TEXT);
            case "cancelled" -> createBadge("CANCELLED", DANGER_LIGHT, DANGER_TEXT);
            case "arrived" -> createBadge("ARRIVED", TEAL_LIGHT, TEAL);
            default -> createBadge(status.toUpperCase(), BG, TEXT_MUTED);
        };
    }

    /**
     * Creates a status badge with custom text and accent color.
     */
    public static Label createStatusBadge(String text, Color accentColor) {
        Color bgColor = deriveColor(accentColor, 0.15);
        return createBadge(text, bgColor, accentColor);
    }

    /**
     * Creates a category badge (Accommodation, Meals, etc.).
     */
    public static Label createCategoryBadge(String category) {
        return switch (category.toLowerCase()) {
            case "accommodation" -> createBadge(category, ACCOMMODATION_BG, ACCOMMODATION_TEXT);
            case "meals" -> createBadge(category, MEALS_BG, MEALS_TEXT);
            case "diet" -> createBadge(category, DIET_BG, DIET_TEXT);
            case "program" -> createBadge(category, PROGRAM_BG, PROGRAM_TEXT);
            case "transport" -> createBadge(category, TRANSPORT_BG, TRANSPORT_TEXT);
            case "course" -> createBadge(category, COURSE_BG, COURSE_TEXT);
            case "services" -> createBadge(category, SERVICES_BG, SERVICES_TEXT);
            default -> createBadge(category, BG, TEXT_MUTED);
        };
    }

    /**
     * Creates a stats card container.
     */
    public static VBox createStatsCard(String title, String value, Color accentColor) {
        VBox card = new VBox(4);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(PADDING_LARGE);
        card.setBackground(createBackground(Color.WHITE, BORDER_RADIUS));
        card.setBorder(createBorder(BORDER_LIGHT, BORDER_RADIUS));

        Label titleLabel = new Label(title);
        titleLabel.setFont(FONT_SMALL);
        titleLabel.setTextFill(TEXT_MUTED);

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        valueLabel.setTextFill(accentColor != null ? accentColor : TEXT);

        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    /**
     * Creates a section header with optional icon.
     */
    public static HBox createSectionHeader(String title, Color accentColor) {
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 16, 12, 16));
        header.setBackground(createBackground(
            accentColor != null ? deriveColor(accentColor, 0.1) : PRIMARY_LIGHT,
            BORDER_RADIUS
        ));

        Label titleLabel = new Label(title);
        titleLabel.setFont(FONT_SUBTITLE);
        titleLabel.setTextFill(accentColor != null ? accentColor : PRIMARY_TEXT);

        header.getChildren().add(titleLabel);
        return header;
    }

    /**
     * Creates a derived color with specified opacity.
     */
    public static Color deriveColor(Color base, double opacity) {
        return Color.color(base.getRed(), base.getGreen(), base.getBlue(), opacity);
    }

    /**
     * Formats a price amount for display with 2 decimal places.
     * Note: amount is in pounds/dollars, not cents.
     */
    public static String formatPrice(double amount) {
        // GWT-compatible: avoid String.format, always show 2 decimal places
        long pence = Math.round(amount * 100);
        long pounds = pence / 100;
        long cents = Math.abs(pence % 100);
        String sign = pence < 0 ? "-" : "";
        return sign + "£" + Math.abs(pounds) + "." + (cents < 10 ? "0" : "") + cents;
    }

    /**
     * Formats a balance amount with appropriate styling hint.
     */
    public static String formatBalance(double balance) {
        if (balance <= 0) {
            return formatPrice(0);
        }
        return formatPrice(balance);
    }

    /**
     * Gets the appropriate color for a balance display.
     */
    public static Color getBalanceColor(double balance) {
        if (balance <= 0) {
            return SUCCESS;
        } else if (balance > 0) {
            return DANGER;
        }
        return TEXT;
    }

    /**
     * Applies row hover effect styling.
     */
    public static void applyRowHoverStyle(Region row, boolean isHovered) {
        if (isHovered) {
            row.setBackground(createBackground(Color.web("#f8fafc"), 0));
        } else {
            row.setBackground(createBackground(Color.TRANSPARENT, 0));
        }
    }

    /**
     * Applies selected row styling.
     */
    public static void applyRowSelectedStyle(Region row, boolean isSelected) {
        if (isSelected) {
            row.setBackground(createBackground(PRIMARY_LIGHT, 0));
            row.setBorder(createBorder(PRIMARY_BORDER, 1, 0));
        } else {
            row.setBackground(createBackground(Color.TRANSPARENT, 0));
            row.setBorder(null);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // CATEGORY HELPER METHODS
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Gets the background color for a category.
     */
    public static Color getCategoryBgColor(String category) {
        if (category == null) return BG;
        return switch (category.toLowerCase()) {
            case "accommodation" -> ACCOMMODATION_BG;
            case "meals" -> MEALS_BG;
            case "diet" -> DIET_BG;
            case "program", "teaching" -> PROGRAM_BG;
            case "transport" -> TRANSPORT_BG;
            case "parking" -> PARKING_BG;
            case "course" -> COURSE_BG;
            case "services" -> SERVICES_BG;
            case "tax" -> TAX_BG;
            case "recording" -> RECORDING_BG;
            case "other", "other_temporal" -> OTHER_BG;
            default -> BG;
        };
    }

    /**
     * Gets the text color for a category.
     */
    public static Color getCategoryTextColor(String category) {
        if (category == null) return TEXT_MUTED;
        return switch (category.toLowerCase()) {
            case "accommodation" -> ACCOMMODATION_TEXT;
            case "meals" -> MEALS_TEXT;
            case "diet" -> DIET_TEXT;
            case "program", "teaching" -> PROGRAM_TEXT;
            case "transport" -> TRANSPORT_TEXT;
            case "parking" -> PARKING_TEXT;
            case "course" -> COURSE_TEXT;
            case "services" -> SERVICES_TEXT;
            case "tax" -> TAX_TEXT;
            case "recording" -> RECORDING_TEXT;
            case "other", "other_temporal" -> OTHER_TEXT;
            default -> TEXT_MUTED;
        };
    }

    /**
     * Gets the icon/accent color for a category.
     */
    public static Color getCategoryIconColor(String category) {
        if (category == null) return TEXT_MUTED;
        return switch (category.toLowerCase()) {
            case "accommodation" -> ACCOMMODATION_ICON;
            case "meals" -> MEALS_ICON;
            case "diet" -> DIET_ICON;
            case "program", "teaching" -> PROGRAM_ICON;
            case "transport" -> TRANSPORT_ICON;
            case "parking" -> PARKING_ICON;
            case "course" -> COURSE_ICON;
            case "services" -> SERVICES_ICON;
            case "tax" -> TAX_ICON;
            case "recording" -> RECORDING_ICON;
            case "other", "other_temporal" -> OTHER_ICON;
            default -> TEXT_MUTED;
        };
    }

    /**
     * Gets the emoji icon for a category.
     */
    public static String getCategoryEmoji(String category) {
        if (category == null) return "📦";
        return switch (category.toLowerCase()) {
            case "accommodation" -> "🛏️";
            case "meals" -> "🍽️";
            case "diet" -> "🥗";
            case "program", "teaching" -> "📚";
            case "transport" -> "🚗";
            case "parking" -> "🅿️";
            case "course" -> "🎓";
            case "services" -> "🔧";
            case "tax" -> "💰";
            case "recording" -> "🎵";
            case "other", "other_temporal" -> "📦";
            default -> "📦";
        };
    }

    /**
     * Creates a category icon box (colored square with emoji).
     */
    public static javafx.scene.layout.StackPane createCategoryIcon(String category, double size) {
        javafx.scene.layout.StackPane icon = new javafx.scene.layout.StackPane();
        icon.setMinSize(size, size);
        icon.setMaxSize(size, size);
        icon.setBackground(createBackground(getCategoryBgColor(category), 4));

        javafx.scene.control.Label emoji = new javafx.scene.control.Label(getCategoryEmoji(category));
        emoji.setFont(Font.font(size * 0.6));
        icon.getChildren().add(emoji);

        return icon;
    }
}
