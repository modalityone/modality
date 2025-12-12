package one.modality.booking.frontoffice.bookingpage.components;

import dev.webfx.extras.i18n.controls.I18nControls;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

import static one.modality.booking.frontoffice.bookingpage.theme.BookingFormStyles.*;

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
     * Wraps an SVG icon in a StackPane for proper centering.
     *
     * @param icon The SVG icon
     * @param size The wrapper size (width and height)
     * @return A StackPane containing the centered icon
     */
    public static StackPane wrapIcon(SVGPath icon, double size) {
        StackPane wrapper = new StackPane(icon);
        wrapper.setMinSize(size, size);
        wrapper.setMaxSize(size, size);
        wrapper.setAlignment(Pos.CENTER);
        return wrapper;
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
        double size = 24;

        // Background rectangle - styled entirely via CSS
        Rectangle rect = new Rectangle(size, size);
        rect.setArcWidth(8);  // 4px radius * 2
        rect.setArcHeight(8);
        rect.getStyleClass().add("booking-form-checkbox-rect");

        // Checkmark - styled via CSS
        SVGPath checkmark = new SVGPath();
        checkmark.setContent(ICON_CHECK);
        checkmark.setScaleX(0.5);
        checkmark.setScaleY(0.5);
        checkmark.setVisible(false);
        checkmark.getStyleClass().add("booking-form-checkbox-checkmark");

        StackPane container = new StackPane(rect, checkmark);
        container.setMinSize(size, size);
        container.setMaxSize(size, size);
        container.setAlignment(Pos.CENTER);
        container.getStyleClass().add("booking-form-checkbox-indicator");

        // Update CSS classes based on selection (colors handled by CSS)
        Runnable updateStyle = () -> {
            boolean selected = selectedProperty.get();
            if (selected) {
                rect.getStyleClass().add("selected");
                container.getStyleClass().add("selected");
                checkmark.setVisible(true);
            } else {
                rect.getStyleClass().remove("selected");
                container.getStyleClass().remove("selected");
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
        outer.getStyleClass().add("booking-form-radio-outer");

        // Inner dot - styled via CSS
        Circle inner = new Circle(dotSize / 2);
        inner.setVisible(false);
        inner.getStyleClass().add("booking-form-radio-inner");

        StackPane container = new StackPane(outer, inner);
        container.setMinSize(size, size);
        container.setMaxSize(size, size);
        container.setAlignment(Pos.CENTER);
        container.getStyleClass().add("booking-form-radio-indicator");

        // Update CSS classes based on selection (colors handled by CSS)
        Runnable updateStyle = () -> {
            boolean selected = selectedProperty.get();
            if (selected) {
                outer.getStyleClass().add("selected");
                inner.getStyleClass().add("selected");
                inner.setVisible(true);
                container.getStyleClass().add("selected");
            } else {
                outer.getStyleClass().remove("selected");
                inner.getStyleClass().remove("selected");
                inner.setVisible(false);
                container.getStyleClass().remove("selected");
            }
        };

        updateStyle.run();
        selectedProperty.addListener((obs, old, val) -> updateStyle.run());

        return container;
    }

    /**
     * Creates a checkmark badge (circular, positioned in corner).
     * Per JSX mockup: absolute positioned top-right, colored bg, white checkmark.
     *
     * @param colorScheme Color scheme for theming
     * @param size        Badge size (24, 28, or 32px)
     * @return A StackPane containing the checkmark badge
     */
    public static StackPane createCheckmarkBadge(BookingFormColorScheme colorScheme, double size) {
        // Circular background
        Circle circle = new Circle(size / 2);
        circle.setFill(colorScheme.getPrimary());

        // Checkmark
        SVGPath checkmark = new SVGPath();
        checkmark.setContent(ICON_CHECK);
        checkmark.setStroke(Color.WHITE);
        checkmark.setStrokeWidth(2.5);
        checkmark.setFill(Color.TRANSPARENT);
        checkmark.setScaleX(size / 48);  // Scale based on size
        checkmark.setScaleY(size / 48);

        StackPane badge = new StackPane(circle, checkmark);
        badge.setMinSize(size, size);
        badge.setMaxSize(size, size);
        badge.setAlignment(Pos.CENTER);
        badge.getStyleClass().add("booking-form-checkmark-badge");

        return badge;
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
        circle.getStyleClass().add("booking-form-checkmark-circle");

        // Checkmark - styled via CSS
        SVGPath checkmark = new SVGPath();
        checkmark.setContent(ICON_CHECK);
        checkmark.setScaleX(size / 48);  // Scale based on size
        checkmark.setScaleY(size / 48);
        checkmark.getStyleClass().add("booking-form-checkmark-icon");

        StackPane badge = new StackPane(circle, checkmark);
        badge.setMinSize(size, size);
        badge.setMaxSize(size, size);
        badge.setAlignment(Pos.CENTER);
        badge.getStyleClass().add("booking-form-checkmark-badge");

        return badge;
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
        card.getStyleClass().add("bookingpage-card");

        // Title - CSS handles font and color
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-font-semibold", "bookingpage-text-dark");

        // Amount - CSS handles font size and primary color
        Label amountLabel = new Label(amount);
        amountLabel.getStyleClass().addAll("bookingpage-price-medium", "bookingpage-text-primary");

        // Description - CSS handles styling
        Label descLabel = new Label(description);
        descLabel.getStyleClass().addAll("bookingpage-label-small", "bookingpage-text-center");
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
            card.getStyleClass().add("selected");
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
    // STATIC/INFORMATIVE CARDS
    // =============================================

    /**
     * Creates a static (non-interactive) card for displaying information.
     * No hover effects, no cursor pointer - purely informative display.
     *
     * <p>Uses CSS class {@code .bookingpage-card-static}</p>
     *
     * @return A styled VBox for adding content
     */
    public static VBox createStaticCard() {
        VBox card = new VBox(0);
        card.getStyleClass().add("bookingpage-card-static");
        card.setPadding(new Insets(20));
        return card;
    }

    /**
     * Creates a static card with themed background (for info boxes like "Standard Rate Applied").
     * Uses the color scheme's selected background color for a subtle themed appearance.
     *
     * @param colorScheme Color scheme for theming
     * @return A styled HBox for adding content
     */
    public static HBox createThemedInfoCard(BookingFormColorScheme colorScheme) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(14, 16, 14, 16));
        Color bgColor = colorScheme.getSelectedBg() != null ? colorScheme.getSelectedBg() : BG_WHITE;
        Color borderColor = colorScheme.getHoverBorder() != null ? colorScheme.getHoverBorder() : BORDER_LIGHT;
        card.setBackground(bg(bgColor, RADII_8));
        card.setBorder(border(borderColor, 1, RADII_8));
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
        btn.getStyleClass().addAll("booking-form-primary-btn", "booking-form-primary-btn-text");

        // Disabled state - update CSS class and cursor
        btn.disabledProperty().addListener((obs, old, disabled) -> {
            if (disabled) {
                if (!btn.getStyleClass().contains("disabled")) {
                    btn.getStyleClass().add("disabled");
                }
                btn.setCursor(Cursor.DEFAULT);
            } else {
                btn.getStyleClass().remove("disabled");
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
        Label arrowLabel = new Label("\u2190 "); // ←
        arrowLabel.getStyleClass().add("booking-form-back-btn-text");
        btn.setGraphic(arrowLabel);
        btn.setContentDisplay(ContentDisplay.LEFT);
        I18nControls.bindI18nProperties(btn, i18nKey);

        btn.setCursor(Cursor.HAND);
        btn.getStyleClass().addAll("booking-form-back-btn", "booking-form-back-btn-text");

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
}
