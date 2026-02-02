package one.modality.hotel.backoffice.activities.reception.util;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.layout.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Styling utilities for the Reception Dashboard.
 * Provides helper methods for consistent component styling across the dashboard.
 *
 * All sizing (padding, spacing, dimensions) is set in Java code per KBS3 guidelines.
 * CSS is used only for colors, fonts, and visual decorations.
 *
 * @author David Hello
 * @author Claude Code
 */
public interface ReceptionStyles {

    // ==========================================
    // CSS Class Constants
    // ==========================================

    // Dashboard layout
    String RECEPTION_DASHBOARD = "reception-dashboard";
    String RECEPTION_HEADER = "reception-header";
    String RECEPTION_CONTENT = "reception-content";
    String RECEPTION_SIDEBAR = "reception-sidebar";

    // Card styles
    String RECEPTION_CARD = "reception-card";
    String RECEPTION_CARD_HEADER = "reception-card-header";
    String RECEPTION_CARD_TITLE = "reception-card-title";
    String RECEPTION_CARD_BODY = "reception-card-body";

    // Stat card styles
    String STAT_CARD = "stat-card";
    String STAT_CARD_CLICKABLE = "stat-card-clickable";
    String STAT_CARD_SELECTED = "stat-card-selected";
    String STAT_CARD_VALUE = "stat-card-value";
    String STAT_CARD_LABEL = "stat-card-label";
    String STAT_CARD_ICON = "stat-card-icon";

    // Guest list styles
    String GUEST_LIST = "guest-list";
    String GUEST_ROW = "guest-row";
    String GUEST_ROW_SELECTED = "guest-row-selected";
    String GUEST_ROW_HOVER = "guest-row-hover";

    // Badge styles (status indicators)
    String STATUS_BADGE = "status-badge";
    String STATUS_EXPECTED = "status-expected";
    String STATUS_CHECKED_IN = "status-checked-in";
    String STATUS_CHECKED_OUT = "status-checked-out";
    String STATUS_NO_SHOW = "status-no-show";
    String STATUS_PRE_BOOKED = "status-pre-booked";
    String STATUS_CANCELLED = "status-cancelled";

    // Tab styles
    String TAB_BAR = "reception-tab-bar";
    String TAB_ITEM = "reception-tab-item";
    String TAB_ITEM_ACTIVE = "reception-tab-item-active";
    String TAB_COUNT = "reception-tab-count";

    // Search and filter styles
    String SEARCH_BOX = "reception-search-box";
    String EVENT_FILTER = "reception-event-filter";

    // Button variants
    String BTN_ICON = "btn-icon";
    String BTN_ACTION = "btn-action";
    String BTN_QUICK_ACTION = "btn-quick-action";

    // Empty state
    String EMPTY_STATE = "empty-state";
    String EMPTY_STATE_ICON = "empty-state-icon";
    String EMPTY_STATE_TEXT = "empty-state-text";

    // ==========================================
    // Layout Constants (Java sizing)
    // ==========================================

    // Card dimensions
    double CARD_WIDTH = 280;
    double CARD_PADDING = 16;
    double CARD_SPACING = 12;
    double CARD_BORDER_RADIUS = 12;

    // Stat card dimensions
    double STAT_CARD_WIDTH = 160;
    double STAT_CARD_HEIGHT = 90;
    double STAT_CARD_PADDING = 12;

    // Header dimensions
    double HEADER_HEIGHT = 60;
    double HEADER_PADDING = 16;

    // Sidebar dimensions
    double SIDEBAR_WIDTH = 300;
    double SIDEBAR_PADDING = 16;

    // Dashboard max width (for centering)
    double DASHBOARD_MAX_WIDTH = 1400;

    // Guest row dimensions
    double GUEST_ROW_HEIGHT = 72;
    double GUEST_ROW_PADDING_H = 16;
    double GUEST_ROW_PADDING_V = 12;

    // Tab bar dimensions
    double TAB_PADDING_H = 16;
    double TAB_PADDING_V = 12;

    // Default spacing
    double SPACING_XS = 4;
    double SPACING_SM = 8;
    double SPACING_MD = 12;
    double SPACING_LG = 16;
    double SPACING_XL = 20;
    double SPACING_2XL = 24;
    double SPACING_3XL = 32;

