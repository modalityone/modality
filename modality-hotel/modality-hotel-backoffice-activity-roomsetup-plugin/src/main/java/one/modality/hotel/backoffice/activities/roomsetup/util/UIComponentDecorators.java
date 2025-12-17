package one.modality.hotel.backoffice.activities.roomsetup.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

/**
 * Utility class providing styled UI components for the RoomSetup module.
 * Centralizes styling to ensure consistency and make theme changes easier.
 *
 * <p>This class follows the Decorator pattern to add consistent styling
 * to JavaFX components. All components use CSS classes with the "roomsetup-"
 * prefix to prevent conflicts with other modules when CSS is merged.
 *
 * <p>CSS classes are defined in:
 * <ul>
 *   <li>modality-hotel-backoffice-activity-roomsetup-javafx@main.css</li>
 *   <li>modality-hotel-backoffice-activity-roomsetup-web@main.css</li>
 * </ul>
 *
 * @author Claude Code
 */
public final class UIComponentDecorators {

    // CSS class names (all prefixed with "roomsetup-" for module namespacing)
    public static final String CSS_TITLE = "roomsetup-title";
    public static final String CSS_SUBTITLE = "roomsetup-subtitle";
    public static final String CSS_CAPTION = "roomsetup-caption";
    public static final String CSS_BODY = "roomsetup-body";
    public static final String CSS_BODY_BOLD = "roomsetup-body-bold";
    public static final String CSS_SMALL = "roomsetup-small";
    public static final String CSS_HINT = "roomsetup-hint";
    public static final String CSS_BADGE_WARNING = "roomsetup-badge-warning";
    public static final String CSS_BADGE_SUCCESS = "roomsetup-badge-success";
    public static final String CSS_BADGE_INFO = "roomsetup-badge-info";
    public static final String CSS_BADGE_SPLIT = "roomsetup-badge-split";
    public static final String CSS_TEXT_FIELD = "roomsetup-text-field";
    public static final String CSS_TEXT_AREA = "roomsetup-text-area";
    public static final String CSS_FIELD_LABEL = "roomsetup-field-label";
    public static final String CSS_CARD = "roomsetup-card";
    public static final String CSS_CARD_HEADER = "roomsetup-card-header";
    public static final String CSS_DIALOG_HEADER = "roomsetup-dialog-header";
    public static final String CSS_DIALOG_CONTAINER = "roomsetup-dialog-container";
    public static final String CSS_CELL = "roomsetup-cell";
    public static final String CSS_CLICKABLE = "roomsetup-clickable";
    public static final String CSS_FILTER_CHIP = "roomsetup-filter-chip";
    public static final String CSS_FILTER_CHIP_ACTIVE = "roomsetup-filter-chip-active";
    public static final String CSS_GROUP_HEADER = "roomsetup-group-header";
    public static final String CSS_GROUP_TITLE = "roomsetup-group-title";
    public static final String CSS_GROUP_COUNT = "roomsetup-group-count";
    public static final String CSS_ROOM_ROW = "roomsetup-room-row";
    public static final String CSS_ROOM_NAME = "roomsetup-room-name";
    public static final String CSS_ROOM_TYPE = "roomsetup-room-type";
    public static final String CSS_ROOM_CAPACITY = "roomsetup-room-capacity";
    public static final String CSS_BUILDING_HEADER = "roomsetup-building-header";
    public static final String CSS_BUILDING_NAME = "roomsetup-building-name";
    public static final String CSS_ZONE_ROW = "roomsetup-zone-row";
    public static final String CSS_ZONE_NAME = "roomsetup-zone-name";
    public static final String CSS_POOL_CARD = "roomsetup-pool-card";
    public static final String CSS_POOL_NAME = "roomsetup-pool-name";
    public static final String CSS_POOL_BADGE_SOURCE = "roomsetup-pool-badge-source";
    public static final String CSS_POOL_BADGE_CATEGORY = "roomsetup-pool-badge-category";
    public static final String CSS_ALLOCATION_ROOM = "roomsetup-allocation-room";
    public static final String CSS_ALLOCATION_POOL_TAG = "roomsetup-allocation-pool-tag";
    public static final String CSS_ALLOCATION_UNASSIGNED = "roomsetup-allocation-unassigned";
    public static final String CSS_TOOLBAR = "roomsetup-toolbar";
    public static final String CSS_ACTION_BUTTON = "roomsetup-action-button";
    public static final String CSS_EMPTY_STATE = "roomsetup-empty-state";
    public static final String CSS_EMPTY_STATE_TEXT = "roomsetup-empty-state-text";
    public static final String CSS_INFO_BAR = "roomsetup-info-bar";
    public static final String CSS_INFO_BAR_TEXT = "roomsetup-info-bar-text";
    public static final String CSS_INFO_BAR_LINK = "roomsetup-info-bar-link";
    public static final String CSS_WARNING_BAR = "roomsetup-warning-bar";
    public static final String CSS_WARNING_BAR_TEXT = "roomsetup-warning-bar-text";
    public static final String CSS_EXPAND_ARROW = "roomsetup-expand-arrow";
    public static final String CSS_EXPAND_ARROW_EXPANDED = "roomsetup-expand-arrow-expanded";

