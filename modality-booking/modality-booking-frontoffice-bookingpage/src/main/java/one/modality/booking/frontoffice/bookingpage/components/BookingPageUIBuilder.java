package one.modality.booking.frontoffice.bookingpage.components;

import dev.webfx.extras.i18n.controls.I18nControls;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
    public static final String ICON_USERS = "M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8zM23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75";

    // Calendar/Date icons
    public static final String ICON_CALENDAR = "M20 7H4a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2zM16 3v4M8 3v4M2 11h20";

    // Location icon
    public static final String ICON_LOCATION = "M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z M12 7a3 3 0 1 0 0 6 3 3 0 0 0 0-6z";

    // Email icon
    public static final String ICON_EMAIL = "M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z M22 6l-10 7L2 6";

    // Check icons
    public static final String ICON_CHECK = "M20 6L9 17l-5-5";
    public static final String ICON_CHECK_CIRCLE = "M22 11.08V12a10 10 0 1 1-5.93-9.14M22 4L12 14.01l-3-3";

    // Warning/Info icons
    public static final String ICON_WARNING = "M12 9v4M12 17h.01M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z";
    public static final String ICON_INFO = "M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20zM12 16v-4M12 8h.01";
    public static final String ICON_ERROR = "M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20zM15 9l-6 6M9 9l6 6";

    // Other common icons
    public static final String ICON_TAG = "M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82zM7 7h.01";
    public static final String ICON_HOME = "M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z";
    public static final String ICON_HEADPHONES = "M3 18v-6a9 9 0 0 1 18 0v6M21 19a2 2 0 0 1-2 2h-1a2 2 0 0 1-2-2v-3a2 2 0 0 1 2-2h3zM3 19a2 2 0 0 0 2 2h1a2 2 0 0 0 2-2v-3a2 2 0 0 0-2-2H3z";

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
     * Per JSX mockup: 2px border, 4-6px radius, white bg -> primary bg when selected.
     *
     * <p>Uses pure CSS for theming - colors come from CSS variables that are
     * automatically updated by theme classes (e.g., .theme-wisdom-blue) on a parent container.</p>
     *
     * @param selectedProperty Property to bind selection state
     * @param colorScheme      Color scheme (ignored - kept for API compatibility, use CSS theme classes instead)
     * @return A StackPane containing the checkbox indicator
     */
    public static StackPane createCheckboxIndicator(BooleanProperty selectedProperty, BookingFormColorScheme colorScheme) {
        return createCheckboxIndicator(selectedProperty);
    }

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
     * Per JSX mockup: 2px border, 50% radius, white inner dot (10px) when selected.
     *
     * @param selectedProperty Property to bind selection state
     * @param colorScheme      Color scheme for theming
     * @return A StackPane containing the radio indicator
     */
    public static StackPane createRadioIndicator(BooleanProperty selectedProperty, BookingFormColorScheme colorScheme) {
        double size = 20;
        double dotSize = 10;

        // Outer circle
        Circle outer = new Circle(size / 2);
        outer.setStrokeWidth(2);

        // Inner dot
        Circle inner = new Circle(dotSize / 2);
        inner.setVisible(false);

        StackPane container = new StackPane(outer, inner);
        container.setMinSize(size, size);
        container.setMaxSize(size, size);
        container.setAlignment(Pos.CENTER);
        container.getStyleClass().add("booking-form-radio-indicator");

        // Update appearance based on selection
        Runnable updateStyle = () -> {
            boolean selected = selectedProperty.get();
            if (selected) {
                outer.setFill(Color.WHITE);
                outer.setStroke(colorScheme.getPrimary());
                inner.setFill(colorScheme.getPrimary());
                inner.setVisible(true);
                container.getStyleClass().add("selected");
            } else {
                outer.setFill(Color.WHITE);
                outer.setStroke(BORDER_LIGHT);
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
     * Wraps content in a card with a checkmark badge positioned in the top-right corner.
     * Per JSX mockup: Badge is positioned INSIDE the card with positive offsets.
     *
     * <p>Badge sizes and offsets from JSX:</p>
     * <ul>
     *   <li>Large cards (packages/rooms): 32px badge, 12px offset</li>
     *   <li>Medium cards: 28px badge, 16px offset</li>
     *   <li>Small cards: 20px badge, 8px offset</li>
     * </ul>
     *
     * @param content          The card content (already laid out)
     * @param selectedProperty Property to bind visibility of the checkmark
     * @param colorScheme      Color scheme for theming
     * @param badgeSize        Badge size (20, 28, or 32)
     * @param badgeOffset      Offset from top-right corner (8, 12, or 16)
     * @return A StackPane with the content and checkmark badge
     */
    public static StackPane wrapWithCheckmarkBadge(
            Region content,
            BooleanProperty selectedProperty,
            BookingFormColorScheme colorScheme,
            double badgeSize,
            double badgeOffset) {

        // Create the checkmark badge
        StackPane badge = createCheckmarkBadge(colorScheme, badgeSize);
        badge.setVisible(selectedProperty.get());
        selectedProperty.addListener((obs, old, val) -> badge.setVisible(val));

        // Position badge in top-right corner with offset INSIDE the card
        StackPane wrapper = new StackPane(content, badge);
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        StackPane.setMargin(badge, new Insets(badgeOffset, badgeOffset, 0, 0));

        return wrapper;
    }

    /**
     * Wraps content in a card with a checkmark badge using default sizing (28px badge, 12px offset).
     *
     * @param content          The card content
     * @param selectedProperty Property to bind visibility
     * @param colorScheme      Color scheme for theming
     * @return A StackPane with the content and checkmark badge
     */
    public static StackPane wrapWithCheckmarkBadge(
            Region content,
            BooleanProperty selectedProperty,
            BookingFormColorScheme colorScheme) {
        return wrapWithCheckmarkBadge(content, selectedProperty, colorScheme, 28, 12);
    }

    // =============================================
    // SELECTABLE CARDS
    // =============================================

    /**
     * Creates a selectable card with checkmark badge (default style for package/room cards).
     *
     * @param title            Card title
     * @param description      Card description (can be null)
     * @param selectedProperty Property to bind selection state
     * @param onSelect         Action to run when card is clicked
     * @param colorScheme      Color scheme for theming
     * @return A styled VBox containing the card
     */
    public static VBox createSelectableCard(
            String title,
            String description,
            BooleanProperty selectedProperty,
            Runnable onSelect,
            BookingFormColorScheme colorScheme) {
        return createSelectableCard(title, description, selectedProperty, onSelect, colorScheme, SelectionIndicatorStyle.CHECKMARK_BADGE);
    }

    /**
     * Creates a selectable card with specified indicator style.
     *
     * @param title            Card title
     * @param description      Card description (can be null)
     * @param selectedProperty Property to bind selection state
     * @param onSelect         Action to run when card is clicked
     * @param colorScheme      Color scheme for theming
     * @param indicatorStyle   Style of selection indicator
     * @return A styled VBox containing the card
     */
    public static VBox createSelectableCard(
            String title,
            String description,
            BooleanProperty selectedProperty,
            Runnable onSelect,
            BookingFormColorScheme colorScheme,
            SelectionIndicatorStyle indicatorStyle) {

        VBox card = new VBox(8);
        card.setPadding(new Insets(20, 24, 20, 24));
        card.setCursor(Cursor.HAND);
        card.getStyleClass().addAll("booking-form-selectable-card", "bookingpage-card");

        // Title
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().addAll("bookingpage-text-lg", "bookingpage-font-semibold", "bookingpage-text-dark");

        // Description
        if (description != null && !description.isEmpty()) {
            Label descLabel = new Label(description);
            descLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-muted");
            descLabel.setWrapText(true);
            card.getChildren().addAll(titleLabel, descLabel);
        } else {
            card.getChildren().add(titleLabel);
        }

        // Selection indicator based on style
        StackPane indicator = null;
        switch (indicatorStyle) {
            case CHECKBOX:
                indicator = createCheckboxIndicator(selectedProperty, colorScheme);
                // Position checkbox on the left
                HBox contentWithCheckbox = new HBox(12);
                contentWithCheckbox.setAlignment(Pos.CENTER_LEFT);
                contentWithCheckbox.getChildren().add(0, indicator);
                contentWithCheckbox.getChildren().addAll(card.getChildren());
                card.getChildren().clear();
                card.getChildren().add(contentWithCheckbox);
                break;

            case RADIO:
                indicator = createRadioIndicator(selectedProperty, colorScheme);
                // Position radio on the left
                HBox contentWithRadio = new HBox(12);
                contentWithRadio.setAlignment(Pos.CENTER_LEFT);
                contentWithRadio.getChildren().add(0, indicator);
                contentWithRadio.getChildren().addAll(card.getChildren());
                card.getChildren().clear();
                card.getChildren().add(contentWithRadio);
                break;

            case CHECKMARK_BADGE:
                // Badge positioned in corner via StackPane (inside card per JSX mockup)
                VBox cardContent = new VBox(8);
                cardContent.getChildren().addAll(card.getChildren());
                card.getChildren().clear();

                StackPane badgeWrapper = wrapWithCheckmarkBadge(cardContent, selectedProperty, colorScheme, 28, 12);
                card.getChildren().add(badgeWrapper);
                break;
        }

        // Update card appearance based on selection
        updateSelectableCardStyle(card, selectedProperty.get(), colorScheme);
        selectedProperty.addListener((obs, old, selected) -> updateSelectableCardStyle(card, selected, colorScheme));

        // Hover effects
        card.setOnMouseEntered(e -> {
            if (!selectedProperty.get()) {
                card.setBackground(bg(colorScheme.getSelectedBg(), RADII_12));
                card.setBorder(border(colorScheme.getHoverBorder(), 2, RADII_12));
            }
        });
        card.setOnMouseExited(e -> updateSelectableCardStyle(card, selectedProperty.get(), colorScheme));

        // Click handler
        card.setOnMouseClicked(e -> {
            if (onSelect != null) {
                onSelect.run();
            }
        });

        return card;
    }

    private static void updateSelectableCardStyle(VBox card, boolean selected, BookingFormColorScheme colorScheme) {
        if (selected) {
            card.setBackground(bg(colorScheme.getSelectedBg(), RADII_12));
            card.setBorder(border(colorScheme.getPrimary(), 2, RADII_12));
            if (!card.getStyleClass().contains("selected")) {
                card.getStyleClass().add("selected");
            }
        } else {
            card.setBackground(bg(BG_WHITE, RADII_12));
            card.setBorder(border(BORDER_LIGHT, 2, RADII_12));
            card.getStyleClass().remove("selected");
        }
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

    /**
     * Creates a toggle button (small selection button for rate types, time slots).
     *
     * @param label            Button label
     * @param selectedProperty Property to bind selection state
     * @param onSelect         Action to run when button is clicked
     * @param colorScheme      Color scheme for theming
     * @return A styled HBox containing the toggle button
     */
    public static HBox createToggleButton(
            String label,
            BooleanProperty selectedProperty,
            Runnable onSelect,
            BookingFormColorScheme colorScheme) {

        HBox button = new HBox();
        button.setAlignment(Pos.CENTER);
        button.setPadding(new Insets(14, 20, 14, 20));
        button.setCursor(Cursor.HAND);
        button.getStyleClass().add("booking-form-toggle-button");

        Label textLabel = new Label(label);
        textLabel.getStyleClass().addAll("bookingpage-text-md", "bookingpage-font-semibold");
        button.getChildren().add(textLabel);

        // Update appearance
        Runnable updateStyle = () -> {
            boolean selected = selectedProperty.get();
            if (selected) {
                button.setBackground(bg(colorScheme.getPrimary(), RADII_8));
                button.setBorder(border(colorScheme.getPrimary(), 2, RADII_8));
                textLabel.setTextFill(Color.WHITE);
                button.getStyleClass().add("selected");
            } else {
                button.setBackground(bg(BG_WHITE, RADII_8));
                button.setBorder(border(BORDER_GRAY, 2, RADII_8));
                textLabel.setTextFill(TEXT_DARK);
                button.getStyleClass().remove("selected");
            }
        };

        updateStyle.run();
        selectedProperty.addListener((obs, old, val) -> updateStyle.run());

        // Hover effects
        button.setOnMouseEntered(e -> {
            if (!selectedProperty.get()) {
                button.setBackground(bg(colorScheme.getSelectedBg(), RADII_8));
                button.setBorder(border(colorScheme.getHoverBorder(), 2, RADII_8));
            }
        });
        button.setOnMouseExited(e -> updateStyle.run());

        // Click handler
        button.setOnMouseClicked(e -> {
            if (onSelect != null) {
                onSelect.run();
            }
        });

        return button;
    }

    /**
     * Creates a checkbox option row (for audio recording, create account, etc.).
     *
     * @param label            Option label
     * @param description      Description text (can be null)
     * @param selectedProperty Property to bind selection state
     * @param colorScheme      Color scheme for theming
     * @return A styled HBox containing the option
     */
    public static HBox createCheckboxOption(
            String label,
            String description,
            BooleanProperty selectedProperty,
            BookingFormColorScheme colorScheme) {

        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(16, 20, 16, 16));
        row.setCursor(Cursor.HAND);
        row.getStyleClass().add("booking-form-checkbox-option");

        // Checkbox indicator
        StackPane checkbox = createCheckboxIndicator(selectedProperty, colorScheme);

        // Text content
        VBox textBox = new VBox(4);
        Label labelText = new Label(label);
        labelText.getStyleClass().addAll("bookingpage-text-md", "bookingpage-font-semibold", "bookingpage-text-dark");
        textBox.getChildren().add(labelText);

        if (description != null && !description.isEmpty()) {
            Label descText = new Label(description);
            descText.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-muted");
            descText.setWrapText(true);
            textBox.getChildren().add(descText);
        }

        row.getChildren().addAll(checkbox, textBox);

        // Update card style based on selection
        updateOptionRowStyle(row, selectedProperty.get(), colorScheme);
        selectedProperty.addListener((obs, old, val) -> updateOptionRowStyle(row, val, colorScheme));

        // Hover effects
        row.setOnMouseEntered(e -> {
            if (!selectedProperty.get()) {
                row.setBackground(bg(colorScheme.getSelectedBg(), RADII_8));
                row.setBorder(border(colorScheme.getHoverBorder(), 2, RADII_8));
            }
        });
        row.setOnMouseExited(e -> updateOptionRowStyle(row, selectedProperty.get(), colorScheme));

        // Click toggles selection
        row.setOnMouseClicked(e -> selectedProperty.set(!selectedProperty.get()));

        return row;
    }

    /**
     * Creates a radio option row (for meals yes/no, exclusive choices).
     *
     * @param label            Option label
     * @param description      Description text (can be null)
     * @param selectedProperty Property to bind selection state
     * @param colorScheme      Color scheme for theming
     * @return A styled HBox containing the option
     */
    public static HBox createRadioOption(
            String label,
            String description,
            BooleanProperty selectedProperty,
            BookingFormColorScheme colorScheme) {

        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(16, 20, 16, 16));
        row.setCursor(Cursor.HAND);
        row.getStyleClass().add("booking-form-radio-option");

        // Radio indicator
        StackPane radio = createRadioIndicator(selectedProperty, colorScheme);

        // Text content
        VBox textBox = new VBox(4);
        Label labelText = new Label(label);
        labelText.getStyleClass().addAll("bookingpage-text-md", "bookingpage-font-semibold", "bookingpage-text-dark");
        textBox.getChildren().add(labelText);

        if (description != null && !description.isEmpty()) {
            Label descText = new Label(description);
            descText.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-muted");
            descText.setWrapText(true);
            textBox.getChildren().add(descText);
        }

        row.getChildren().addAll(radio, textBox);

        // Update row style based on selection
        updateOptionRowStyle(row, selectedProperty.get(), colorScheme);
        selectedProperty.addListener((obs, old, val) -> updateOptionRowStyle(row, val, colorScheme));

        // Hover effects
        row.setOnMouseEntered(e -> {
            if (!selectedProperty.get()) {
                row.setBackground(bg(colorScheme.getSelectedBg(), RADII_8));
                row.setBorder(border(colorScheme.getHoverBorder(), 2, RADII_8));
            }
        });
        row.setOnMouseExited(e -> updateOptionRowStyle(row, selectedProperty.get(), colorScheme));

        // Click handler (note: for radio, caller should manage exclusive selection)
        row.setOnMouseClicked(e -> selectedProperty.set(true));

        return row;
    }

    private static void updateOptionRowStyle(HBox row, boolean selected, BookingFormColorScheme colorScheme) {
        if (selected) {
            row.setBackground(bg(colorScheme.getSelectedBg(), RADII_8));
            row.setBorder(border(colorScheme.getPrimary(), 2, RADII_8));
        } else {
            row.setBackground(bg(BG_WHITE, RADII_8));
            row.setBorder(border(BORDER_LIGHT, 2, RADII_8));
        }
    }

    // =============================================
    // ICON + LABEL ROWS
    // =============================================

    /**
     * Creates a single-line icon + label row.
     *
     * @param svgPath   The SVG icon path
     * @param text      The label text
     * @param iconColor The icon stroke color
     * @return An HBox containing the icon and label
     */
    public static HBox createIconLabel(String svgPath, String text, Color iconColor) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        SVGPath icon = createIcon(svgPath, iconColor, 0.7);
        StackPane iconWrapper = wrapIcon(icon, 20);

        Label label = new Label(text);
        label.getStyleClass().addAll("bookingpage-text-md", "bookingpage-text-dark");

        row.getChildren().addAll(iconWrapper, label);
        return row;
    }

    /**
     * Creates a two-line icon + label pair (title + subtitle).
     *
     * @param svgPath       The SVG icon path
     * @param primaryText   The primary (title) text
     * @param secondaryText The secondary (subtitle) text
     * @param iconColor     The icon stroke color
     * @return An HBox containing the icon and labels
     */
    public static HBox createIconLabelPair(String svgPath, String primaryText, String secondaryText, Color iconColor) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.TOP_LEFT);

        SVGPath icon = createIcon(svgPath, iconColor, 0.7);
        StackPane iconWrapper = wrapIcon(icon, 20);

        VBox textBox = new VBox(2);
        Label primary = new Label(primaryText);
        primary.getStyleClass().addAll("bookingpage-text-md", "bookingpage-font-semibold", "bookingpage-text-dark");

        Label secondary = new Label(secondaryText);
        secondary.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-muted");

        textBox.getChildren().addAll(primary, secondary);
        row.getChildren().addAll(iconWrapper, textBox);
        return row;
    }

    /**
     * Creates an attendee info row (person icon + name + email).
     *
     * @param name        The attendee name
     * @param email       The attendee email
     * @param colorScheme Color scheme for icon color
     * @return An HBox containing the attendee information
     */
    public static HBox createAttendeeRow(String name, String email, BookingFormColorScheme colorScheme) {
        return createIconLabelPair(ICON_USER, name, email, colorScheme.getPrimary());
    }

    /**
     * Creates an event date row (calendar icon + formatted dates).
     *
     * @param startDate   Event start date
     * @param endDate     Event end date
     * @param colorScheme Color scheme for icon color
     * @return An HBox containing the date information
     */
    public static HBox createEventDateRow(LocalDate startDate, LocalDate endDate, BookingFormColorScheme colorScheme) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy");
        String dateText;
        if (startDate != null && endDate != null) {
            if (startDate.equals(endDate)) {
                dateText = startDate.format(formatter);
            } else {
                dateText = startDate.format(formatter) + " - " + endDate.format(formatter);
            }
        } else if (startDate != null) {
            dateText = startDate.format(formatter);
        } else {
            dateText = "";
        }

        return createIconLabel(ICON_CALENDAR, dateText, colorScheme.getPrimary());
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
     * Creates a static (non-interactive) card with specified padding.
     *
     * @param padding Padding for the card content
     * @return A styled VBox for adding content
     */
    public static VBox createStaticCard(Insets padding) {
        VBox card = new VBox(0);
        card.getStyleClass().add("bookingpage-card-static");
        card.setPadding(padding);
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

    /**
     * Creates a static card with icon and two-line text content.
     * Useful for summary sections showing attendee info, event info, etc.
     *
     * @param svgPath       Icon SVG path
     * @param primaryText   Primary (title) text
     * @param secondaryText Secondary (subtitle) text
     * @param iconColor     Icon color
     * @return A styled HBox with icon and text
     */
    public static HBox createStaticInfoRow(String svgPath, String primaryText, String secondaryText, Color iconColor) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.TOP_LEFT);

        SVGPath icon = createIcon(svgPath, iconColor, 0.7);
        StackPane iconWrapper = wrapIcon(icon, 20);

        VBox textBox = new VBox(2);
        Label primary = new Label(primaryText);
        primary.getStyleClass().addAll("bookingpage-text-md", "bookingpage-font-semibold", "bookingpage-text-dark");

        Label secondary = new Label(secondaryText);
        secondary.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-muted");

        textBox.getChildren().addAll(primary, secondary);
        row.getChildren().addAll(iconWrapper, textBox);
        return row;
    }

    // =============================================
    // INFO/WARNING/ERROR BOXES
    // =============================================

    /**
     * Creates an info/warning/error box with appropriate styling.
     *
     * @param message     The message to display
     * @param type        The type of info box
     * @param colorScheme Color scheme for INFO type
     * @return A styled HBox containing the message
     */
    public static HBox createInfoBox(String message, InfoBoxType type, BookingFormColorScheme colorScheme) {
        String iconPath;
        switch (type) {
            case SUCCESS:
                iconPath = ICON_CHECK_CIRCLE;
                break;
            case WARNING:
                iconPath = ICON_WARNING;
                break;
            case ERROR:
                iconPath = ICON_ERROR;
                break;
            default:
                iconPath = ICON_INFO;
        }
        return createInfoBox(iconPath, message, type, colorScheme);
    }

    /**
     * Creates an info/warning/error box with custom icon.
     *
     * @param svgPath     Custom SVG icon path
     * @param message     The message to display
     * @param type        The type of info box
     * @param colorScheme Color scheme for INFO type
     * @return A styled HBox containing the message
     */
    public static HBox createInfoBox(String svgPath, String message, InfoBoxType type, BookingFormColorScheme colorScheme) {
        HBox box = new HBox(12);
        box.setAlignment(Pos.TOP_LEFT);
        box.setPadding(new Insets(12, 16, 12, 16));

        // Determine colors based on type
        Color bgColor, borderColor, iconColor, textColor;
        switch (type) {
            case SUCCESS:
                bgColor = Color.web("#d4edda");
                borderColor = colorScheme.getPrimary();
                iconColor = SUCCESS;
                textColor = TEXT_DARK;
                box.getStyleClass().add("booking-form-info-box-success");
                // Left border style
                box.setBackground(bg(bgColor, RADII_8));
                box.setBorder(borderLeft(borderColor, 4, RADII_8));
                break;
            case WARNING:
                bgColor = WARNING_BG;
                borderColor = WARNING_BORDER;
                iconColor = WARNING_ICON;
                textColor = WARNING_TEXT;
                box.getStyleClass().addAll("bookingpage-warning-box", "booking-form-info-box-warning");
                box.setBackground(bg(bgColor, RADII_8));
                box.setBorder(border(borderColor, 1, RADII_8));
                break;
            case ERROR:
                bgColor = ERROR_BG;
                borderColor = ERROR_BORDER;
                iconColor = DANGER;
                textColor = Color.web("#842029");
                box.getStyleClass().addAll("bookingpage-error-box", "booking-form-info-box-error");
                box.setBackground(bg(bgColor, RADII_8));
                box.setBorder(border(borderColor, 1, RADII_8));
                break;
            default: // INFO
                bgColor = colorScheme.getSelectedBg();
                borderColor = colorScheme.getPrimary();
                iconColor = colorScheme.getPrimary();
                textColor = TEXT_DARK;
                box.getStyleClass().add("booking-form-info-box-info");
                box.setBackground(bg(bgColor, RADII_8));
                box.setBorder(borderLeft(borderColor, 4, RADII_8));
        }

        // Icon
        SVGPath icon = createIcon(svgPath, iconColor, 0.75);
        StackPane iconWrapper = wrapIcon(icon, 20);

        // Message
        Label messageLabel = new Label(message);
        messageLabel.setTextFill(textColor);
        messageLabel.getStyleClass().add("bookingpage-text-sm");
        messageLabel.setWrapText(true);
        HBox.setHgrow(messageLabel, Priority.ALWAYS);

        box.getChildren().addAll(iconWrapper, messageLabel);
        return box;
    }

    // =============================================
    // PRICE DISPLAY ROWS
    // =============================================

    /**
     * Creates a simple price row (label on left, amount on right).
     *
     * @param label          The label text
     * @param amount         The amount in currency units (e.g., 150.00)
     * @param currencySymbol The currency symbol (e.g., "£")
     * @return An HBox containing the price row
     */
    public static HBox createPriceRow(String label, double amount, String currencySymbol) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);

        Label labelText = new Label(label);
        labelText.getStyleClass().addAll("bookingpage-text-md", "bookingpage-text-dark");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label amountText = new Label(currencySymbol + String.format("%.2f", amount));
        amountText.getStyleClass().addAll("bookingpage-text-md", "bookingpage-font-semibold", "bookingpage-text-dark");

        row.getChildren().addAll(labelText, spacer, amountText);
        return row;
    }

    /**
     * Creates a price row with primary color for the amount.
     *
     * @param label          The label text
     * @param amount         The amount in currency units
     * @param currencySymbol The currency symbol
     * @param colorScheme    Color scheme for amount color
     * @return An HBox containing the price row
     */
    public static HBox createPriceRow(String label, double amount, String currencySymbol, BookingFormColorScheme colorScheme) {
        HBox row = createPriceRow(label, amount, currencySymbol);
        // Get the amount label (last child) and set primary color
        if (!row.getChildren().isEmpty()) {
            Label amountLabel = (Label) row.getChildren().get(row.getChildren().size() - 1);
            amountLabel.setTextFill(colorScheme.getPrimary());
        }
        return row;
    }

    /**
     * Creates a total row (bold, larger font, with top divider).
     *
     * @param label          The label text (e.g., "Total Cost")
     * @param amount         The total amount
     * @param currencySymbol The currency symbol
     * @param colorScheme    Color scheme for amount color
     * @return An HBox containing the total row
     */
    public static HBox createTotalRow(String label, double amount, String currencySymbol, BookingFormColorScheme colorScheme) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(16, 0, 0, 0));
        row.setBorder(borderTop(BORDER_GRAY, 1));

        Label labelText = new Label(label);
        labelText.getStyleClass().addAll("bookingpage-text-lg", "bookingpage-font-bold", "bookingpage-text-dark");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label amountText = new Label(currencySymbol + String.format("%.2f", amount));
        amountText.getStyleClass().addAll("bookingpage-price-medium", "bookingpage-font-bold");
        amountText.setTextFill(colorScheme.getPrimary());

        row.getChildren().addAll(labelText, spacer, amountText);
        return row;
    }

    // =============================================
    // NAVIGATION BUTTONS
    // =============================================

    /**
     * Creates a primary action button (Continue, Sign In, Submit, etc.).
     * Styled with the color scheme's primary color, with hover and disabled states.
     * This version uses a static color scheme - use the ObjectProperty version for dynamic theming.
     *
     * @param i18nKey     The i18n key for the button text
     * @param colorScheme Color scheme for theming
     * @return A styled Button
     */
    public static Button createPrimaryButton(Object i18nKey, BookingFormColorScheme colorScheme) {
        Button btn = I18nControls.newButton(i18nKey);
        applyPrimaryButtonStyle(btn, colorScheme);
        return btn;
    }

    /**
     * Creates a primary action button with dynamic color scheme support.
     * The button will automatically update when the color scheme property changes.
     *
     * @param i18nKey            The i18n key for the button text
     * @param colorSchemeProperty Property containing the color scheme (for dynamic updates)
     * @return A styled Button that reacts to color scheme changes
     */
    public static Button createPrimaryButton(Object i18nKey, ObjectProperty<BookingFormColorScheme> colorSchemeProperty) {
        Button btn = I18nControls.newButton(i18nKey);
        applyPrimaryButtonStyle(btn, colorSchemeProperty);
        return btn;
    }

    /**
     * Applies primary button styling to an existing button.
     * Useful when you need to customize the button further after creation.
     *
     * @param btn         The button to style
     * @param colorScheme Color scheme for theming
     */
    public static void applyPrimaryButtonStyle(Button btn, BookingFormColorScheme colorScheme) {
        btn.setPadding(new Insets(14, 32, 14, 32));
        btn.setCursor(Cursor.HAND);
        applyPrimaryButtonNormalStyle(btn, colorScheme);

        // Hover effects
        btn.setOnMouseEntered(e -> {
            if (!btn.isDisabled()) {
                applyPrimaryButtonHoverStyle(btn, colorScheme);
            }
        });
        btn.setOnMouseExited(e -> {
            if (!btn.isDisabled()) {
                applyPrimaryButtonNormalStyle(btn, colorScheme);
            }
        });

        // Disabled state
        btn.disabledProperty().addListener((obs, old, disabled) -> {
            if (disabled) {
                applyPrimaryButtonDisabledStyle(btn);
                btn.setCursor(Cursor.DEFAULT);
            } else {
                applyPrimaryButtonNormalStyle(btn, colorScheme);
                btn.setCursor(Cursor.HAND);
            }
        });
    }

    /**
     * Applies primary button styling with dynamic color scheme support.
     * The button will automatically update when the color scheme property changes.
     *
     * @param btn                 The button to style
     * @param colorSchemeProperty Property containing the color scheme (for dynamic updates)
     */
    public static void applyPrimaryButtonStyle(Button btn, ObjectProperty<BookingFormColorScheme> colorSchemeProperty) {
        btn.setPadding(new Insets(14, 32, 14, 32));
        btn.setCursor(Cursor.HAND);
        applyPrimaryButtonNormalStyle(btn, colorSchemeProperty.get());

        // Hover effects - use property.get() to get current value
        btn.setOnMouseEntered(e -> {
            if (!btn.isDisabled()) {
                applyPrimaryButtonHoverStyle(btn, colorSchemeProperty.get());
            }
        });
        btn.setOnMouseExited(e -> {
            if (!btn.isDisabled()) {
                applyPrimaryButtonNormalStyle(btn, colorSchemeProperty.get());
            }
        });

        // Disabled state - use property.get() to get current value
        btn.disabledProperty().addListener((obs, old, disabled) -> {
            if (disabled) {
                applyPrimaryButtonDisabledStyle(btn);
                btn.setCursor(Cursor.DEFAULT);
            } else {
                applyPrimaryButtonNormalStyle(btn, colorSchemeProperty.get());
                btn.setCursor(Cursor.HAND);
            }
        });

        // Update style when color scheme changes
        colorSchemeProperty.addListener((obs, old, newScheme) -> {
            if (!btn.isDisabled()) {
                applyPrimaryButtonNormalStyle(btn, newScheme);
            }
        });
    }

    private static void applyPrimaryButtonNormalStyle(Button btn, BookingFormColorScheme colorScheme) {
        btn.setTranslateY(0);
        btn.setBackground(bg(colorScheme.getPrimary(), RADII_8));
        btn.setTextFill(Color.WHITE);
        btn.setFont(fontSemiBold(16));
        btn.setEffect(SHADOW_BUTTON);
    }

    private static void applyPrimaryButtonHoverStyle(Button btn, BookingFormColorScheme colorScheme) {
        btn.setTranslateY(-2);
        btn.setBackground(bg(colorScheme.getPrimary(), RADII_8));
        btn.setTextFill(Color.WHITE);
        btn.setFont(fontSemiBold(16));
        btn.setEffect(SHADOW_BUTTON_HOVER);
    }

    private static void applyPrimaryButtonDisabledStyle(Button btn) {
        btn.setTranslateY(0);
        btn.setBackground(bg(BORDER_MEDIUM, RADII_8));
        btn.setTextFill(Color.WHITE);
        btn.setFont(fontSemiBold(16));
        btn.setEffect(null);
    }

    /**
     * Creates a back/secondary button with arrow prefix.
     * Styled with transparent background and subtle hover effect.
     *
     * @param i18nKey The i18n key for the button text (typically "Back")
     * @return A styled Button
     */
    public static Button createBackButton(Object i18nKey) {
        Button btn = new Button();
        // Arrow prefix as graphic
        Label arrowLabel = new Label("\u2190 "); // ←
        arrowLabel.getStyleClass().addAll("bookingpage-text-lg", "bookingpage-text-secondary");
        btn.setGraphic(arrowLabel);
        btn.setContentDisplay(ContentDisplay.LEFT);
        I18nControls.bindI18nProperties(btn, i18nKey);

        btn.setPadding(new Insets(14, 32, 14, 32));
        btn.setCursor(Cursor.HAND);
        applyBackButtonNormalStyle(btn);

        // Hover effects
        btn.setOnMouseEntered(e -> applyBackButtonHoverStyle(btn));
        btn.setOnMouseExited(e -> applyBackButtonNormalStyle(btn));

        return btn;
    }

    private static void applyBackButtonNormalStyle(Button btn) {
        btn.setBackground(Background.EMPTY);
        btn.setBorder(Border.EMPTY);
        btn.setTextFill(TEXT_SECONDARY);
        btn.setFont(fontMedium(15));
    }

    private static void applyBackButtonHoverStyle(Button btn) {
        btn.setBackground(bg(Color.web("#f8f9fa"), RADII_8));
        btn.setBorder(Border.EMPTY);
        btn.setTextFill(TEXT_DARK);
        btn.setFont(fontMedium(15));
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