    // ==========================================
    // Table Column Widths (for GridPane alignment)
    // ==========================================

    // Column indices
    int COL_CHECKBOX = 0;
    int COL_GUEST = 1;
    int COL_EVENT = 2;
    int COL_ROOM = 3;
    int COL_DATES = 4;
    int COL_BALANCE = 5;
    int COL_STATUS = 6;
    int COL_ACTIONS = 7;

    // Column widths (fixed columns)
    double COL_CHECKBOX_WIDTH = 24;
    double COL_EVENT_WIDTH = 120;
    double COL_ROOM_WIDTH = 100;
    double COL_DATES_WIDTH = 100;
    double COL_BALANCE_WIDTH = 80;
    double COL_STATUS_WIDTH = 120;
    double COL_ACTIONS_WIDTH = 180;

    // ==========================================
    // Component Factory Methods
    // ==========================================

    /**
     * Creates a styled card container for the dashboard.
     */
    static VBox createCard() {
        VBox card = new VBox(CARD_SPACING);
        card.getStyleClass().add(RECEPTION_CARD);
        card.setPadding(new Insets(CARD_PADDING));
        card.setPrefWidth(CARD_WIDTH);
        return card;
    }

    /**
     * Creates a styled card container with custom width.
     */
    static VBox createCard(double width) {
        VBox card = createCard();
        card.setPrefWidth(width);
        return card;
    }

    /**
     * Creates a card header section with title.
     */
    static HBox createCardHeader(Label title) {
        HBox header = new HBox(SPACING_SM);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add(RECEPTION_CARD_HEADER);
        title.getStyleClass().add(RECEPTION_CARD_TITLE);
        header.getChildren().add(title);
        return header;
    }

    /**
     * Creates a stat card for the stats bar.
     */
    static VBox createStatCard() {
        VBox card = new VBox(SPACING_XS);
        card.getStyleClass().add(STAT_CARD);
        card.setPadding(new Insets(STAT_CARD_PADDING));
        card.setPrefWidth(STAT_CARD_WIDTH);
        card.setPrefHeight(STAT_CARD_HEIGHT);
        card.setAlignment(Pos.CENTER_LEFT);
        return card;
    }

    /**
     * Creates a clickable stat card.
     */
    static VBox createClickableStatCard() {
        VBox card = createStatCard();
        card.getStyleClass().add(STAT_CARD_CLICKABLE);
        return card;
    }

    /**
     * Creates a flexible stat card that grows to fill available space.
     * Use HBox.setHgrow(card, Priority.ALWAYS) on the container.
     */
    static VBox createFlexibleStatCard() {
        VBox card = new VBox(SPACING_XS);
        card.getStyleClass().addAll(STAT_CARD, STAT_CARD_CLICKABLE);
        card.setPadding(new Insets(STAT_CARD_PADDING));
        card.setMinWidth(120); // Minimum width
        card.setMaxWidth(Double.MAX_VALUE); // Allow to grow
        card.setPrefHeight(STAT_CARD_HEIGHT);
        card.setAlignment(Pos.CENTER_LEFT);
        return card;
    }