    // Site comparison CSS classes
    public static final String CSS_COMPARISON_COLUMN_BOTH = "roomsetup-comparison-column-both";
    public static final String CSS_COMPARISON_COLUMN_GLOBAL = "roomsetup-comparison-column-global";
    public static final String CSS_COMPARISON_COLUMN_EVENT = "roomsetup-comparison-column-event";
    public static final String CSS_COMPARISON_TEXT_BOTH = "roomsetup-comparison-text-both";
    public static final String CSS_COMPARISON_TEXT_GLOBAL = "roomsetup-comparison-text-global";
    public static final String CSS_COMPARISON_TEXT_EVENT = "roomsetup-comparison-text-event";
    public static final String CSS_COMPARISON_RESOURCE_LINKED = "roomsetup-comparison-resource-linked";
    public static final String CSS_COMPARISON_RESOURCE_SELECTED = "roomsetup-comparison-resource-selected";
    public static final String CSS_COMPARISON_WARNING_BOX = "roomsetup-comparison-warning-box";
    public static final String CSS_COMPARISON_WARNING_ICON = "roomsetup-comparison-warning-icon";
    public static final String CSS_COMPARISON_WARNING_TEXT = "roomsetup-comparison-warning-text";
    public static final String CSS_COMPARISON_LINK_BADGE = "roomsetup-comparison-link-badge";
    public static final String CSS_COMPARISON_UNLINK_BTN = "roomsetup-comparison-unlink-btn";

    // Color picker CSS classes
    public static final String CSS_COLOR_CELL = "roomsetup-color-cell";
    public static final String CSS_COLOR_CELL_SELECTED = "roomsetup-color-cell-selected";
    public static final String CSS_COLOR_AMBER = "roomsetup-color-amber";
    public static final String CSS_COLOR_RED = "roomsetup-color-red";
    public static final String CSS_COLOR_PINK = "roomsetup-color-pink";
    public static final String CSS_COLOR_PURPLE = "roomsetup-color-purple";
    public static final String CSS_COLOR_BLUE = "roomsetup-color-blue";
    public static final String CSS_COLOR_CYAN = "roomsetup-color-cyan";
    public static final String CSS_COLOR_GREEN = "roomsetup-color-green";
    public static final String CSS_COLOR_SLATE = "roomsetup-color-slate";
    // Color cell classes for selection styling (used with CSS_COLOR_CELL_SELECTED)
    public static final String CSS_COLOR_CELL_AMBER = "roomsetup-color-cell-amber";
    public static final String CSS_COLOR_CELL_RED = "roomsetup-color-cell-red";
    public static final String CSS_COLOR_CELL_PINK = "roomsetup-color-cell-pink";
    public static final String CSS_COLOR_CELL_PURPLE = "roomsetup-color-cell-purple";
    public static final String CSS_COLOR_CELL_BLUE = "roomsetup-color-cell-blue";
    public static final String CSS_COLOR_CELL_CYAN = "roomsetup-color-cell-cyan";
    public static final String CSS_COLOR_CELL_GREEN = "roomsetup-color-cell-green";
    public static final String CSS_COLOR_CELL_SLATE = "roomsetup-color-cell-slate";

    // Gender toggle button CSS classes
    public static final String CSS_GENDER_TOGGLE = "roomsetup-gender-toggle";
    public static final String CSS_GENDER_TOGGLE_SELECTED = "roomsetup-gender-toggle-selected";

    private UIComponentDecorators() {
        // Utility class - prevent instantiation
    }

    // ============== Typography ==============

