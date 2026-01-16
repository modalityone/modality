package one.modality.booking.frontoffice.bookingpage.components;

import dev.webfx.extras.i18n.controls.I18nControls;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

/**
 * A styled section header with icon, title, and themed background.
 * Uses pure CSS for theming - colors come from CSS variables that can be
 * overridden by theme classes (e.g., .theme-wisdom-blue) on a parent container.
 *
 * <p>CSS classes used:</p>
 * <ul>
 *   <li>{@code .booking-form-section-header} - main container styling</li>
 *   <li>{@code .booking-form-section-header-title} - title text styling</li>
 * </ul>
 *
 * <p>Design:</p>
 * <ul>
 *   <li>Light background (--booking-form-selected-bg)</li>
 *   <li>4px left border with primary color (--booking-form-primary)</li>
 *   <li>Icon on the left</li>
 *   <li>Title text</li>
 *   <li>8px border radius</li>
 * </ul>
 *
 * @author Bruno Salmon
 */
public class StyledSectionHeader extends HBox {

    // Common SVG icon paths
    public static final String ICON_CALENDAR = "M20 7H4a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2zM16 3v4M8 3v4M2 11h20";
    public static final String ICON_TAG = "M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z";
    public static final String ICON_PLUS_CIRCLE = "M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20zM12 8v8M8 12h8";
    public static final String ICON_USERS = "M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8zM23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75";
    public static final String ICON_CLIPBOARD = "M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2M9 2h6a1 1 0 0 1 1 1v2a1 1 0 0 1-1 1H9a1 1 0 0 1-1-1V3a1 1 0 0 1 1-1z";
    public static final String ICON_CHECK_CIRCLE = "M22 11.08V12a10 10 0 1 1-5.93-9.14M22 4L12 14.01l-3-3";
    public static final String ICON_HEADPHONES = "M3 18v-6a9 9 0 0 1 18 0v6M21 19a2 2 0 0 1-2 2h-1a2 2 0 0 1-2-2v-3a2 2 0 0 1 2-2h3zM3 19a2 2 0 0 0 2 2h1a2 2 0 0 0 2-2v-3a2 2 0 0 0-2-2H3z";
    public static final String ICON_TICKET = "M2 9a3 3 0 0 1 3-3h14a3 3 0 0 1 3 3a3 3 0 0 1-3 3a3 3 0 0 1 3 3a3 3 0 0 1-3 3H5a3 3 0 0 1-3-3a3 3 0 0 1 3-3a3 3 0 0 1-3-3z";
    public static final String ICON_CREDIT_CARD = "M2 5h20a2 2 0 012 2v10a2 2 0 01-2 2H2a2 2 0 01-2-2V7a2 2 0 012-2z M2 10h20";
    public static final String ICON_CHECKLIST = "M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2 M9 3h6v4H9V3 M9 14l2 2 4-4";
    public static final String ICON_HOME = "M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z M9 22V12h6v10";
    public static final String ICON_UTENSILS = "M3 2v7c0 1.1.9 2 2 2h2a2 2 0 0 0 2-2V2 M7 2v20 M21 15V2c-2.5 0-5 2-5 5v6c0 1.1.9 2 2 2h1 M18 22v-7";
    public static final String ICON_CAR = "M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9L18 10l-2-5c-.2-.5-.7-.8-1.3-.9H9.3c-.6.1-1.1.4-1.3.9L6 10l-2.5 1.1C2.7 11.3 2 12.1 2 13v3c0 .6.4 1 1 1h2m14 0a2 2 0 1 1-4 0 2 2 0 0 1 4 0zm-12 0a2 2 0 1 1-4 0 2 2 0 0 1 4 0z";
    public static final String ICON_PLANE = "M17.8 19.2 16 11l3.5-3.5C21 6 21.5 4 21 3c-1-.5-3 0-4.5 1.5L13 8 4.8 6.2c-.5-.1-.9.1-1.1.5l-.3.5c-.2.5-.1 1 .3 1.3L9 12l-2 3H4l-1 1 3 2 2 3 1-1v-3l3-2 3.5 5.3c.3.4.8.5 1.3.3l.5-.2c.4-.3.6-.7.5-1.2z";

