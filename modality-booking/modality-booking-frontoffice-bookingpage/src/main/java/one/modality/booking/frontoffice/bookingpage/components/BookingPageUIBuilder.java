package one.modality.booking.frontoffice.bookingpage.components;
import one.modality.booking.frontoffice.bookingpage.BookingPageCssSelectors;

import dev.webfx.extras.i18n.controls.I18nControls;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

import static one.modality.booking.frontoffice.bookingpage.BookingPageCssSelectors.*;

/**
 * Factory class for creating common UI elements used across booking form pages.
 * Provides consistent styling and reduces code duplication.
 *
 * <p>This class creates:</p>
 * <ul>
 *   <li>Selection indicators (checkbox, radio, checkmark badge)</li>
 *   <li>Selectable cards with various styles</li>
 *   <li>Icon + label rows for displaying information</li>
 *   <li>Info/warning/error boxes</li>
 *   <li>Price display rows</li>
 * </ul>
 *
 * <p>All components use CSS classes for theming and Java for sizing (per project conventions).</p>
 *
 * @author Bruno Salmon
 */
public final class BookingPageUIBuilder {

    private BookingPageUIBuilder() {} // Utility class

    // =============================================
    // SVG ICON PATHS (Feather/Lucide icons, 24x24 viewBox)
    // =============================================

    // User/Person icons
    public static final String ICON_USER = "M12 4a4 4 0 100 8 4 4 0 000-8z M4 20c0-4 4-6 8-6s8 2 8 6";

    // Check icons
    public static final String ICON_CHECK = "M20 6L9 17l-5-5";

    // Other common icons
    public static final String ICON_TAG = "M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82zM7 7h.01";
    public static final String ICON_ENVELOPE = "M2 4h20a2 2 0 012 2v12a2 2 0 01-2 2H2a2 2 0 01-2-2V6a2 2 0 012-2z M22 6l-10 7L2 6";
    public static final String ICON_CREDIT_CARD = "M2 5h20a2 2 0 012 2v10a2 2 0 01-2 2H2a2 2 0 01-2-2V7a2 2 0 012-2z M2 10h20";

    // Festival/Booking specific icons (from JSX mockup FestivalRegistrationV2.jsx)
    /** Sun icon for lunch/daytime - 24x24 viewBox */
    public static final String ICON_SUN = "M12 2v2M12 20v2M4.93 4.93l1.41 1.41M17.66 17.66l1.41 1.41M2 12h2M20 12h2M6.34 17.66l-1.41 1.41M19.07 4.93l-1.41 1.41";
    /** Sun circle (center part of sun icon) - use with ICON_SUN */
    public static final String ICON_SUN_CIRCLE = "M12 8a4 4 0 100 8 4 4 0 000-8z";

    /** Moon/crescent icon for dinner/evening - 24x24 viewBox */
    public static final String ICON_MOON = "M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z";

    /** Plane/shuttle icon for transport - 24x24 viewBox */
    public static final String ICON_PLANE = "M17.8 19.2 16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.5-.1 1 .3 1.3L9 12l-2 3H4l-1 1 3 2 2 3 1-1v-3l3-2 3.5 5.3c.3.4.8.5 1.3.3l.5-.2c.4-.3.6-.7.5-1.2z";

    /** Car icon for parking/transport - 24x24 viewBox */
    public static final String ICON_CAR = "M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9L18 10l-2-5c-.2-.5-.7-.8-1.3-.9H9.3c-.6.1-1.1.4-1.3.9L6 10l-2.5 1.1C2.7 11.3 2 12.1 2 13v3c0 .6.4 1 1 1h2m14 0a2 2 0 1 1-4 0 2 2 0 0 1 4 0zm-12 0a2 2 0 1 1-4 0 2 2 0 0 1 4 0z";

    /** Home icon for accommodation - 24x24 viewBox */
    public static final String ICON_HOME = "M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z";
    /** Home icon door part */
    public static final String ICON_HOME_DOOR = "M9 22V12h6v10";

    /** Info icon (circle with i) - 24x24 viewBox */
    public static final String ICON_INFO_CIRCLE = "M12 2a10 10 0 100 20 10 10 0 000-20z";
    /** Info icon (letter i inside) */
    public static final String ICON_INFO_I = "M12 16v-4M12 8h.01";

    /** Utensils/fork-knife icon for meals - 24x24 viewBox */
    public static final String ICON_UTENSILS = "M3 2v7c0 1.1.9 2 2 2h4a2 2 0 0 0 2-2V2M7 2v20M21 15V2a5 5 0 0 0-5 5v6c0 1.1.9 2 2 2h3zm0 0v7";

    /** Plus circle icon for additional options - 24x24 viewBox */
    public static final String ICON_PLUS_CIRCLE = "M12 2a10 10 0 100 20 10 10 0 000-20zM12 8v8M8 12h8";

    /** Calendar icon for dates - 24x24 viewBox */
    public static final String ICON_CALENDAR = "M3 6h18v15a2 2 0 01-2 2H5a2 2 0 01-2-2V6zM3 10h18M7 3v4M17 3v4";

    /** Location/map pin icon - 24x24 viewBox */
    public static final String ICON_LOCATION = "M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z";
    /** Location pin inner circle */
    public static final String ICON_LOCATION_DOT = "M12 7a3 3 0 100 6 3 3 0 000-6z";

    /** Clock icon for time - 24x24 viewBox */
    public static final String ICON_CLOCK = "M12 3a9 9 0 100 18 9 9 0 000-18z";
    /** Clock hands */
    public static final String ICON_CLOCK_HANDS = "M12 6v6l4 4";

    // =============================================
    // EFFECTS AND SHADOWS
    // =============================================

    /** Standard card shadow - moved from BookingFormStyles for GWT compatibility */
    public static final javafx.scene.effect.DropShadow SHADOW_CARD = createDropShadow(8, 0.1, 0, 2);

    /**
     * Creates a drop shadow effect.
     * @param radius Blur radius
     * @param opacity Shadow opacity (0.0 to 1.0)
     * @param offsetX Horizontal offset
     * @param offsetY Vertical offset
     * @return A configured DropShadow effect
     */
    public static javafx.scene.effect.DropShadow createDropShadow(double radius, double opacity, double offsetX, double offsetY) {
        return new javafx.scene.effect.DropShadow(
            javafx.scene.effect.BlurType.THREE_PASS_BOX,
            Color.rgb(0, 0, 0, opacity),
            radius, 0, offsetX, offsetY
        );
    }

    /**
     * Creates a focus shadow effect for input fields.
     * @return A subtle focus shadow
     */
    public static javafx.scene.effect.DropShadow createFocusShadow() {
        return createDropShadow(3, 0.3, 0, 0);
    }

    // =============================================
    // SELECTION INDICATOR STYLES
    // =============================================

    /**
     * Styles for selection indicators in selectable cards/options.
     */
    public enum SelectionIndicatorStyle {
        /** Square checkbox with checkmark (24px, 4-6px radius) */
        CHECKBOX,
        /** Circular radio with inner dot (20px) */
        RADIO,
        /** Circular badge with checkmark (28-32px, positioned in corner) */
        CHECKMARK_BADGE
    }

    /**
     * Types of info/alert boxes.
     */
    public enum InfoBoxType {
        /** Green left border, light green background */
        SUCCESS,
        /** Yellow/amber background, info icon */
        WARNING,
        /** Red background, error icon */
        ERROR,
        /** Theme colored border, light background */
        INFO,
        /** Transparent background, theme colored border all around */
        OUTLINE_PRIMARY,
        /** Neutral gray background, gray border (for "Price includes" type messages) */
        NEUTRAL
    }