    /**
     * Creates a large title label (20px, bold).
     */
    public static Label titleLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add(CSS_TITLE);
        return label;
    }

    /**
     * Creates a subtitle label (14px, secondary color).
     */
    public static Label subtitleLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add(CSS_SUBTITLE);
        return label;
    }

    /**
     * Creates a section header/caption label (12px, bold, uppercase style).
     */
    public static Label captionLabel(String text) {
        Label label = new Label(text.toUpperCase());
        label.getStyleClass().add(CSS_CAPTION);
        return label;
    }

    /**
     * Creates a body text label (13px, secondary color).
     */
    public static Label bodyLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add(CSS_BODY);
        return label;
    }

    /**
     * Creates a bold body text label (primary color).
     */
    public static Label boldBodyLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add(CSS_BODY_BOLD);
        return label;
    }

    /**
     * Creates a small text label (11px).
     */
    public static Label smallLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add(CSS_SMALL);
        return label;
    }

    // ============== Badges ==============

    /**
     * Creates a warning badge (yellow background, brown text).
     */
    public static Label warningBadge(String text) {
        Label label = new Label(text);
        label.getStyleClass().add(CSS_BADGE_WARNING);
        label.setPadding(new Insets(4, 8, 4, 8));
        return label;
    }

    /**
     * Creates a success badge (green background).
     */
    public static Label successBadge(String text) {
        Label label = new Label(text);
        label.getStyleClass().add(CSS_BADGE_SUCCESS);
        label.setPadding(new Insets(4, 10, 4, 10));
        return label;
    }

    /**
     * Creates an info badge (blue background).
     */
    public static Label infoBadge(String text) {
        Label label = new Label(text);
        label.getStyleClass().add(CSS_BADGE_INFO);
        label.setPadding(new Insets(4, 10, 4, 10));
        return label;
    }

    /**
     * Creates a split/multi badge (purple background).
     */
    public static Label splitBadge(String text) {
        Label label = new Label(text);
        label.getStyleClass().add(CSS_BADGE_SPLIT);
        label.setPadding(new Insets(4, 8, 4, 8));
        return label;
    }

    /**
     * Creates a count badge (light info style).
     */
    public static Label countBadge(int count, String suffix) {
        return infoBadge(count + " " + suffix);
    }

    // ============== Form Fields ==============

    /**
     * Styles a text field with standard form styling.
     */
    public static TextField styledTextField(String promptText) {
        TextField field = new TextField();
        field.setPromptText(promptText);
        field.getStyleClass().add(CSS_TEXT_FIELD);
        field.setPadding(new Insets(12, 14, 12, 14));
        return field;
    }

    /**
     * Styles a text area with standard form styling.
     */
    public static TextArea styledTextArea(String promptText, int prefRowCount) {
        TextArea area = new TextArea();
        area.setPromptText(promptText);
        area.setPrefRowCount(prefRowCount);
        area.getStyleClass().add(CSS_TEXT_AREA);
        area.setPadding(new Insets(12, 14, 12, 14));
        return area;
    }

    /**
     * Creates a form field label (caption style).
     */
    public static Label fieldLabel(String text) {
        Label label = new Label(text.toUpperCase());
        label.getStyleClass().add(CSS_FIELD_LABEL);
        return label;
    }

    // ============== Containers ==============

    /**
     * Creates a card panel with white background and border.
     */
    public static VBox cardPanel() {
        VBox panel = new VBox();
        panel.getStyleClass().add(CSS_CARD);
        return panel;
    }

    /**
     * Creates a card header with light gray background.
     */
    public static HBox cardHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 20, 14, 20));
        header.getStyleClass().add(CSS_CARD_HEADER);
        return header;
    }

    /**
     * Creates a dialog header with bottom border.
     */
    public static HBox dialogHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(12);
        header.setPadding(new Insets(0, 0, 16, 0));
        header.getStyleClass().add(CSS_DIALOG_HEADER);
        return header;
    }

    /**
     * Creates a dialog container with standard padding.
     */
    public static VBox dialogContainer(double minWidth) {
        VBox container = new VBox();
        container.setSpacing(20);
        container.setPadding(new Insets(24));
        container.setMinWidth(minWidth);
        container.getStyleClass().add(CSS_DIALOG_CONTAINER);
        return container;
    }

    // ============== Styling Helpers ==============

    /**
     * Returns the standard cell CSS class.
     */
    public static String getCellClass() {
        return CSS_CELL;
    }

    /**
     * Returns the clickable CSS class.
     */
    public static String getClickableClass() {
        return CSS_CLICKABLE;
    }

    /**
     * Adds clickable cursor style to a node.
     */
    public static void makeClickable(javafx.scene.Node node) {
        node.getStyleClass().add(CSS_CLICKABLE);
    }

    // ============== Color Utilities ==============

    /**
     * Converts a hex color to rgba format with specified opacity.
     * This is needed because hex alpha notation (#RRGGBBAA) doesn't work in GWT/web CSS.
     *
     * @param hexColor The hex color (e.g., "#d97706" or "d97706")
     * @param opacity  The opacity value (0.0 to 1.0)
     * @return An rgba() string (e.g., "rgba(217, 119, 6, 0.13)")
     */
    public static String hexToRgba(String hexColor, double opacity) {
        if (hexColor == null || hexColor.isEmpty()) {
            return "rgba(0, 0, 0, " + opacity + ")";
        }
        String hex = hexColor.startsWith("#") ? hexColor.substring(1) : hexColor;
        if (hex.length() < 6) {
            return "rgba(0, 0, 0, " + opacity + ")";
        }
        try {
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            return "rgba(" + r + ", " + g + ", " + b + ", " + opacity + ")";
        } catch (NumberFormatException e) {
            return "rgba(0, 0, 0, " + opacity + ")";
        }
    }

    /**
     * Creates a style string for a selected icon cell with the given color.
     * Note: Uses -fx- properties (JavaFX style) which work on desktop. For web,
     * the rgba() format ensures transparency works correctly.
     */
    public static String getIconCellSelectedStyle(String color) {
        return "-fx-background-color: " + hexToRgba(color, 0.13) + "; " +
               "-fx-background-radius: 8; " +
               "-fx-border-color: " + color + "; " +
               "-fx-border-width: 2; " +
               "-fx-border-radius: 8; " +
               "-fx-cursor: hand;";
    }

    /**
     * Creates a style string for an unselected icon cell.
     */
    public static String getIconCellUnselectedStyle() {
        return "-fx-background-color: #f5f5f4; -fx-background-radius: 8; -fx-cursor: hand;";
    }

    /**
     * Creates a style string for a selected color cell with the given color.
     */
    public static String getColorCellSelectedStyle(String color) {
        return "-fx-background-color: " + hexToRgba(color, 0.13) + "; " +
               "-fx-background-radius: 10; " +
               "-fx-border-color: " + color + "; " +
               "-fx-border-width: 2; " +
               "-fx-border-radius: 10; " +
               "-fx-cursor: hand;";
    }

    /**
     * Creates a style string for an unselected color cell.
     */
    public static String getColorCellUnselectedStyle() {
        return "-fx-background-color: transparent; -fx-background-radius: 10; -fx-cursor: hand;";
    }

    /**
     * Creates a style string for the icon preview pane with the given color.
     */
    public static String getIconPreviewStyle(String color) {
        return "-fx-background-color: " + hexToRgba(color, 0.13) + "; " +
               "-fx-background-radius: 12; " +
               "-fx-border-color: " + hexToRgba(color, 0.4) + "; " +
               "-fx-border-radius: 12;";
    }

    /**
     * Creates a style string for a pool allocation row with the given color.
     * @param color The pool color
     * @param isActive Whether the row is selected/active
     */
    public static String getPoolRowStyle(String color, boolean isActive) {
        if (isActive) {
            return "-fx-background-color: " + hexToRgba(color, 0.08) + "; " +
                   "-fx-background-radius: 10; " +
                   "-fx-border-color: " + color + "; " +
                   "-fx-border-width: 2; " +
                   "-fx-border-radius: 10; -fx-cursor: hand;";
        } else {
            return "-fx-background-color: white; " +
                   "-fx-background-radius: 10; " +
                   "-fx-border-color: #e5e5e5; " +
                   "-fx-border-width: 1; " +
                   "-fx-border-radius: 10; -fx-cursor: hand;";
        }
    }

    /**
     * Creates a style string for pool icon background.
     */
    public static String getPoolIconStyle(String color) {
        return "-fx-background-color: " + hexToRgba(color, 0.08) + "; -fx-background-radius: 10;";
    }

    /**
     * Creates a style string for a pool filter chip button.
     * @param color The pool color
     * @param isSelected Whether the filter is currently selected
     */
    public static String getPoolFilterChipStyle(String color, boolean isSelected) {
        if (isSelected) {
            return "-fx-background-color: " + hexToRgba(color, 0.13) + "; " +
                   "-fx-text-fill: " + color + "; " +
                   "-fx-padding: 8 14; -fx-background-radius: 8; " +
                   "-fx-border-color: " + color + "; -fx-border-width: 2; " +
                   "-fx-border-radius: 8; -fx-cursor: hand; -fx-font-weight: 600;";
        } else {
            return "-fx-background-color: white; " +
                   "-fx-text-fill: " + color + "; " +
                   "-fx-padding: 8 14; -fx-background-radius: 8; " +
                   "-fx-border-color: #e5e5e5; -fx-border-radius: 8; -fx-cursor: hand;";
        }
    }

    // ============== WebFX-Compatible Styling Methods ==============
    // These methods apply styles using Background/Border objects instead of setStyle()
    // which is required for WebFX GWT compilation to work correctly.

    /**
     * Applies selected icon cell styling using Background/Border objects (WebFX compatible).
     */
    public static void applyIconCellSelectedStyle(Region region, String color) {
        Color baseColor = Color.web(color);
        Color bgColor = Color.color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 0.13);
        region.setBackground(new Background(new BackgroundFill(bgColor, new CornerRadii(8), null)));
        region.setBorder(new Border(new BorderStroke(baseColor, BorderStrokeStyle.SOLID, new CornerRadii(8), new BorderWidths(2))));
        region.setCursor(Cursor.HAND);
    }

    /**
     * Applies unselected icon cell styling using Background/Border objects (WebFX compatible).
     */
    public static void applyIconCellUnselectedStyle(Region region) {
        region.setBackground(new Background(new BackgroundFill(Color.web("#f5f5f4"), new CornerRadii(8), null)));
        region.setBorder(null);
        region.setCursor(Cursor.HAND);
    }

    /**
     * Gets the CSS class for a color swatch based on hex color.
     * @param hex The hex color code (e.g., "#d97706")
     * @return The CSS class name for the color swatch
     */
    public static String getColorSwatchClass(String hex) {
        switch (hex.toLowerCase()) {
            case "#d97706": return CSS_COLOR_AMBER;
            case "#dc2626": return CSS_COLOR_RED;
            case "#db2777": return CSS_COLOR_PINK;
            case "#7c3aed": return CSS_COLOR_PURPLE;
            case "#2563eb": return CSS_COLOR_BLUE;
            case "#0891b2": return CSS_COLOR_CYAN;
            case "#059669": return CSS_COLOR_GREEN;
            case "#475569": return CSS_COLOR_SLATE;
            default: return CSS_COLOR_AMBER; // Default fallback
        }
    }

    /**
     * Gets the CSS class for a color cell container based on hex color.
     * @param hex The hex color code (e.g., "#d97706")
     * @return The CSS class name for the color cell container
     */
    public static String getColorCellClass(String hex) {
        switch (hex.toLowerCase()) {
            case "#d97706": return CSS_COLOR_CELL_AMBER;
            case "#dc2626": return CSS_COLOR_CELL_RED;
            case "#db2777": return CSS_COLOR_CELL_PINK;
            case "#7c3aed": return CSS_COLOR_CELL_PURPLE;
            case "#2563eb": return CSS_COLOR_CELL_BLUE;
            case "#0891b2": return CSS_COLOR_CELL_CYAN;
            case "#059669": return CSS_COLOR_CELL_GREEN;
            case "#475569": return CSS_COLOR_CELL_SLATE;
            default: return CSS_COLOR_CELL_AMBER; // Default fallback
        }
    }

    /**
     * Applies selected color cell styling using CSS classes (WebFX compatible).
     * @param region The region to style
     * @param hex The hex color for selection styling
     */
    public static void applyColorCellSelectedStyle(Region region, String hex) {
        region.getStyleClass().remove(CSS_COLOR_CELL);
        if (!region.getStyleClass().contains(CSS_COLOR_CELL_SELECTED)) {
            region.getStyleClass().add(CSS_COLOR_CELL_SELECTED);
        }
        // Add the color-specific cell class for border/background styling
        String cellClass = getColorCellClass(hex);
        if (!region.getStyleClass().contains(cellClass)) {
            region.getStyleClass().add(cellClass);
        }
        region.setCursor(Cursor.HAND);
    }

    /**
     * Applies unselected color cell styling using CSS classes (WebFX compatible).
     * @param region The region to style
     */
    public static void applyColorCellUnselectedStyle(Region region) {
        region.getStyleClass().remove(CSS_COLOR_CELL_SELECTED);
        // Remove all color cell classes
        region.getStyleClass().removeAll(
            CSS_COLOR_CELL_AMBER, CSS_COLOR_CELL_RED, CSS_COLOR_CELL_PINK,
            CSS_COLOR_CELL_PURPLE, CSS_COLOR_CELL_BLUE, CSS_COLOR_CELL_CYAN,
            CSS_COLOR_CELL_GREEN, CSS_COLOR_CELL_SLATE
        );
        if (!region.getStyleClass().contains(CSS_COLOR_CELL)) {
            region.getStyleClass().add(CSS_COLOR_CELL);
        }
        region.setCursor(Cursor.HAND);
    }

    /**
     * Applies icon preview pane styling using Background/Border objects (WebFX compatible).
     */
    public static void applyIconPreviewStyle(Region region, String color) {
        Color baseColor = Color.web(color);
        Color bgColor = Color.color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 0.13);
        Color borderColor = Color.color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 0.4);
        region.setBackground(new Background(new BackgroundFill(bgColor, new CornerRadii(12), null)));
        region.setBorder(new Border(new BorderStroke(borderColor, BorderStrokeStyle.SOLID, new CornerRadii(12), new BorderWidths(1))));
    }

    /**
     * Applies pool row styling using Background/Border objects (WebFX compatible).
     */
    public static void applyPoolRowStyle(Region region, String color, boolean isActive) {
        if (isActive) {
            Color baseColor = Color.web(color);
            Color bgColor = Color.color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 0.08);
            region.setBackground(new Background(new BackgroundFill(bgColor, new CornerRadii(10), null)));
            region.setBorder(new Border(new BorderStroke(baseColor, BorderStrokeStyle.SOLID, new CornerRadii(10), new BorderWidths(2))));
        } else {
            region.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(10), null)));
            region.setBorder(new Border(new BorderStroke(Color.web("#e5e5e5"), BorderStrokeStyle.SOLID, new CornerRadii(10), new BorderWidths(1))));
        }
        region.setCursor(Cursor.HAND);
    }

    /**
     * Applies pool icon background styling using Background/Border objects (WebFX compatible).
     */
    public static void applyPoolIconStyle(Region region, String color) {
        Color baseColor = Color.web(color);
        Color bgColor = Color.color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 0.08);
        region.setBackground(new Background(new BackgroundFill(bgColor, new CornerRadii(10), null)));
    }

    /**
     * Applies a simple background color with corner radius (WebFX compatible).
     */
    public static void applyBackground(Region region, String hexColor, double cornerRadius) {
        region.setBackground(new Background(new BackgroundFill(Color.web(hexColor), new CornerRadii(cornerRadius), null)));
    }

    /**
     * Applies a background color with corner radius and border (WebFX compatible).
     */
    public static void applyBackgroundWithBorder(Region region, String bgHexColor, double cornerRadius, String borderHexColor, double borderWidth) {
        region.setBackground(new Background(new BackgroundFill(Color.web(bgHexColor), new CornerRadii(cornerRadius), null)));
        region.setBorder(new Border(new BorderStroke(Color.web(borderHexColor), BorderStrokeStyle.SOLID, new CornerRadii(cornerRadius), new BorderWidths(borderWidth))));
    }

    /**
     * Applies pool filter chip styling using Background/Border objects (WebFX compatible).
     * This replaces setStyle() calls which don't work in WebFX GWT compilation.
     *
     * @param region The button/region to style
     * @param color The pool color (hex string)
     * @param isSelected Whether the chip is currently selected
     */
    public static void applyPoolFilterChipStyle(Region region, String color, boolean isSelected) {
        Color baseColor = Color.web(color);
        if (isSelected) {
            // Selected: colored background (13% opacity), colored border
            Color bgColor = Color.color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 0.13);
            region.setBackground(new Background(new BackgroundFill(bgColor, new CornerRadii(8), null)));
            region.setBorder(new Border(new BorderStroke(baseColor, BorderStrokeStyle.SOLID, new CornerRadii(8), new BorderWidths(2))));
        } else {
            // Unselected: white background, gray border
            region.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(8), null)));
            region.setBorder(new Border(new BorderStroke(Color.web("#e5e5e5"), BorderStrokeStyle.SOLID, new CornerRadii(8), new BorderWidths(1))));
        }
        region.setPadding(new Insets(8, 14, 8, 14));
        region.setCursor(Cursor.HAND);
        // Set text color if it's a Labeled control (Button, Label, etc.)
        if (region instanceof javafx.scene.control.Labeled) {
            ((javafx.scene.control.Labeled) region).setTextFill(baseColor);
        }
    }

    /**
     * Applies unassigned filter chip styling using Background/Border objects (WebFX compatible).
     * This replaces setStyle() calls which don't work in WebFX GWT compilation.
     *
     * @param region The button/region to style
     * @param isSelected Whether the chip is currently selected
     * @param hasUnassigned Whether there are unassigned rooms (affects inactive state color)
     */
    public static void applyUnassignedFilterChipStyle(Region region, boolean isSelected, boolean hasUnassigned) {
        Color amberBg = Color.web("#fef3c7");
        Color amberText = Color.web("#92400e");
        Color grayText = Color.web("#78716c");
        Color grayBorder = Color.web("#e5e5e5");

        if (isSelected) {
            // Selected: amber background, amber border
            region.setBackground(new Background(new BackgroundFill(amberBg, new CornerRadii(8), null)));
            region.setBorder(new Border(new BorderStroke(amberText, BorderStrokeStyle.SOLID, new CornerRadii(8), new BorderWidths(2))));
            if (region instanceof javafx.scene.control.Labeled) {
                ((javafx.scene.control.Labeled) region).setTextFill(amberText);
            }
        } else {
            // Unselected: amber or white background depending on unassigned count
            Color bgColor = hasUnassigned ? amberBg : Color.WHITE;
            Color textColor = hasUnassigned ? amberText : grayText;
            region.setBackground(new Background(new BackgroundFill(bgColor, new CornerRadii(8), null)));
            region.setBorder(new Border(new BorderStroke(grayBorder, BorderStrokeStyle.SOLID, new CornerRadii(8), new BorderWidths(1))));
            if (region instanceof javafx.scene.control.Labeled) {
                ((javafx.scene.control.Labeled) region).setTextFill(textColor);
            }
        }
        region.setPadding(new Insets(8, 14, 8, 14));
        region.setCursor(Cursor.HAND);
    }

    // ============== Site Comparison Styling Methods ==============

    // Site comparison colors
    private static final String COLOR_BOTH_BG = "#d4edda";
    private static final String COLOR_BOTH_TEXT = "#155724";
    private static final String COLOR_GLOBAL_BG = "#fff3cd";
    private static final String COLOR_GLOBAL_TEXT = "#856404";
    private static final String COLOR_EVENT_BG = "#f8d7da";
    private static final String COLOR_EVENT_TEXT = "#721c24";
    private static final String COLOR_LINKED = "#2e7d32";
    private static final String COLOR_SELECTED_BG = "#1976d2";
    private static final String COLOR_WARNING_BG = "#fff3cd";
    private static final String COLOR_WARNING_TEXT = "#856404";
    private static final String COLOR_UNLINK_BTN = "#d32f2f";

    /**
     * Applies comparison column styling with the given type (WebFX compatible).
     * @param region The region to style
     * @param type One of "both", "global", or "event"
     */
    public static void applyComparisonColumnStyle(Region region, String type) {
        String bgColor;
        switch (type) {
            case "both": bgColor = COLOR_BOTH_BG; break;
            case "global": bgColor = COLOR_GLOBAL_BG; break;
            case "event": bgColor = COLOR_EVENT_BG; break;
            default: bgColor = COLOR_BOTH_BG;
        }
        region.setBackground(new Background(new BackgroundFill(Color.web(bgColor), new CornerRadii(6), null)));
        region.setMinWidth(200);
        region.setPadding(new Insets(12));
    }

    /**
     * Applies comparison header text styling (WebFX compatible).
     * @param label The label to style
     * @param type One of "both", "global", or "event"
     */
    public static void applyComparisonHeaderStyle(Label label, String type) {
        Color textColor;
        switch (type) {
            case "both": textColor = Color.web(COLOR_BOTH_TEXT); break;
            case "global": textColor = Color.web(COLOR_GLOBAL_TEXT); break;
            case "event": textColor = Color.web(COLOR_EVENT_TEXT); break;
            default: textColor = Color.web(COLOR_BOTH_TEXT);
        }
        label.setTextFill(textColor);
        label.getStyleClass().add(CSS_BODY_BOLD);
    }

    /**
     * Applies comparison resource text styling (WebFX compatible).
     * @param label The label to style
     * @param type One of "both", "global", or "event"
     */
    public static void applyComparisonTextStyle(Label label, String type) {
        Color textColor;
        switch (type) {
            case "both": textColor = Color.web(COLOR_BOTH_TEXT); break;
            case "global": textColor = Color.web(COLOR_GLOBAL_TEXT); break;
            case "event": textColor = Color.web(COLOR_EVENT_TEXT); break;
            default: textColor = Color.web(COLOR_BOTH_TEXT);
        }
        label.setTextFill(textColor);
    }

    /**
     * Applies linked resource styling - green italic text (WebFX compatible).
     */
    public static void applyLinkedResourceStyle(Label label) {
        label.setTextFill(Color.web(COLOR_LINKED));
        label.getStyleClass().add(CSS_COMPARISON_RESOURCE_LINKED);
    }

    /**
     * Applies selected resource styling - white on blue background (WebFX compatible).
     */
    public static void applySelectedResourceStyle(Label label) {
        label.setTextFill(Color.WHITE);
        label.setBackground(new Background(new BackgroundFill(Color.web(COLOR_SELECTED_BG), new CornerRadii(3), null)));
        label.setPadding(new Insets(2, 6, 2, 6));
    }

    /**
     * Applies warning box styling (WebFX compatible).
     */
    public static void applyWarningBoxStyle(Region region) {
        region.setBackground(new Background(new BackgroundFill(Color.web(COLOR_WARNING_BG), new CornerRadii(8), null)));
        region.setPadding(new Insets(40, 24, 40, 24));
    }

    /**
     * Applies warning icon styling (WebFX compatible).
     */
    public static void applyWarningIconStyle(Label label) {
        label.setTextFill(Color.web(COLOR_WARNING_TEXT));
        label.getStyleClass().add(CSS_TITLE);
    }

    /**
     * Applies warning text styling (WebFX compatible).
     */
    public static void applyWarningTextStyle(Label label) {
        label.setTextFill(Color.web(COLOR_WARNING_TEXT));
        label.setWrapText(true);
    }

    /**
     * Applies unlink button styling (WebFX compatible).
     */
    public static void applyUnlinkButtonStyle(javafx.scene.control.ButtonBase button) {
        button.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));
        button.setTextFill(Color.web(COLOR_UNLINK_BTN));
        button.getStyleClass().add(CSS_CLICKABLE);
    }

    /**
     * Applies link badge styling with the given color (WebFX compatible).
     * @param label The label to style
     * @param badgeColor The hex color for the badge background
     */
    public static void applyLinkBadgeStyle(Label label, String badgeColor) {
        label.setBackground(new Background(new BackgroundFill(Color.web(badgeColor), new CornerRadii(3), null)));
        label.setTextFill(Color.WHITE);
        label.setPadding(new Insets(1, 4, 1, 4));
        label.getStyleClass().add(CSS_SMALL);
    }

    // ============== Gender Toggle Button Styling ==============

    // Gender toggle colors
    private static final String COLOR_GENDER_BG_DEFAULT = "#ffffff";
    private static final String COLOR_GENDER_TEXT_DEFAULT = "#78716c";
    private static final String COLOR_GENDER_BORDER_DEFAULT = "#e5e5e5";
    private static final String COLOR_GENDER_BG_SELECTED = "#fef3c7";
    private static final String COLOR_GENDER_TEXT_SELECTED = "#f59e0b";
    private static final String COLOR_GENDER_BORDER_SELECTED = "#f59e0b";

    /**
     * Applies unselected gender toggle button styling (WebFX compatible).
     * White background, gray text and border.
     *
     * @param button The toggle button to style
     */
    public static void applyGenderToggleUnselectedStyle(javafx.scene.control.ToggleButton button) {
        button.setBackground(new Background(new BackgroundFill(Color.web(COLOR_GENDER_BG_DEFAULT), new CornerRadii(10), null)));
        button.setBorder(new Border(new BorderStroke(Color.web(COLOR_GENDER_BORDER_DEFAULT), BorderStrokeStyle.SOLID, new CornerRadii(10), new BorderWidths(1))));
        button.setTextFill(Color.web(COLOR_GENDER_TEXT_DEFAULT));
        button.setCursor(Cursor.HAND);
        button.setPadding(new Insets(12, 16, 12, 16));
    }

    /**
     * Applies selected gender toggle button styling (WebFX compatible).
     * Yellow/amber background with amber text and border for visual emphasis.
     *
     * @param button The toggle button to style
     */
    public static void applyGenderToggleSelectedStyle(javafx.scene.control.ToggleButton button) {
        button.setBackground(new Background(new BackgroundFill(Color.web(COLOR_GENDER_BG_SELECTED), new CornerRadii(10), null)));
        button.setBorder(new Border(new BorderStroke(Color.web(COLOR_GENDER_BORDER_SELECTED), BorderStrokeStyle.SOLID, new CornerRadii(10), new BorderWidths(2))));
        button.setTextFill(Color.web(COLOR_GENDER_TEXT_SELECTED));
        button.setCursor(Cursor.HAND);
        button.setPadding(new Insets(12, 16, 12, 16));
    }

    /**
     * Applies appropriate gender toggle button styling based on selection state (WebFX compatible).
     * Use this method in selection change listeners for dynamic styling updates.
     *
     * @param button The toggle button to style
     * @param isSelected Whether the button is currently selected
     */
    public static void applyGenderToggleStyle(javafx.scene.control.ToggleButton button, boolean isSelected) {
        if (isSelected) {
            applyGenderToggleSelectedStyle(button);
        } else {
            applyGenderToggleUnselectedStyle(button);
        }
    }
}
