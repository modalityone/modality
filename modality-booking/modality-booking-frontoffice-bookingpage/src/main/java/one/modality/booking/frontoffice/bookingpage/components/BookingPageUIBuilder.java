package one.modality.booking.frontoffice.bookingpage.components;

import dev.webfx.extras.i18n.controls.I18nControls;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import dev.webfx.extras.webtext.HtmlText;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

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
        INFO,
        /** Transparent background, theme colored border all around */
        OUTLINE_PRIMARY
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
        rect.getStyleClass().add("booking-form-checkbox-rect");

        // Checkmark - styled via CSS
        SVGPath checkmark = new SVGPath();
        checkmark.setContent(ICON_CHECK);
        checkmark.setScaleX(0.42);
        checkmark.setScaleY(0.42);
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
        card.getStyleClass().add("bookingpage-passive-card");
        card.setPadding(new Insets(20));
        return card;
    }

    /**
     * Creates a passive card with themed background (for info boxes like "Standard Rate Applied").
     * Uses the color scheme's selected background color for a subtle themed appearance.
     *
     * @param colorScheme Color scheme for theming
     * @return A styled HBox for adding content
     */
    public static HBox createThemedPassiveCard(BookingFormColorScheme colorScheme) {
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
        card.getStyleClass().add("bookingpage-checkbox-card");

        // Checkbox indicator on left - with color scheme support
        StackPane checkbox = createColorSchemeCheckboxIndicator(selectedProperty, colorSchemeProperty);

        card.getChildren().addAll(checkbox, content);

        // Apply Java-based styling when color scheme is provided (CSS unreliable in WebFX/GWT)
        if (colorSchemeProperty != null) {
            CornerRadii radii = new CornerRadii(8);
            Runnable updateCardStyle = () -> {
                boolean selected = selectedProperty.get();
                BookingFormColorScheme scheme = colorSchemeProperty.get();
                if (scheme == null) scheme = BookingFormColorScheme.DEFAULT;

                if (selected) {
                    card.setBorder(new Border(new BorderStroke(
                        scheme.getPrimary(), BorderStrokeStyle.SOLID, radii, new BorderWidths(2))));
                } else {
                    card.setBorder(new Border(new BorderStroke(
                        Color.web("#dee2e6"), BorderStrokeStyle.SOLID, radii, new BorderWidths(2))));
                }
            };

            updateCardStyle.run();
            selectedProperty.addListener((obs, old, val) -> updateCardStyle.run());
            colorSchemeProperty.addListener((obs, old, val) -> updateCardStyle.run());

            // Click handler
            card.setOnMouseClicked(e -> selectedProperty.set(!selectedProperty.get()));
        } else {
            // Fall back to CSS-based styling
            makeSelectable(card, selectedProperty);
        }

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
        card.getStyleClass().add("bookingpage-radio-card");

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
            card.getStyleClass().add("selected");
        }
        selectedProperty.addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                if (!card.getStyleClass().contains("selected")) {
                    card.getStyleClass().add("selected");
                }
            } else {
                card.getStyleClass().remove("selected");
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
        Label arrowLabel = new Label("← "); // ←
        arrowLabel.getStyleClass().add("booking-form-back-btn-text");
        btn.setGraphic(arrowLabel);
        btn.setContentDisplay(ContentDisplay.LEFT);
        I18nControls.bindI18nProperties(btn, i18nKey);

        btn.setCursor(Cursor.HAND);
        btn.setPadding(new Insets(14, 32, 14, 32)); // Set in Java for WebFX/GWT compatibility
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

    // =============================================
    // TEXT & LABEL FACTORIES
    // =============================================

    /**
     * Creates a label with muted/secondary styling.
     * Uses CSS classes: bookingpage-text-base, bookingpage-text-muted
     *
     * @param i18nKey The i18n key for the label text
     * @return A styled Label
     */
    public static Label createMutedLabel(Object i18nKey) {
        Label label = I18nControls.newLabel(i18nKey);
        label.getStyleClass().addAll("bookingpage-text-base", "bookingpage-text-muted");
        return label;
    }

    /**
     * Creates a bullet point with HTML text (supports bold text).
     * The format is: "• prefix<b>boldText</b>suffix"
     *
     * @param prefix   Text before the bold part
     * @param boldText Text to be bold
     * @param suffix   Text after the bold part
     * @param cssClasses Additional CSS classes to add
     * @return An HtmlText with the bullet point
     */
    public static HtmlText createBulletPoint(String prefix, String boldText, String suffix, String... cssClasses) {
        HtmlText htmlText = new HtmlText();
        String html = "• " + (prefix != null ? prefix : "") +
                      (boldText != null ? "<b>" + boldText + "</b>" : "") +
                      (suffix != null ? suffix : "");
        htmlText.setText(html);
        if (cssClasses != null && cssClasses.length > 0) {
            htmlText.getStyleClass().addAll(cssClasses);
        }
        return htmlText;
    }

    /**
     * Creates an info/warning/error box for displaying messages.
     * Uses CSS classes based on the box type.
     *
     * <p>CSS classes used:</p>
     * <ul>
     *   <li>{@code .bookingpage-info-box} - base container styling</li>
     *   <li>{@code .bookingpage-info-box-success} - green styling</li>
     *   <li>{@code .bookingpage-info-box-warning} - yellow/amber styling</li>
     *   <li>{@code .bookingpage-info-box-error} - red styling</li>
     *   <li>{@code .bookingpage-info-box-info} - theme-colored styling</li>
     * </ul>
     *
     * @param message The message text to display
     * @param type    The type of info box (SUCCESS, WARNING, ERROR, INFO)
     * @return A styled HBox containing an icon and message
     */
    public static HBox createInfoBox(String message, InfoBoxType type) {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(14, 16, 14, 16));
        box.getStyleClass().add("bookingpage-info-box");

        // Icon based on type
        String iconUnicode;
        String typeClass = switch (type) {
            case SUCCESS -> {
                iconUnicode = "✓"; // ✓
                yield "bookingpage-info-box-success";
            }
            case WARNING -> {
                iconUnicode = "⚠"; // ⚠
                yield "bookingpage-info-box-warning";
            }
            case ERROR -> {
                iconUnicode = "✖"; // ✖
                yield "bookingpage-info-box-error";
            }
            case OUTLINE_PRIMARY -> {
                iconUnicode = "ℹ"; // ℹ
                yield "bookingpage-info-box-outline-primary";
            }
            default -> {
                iconUnicode = "ℹ"; // ℹ
                yield "bookingpage-info-box-info";
            }
        };

        box.getStyleClass().add(typeClass);

        Label iconLabel = new Label(iconUnicode);
        iconLabel.getStyleClass().add("bookingpage-info-box-icon");

        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("bookingpage-info-box-message");
        messageLabel.setWrapText(true);

        box.getChildren().addAll(iconLabel, messageLabel);
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
        box.getStyleClass().add("bookingpage-info-box");

        // Icon based on type
        String iconUnicode;
        String typeClass = switch (type) {
            case SUCCESS -> {
                iconUnicode = "✓"; // ✓
                yield "bookingpage-info-box-success";
            }
            case WARNING -> {
                iconUnicode = "⚠"; // ⚠
                yield "bookingpage-info-box-warning";
            }
            case ERROR -> {
                iconUnicode = "✖"; // ✖
                yield "bookingpage-info-box-error";
            }
            case OUTLINE_PRIMARY -> {
                iconUnicode = "ℹ"; // ℹ
                yield "bookingpage-info-box-outline-primary";
            }
            default -> {
                iconUnicode = "ℹ"; // ℹ
                yield "bookingpage-info-box-info";
            }
        };

        box.getStyleClass().add(typeClass);

        Label iconLabel = new Label(iconUnicode);
        iconLabel.getStyleClass().add("bookingpage-info-box-icon");

        Label messageLabel = I18nControls.newLabel(i18nKey);
        messageLabel.getStyleClass().add("bookingpage-info-box-message");
        messageLabel.setWrapText(true);

        box.getChildren().addAll(iconLabel, messageLabel);
        return box;
    }

    // =============================================
    // FORM ELEMENTS
    // =============================================

    /**
     * Creates a labeled text field with standard styling.
     * The label is placed above the text field.
     *
     * <p>CSS classes used:</p>
     * <ul>
     *   <li>{@code .bookingpage-text-sm} - label styling</li>
     *   <li>{@code .bookingpage-font-medium} - label font weight</li>
     *   <li>{@code .bookingpage-input} - text field styling</li>
     * </ul>
     *
     * @param labelI18nKey  The i18n key for the label
     * @param valueProperty Property to bind the text field value
     * @param promptText    Prompt text to show when empty (optional)
     * @return A VBox containing the label and text field
     */
    public static VBox createLabeledTextField(Object labelI18nKey, StringProperty valueProperty, String promptText) {
        VBox container = new VBox(8);

        Label label = I18nControls.newLabel(labelI18nKey);
        label.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-font-medium", "bookingpage-text-dark");

        TextField textField = new TextField();
        if (promptText != null) {
            textField.setPromptText(promptText);
        }
        if (valueProperty != null) {
            textField.textProperty().bindBidirectional(valueProperty);
        }
        textField.getStyleClass().add("bookingpage-input");
        textField.setPadding(new Insets(12, 16, 12, 16));

        container.getChildren().addAll(label, textField);
        return container;
    }

    /**
     * Result class for verification code field creation.
     * Contains both the container and the individual digit fields for further customization.
     */
    public static class VerificationCodeResult {
        private final HBox container;

        public VerificationCodeResult(HBox container, TextField[] digitFields) {
            this.container = container;
        }

        /** Returns the HBox container with all digit fields */
        public HBox getContainer() {
            return container;
        }

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

}
