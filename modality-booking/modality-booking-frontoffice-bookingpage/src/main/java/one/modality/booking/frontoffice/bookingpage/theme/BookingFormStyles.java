package one.modality.booking.frontoffice.bookingpage.theme;

import javafx.geometry.Insets;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Provides reusable styling constants and helper methods for booking form UI components.
 * Uses pure JavaFX API calls instead of CSS strings to ensure GWT/WebFX compatibility.
 *
 * <p><b>Migration Note:</b> This class is being migrated to pure CSS-based theming.
 * Many methods are now deprecated in favor of CSS classes defined in:
 * <ul>
 *   <li>modality-booking-frontoffice-bookingpage-javafx@main.css (JavaFX)</li>
 *   <li>modality-booking-frontoffice-bookingpage-web@main.css (Web/GWT)</li>
 * </ul>
 *
 * <p><b>What still requires Java:</b></p>
 * <ul>
 *   <li>SVGPath stroke colors - CSS cannot style SVG strokes in WebFX</li>
 *   <li>Circle/shape fills - CSS unreliable for shapes in GWT</li>
 *   <li>Complex dynamic effects - drop shadows on specific interactions</li>
 * </ul>
 *
 * <p><b>Use CSS classes instead:</b></p>
 * <ul>
 *   <li>Static backgrounds → .bookingpage-card, .bookingpage-bg-light, etc.</li>
 *   <li>Borders → .bookingpage-border-light, .bookingpage-rounded, etc.</li>
 *   <li>Theme colors → CSS variables via theme classes on parent container</li>
 *   <li>Text styling → .bookingpage-text-dark, .bookingpage-font-bold, etc.</li>
 * </ul>
 *
 * @author Bruno Salmon
 */
public final class BookingFormStyles {

    private BookingFormStyles() {} // Utility class

    // =============================================
    // STATIC COLOR CONSTANTS (Bootstrap-derived)
    // =============================================

    // Border colors
    public static final Color BORDER_LIGHT = Color.web("#dee2e6");
    public static final Color BORDER_GRAY = Color.web("#E6E7E7");
    public static final Color BORDER_MEDIUM = Color.web("#adb5bd");

    // Background colors
    public static final Color BG_WHITE = Color.WHITE;
    public static final Color BG_LIGHT = Color.web("#f8f9fa");
    public static final Color BG_LIGHTER = Color.web("#F4F4F4");

    // Text colors
    public static final Color TEXT_DARK = Color.web("#212529");
    public static final Color TEXT_MUTED = Color.web("#6c757d");
    public static final Color TEXT_SECONDARY = Color.web("#495057");

    // Status colors
    public static final Color SUCCESS = Color.web("#198754");
    public static final Color DANGER = Color.web("#dc3545");
    public static final Color WARNING_TEXT = Color.web("#8B6914");
    public static final Color WARNING_ICON = Color.web("#D97706");

    // Warning box colors
    public static final Color WARNING_BG = Color.web("#FFF9E6");
    public static final Color WARNING_BORDER = Color.web("#FFE58F");

    // Error box colors
    public static final Color ERROR_BG = Color.web("#f8d7da");
    public static final Color ERROR_BORDER = Color.web("#f5c2c7");

    // Progress/divider colors
    public static final Color PROGRESS_TRACK = Color.web("#e0e0e0");

    // =============================================
    // REUSABLE CORNER RADII
    // =============================================

    public static final CornerRadii RADII_2 = new CornerRadii(2);
    public static final CornerRadii RADII_6 = new CornerRadii(6);
    public static final CornerRadii RADII_8 = new CornerRadii(8);
    public static final CornerRadii RADII_12 = new CornerRadii(12);
    public static final CornerRadii RADII_14 = new CornerRadii(14);
    public static final CornerRadii RADII_18 = new CornerRadii(18);
    public static final CornerRadii RADII_20 = new CornerRadii(20);
    public static final CornerRadii RADII_FULL = new CornerRadii(1000); // For pills/circles

    // =============================================
    // STANDARD DROP SHADOWS
    // =============================================

    /** Standard card shadow */
    public static final DropShadow SHADOW_CARD = createDropShadow(8, 0.1, 0, 2);

    /** Subtle button shadow */
    public static final DropShadow SHADOW_BUTTON = createDropShadow(4, 0.1, 0, 2);

    /** Elevated button shadow (hover state) */
    public static final DropShadow SHADOW_BUTTON_HOVER = createDropShadow(8, 0.15, 0, 4);