    /**
     * Types of status badges.
     */
    public enum BadgeType {
        /** Green badge for success/paid/completed states */
        SUCCESS,
        /** Yellow/amber badge for warning/partial states */
        WARNING,
        /** Red badge for error/danger states */
        DANGER,
        /** Theme-colored badge for info states */
        INFO
    }

    // =============================================
    // ICON CREATION
    // =============================================

    /**
     * Creates a styled SVG icon with the specified path, color and scale.
     *
     * @param svgPath     The SVG path content
     * @param strokeColor The stroke color
     * @param scale       Scale factor (e.g., 0.7 for 70% size)
     * @return A styled SVGPath node
     */
    public static SVGPath createIcon(String svgPath, Color strokeColor, double scale) {
        SVGPath icon = new SVGPath();
        icon.setContent(svgPath);
        icon.setStroke(strokeColor);
        icon.setStrokeWidth(2);
        icon.setFill(Color.TRANSPARENT);
        icon.setScaleX(scale);
        icon.setScaleY(scale);
        return icon;
    }

    /**
     * Creates a styled SVG icon with default scale (0.7).
     */
    public static SVGPath createIcon(String svgPath, Color strokeColor) {
        return createIcon(svgPath, strokeColor, 0.7);
    }

    /**
     * Creates a themed SVG icon with custom scale that uses the theme primary color via CSS.
     *
     * <p>CSS class: {@code .bookingpage-icon-primary}</p>
     *
     * @param svgPath The SVG path content
     * @param scale   Scale factor (e.g., 0.7 for 70% size)
     * @return A styled SVGPath node with theme primary color
     */
    public static SVGPath createThemedIcon(String svgPath, double scale) {
        SVGPath icon = new SVGPath();
        icon.setContent(svgPath);
        icon.getStyleClass().add(bookingpage_icon_primary);
        icon.setStrokeWidth(2);
        icon.setFill(Color.TRANSPARENT);
        icon.setScaleX(scale);
        icon.setScaleY(scale);
        return icon;
    }

    /**
     * Creates a muted/gray SVG icon with custom scale via CSS.
     *
     * <p>CSS class: {@code .bookingpage-icon-muted}</p>
     *
     * @param svgPath The SVG path content
     * @param scale   Scale factor (e.g., 0.7 for 70% size)
     * @return A styled SVGPath node with muted gray color
     */
    public static SVGPath createMutedIcon(String svgPath, double scale) {
        SVGPath icon = new SVGPath();
        icon.setContent(svgPath);
        icon.getStyleClass().add(bookingpage_icon_muted);
        icon.setStrokeWidth(2);
        icon.setFill(Color.TRANSPARENT);
        icon.setScaleX(scale);
        icon.setScaleY(scale);
        return icon;
    }

    /**
     * Creates a composite sun icon (rays + center circle) for lunch/daytime.
     * Uses both ICON_SUN (rays) and ICON_SUN_CIRCLE (center) paths.
     *
     * @param strokeColor The stroke color for the icon
     * @param scale       Scale factor
     * @return A StackPane containing the composite sun icon
     */
    public static StackPane createSunIcon(Color strokeColor, double scale) {
        SVGPath rays = new SVGPath();
        rays.setContent(ICON_SUN);
        rays.setStroke(strokeColor);
        rays.setStrokeWidth(2);
        rays.setFill(Color.TRANSPARENT);
        rays.setScaleX(scale);
        rays.setScaleY(scale);

        SVGPath circle = new SVGPath();
        circle.setContent(ICON_SUN_CIRCLE);
        circle.setStroke(strokeColor);
        circle.setStrokeWidth(2);
        circle.setFill(Color.TRANSPARENT);
        circle.setScaleX(scale);
        circle.setScaleY(scale);

        StackPane container = new StackPane(rays, circle);
        container.setAlignment(Pos.CENTER);
        return container;
    }

    /**
     * Creates a "SOLD OUT" corner ribbon for accommodation cards.
     * Positioned in top-right corner with diagonal rotation.
     * Uses warm gray color (#78716c) as per JSX mockup.
     *
     * <p>CSS classes: {@code .bookingpage-soldout-ribbon}, {@code .bookingpage-soldout-ribbon-text}</p>
     *
     * @return A StackPane containing the rotated ribbon
     */
    public static StackPane createSoldOutRibbon() {
        Label label = new Label("SOLD OUT");
        label.getStyleClass().add(bookingpage_soldout_ribbon_text);
        label.setPadding(new Insets(4, 40, 4, 40));

        StackPane ribbon = new StackPane(label);
        ribbon.getStyleClass().add(bookingpage_soldout_ribbon);
        // Rotate 45 degrees
        ribbon.getTransforms().add(new javafx.scene.transform.Rotate(45, 0, 0));
        ribbon.setAlignment(Pos.CENTER);

        return ribbon;
    }

    /**
     * Creates a constraint badge with info icon (e.g., "Full Festival Only", "Minimum 3 nights").
     * Uses light blue background (#E3F2FD) and dark blue text (#0D47A1) per JSX mockup.
     *
     * <p>CSS classes: {@code .bookingpage-badge-constraint}, {@code .bookingpage-badge-constraint-text},
     * {@code .bookingpage-badge-constraint-icon}, with {@code .disabled} modifier for sold out state</p>
     *
     * @param text      The constraint text to display
     * @param isSoldOut Whether the card is sold out (uses gray styling if true)
     * @return An HBox containing the info icon and constraint text
     */
    public static HBox createConstraintBadge(String text, boolean isSoldOut) {
        HBox badge = new HBox(5);
        badge.setAlignment(Pos.CENTER_LEFT);
        badge.setPadding(new Insets(4, 10, 4, 10));
        badge.getStyleClass().add(bookingpage_badge_constraint);

        // Info icon (small, 12x12) - CSS handles colors
        SVGPath infoCircle = new SVGPath();
        infoCircle.setContent("M12 2a10 10 0 100 20 10 10 0 000-20z");
        infoCircle.setScaleX(0.5);
        infoCircle.setScaleY(0.5);
        infoCircle.getStyleClass().add(bookingpage_badge_constraint_icon);

        SVGPath infoI = new SVGPath();
        infoI.setContent("M12 16v-4M12 8h.01");
        infoI.setScaleX(0.5);
        infoI.setScaleY(0.5);
        infoI.getStyleClass().add(bookingpage_badge_constraint_icon);

        StackPane infoIcon = new StackPane(infoCircle, infoI);
        infoIcon.setMinSize(12, 12);
        infoIcon.setMaxSize(12, 12);

        Label textLabel = new Label(text);
        textLabel.getStyleClass().add(bookingpage_badge_constraint_text);

        // Apply disabled class for sold out state - CSS handles colors
        if (isSoldOut) {
            badge.getStyleClass().add(disabled);
        }

        badge.getChildren().addAll(infoIcon, textLabel);
        return badge;
    }

    /**
     * Creates a "LIMITED" availability badge with yellow/amber styling.
     * Uses yellow background (#fff3cd) and dark amber text (#856404) per JSX mockup.
     *
     * <p>CSS class: {@code .bookingpage-badge-limited}</p>
     *
     * @return A Label styled as a limited availability badge
     */
    public static Label createLimitedBadge() {
        Label badge = new Label("LIMITED");
        badge.getStyleClass().add(bookingpage_badge_limited);
        badge.setPadding(new Insets(4, 8, 4, 8));
        return badge;
    }

    // =============================================
    // SELECTION INDICATORS
    // =============================================

