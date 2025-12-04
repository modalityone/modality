package one.modality.hotel.backoffice.activities.roomsetup.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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
}