    private final Label titleLabel;
    private final SVGPath iconPath;

    // Kept for API compatibility - theming is now CSS-based, so this is a no-op
    private final ObjectProperty<BookingFormColorScheme> colorScheme = new SimpleObjectProperty<>(BookingFormColorScheme.DEFAULT);

    public StyledSectionHeader(Object titleI18nKey, String svgIconPath) {
        // Icon
        iconPath = new SVGPath();
        iconPath.setContent(svgIconPath);
        iconPath.setStroke(Color.web("#64748b"));
        iconPath.setStrokeWidth(2);
        iconPath.setFill(Color.TRANSPARENT);
        iconPath.setScaleX(0.83); // Scale 24px icon to ~20px
        iconPath.setScaleY(0.83);

        // We need to use a StackPane to properly center the SVG
        StackPane iconWrapper = new StackPane(iconPath);
        iconWrapper.setMinSize(20, 20);
        iconWrapper.setMaxSize(20, 20);
        iconWrapper.setAlignment(Pos.CENTER);

        // Title - use I18nControls.newLabel for proper translation
        titleLabel = I18nControls.newLabel(titleI18nKey);
        titleLabel.getStyleClass().add("booking-form-section-header-title");

        // Layout
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(10);
        getChildren().addAll(iconWrapper, titleLabel);
        getStyleClass().add("booking-form-section-header");

        // Sizing in Java (per project conventions - CSS handles colors only)
        setPadding(new Insets(14, 18, 14, 16));
        setMinHeight(48);
        // Colors and theming are handled purely by CSS classes
    }

    /**
     * Alternative constructor without I18n key - uses plain text.
     */
    public StyledSectionHeader(String title, String svgIconPath) {
        // Icon
        iconPath = new SVGPath();
        iconPath.setContent(svgIconPath);
        iconPath.setStroke(Color.web("#64748b"));
        iconPath.setStrokeWidth(2);
        iconPath.setFill(Color.TRANSPARENT);
        iconPath.setScaleX(0.83);
        iconPath.setScaleY(0.83);

        StackPane iconWrapper = new StackPane(iconPath);
        iconWrapper.setMinSize(20, 20);
        iconWrapper.setMaxSize(20, 20);
        iconWrapper.setAlignment(Pos.CENTER);

        // Title
        titleLabel = new Label(title);
        titleLabel.getStyleClass().add("booking-form-section-header-title");

        // Layout
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(10);
        getChildren().addAll(iconWrapper, titleLabel);
        getStyleClass().add("booking-form-section-header");

        // Sizing in Java (per project conventions - CSS handles colors only)
        setPadding(new Insets(14, 18, 14, 16));
        setMinHeight(48);
        // Colors and theming are handled purely by CSS classes
    }

    // === Property accessors ===

    /**
     * @deprecated Color scheme is now handled via CSS classes on parent container.
     * Use theme classes like "theme-wisdom-blue" on a parent element instead.
     * This property is kept for API compatibility but setting it has no effect.
     */
    @Deprecated
    public ObjectProperty<BookingFormColorScheme> colorSchemeProperty() {
        return colorScheme;
    }

    /**
     * @deprecated Color scheme is now handled via CSS classes on parent container.
     * Use theme classes like "theme-wisdom-blue" on a parent element instead.
     */
    @Deprecated
    public BookingFormColorScheme getColorScheme() {
        return colorScheme.get();
    }

    /**
     * @deprecated Color scheme is now handled via CSS classes on parent container.
     * Use theme classes like "theme-wisdom-blue" on a parent element instead.
     */
    @Deprecated
    public void setColorScheme(BookingFormColorScheme scheme) {
        colorScheme.set(scheme);
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

}