    /**
     * Creates a checkbox indicator (square, 24px, with checkmark when selected).
     * This overload accepts an ObjectProperty for API compatibility but colors are CSS-based.
     *
     * @param selectedProperty      Property to bind selection state
     * @param colorSchemeProperty   Property (ignored - kept for API compatibility, use CSS theme classes instead)
     * @return A StackPane containing the checkbox indicator
     */
    public static StackPane createCheckboxIndicator(BooleanProperty selectedProperty, ObjectProperty<BookingFormColorScheme> colorSchemeProperty) {
        return createCheckboxIndicator(selectedProperty);
    }

    /**
     * Creates a checkbox indicator (square, 24px, with checkmark when selected).
     * Uses pure CSS for theming via CSS variables.
     *
     * <p>CSS classes used:</p>
     * <ul>
     *   <li>{@code .booking-form-checkbox-indicator} - container</li>
     *   <li>{@code .booking-form-checkbox-rect} - the rectangle background (styled via CSS)</li>
     *   <li>{@code .booking-form-checkbox-checkmark} - the checkmark SVG (styled via CSS)</li>
     *   <li>{@code .selected} - added when selected</li>
     * </ul>
     *
     * @param selectedProperty Property to bind selection state
     * @return A StackPane containing the checkbox indicator
     */
    public static StackPane createCheckboxIndicator(BooleanProperty selectedProperty) {
        double size = 20;

        // Background rectangle - styled entirely via CSS
        Rectangle rect = new Rectangle(size, size);
        rect.setArcWidth(6);  // 3px radius * 2
        rect.setArcHeight(6);
        rect.getStyleClass().add(booking_form_checkbox_rect);

        // Checkmark - styled via CSS
        SVGPath checkmark = new SVGPath();
        checkmark.setContent(ICON_CHECK);
        checkmark.setScaleX(0.42);
        checkmark.setScaleY(0.42);
        checkmark.setVisible(false);
        checkmark.getStyleClass().add(booking_form_checkbox_checkmark);

        StackPane container = new StackPane(rect, checkmark);
        container.setMinSize(size, size);
        container.setMaxSize(size, size);
        container.setAlignment(Pos.CENTER);
        container.getStyleClass().add(booking_form_checkbox_indicator);

        // Update CSS classes based on selection (colors handled by CSS)
        Runnable updateStyle = () -> {
            boolean selected = selectedProperty.get();
            if (selected) {
                rect.getStyleClass().add(BookingPageCssSelectors.selected);
                container.getStyleClass().add(BookingPageCssSelectors.selected);
                checkmark.setVisible(true);
            } else {
                rect.getStyleClass().remove(BookingPageCssSelectors.selected);
                container.getStyleClass().remove(BookingPageCssSelectors.selected);
                checkmark.setVisible(false);
            }
        };

        updateStyle.run();
        selectedProperty.addListener((obs, old, val) -> updateStyle.run());

        return container;
    }

    /**
     * Creates a radio indicator (circular, 20px, with inner dot when selected).
     * Uses pure CSS for theming via CSS variables.
     *
     * <p>CSS classes used:</p>
     * <ul>
     *   <li>{@code .booking-form-radio-indicator} - container</li>
     *   <li>{@code .booking-form-radio-outer} - outer circle (styled via CSS)</li>
     *   <li>{@code .booking-form-radio-inner} - inner dot (styled via CSS)</li>
     *   <li>{@code .selected} - added when selected</li>
     * </ul>
     *
     * @param selectedProperty Property to bind selection state
     * @return A StackPane containing the radio indicator
     */
    public static StackPane createRadioIndicator(BooleanProperty selectedProperty) {
        double size = 20;
        double dotSize = 10;

        // Outer circle - styled via CSS
        Circle outer = new Circle(size / 2);
        outer.getStyleClass().add(booking_form_radio_outer);

        // Inner dot - styled via CSS
        Circle inner = new Circle(dotSize / 2);
        inner.setVisible(false);
        inner.getStyleClass().add(booking_form_radio_inner);

        StackPane container = new StackPane(outer, inner);
        container.setMinSize(size, size);
        container.setMaxSize(size, size);
        container.setAlignment(Pos.CENTER);
        container.getStyleClass().add(booking_form_radio_indicator);

        // Update CSS classes based on selection (colors handled by CSS)
        Runnable updateStyle = () -> {
            boolean selected = selectedProperty.get();
            if (selected) {
                outer.getStyleClass().add(BookingPageCssSelectors.selected);
                inner.getStyleClass().add(BookingPageCssSelectors.selected);
                inner.setVisible(true);
                container.getStyleClass().add(BookingPageCssSelectors.selected);
            } else {
                outer.getStyleClass().remove(BookingPageCssSelectors.selected);
                inner.getStyleClass().remove(BookingPageCssSelectors.selected);
                inner.setVisible(false);
                container.getStyleClass().remove(BookingPageCssSelectors.selected);
            }
        };

        updateStyle.run();
        selectedProperty.addListener((obs, old, val) -> updateStyle.run());

        return container;
    }

    /**
     * Creates a checkmark badge that uses CSS for theming (circular, positioned in corner).
     * The badge color comes from CSS variables, allowing dynamic theme switching.
     *
     * <p>CSS classes used:</p>
     * <ul>
     *   <li>{@code .booking-form-checkmark-badge} - container</li>
     *   <li>{@code .booking-form-checkmark-circle} - circular background (styled via CSS)</li>
     *   <li>{@code .booking-form-checkmark-icon} - checkmark SVG (styled via CSS)</li>
     * </ul>
     *
     * @param size Badge size (24, 28, or 32px)
     * @return A StackPane containing the checkmark badge
     */
    public static StackPane createCheckmarkBadgeCss(double size) {
        // Circular background - styled via CSS
        Circle circle = new Circle(size / 2);
        circle.getStyleClass().add(booking_form_checkmark_circle);

        // Checkmark - styled via CSS
        SVGPath checkmark = new SVGPath();
        checkmark.setContent(ICON_CHECK);
        checkmark.setScaleX(size / 48);  // Scale based on size
        checkmark.setScaleY(size / 48);
        checkmark.getStyleClass().add(booking_form_checkmark_icon);

        StackPane badge = new StackPane(circle, checkmark);
        badge.setMinSize(size, size);
        badge.setMaxSize(size, size);
        badge.setAlignment(Pos.CENTER);
        badge.getStyleClass().add("booking-form-checkmark-badge");

        return badge;
    }

    /**
     * Creates an empty circle indicator for unselected states.
     * Used alongside checkmark badges to show selection affordance when not selected.
     *
     * <p>CSS classes used:</p>
     * <ul>
     *   <li>{@code .booking-form-empty-circle} - container with border styling</li>
     * </ul>
     *
     * @param size Circle size in pixels
     * @return A StackPane containing the empty circle indicator
     */
    public static StackPane createEmptyCircleIndicator(double size) {
        Circle circle = new Circle(size / 2);
        circle.getStyleClass().add(booking_form_empty_circle);

        StackPane container = new StackPane(circle);
        container.setMinSize(size, size);
        container.setMaxSize(size, size);
        container.setAlignment(Pos.CENTER);

        return container;
    }

    /**
     * Creates a payment option card (for deposit, custom amount, full payment).
     * Centered layout with title, amount, and description.
     * Uses CSS classes for all styling - no Java-based colors or fonts.
     *
     * @param title           Card title (e.g., "Minimum Deposit")
     * @param amount          Amount to display (formatted with currency)
     * @param description     Description text (e.g., "10% deposit required")
     * @param selected        Whether this option is currently selected
     * @param onSelect        Action to run when card is clicked
     * @return A styled VBox containing the payment option card
     * This method bundles content creation with card creation - prefer building content separately.
     */
    public static VBox createPaymentOptionCard(
            String title,
            String amount,
            String description,
            boolean selected,
            Runnable onSelect) {

        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(16));
        card.setCursor(Cursor.HAND);
        card.setMaxWidth(Double.MAX_VALUE);
        card.getStyleClass().add(bookingpage_card);