    /**
     * Creates a guest row container (legacy HBox approach).
     */
    static HBox createGuestRow() {
        HBox row = new HBox(SPACING_MD);
        row.getStyleClass().add(GUEST_ROW);
        row.setPadding(new Insets(GUEST_ROW_PADDING_V, GUEST_ROW_PADDING_H, GUEST_ROW_PADDING_V, GUEST_ROW_PADDING_H));
        row.setPrefHeight(GUEST_ROW_HEIGHT);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /**
     * Creates column constraints for the guest table GridPane.
     * @param includeBulkCheckbox Whether to include the checkbox column
     * @return List of ColumnConstraints for the GridPane
     */
    static List<ColumnConstraints> createTableColumnConstraints(boolean includeBulkCheckbox) {
        List<ColumnConstraints> constraints = new ArrayList<>();

        // Checkbox column (only when bulk mode is enabled)
        if (includeBulkCheckbox) {
            ColumnConstraints checkbox = new ColumnConstraints();
            checkbox.setPrefWidth(COL_CHECKBOX_WIDTH);
            checkbox.setMinWidth(COL_CHECKBOX_WIDTH);
            checkbox.setMaxWidth(COL_CHECKBOX_WIDTH);
            checkbox.setHgrow(Priority.NEVER);
            constraints.add(checkbox);
        }

        // Guest column (flexible - takes remaining space)
        ColumnConstraints guest = new ColumnConstraints();
        guest.setMinWidth(150);
        guest.setHgrow(Priority.ALWAYS);
        constraints.add(guest);

        // Event column (fixed)
        ColumnConstraints event = new ColumnConstraints();
        event.setPrefWidth(COL_EVENT_WIDTH);
        event.setMinWidth(COL_EVENT_WIDTH);
        event.setHgrow(Priority.NEVER);
        constraints.add(event);

        // Room column (fixed)
        ColumnConstraints room = new ColumnConstraints();
        room.setPrefWidth(COL_ROOM_WIDTH);
        room.setMinWidth(COL_ROOM_WIDTH);
        room.setHgrow(Priority.NEVER);
        constraints.add(room);

        // Dates column (fixed)
        ColumnConstraints dates = new ColumnConstraints();
        dates.setPrefWidth(COL_DATES_WIDTH);
        dates.setMinWidth(COL_DATES_WIDTH);
        dates.setHgrow(Priority.NEVER);
        constraints.add(dates);

        // Balance column (fixed)
        ColumnConstraints balance = new ColumnConstraints();
        balance.setPrefWidth(COL_BALANCE_WIDTH);
        balance.setMinWidth(COL_BALANCE_WIDTH);
        balance.setHgrow(Priority.NEVER);
        balance.setHalignment(javafx.geometry.HPos.RIGHT);
        constraints.add(balance);

        // Status column (fixed)
        ColumnConstraints status = new ColumnConstraints();
        status.setPrefWidth(COL_STATUS_WIDTH);
        status.setMinWidth(COL_STATUS_WIDTH);
        status.setHgrow(Priority.NEVER);
        constraints.add(status);

        // Actions column (fixed)
        ColumnConstraints actions = new ColumnConstraints();
        actions.setPrefWidth(COL_ACTIONS_WIDTH);
        actions.setMinWidth(COL_ACTIONS_WIDTH);
        actions.setHgrow(Priority.NEVER);
        actions.setHalignment(javafx.geometry.HPos.RIGHT);
        constraints.add(actions);

        return constraints;
    }

    /**
     * Creates a GridPane configured for the guest table.
     */
    static GridPane createGuestTableGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(SPACING_MD);
        grid.setVgap(0);
        grid.setPadding(new Insets(0, GUEST_ROW_PADDING_H, 0, GUEST_ROW_PADDING_H));
        // Column constraints will be set dynamically based on bulk mode
        return grid;
    }

    /**
     * Creates an empty state container.
     */
    static VBox createEmptyState(Label iconLabel, Label textLabel) {
        VBox container = new VBox(SPACING_MD);
        container.getStyleClass().add(EMPTY_STATE);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(SPACING_3XL));

        if (iconLabel != null) {
            iconLabel.getStyleClass().add(EMPTY_STATE_ICON);
            container.getChildren().add(iconLabel);
        }
        if (textLabel != null) {
            textLabel.getStyleClass().add(EMPTY_STATE_TEXT);
            textLabel.setWrapText(true);
            container.getChildren().add(textLabel);
        }