    // =============================================
    // BACKGROUND HELPER METHODS
    // =============================================

    /**
     * Creates a simple solid background with no corner radius.
     */
    public static Background bg(Color color) {
        return new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY));
    }

    /**
     * Creates a solid background with corner radius.
     */
    public static Background bg(Color color, CornerRadii radii) {
        return new Background(new BackgroundFill(color, radii, Insets.EMPTY));
    }

    /**
     * Creates a solid background with corner radius.
     */
    public static Background bg(Color color, double radius) {
        return new Background(new BackgroundFill(color, new CornerRadii(radius), Insets.EMPTY));
    }

    // =============================================
    // BORDER HELPER METHODS
    // =============================================

    /**
     * Creates a solid border with the given color, width and corner radius.
     */
    public static Border border(Color color, double width, CornerRadii radii) {
        return new Border(new BorderStroke(
                color,
                BorderStrokeStyle.SOLID,
                radii,
                new BorderWidths(width)
        ));
    }

    /**
     * Creates a solid border with the given color, width and corner radius.
     */
    public static Border border(Color color, double width, double radius) {
        return border(color, width, new CornerRadii(radius));
    }

    /**
     * Creates a border with different widths on each side (top, right, bottom, left).
     */
    public static Border border(Color color, double top, double right, double bottom, double left, CornerRadii radii) {
        return new Border(new BorderStroke(
                color,
                BorderStrokeStyle.SOLID,
                radii,
                new BorderWidths(top, right, bottom, left)
        ));
    }

    /**
     * Creates a left-only border (commonly used for section headers).
     */
    public static Border borderLeft(Color color, double width, CornerRadii radii) {
        return new Border(new BorderStroke(
                color,
                BorderStrokeStyle.SOLID,
                radii,
                new BorderWidths(0, 0, 0, width)
        ));
    }

    /**
     * Creates a bottom-only border (commonly used for dividers).
     */
    public static Border borderBottom(Color color, double width) {
        return new Border(new BorderStroke(
                Color.TRANSPARENT, Color.TRANSPARENT, color, Color.TRANSPARENT,
                BorderStrokeStyle.NONE, BorderStrokeStyle.NONE, BorderStrokeStyle.SOLID, BorderStrokeStyle.NONE,
                CornerRadii.EMPTY,
                new BorderWidths(0, 0, width, 0),
                Insets.EMPTY
        ));
    }

    /**
     * Creates a top-only border (commonly used for dividers).
     */
    public static Border borderTop(Color color, double width) {
        return new Border(new BorderStroke(
                color, Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT,
                BorderStrokeStyle.SOLID, BorderStrokeStyle.NONE, BorderStrokeStyle.NONE, BorderStrokeStyle.NONE,
                CornerRadii.EMPTY,
                new BorderWidths(width, 0, 0, 0),
                Insets.EMPTY
        ));
    }

    // =============================================
    // COMBINED STYLE HELPER METHODS
    // =============================================

    /**
     * Applies a card style with background, border and corner radius to a Region.
     *
     * @deprecated Use CSS class instead. For standard cards use {@code .bookingpage-card}.
     * For custom colors, add theme CSS classes to parent container.
     */
    @Deprecated
    public static void applyCardStyle(Region node, Color bgColor, Color borderColor, CornerRadii radii, double borderWidth) {
        node.setBackground(bg(bgColor, radii));
        node.setBorder(border(borderColor, borderWidth, radii));
    }

    /**
     * Applies a card style with background, border, corner radius and shadow.
     *
     * @deprecated Use CSS class {@code .bookingpage-card} with appropriate shadow styling.
     */
    @Deprecated
    public static void applyCardStyleWithShadow(Region node, Color bgColor, Color borderColor, CornerRadii radii, double borderWidth) {
        applyCardStyle(node, bgColor, borderColor, radii, borderWidth);
        node.setEffect(SHADOW_CARD);
    }

    /**
     * Applies a section header style with background and left border accent.
     *
     * @deprecated Use CSS class {@code .booking-form-section-header} instead.
     * Theme colors are handled via CSS variables.
     */
    @Deprecated
    public static void applySectionHeaderStyle(Region node, Color bgColor, Color accentColor, CornerRadii radii) {
        node.setBackground(bg(bgColor, radii));
        node.setBorder(borderLeft(accentColor, 4, radii));
    }

    /**
     * Applies warning box style.
     *
     * @deprecated Use CSS class {@code .bookingpage-warning-box} instead.
     */
    @Deprecated
    public static void applyWarningBoxStyle(Region node) {
        applyCardStyle(node, WARNING_BG, WARNING_BORDER, RADII_8, 1);
    }

    /**
     * Applies error box style.
     *
     * @deprecated Use CSS class {@code .bookingpage-error-box} instead.
     */
    @Deprecated
    public static void applyErrorBoxStyle(Region node) {
        applyCardStyle(node, ERROR_BG, ERROR_BORDER, RADII_8, 1);
    }

    // =============================================
    // DROP SHADOW HELPER METHODS
    // =============================================

    /**
     * Creates a drop shadow effect.
     */
    public static DropShadow createDropShadow(double radius, double opacity, double offsetX, double offsetY) {
        return new DropShadow(BlurType.THREE_PASS_BOX, Color.rgb(0, 0, 0, opacity), radius, 0, offsetX, offsetY);
    }

    /**
     * Creates a gaussian drop shadow effect.
     */
    public static DropShadow createGaussianDropShadow(double radius, double opacity, double offsetX, double offsetY) {
        return new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, opacity), radius, 0, offsetX, offsetY);
    }

    // =============================================
    // FONT HELPER METHODS
    // =============================================

    /**
     * Creates a font with the specified size and weight.
     */
    public static Font font(double size, FontWeight weight) {
        return Font.font("System", weight, size);
    }

    /**
     * Creates a regular font with the specified size.
     */
    public static Font font(double size) {
        return Font.font("System", FontWeight.NORMAL, size);
    }

    /**
     * Creates a bold font with the specified size.
     */
    public static Font fontBold(double size) {
        return Font.font("System", FontWeight.BOLD, size);
    }

    /**
     * Creates a semi-bold (600) font with the specified size.
     */
    public static Font fontSemiBold(double size) {
        return Font.font("System", FontWeight.SEMI_BOLD, size);
    }

    /**
     * Creates a medium (500) font with the specified size.
     */
    public static Font fontMedium(double size) {
        return Font.font("System", FontWeight.MEDIUM, size);
    }

    // =============================================
    // COMMON STYLING PATTERNS
    // =============================================

    /**
     * Standard white card with light border and 8px radius.
     * @deprecated Use CSS class {@code .bookingpage-card} instead.
     */
    @Deprecated
    public static void applyStandardCard(Region node) {
        applyCardStyle(node, BG_WHITE, BORDER_LIGHT, RADII_8, 1);
    }

    /**
     * Standard white card with light border and 12px radius.
     * @deprecated Use CSS classes {@code .bookingpage-card .bookingpage-rounded-lg} instead.
     */
    @Deprecated
    public static void applyStandardCardLarge(Region node) {
        applyCardStyle(node, BG_WHITE, BORDER_LIGHT, RADII_12, 1);
    }

    /**
     * Light gray card with border and 8px radius.
     * @deprecated Use CSS class {@code .bookingpage-card-light} instead.
     */
    @Deprecated
    public static void applyLightCard(Region node) {
        applyCardStyle(node, BG_LIGHT, BORDER_LIGHT, RADII_8, 1);
    }

    /**
     * Lighter gray background with 12px radius, no border.
     * @deprecated Use CSS class {@code .bookingpage-card-lighter} instead.
     */
    @Deprecated
    public static void applyLighterBackground(Region node) {
        node.setBackground(bg(BG_LIGHTER, RADII_12));
    }

    /**
     * Success circle background (green, fully rounded).
     * @deprecated Use CSS class {@code .bookingpage-success-circle} instead.
     */
    @Deprecated
    public static void applySuccessCircle(Region node) {
        node.setBackground(bg(SUCCESS, RADII_FULL));
    }

    /**
     * Applies a divider line at the top.
     * @deprecated Use CSS class {@code .bookingpage-divider-top} or {@code .bookingpage-divider-thin-top} instead.
     */
    @Deprecated
    public static void applyTopDivider(Region node, double width) {
        node.setBorder(borderTop(BORDER_GRAY, width));
    }

    /**
     * Applies a divider line at the bottom.
     * @deprecated Use CSS class {@code .bookingpage-divider-thin-bottom} instead.
     */
    @Deprecated
    public static void applyBottomDivider(Region node, double width) {
        node.setBorder(borderBottom(BG_LIGHTER, width));
    }
}