        // Title - CSS handles font and color
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().addAll(bookingpage_text_sm, bookingpage_font_semibold, bookingpage_text_dark);

        // Amount - CSS handles font size and primary color
        Label amountLabel = new Label(amount);
        amountLabel.getStyleClass().addAll(bookingpage_price_medium, bookingpage_text_primary);

        // Description - CSS handles styling
        Label descLabel = new Label(description);
        descLabel.getStyleClass().addAll(bookingpage_label_small, bookingpage_text_center);
        descLabel.setWrapText(true);

        // Content container
        VBox content = new VBox(8);
        content.setAlignment(Pos.CENTER);
        content.getChildren().addAll(titleLabel, amountLabel, descLabel);

        // Add checkmark badge for selected state
        if (selected) {
            StackPane checkmarkBadge = createCheckmarkBadgeCss(24);
            StackPane wrapper = new StackPane(content, checkmarkBadge);
            StackPane.setAlignment(checkmarkBadge, Pos.TOP_RIGHT);
            StackPane.setMargin(checkmarkBadge, new Insets(-8, -8, 0, 0));
            card.getChildren().add(wrapper);
            card.getStyleClass().add(BookingPageCssSelectors.selected);
        } else {
            card.getChildren().add(content);
        }

        // Click handler
        card.setOnMouseClicked(e -> {
            if (onSelect != null) {
                onSelect.run();
            }
        });