        return container;
    }

    // ==========================================
    // Styling Helper Methods
    // ==========================================

    /**
     * Applies style classes to a node.
     */
    static <N extends Node> N style(N node, String... styles) {
        for (String style : styles) {
            if (!node.getStyleClass().contains(style)) {
                node.getStyleClass().add(style);
            }
        }
        return node;
    }

    /**
     * Removes style classes from a node.
     */
    static <N extends Node> N removeStyle(N node, String... styles) {
        node.getStyleClass().removeAll(styles);
        return node;
    }

    /**
     * Toggles a style class on a node.
     */
    static <N extends Node> N toggleStyle(N node, String style, boolean add) {
        if (add) {
            if (!node.getStyleClass().contains(style)) {
                node.getStyleClass().add(style);
            }
        } else {
            node.getStyleClass().remove(style);
        }
        return node;
    }

    // ==========================================
    // Status Badge Methods
    // ==========================================

    /**
     * Creates a status badge with appropriate styling.
     */
    static Label createStatusBadge(String status) {
        Label badge = new Label();
        badge.getStyleClass().add(STATUS_BADGE);
        setStatusStyle(badge, status);
        badge.setPadding(new Insets(4, 10, 4, 10));
        return badge;
    }

    /**
     * Sets the status style on a badge based on status string.
     */
    static void setStatusStyle(Label badge, String status) {
        // Remove existing status styles
        badge.getStyleClass().removeAll(
            STATUS_EXPECTED, STATUS_CHECKED_IN, STATUS_CHECKED_OUT,
            STATUS_NO_SHOW, STATUS_PRE_BOOKED, STATUS_CANCELLED
        );

        // Add appropriate style based on status
        if (status == null) return;

        switch (status.toLowerCase()) {
            case "expected":
                badge.getStyleClass().add(STATUS_EXPECTED);
                break;
            case "checked-in":
            case "checkedin":
            case "arrived":
                badge.getStyleClass().add(STATUS_CHECKED_IN);
                break;
            case "checked-out":
            case "checkedout":
            case "departed":
                badge.getStyleClass().add(STATUS_CHECKED_OUT);
                break;
            case "no-show":
            case "noshow":
                badge.getStyleClass().add(STATUS_NO_SHOW);
                break;
            case "pre-booked":
            case "prebooked":
            case "unconfirmed":
                badge.getStyleClass().add(STATUS_PRE_BOOKED);
                break;
            case "cancelled":
            case "canceled":
                badge.getStyleClass().add(STATUS_CANCELLED);
                break;
            default:
                badge.getStyleClass().add(STATUS_EXPECTED);
        }
    }

    // ==========================================
    // Tab Styling Methods
    // ==========================================

    /**
     * Creates a tab item button.
     */
    static <N extends Node> N tabItem(N node) {
        node.getStyleClass().add(TAB_ITEM);
        if (node instanceof Labeled) {
            ((Labeled) node).setPadding(new Insets(TAB_PADDING_V, TAB_PADDING_H, TAB_PADDING_V, TAB_PADDING_H));
        }
        return node;
    }

    /**
     * Sets a tab item as active/inactive.
     */
    static <N extends Node> N setTabActive(N node, boolean active) {
        return toggleStyle(node, TAB_ITEM_ACTIVE, active);
    }

    // ==========================================
    // Button Styling Methods (leveraging Bootstrap)
    // ==========================================

    /**
     * Creates an icon-only button.
     */
    static <N extends Node> N iconButton(N button) {
        style(button, BTN_ICON);
        if (button instanceof Labeled) {
            ((Labeled) button).setPadding(new Insets(8));
        }
        if (button instanceof Region) {
            ((Region) button).setMinWidth(36);
            ((Region) button).setPrefWidth(36);
            ((Region) button).setMinHeight(36);
            ((Region) button).setPrefHeight(36);
        }
        return button;
    }

    /**
     * Creates a quick action button.
     */
    static <N extends Node> N quickActionButton(N button) {
        Bootstrap.button(button, Bootstrap.SUCCESS);
        style(button, BTN_QUICK_ACTION);
        if (button instanceof Labeled) {
            ((Labeled) button).setPadding(new Insets(8, 16, 8, 16));
        }
        return button;
    }

    /**
     * Creates a primary action button.
     */
    static <N extends Node> N primaryActionButton(N button) {
        return Bootstrap.primaryButton(button);
    }

    /**
     * Creates a secondary action button.
     */
    static <N extends Node> N secondaryActionButton(N button) {
        return Bootstrap.secondaryButton(button);
    }

    /**
     * Creates a danger action button.
     */
    static <N extends Node> N dangerActionButton(N button) {
        return Bootstrap.dangerButton(button);
    }

    /**
     * Creates a success action button.
     */
    static <N extends Node> N successActionButton(N button) {
        return Bootstrap.successButton(button);
    }
}