        return card;
    }

    // =============================================
    // PASSIVE (NON-INTERACTIVE) CARDS
    // =============================================

    /**
     * Creates a passive (non-interactive) card for displaying information.
     * No hover effects, no cursor pointer - purely informative display.
     *
     * <p>Uses CSS class {@code .bookingpage-passive-card}</p>
     *
     * @return A styled VBox for adding content
     */
    public static VBox createPassiveCard() {
        VBox card = new VBox(0);
        card.getStyleClass().add(bookingpage_passive_card);
        card.setPadding(new Insets(20));
        return card;
    }

    // =============================================
    // CHECKBOX AND RADIO CARDS
    // =============================================

    /**
     * Creates a card with an embedded checkbox indicator on the left, with color scheme support.
     * Applies border and checkbox colors in Java per project guidelines for WebFX/GWT compatibility.
     *
     * @param content              The content to display (typically labels/descriptions)
     * @param selectedProperty     Property to bind selection state (toggles on click)
     * @param colorSchemeProperty  Property for color scheme (used for card border and checkbox when selected)
     * @return A styled HBox containing checkbox indicator + content
     */
    public static HBox createCheckboxCard(javafx.scene.Node content, BooleanProperty selectedProperty,
            ObjectProperty<BookingFormColorScheme> colorSchemeProperty) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16));
        card.setCursor(Cursor.HAND);
        card.getStyleClass().add(bookingpage_checkbox_card);

        // Checkbox indicator on left - with color scheme support
        StackPane checkbox = createColorSchemeCheckboxIndicator(selectedProperty, colorSchemeProperty);

        card.getChildren().addAll(checkbox, content);

        // Use CSS for all visual styling (hover, selected states)
        // Java only toggles the 'selected' class
        makeSelectable(card, selectedProperty);

        return card;
    }

    /**
     * Creates a checkbox indicator with color scheme support applied in Java.
     * For WebFX/GWT compatibility, colors are set programmatically.
     */
    private static StackPane createColorSchemeCheckboxIndicator(BooleanProperty selectedProperty,
            ObjectProperty<BookingFormColorScheme> colorSchemeProperty) {
        if (colorSchemeProperty == null) {
            return createCheckboxIndicator(selectedProperty);
        }

        double size = 20;

        // Background rectangle
        Rectangle rect = new Rectangle(size, size);
        rect.setArcWidth(6);
        rect.setArcHeight(6);

        // Checkmark
        SVGPath checkmark = new SVGPath();
        checkmark.setContent(ICON_CHECK);
        checkmark.setScaleX(0.42);
        checkmark.setScaleY(0.42);
        checkmark.setVisible(false);

        StackPane container = new StackPane(rect, checkmark);
        container.setMinSize(size, size);
        container.setMaxSize(size, size);
        container.setAlignment(Pos.CENTER);

        // Apply colors from color scheme in Java
        Runnable updateStyle = () -> {
            boolean selected = selectedProperty.get();
            BookingFormColorScheme scheme = colorSchemeProperty.get();
            if (scheme == null) scheme = BookingFormColorScheme.DEFAULT;

            if (selected) {
                rect.setFill(scheme.getPrimary());
                rect.setStroke(scheme.getPrimary());
                checkmark.setFill(Color.WHITE);
                checkmark.setStroke(Color.WHITE);
                checkmark.setVisible(true);
            } else {
                rect.setFill(Color.WHITE);
                rect.setStroke(Color.web("#D1D5DB"));
                checkmark.setVisible(false);
            }
        };

        updateStyle.run();
        selectedProperty.addListener((obs, old, val) -> updateStyle.run());
        colorSchemeProperty.addListener((obs, old, val) -> updateStyle.run());

        return container;
    }

    /**
     * Creates a card with an embedded radio indicator on the left.
     * Ideal for single-select groups (payment methods, rate types, etc.).
     *
     * <p>CSS classes used:</p>
     * <ul>
     *   <li>{@code .bookingpage-radio-card} - card styling</li>
     *   <li>{@code .selected} - added when selected</li>
     * </ul>
     *
     * @param content          The content to display (typically icon + labels)
     * @param selectedProperty Property to bind selection state
     * @param onSelect         Action to run when card is selected (for radio group management)
     * @return A styled HBox containing radio indicator + content
     */
    public static HBox createRadioCard(javafx.scene.Node content, BooleanProperty selectedProperty, Runnable onSelect) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16));
        card.setCursor(Cursor.HAND);
        card.getStyleClass().add(bookingpage_radio_card);

        // Radio indicator on left
        StackPane radio = createRadioIndicator(selectedProperty);

        card.getChildren().addAll(radio, content);

        // Click triggers the onSelect callback (for radio group management)
        card.setOnMouseClicked(e -> {
            if (onSelect != null) {
                onSelect.run();
            }
        });

        // Update CSS class based on selection
        if (selectedProperty.get()) {
            card.getStyleClass().add(selected);
        }
        selectedProperty.addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                if (!card.getStyleClass().contains(selected)) {
                    card.getStyleClass().add(selected);
                }
            } else {
                card.getStyleClass().remove(selected);
            }
        });

        return card;
    }

    // =============================================
    // NAVIGATION BUTTONS
    // =============================================

    /**
     * Creates a primary action button with dynamic color scheme support.
     * This overload with colorSchemeProperty is kept for API compatibility.
     *
     * @param i18nKey            The i18n key for the button text
     * @param colorSchemeProperty Property (ignored - kept for API compatibility, use CSS theme classes instead)
     * @return A styled Button
     */
    public static Button createPrimaryButton(Object i18nKey, ObjectProperty<BookingFormColorScheme> colorSchemeProperty) {
        return createPrimaryButton(i18nKey);
    }

    /**
     * Creates a primary action button (Continue, Sign In, Submit, etc.).
     * Uses CSS for theming via CSS variables.
     *
     * <p>CSS classes used:</p>
     * <ul>
     *   <li>{@code .booking-form-primary-btn} - button styling with hover state (styled via CSS)</li>
     *   <li>{@code .booking-form-primary-btn-text} - text styling</li>
     *   <li>{@code .disabled} - added when disabled</li>
     * </ul>
     *
     * @param i18nKey The i18n key for the button text
     * @return A styled Button
     */
    public static Button createPrimaryButton(Object i18nKey) {
        Button btn = I18nControls.newButton(i18nKey);
        btn.setCursor(Cursor.HAND);
        btn.setPadding(new Insets(14, 32, 14, 32)); // Set in Java for WebFX/GWT compatibility
        btn.getStyleClass().addAll(booking_form_primary_btn, booking_form_primary_btn_text);

        // Disabled state - update CSS class and cursor
        btn.disabledProperty().addListener((obs, old, disabled) -> {
            if (disabled) {
                if (!btn.getStyleClass().contains(BookingPageCssSelectors.disabled)) {
                    btn.getStyleClass().add(BookingPageCssSelectors.disabled);
                }
                btn.setCursor(Cursor.DEFAULT);
            } else {
                btn.getStyleClass().remove(BookingPageCssSelectors.disabled);
                btn.setCursor(Cursor.HAND);
            }
        });

        // Hover effects handled by CSS via .booking-form-primary-btn:hover

        return btn;
    }

    /**
     * Creates a back/secondary button with arrow prefix.
     * Uses CSS for theming via CSS variables.
     *
     * <p>CSS classes used:</p>
     * <ul>
     *   <li>{@code .booking-form-back-btn} - button styling with hover state (styled via CSS)</li>
     *   <li>{@code .booking-form-back-btn-text} - text styling</li>
     * </ul>
     *
     * @param i18nKey The i18n key for the button text (typically "Back")
     * @return A styled Button
     */
    public static Button createBackButton(Object i18nKey) {
        Button btn = new Button();
        // Arrow prefix as graphic
        Label arrowLabel = new Label("← "); // ←
        arrowLabel.getStyleClass().add(booking_form_back_btn_text);
        btn.setGraphic(arrowLabel);
        btn.setContentDisplay(ContentDisplay.LEFT);
        I18nControls.bindI18nProperties(btn, i18nKey);

        btn.setCursor(Cursor.HAND);
        btn.setPadding(new Insets(14, 32, 14, 32)); // Set in Java for WebFX/GWT compatibility
        btn.getStyleClass().addAll(booking_form_back_btn, booking_form_back_btn_text);

        // Hover effects handled by CSS via .booking-form-back-btn:hover

        return btn;
    }

    /**
     * Creates a navigation button row (HBox) for back and primary buttons.
     * Back button on left, spacer in middle, primary button on right.
     *
     * @return A styled HBox for navigation buttons
     */
    public static HBox createNavigationButtonRow() {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER);
        return row;
    }

    // =============================================
    // TEXT & LABEL FACTORIES
    // =============================================

    /**
     * Creates a themed hyperlink with primary color styling via CSS.
     * The hyperlink color is applied via CSS.
     *
     * <p>CSS classes: {@code .bookingpage-link-primary}</p>
     *
     * @param i18nKey The i18n key for the hyperlink text
     * @return A styled Hyperlink
     */
    public static Hyperlink createThemedHyperlink(Object i18nKey) {
        Hyperlink link = new Hyperlink();
        I18nControls.bindI18nProperties(link, i18nKey);
        link.getStyleClass().add(bookingpage_link_primary);
        link.setCursor(Cursor.HAND);
        return link;
    }

    /**
     * Creates a status badge for displaying states like "Paid", "Pending", etc.
     * Uses CSS classes for styling based on badge type.
     *
     * <p>CSS classes used:</p>
     * <ul>
     *   <li>{@code .bookingpage-badge} - base styling</li>
     *   <li>{@code .bookingpage-badge-success} - green for SUCCESS</li>
     *   <li>{@code .bookingpage-badge-warning} - yellow/amber for WARNING</li>
     *   <li>{@code .bookingpage-badge-danger} - red for DANGER</li>
     *   <li>{@code .bookingpage-badge-info} - theme color for INFO</li>
     * </ul>
     *
     * @param text The badge text (e.g., "Paid", "Pending")
     * @param type The badge type (SUCCESS, WARNING, DANGER, INFO)
     * @return A styled Label
     */
    public static Label createStatusBadge(String text, BadgeType type) {
        Label badge = new Label(text);
        badge.getStyleClass().add(bookingpage_badge);
        badge.getStyleClass().add("bookingpage-badge-" + type.name().toLowerCase());
        badge.setPadding(new Insets(4, 8, 4, 8));
        return badge;
    }

    /**
     * Creates a themed circle for icon backgrounds using CSS.
     * The circle uses the theme's selected background color.
     *
     * <p>CSS class: {@code .bookingpage-icon-circle-themed}</p>
     *
     * @param size Circle diameter in pixels
     * @return A styled StackPane that can contain an icon
     */
    public static StackPane createThemedIconCircle(double size) {
        StackPane circle = new StackPane();
        circle.setMinSize(size, size);
        circle.setMaxSize(size, size);
        circle.getStyleClass().add(bookingpage_icon_circle_themed);
        circle.setAlignment(Pos.CENTER);
        return circle;
    }

    /**
     * Creates an info/warning/error box for displaying messages.
     * Uses CSS classes for theming - colors come from CSS variables for theme compliance.
     *
     * <p>Style variants (styled via CSS):</p>
     * <ul>
     *   <li>{@code INFO} - Theme-colored background and border (uses CSS variables)</li>
     *   <li>{@code SUCCESS} - Green background with left border</li>
     *   <li>{@code WARNING} - Amber/yellow background with border</li>
     *   <li>{@code ERROR} - Red background with border</li>
     *   <li>{@code OUTLINE_PRIMARY} - Transparent background, primary color border</li>
     * </ul>
     *
     * <p>CSS classes used:</p>
     * <ul>
     *   <li>{@code .bookingpage-info-box} - base styling</li>
     *   <li>{@code .bookingpage-info-box-success} - success variant</li>
     *   <li>{@code .bookingpage-info-box-warning} - warning variant</li>
     *   <li>{@code .bookingpage-info-box-error} - error variant</li>
     *   <li>{@code .bookingpage-info-box-info} - info variant (theme colored)</li>
     *   <li>{@code .bookingpage-info-box-outline-primary} - outline variant</li>
     * </ul>
     *
     * @param message The message text to display
     * @param type    The type of info box (SUCCESS, WARNING, ERROR, INFO, OUTLINE_PRIMARY)
     * @return A styled HBox containing an icon and message
     */
    public static HBox createInfoBox(String message, InfoBoxType type) {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(14, 16, 14, 16));
        box.getStyleClass().add(bookingpage_info_box);

        // Icon based on type - CSS handles colors
        String iconUnicode;
        String typeClass = switch (type) {
            case SUCCESS -> {
                iconUnicode = "\u2713"; // ✓
                yield "bookingpage-info-box-success";
            }
            case WARNING -> {
                iconUnicode = "\u26A0"; // ⚠
                yield "bookingpage-info-box-warning";
            }
            case ERROR -> {
                iconUnicode = "\u2716"; // ✖
                yield "bookingpage-info-box-error";
            }
            case OUTLINE_PRIMARY -> {
                iconUnicode = "\u2139"; // ℹ
                yield "bookingpage-info-box-outline-primary";
            }
            case NEUTRAL -> {
                iconUnicode = ""; // No icon for neutral boxes
                yield "bookingpage-info-box-neutral";
            }
            default -> { // INFO
                iconUnicode = "\u2139"; // ℹ
                yield "bookingpage-info-box-info";
            }
        };

        box.getStyleClass().add(typeClass);

        // Only add icon if not empty (NEUTRAL type has no icon)
        if (!iconUnicode.isEmpty()) {
            Label iconLabel = new Label(iconUnicode);
            iconLabel.getStyleClass().add(bookingpage_info_box_icon);
            box.getChildren().add(iconLabel);
        }

        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add(bookingpage_info_box_message);
        messageLabel.setWrapText(true);

        box.getChildren().add(messageLabel);
        return box;
    }

    /**
     * Creates an info box with an i18n key for the message.
     *
     * @param i18nKey The i18n key for the message
     * @param type    The type of info box
     * @return A styled HBox containing an icon and message
     */
    public static HBox createInfoBox(Object i18nKey, InfoBoxType type) {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(14, 16, 14, 16));
        box.getStyleClass().add(bookingpage_info_box);

        // Icon based on type - CSS handles colors
        String iconUnicode;
        String typeClass;
        switch (type) {
            case SUCCESS -> {
                iconUnicode = "\u2713"; // ✓
                typeClass = "bookingpage-info-box-success";
            }
            case WARNING -> {
                iconUnicode = "\u26A0"; // ⚠
                typeClass = "bookingpage-info-box-warning";
            }
            case ERROR -> {
                iconUnicode = "\u2716"; // ✖
                typeClass = "bookingpage-info-box-error";
            }
            case OUTLINE_PRIMARY -> {
                iconUnicode = "\u2139"; // ℹ
                typeClass = "bookingpage-info-box-outline-primary";
            }
            case NEUTRAL -> {
                iconUnicode = ""; // No icon for neutral boxes
                typeClass = "bookingpage-info-box-neutral";
            }
            default -> { // INFO
                iconUnicode = "\u2139"; // ℹ
                typeClass = "bookingpage-info-box-info";
            }
        }

        box.getStyleClass().add(typeClass);

        // Only add icon if not empty (NEUTRAL type has no icon)
        if (!iconUnicode.isEmpty()) {
            Label iconLabel = new Label(iconUnicode);
            iconLabel.getStyleClass().add(bookingpage_info_box_icon);
            box.getChildren().add(iconLabel);
        }

        Label messageLabel = I18nControls.newLabel(i18nKey);
        messageLabel.getStyleClass().add(bookingpage_info_box_message);
        messageLabel.setWrapText(true);

        box.getChildren().add(messageLabel);
        return box;
    }

    // =============================================
    // FORM ELEMENTS
    // =============================================

    /**
     * Result class for verification code field creation.
     * Contains both the container and the individual digit fields for further customization.
     */
    public static class VerificationCodeResult {
        private final HBox container;
        private final javafx.scene.control.TextField[] digitFields;

        public VerificationCodeResult(HBox container, javafx.scene.control.TextField[] digitFields) {
            this.container = container;
            this.digitFields = digitFields;
        }

        /** Returns the HBox container with all digit fields */
        public HBox getContainer() {
            return container;
        }

        /** Returns the array of 6 TextField digit fields */
        public javafx.scene.control.TextField[] getDigitFields() {
            return digitFields;
        }
    }

    /**
     * Creates a container with 6 digit input fields for verification codes.
     * Provides auto-advance, backspace navigation, and paste support.
     *
     * <p>CSS class used: {@code .bookingpage-digit-field}, {@code .bookingpage-input-bordered}, {@code .bookingpage-input-focused}</p>
     *
     * @param onCodeChange Runnable to call when any digit changes (for validation/updates)
     * @param onPasteDigits Consumer that handles pasting multiple digits (digits, startIndex)
     * @return VerificationCodeResult containing the container and digit fields array
     */
    public static VerificationCodeResult createVerificationCodeFields(
            Runnable onCodeChange,
            java.util.function.BiConsumer<String, Integer> onPasteDigits) {

        javafx.scene.control.TextField[] digitFields = new javafx.scene.control.TextField[6];
        HBox container = new HBox(8);
        container.setAlignment(Pos.CENTER);

        for (int i = 0; i < 6; i++) {
            final int index = i;
            javafx.scene.control.TextField digitField = new javafx.scene.control.TextField();
            digitField.setPrefWidth(48);
            digitField.setMaxWidth(48);
            digitField.setPrefHeight(48);
            digitField.setMinHeight(48);
            digitField.setAlignment(Pos.CENTER);
            digitField.getStyleClass().add(bookingpage_digit_field);
            digitField.setPadding(new Insets(12, 0, 12, 0));

            // Handle input - only allow single digit
            digitField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && !newVal.isEmpty()) {
                    // Handle paste of multiple digits
                    String digits = newVal.replaceAll("\\D", "");
                    if (digits.length() > 1) {
                        // Distribute pasted digits across fields
                        if (onPasteDigits != null) {
                            onPasteDigits.accept(digits, index);
                        }
                        return;
                    }
                    // Single digit - keep only first digit
                    if (digits.length() == 1) {
                        if (!digits.equals(newVal)) {
                            digitField.setText(digits);
                        }
                        // Auto-advance to next field
                        if (index < 5) {
                            digitFields[index + 1].requestFocus();
                        }
                        // Update combined code
                        if (onCodeChange != null) {
                            onCodeChange.run();
                        }
                    } else {
                        digitField.setText("");
                    }
                } else {
                    if (onCodeChange != null) {
                        onCodeChange.run();
                    }
                }
            });

            // Handle backspace to go to previous field
            digitField.setOnKeyPressed(e -> {
                if (e.getCode() == javafx.scene.input.KeyCode.BACK_SPACE && digitField.getText().isEmpty() && index > 0) {
                    digitFields[index - 1].requestFocus();
                    digitFields[index - 1].clear();
                }
            });

            // Set initial border styling via CSS class
            digitField.getStyleClass().add(bookingpage_input_bordered);

            // Focus styling - toggle CSS class on focus (theme colors via CSS variables)
            digitField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (isFocused) {
                    digitField.getStyleClass().remove(bookingpage_input_bordered);
                    digitField.getStyleClass().add(bookingpage_input_focused);
                } else {
                    digitField.getStyleClass().remove(bookingpage_input_focused);
                    digitField.getStyleClass().add(bookingpage_input_bordered);
                }
            });

            digitFields[i] = digitField;
            container.getChildren().add(digitField);
        }

        return new VerificationCodeResult(container, digitFields);
    }

    // =============================================
    // UTILITY METHODS
    // =============================================

    /**
     * Formats a date range for display.
     * Handles same month, different month, and different year cases.
     *
     * <p>Examples with shortMonth=true:</p>
     * <ul>
     *   <li>Same month: "1 - 5 Jan 2026"</li>
     *   <li>Different months: "27 Feb - 1 Mar 2026"</li>
     *   <li>Different years: "27 Dec 2025 - 1 Jan 2026"</li>
     * </ul>
     *
     * <p>Examples with shortMonth=false:</p>
     * <ul>
     *   <li>Same month: "1 - 5 January 2026"</li>
     *   <li>Different months: "27 February - 1 March 2026"</li>
     *   <li>Different years: "27 December 2025 - 1 January 2026"</li>
     * </ul>
     *
     * @param start      Start date
     * @param end        End date
     * @param shortMonth Whether to use short month names (Jan) or full names (January)
     * @return Formatted date range string
     */
    public static String formatDateRange(LocalDate start, LocalDate end, boolean shortMonth) {
        if (start == null || end == null) {
            return "";
        }

        if (shortMonth) {
            // Use TextStyle for short month names (e.g., "Jan", "Feb")
            String firstDay = String.valueOf(start.getDayOfMonth());
            String firstMonth = start.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

            String lastDay = String.valueOf(end.getDayOfMonth());
            String lastMonth = end.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            int endYear = end.getYear();

            if (start.getMonth() == end.getMonth() && start.getYear() == end.getYear()) {
                // Same month: "1 - 5 Jan 2026"
                return firstDay + " - " + lastDay + " " + lastMonth + " " + endYear;
            } else if (start.getYear() == end.getYear()) {
                // Different months, same year: "27 Feb - 1 Mar 2026"
                return firstDay + " " + firstMonth + " - " + lastDay + " " + lastMonth + " " + endYear;
            } else {
                // Different years: "27 Dec 2025 - 1 Jan 2026"
                return firstDay + " " + firstMonth + " " + start.getYear() + " - " + lastDay + " " + lastMonth + " " + endYear;
            }
        } else {
            // Use DateTimeFormatter for full month names
            DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("d", Locale.ENGLISH);
            DateTimeFormatter dayMonthFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale.ENGLISH);
            DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH);

            if (start.getMonth() == end.getMonth() && start.getYear() == end.getYear()) {
                // Same month: "1 - 5 January 2026"
                return start.format(dayFormatter) + " - " + end.format(fullFormatter);
            } else if (start.getYear() == end.getYear()) {
                // Different months, same year: "27 February - 1 March 2026"
                return start.format(dayMonthFormatter) + " - " + end.format(fullFormatter);
            } else {
                // Different years: "27 December 2025 - 1 January 2026"
                return start.format(fullFormatter) + " - " + end.format(fullFormatter);
            }
        }
    }

    /**
     * Formats a date range with short month names (e.g., "Jan", "Feb").
     * Convenience method that calls {@link #formatDateRange(LocalDate, LocalDate, boolean)} with shortMonth=true.
     *
     * @param start Start date
     * @param end   End date
     * @return Formatted date range string with short month names
     */
    public static String formatDateRangeShort(LocalDate start, LocalDate end) {
        return formatDateRange(start, end, true);
    }

    /**
     * Formats a date range with full month names (e.g., "January", "February").
     * Convenience method that calls {@link #formatDateRange(LocalDate, LocalDate, boolean)} with shortMonth=false.
     *
     * @param start Start date
     * @param end   End date
     * @return Formatted date range string with full month names
     */
    public static String formatDateRangeFull(LocalDate start, LocalDate end) {
        return formatDateRange(start, end, false);
    }

    /**
     * Makes a region (typically a card) selectable by clicking.
     * Adds/removes the "selected" CSS class based on the selection state.
     * Also sets the cursor to HAND for better UX.
     *
     * <p>This is a common pattern used in booking forms for:</p>
     * <ul>
     *   <li>Payment option cards</li>
     *   <li>Rate type cards</li>
     *   <li>Member selection cards</li>
     *   <li>Any toggleable option</li>
     * </ul>
     *
     * @param card             The region to make selectable
     * @param selectedProperty Property to bind selection state (will be toggled on click)
     */
    public static void makeSelectable(Region card, BooleanProperty selectedProperty) {
        makeSelectable(card, selectedProperty, "selected");
    }

    /**
     * Makes a region (typically a card) selectable by clicking with a custom CSS class.
     * Adds/removes the specified CSS class based on the selection state.
     * Also sets the cursor to HAND for better UX.
     *
     * @param card             The region to make selectable
     * @param selectedProperty Property to bind selection state (will be toggled on click)
     * @param selectedClass    CSS class to add when selected (default: "selected")
     */
    public static void makeSelectable(Region card, BooleanProperty selectedProperty, String selectedClass) {
        card.setCursor(Cursor.HAND);
        card.setOnMouseClicked(e -> selectedProperty.set(!selectedProperty.get()));

        // Initial state
        if (selectedProperty.get()) {
            card.getStyleClass().add(selectedClass);
        }

        // Listen for changes
        selectedProperty.addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                if (!card.getStyleClass().contains(selectedClass)) {
                    card.getStyleClass().add(selectedClass);
                }
            } else {
                card.getStyleClass().remove(selectedClass);
            }
        });
    }

    // =============================================
    // DATE CARD PAST STATE STYLING
    // =============================================

    /**
     * CSS class for past/disabled date cards.
     */
    public static final String CSS_DATE_CARD_PAST = "bookingpage-date-card-past";

    // =============================================
    // ACCOMMODATION INFO BOX
    // =============================================

    /**
     * Creates a "Price includes" info box for accommodation sections.
     * Uses the JSX mockup style: gray background, thin border, bold "Price includes:" label,
     * followed by primary text and secondary muted text.
     *
     * <p>CSS classes used:</p>
     * <ul>
     *   <li>{@code .bookingpage-price-includes-box} - container styling</li>
     *   <li>{@code .bookingpage-price-includes-bold} - bold label styling</li>
     *   <li>{@code .bookingpage-price-includes-primary} - primary text styling</li>
     *   <li>{@code .bookingpage-price-includes-secondary} - secondary text styling</li>
     * </ul>
     *
     * @param primaryText   The main text after "Price includes:" (e.g., "All teachings, accommodation, and meals for the full event.")
     * @param secondaryText The secondary muted text in parentheses (e.g., "You can adjust dates and options like meals in the next step")
     * @return A styled HBox containing the info text
     */
    public static HBox createPriceIncludesInfoBox(String primaryText, String secondaryText) {
        HBox box = new HBox(0);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(14, 18, 14, 18));
        box.getStyleClass().add(bookingpage_price_includes_box);

        // Create a TextFlow-like layout using VBox with wrapping labels
        // Since TextFlow may have GWT compatibility issues, use labels

        // VBox to stack the text properly for wrapping
        VBox textContainer = new VBox(2);
        textContainer.setAlignment(Pos.CENTER_LEFT);

        // Bold "Price includes:" label on its own line
        Label boldLabel = new Label("Price includes:");
        boldLabel.getStyleClass().add(bookingpage_price_includes_bold);

        // Primary text on next line, will wrap naturally
        Label primaryLabel = new Label(primaryText);
        primaryLabel.getStyleClass().add(bookingpage_price_includes_primary);
        primaryLabel.setWrapText(true);
        primaryLabel.setMaxWidth(Double.MAX_VALUE);

        textContainer.getChildren().addAll(boldLabel, primaryLabel);

        // Secondary text (if provided)
        if (secondaryText != null && !secondaryText.isEmpty()) {
            Label secondaryLabel = new Label("(" + secondaryText + ")");
            secondaryLabel.getStyleClass().add(bookingpage_price_includes_secondary);
            secondaryLabel.setWrapText(true);
            secondaryLabel.setMaxWidth(Double.MAX_VALUE);
            textContainer.getChildren().add(secondaryLabel);
        }

        box.getChildren().add(textContainer);
        HBox.setHgrow(textContainer, Priority.ALWAYS);

        return box;
    }

    /**
     * Creates an amber-styled "Price includes" info box for accommodation sections.
     * Uses the FestivalRegistrationV2.jsx mockup style: amber/yellow background with
     * 4px orange left border accent, info icon, bold "Price includes:" label,
     * followed by primary text and secondary muted text.
     *
     * <p>CSS classes used:</p>
     * <ul>
     *   <li>{@code .bookingpage-price-includes-box-amber} - container styling</li>
     *   <li>{@code .bookingpage-price-includes-amber-icon} - info icon styling</li>
     *   <li>{@code .bookingpage-price-includes-amber-bold} - bold label styling</li>
     *   <li>{@code .bookingpage-price-includes-amber-primary} - primary text styling</li>
     *   <li>{@code .bookingpage-price-includes-amber-secondary} - secondary text styling</li>
     * </ul>
     *
     * @param primaryText   The main text after "Price includes:" (e.g., "All teachings, accommodation, and meals for the full event.")
     * @param secondaryText The secondary muted text (e.g., "You can adjust dates and options like meals in the next step")
     * @return A styled HBox containing the info icon and text
     */
    public static HBox createAmberPriceIncludesInfoBox(String primaryText, String secondaryText) {
        HBox box = new HBox(12);
        box.setAlignment(Pos.TOP_LEFT);
        box.setPadding(new Insets(14, 18, 14, 18));
        box.getStyleClass().add(bookingpage_price_includes_box_amber);

        // Info icon (circle with "i") - amber colored
        SVGPath infoCircle = new SVGPath();
        infoCircle.setContent("M12 2a10 10 0 100 20 10 10 0 000-20z");
        infoCircle.setScaleX(0.83);  // Scale to ~20px from 24px viewBox
        infoCircle.setScaleY(0.83);
        infoCircle.setStroke(Color.web("#D97706"));  // Amber stroke
        infoCircle.setStrokeWidth(2);
        infoCircle.setFill(Color.TRANSPARENT);

        SVGPath infoI = new SVGPath();
        infoI.setContent("M12 16v-4M12 8h.01");
        infoI.setScaleX(0.83);
        infoI.setScaleY(0.83);
        infoI.setStroke(Color.web("#D97706"));  // Amber stroke
        infoI.setStrokeWidth(2);
        infoI.setFill(Color.TRANSPARENT);

        StackPane iconContainer = new StackPane(infoCircle, infoI);
        iconContainer.setMinSize(20, 20);
        iconContainer.setMaxSize(20, 20);
        iconContainer.setAlignment(Pos.CENTER);

        // VBox to stack the text properly for wrapping
        VBox textContainer = new VBox(4);
        textContainer.setAlignment(Pos.TOP_LEFT);

        // Bold "Price includes:" followed by primary text on same conceptual line
        Label boldLabel = new Label("Price includes:");
        boldLabel.getStyleClass().add(bookingpage_price_includes_amber_bold);

        // Primary text on next line, will wrap naturally
        Label primaryLabel = new Label(primaryText);
        primaryLabel.getStyleClass().add(bookingpage_price_includes_amber_primary);
        primaryLabel.setWrapText(true);
        primaryLabel.setMaxWidth(Double.MAX_VALUE);

        textContainer.getChildren().addAll(boldLabel, primaryLabel);

        // Secondary text (if provided)
        if (secondaryText != null && !secondaryText.isEmpty()) {
            Label secondaryLabel = new Label(secondaryText);
            secondaryLabel.getStyleClass().add(bookingpage_price_includes_amber_secondary);
            secondaryLabel.setWrapText(true);
            secondaryLabel.setMaxWidth(Double.MAX_VALUE);
            VBox.setMargin(secondaryLabel, new Insets(4, 0, 0, 0));
            textContainer.getChildren().add(secondaryLabel);
        }

        box.getChildren().addAll(iconContainer, textContainer);
        HBox.setHgrow(textContainer, Priority.ALWAYS);

        return box;
    }

    // =============================================
    // NO ACCOMMODATION SEPARATOR
    // =============================================

    /** SVG path for map pin outline (24x24 viewBox) */
    public static final String ICON_MAP_PIN_OUTLINE = "M12 22s-8-4.5-8-11.8A8 8 0 0 1 12 2a8 8 0 0 1 8 8.2c0 7.3-8 11.8-8 11.8z";

    /** SVG path for X mark line 1 (inside map pin) */
    public static final String ICON_X_LINE_1 = "M9 9L15 15";

    /** SVG path for X mark line 2 (inside map pin) */
    public static final String ICON_X_LINE_2 = "M15 9L9 15";

    /**
     * Creates a "No Accommodation" separator for use between accommodation options
     * and day visitor options in the accommodation selection section.
     *
     * <p>Structure: [horizontal line] [pill with icon + text] [horizontal line]</p>
     *
     * <p>CSS classes used:</p>
     * <ul>
     *   <li>{@code .bookingpage-no-accommodation-line} - horizontal line styling</li>
     *   <li>{@code .bookingpage-no-accommodation-pill} - center pill styling</li>
     *   <li>{@code .bookingpage-no-accommodation-text} - text styling</li>
     * </ul>
     *
     * @return A styled HBox containing the separator
     */
    public static HBox createNoAccommodationSeparator() {
        HBox separator = new HBox(12);
        separator.setAlignment(Pos.CENTER);

        // Left horizontal line (2px height, gray #D1D5DB)
        Region leftLine = new Region();
        leftLine.setMinHeight(2);
        leftLine.setMaxHeight(2);
        leftLine.getStyleClass().add(bookingpage_no_accommodation_line);
        HBox.setHgrow(leftLine, Priority.ALWAYS);

        // Center pill with icon and text
        HBox pill = new HBox(10);
        pill.setAlignment(Pos.CENTER);
        pill.setPadding(new Insets(10, 20, 10, 20));
        pill.getStyleClass().add(bookingpage_no_accommodation_pill);

        // Map pin with X icon
        StackPane mapIcon = createMapPinWithXIcon();

        // "NO ACCOMMODATION" text (uppercase)
        Label text = new Label("NO ACCOMMODATION");
        text.getStyleClass().add(bookingpage_no_accommodation_text);

        pill.getChildren().addAll(mapIcon, text);

        // Right horizontal line
        Region rightLine = new Region();
        rightLine.setMinHeight(2);
        rightLine.setMaxHeight(2);
        rightLine.getStyleClass().add(bookingpage_no_accommodation_line);
        HBox.setHgrow(rightLine, Priority.ALWAYS);

        separator.getChildren().addAll(leftLine, pill, rightLine);
        return separator;
    }

    /**
     * Creates a map pin icon with an X inside, used for the "No Accommodation" separator.
     * Uses gray stroke color (#4B5563).
     *
     * @return A StackPane containing the composite icon
     */
    public static StackPane createMapPinWithXIcon() {
        // Pin outline
        SVGPath pin = new SVGPath();
        pin.setContent(ICON_MAP_PIN_OUTLINE);
        pin.setStroke(Color.web("#4B5563"));
        pin.setStrokeWidth(2.5);
        pin.setFill(Color.TRANSPARENT);
        pin.setScaleX(0.75);  // Scale to 18x18 from 24x24 viewBox
        pin.setScaleY(0.75);

        // X line 1
        SVGPath xLine1 = new SVGPath();
        xLine1.setContent(ICON_X_LINE_1);
        xLine1.setStroke(Color.web("#4B5563"));
        xLine1.setStrokeWidth(2.5);
        xLine1.setFill(Color.TRANSPARENT);
        xLine1.setScaleX(0.75);
        xLine1.setScaleY(0.75);

        // X line 2
        SVGPath xLine2 = new SVGPath();
        xLine2.setContent(ICON_X_LINE_2);
        xLine2.setStroke(Color.web("#4B5563"));
        xLine2.setStrokeWidth(2.5);
        xLine2.setFill(Color.TRANSPARENT);
        xLine2.setScaleX(0.75);
        xLine2.setScaleY(0.75);

        StackPane container = new StackPane(pin, xLine1, xLine2);
        container.setMinSize(18, 18);
        container.setMaxSize(18, 18);
        container.setAlignment(Pos.CENTER);
        return container;
    }

    /**
     * Applies past date styling to a card using pure CSS.
     * Past dates are displayed but not selectable (grayed out, no hover effects).
     *
     * <p>CSS classes used:</p>
     * <ul>
     *   <li>{@code .bookingpage-date-card-past} - grayed out, no pointer events</li>
     * </ul>
     *
     * @param card The card node to style
     * @param isPastDate Whether the card represents a past date
     */
    public static void applyPastDateStyle(Region card, boolean isPastDate) {
        if (isPastDate) {
            if (!card.getStyleClass().contains(CSS_DATE_CARD_PAST)) {
                card.getStyleClass().add(CSS_DATE_CARD_PAST);
            }
        } else {
            card.getStyleClass().remove(CSS_DATE_CARD_PAST);
        }
    }

}
