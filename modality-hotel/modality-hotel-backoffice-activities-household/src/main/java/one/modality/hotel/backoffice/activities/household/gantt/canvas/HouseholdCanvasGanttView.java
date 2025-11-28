package one.modality.hotel.backoffice.activities.household.gantt.canvas;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.time.layout.bar.LocalDateBar;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.collections.ListChangeListener;
import javafx.collections.SetChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import one.modality.base.shared.entities.CleaningState;
import one.modality.base.shared.entities.Resource;
import one.modality.hotel.backoffice.accommodation.AccommodationPresentationModel;
import one.modality.hotel.backoffice.activities.household.HouseholdI18nKeys;
import one.modality.hotel.backoffice.activities.household.gantt.data.HouseholdGanttDataLoader;
import one.modality.hotel.backoffice.activities.household.gantt.adapter.EntityDataAdapter;
import one.modality.hotel.backoffice.activities.household.gantt.model.GanttRoomData;
import one.modality.hotel.backoffice.activities.household.gantt.model.RoomStatus;
import one.modality.hotel.backoffice.activities.household.gantt.presenter.GanttFilterManager;
import one.modality.hotel.backoffice.activities.household.gantt.presenter.GanttPresenter;
import one.modality.hotel.backoffice.activities.household.gantt.renderer.GanttColorScheme;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Canvas-based Gantt view implementation for household/housekeeping management.
 * <p>
 * This class serves as a drop-in replacement for the GridPane-based HouseholdGanttView,
 * using the hybrid approach to preserve all existing business logic while gaining
 * Canvas performance benefits.
 * <p>
 * Architecture:
 * - Uses HouseholdGanttCanvas for rendering
 * - Uses HouseholdBarAdapter to convert presenter data to Canvas format
 * - Maintains the same external API as the original HouseholdGanttView
 * - Reuses GanttPresenter for all business logic
 *
 * @author Claude Code Assistant
 */
public class HouseholdCanvasGanttView {

    private final GanttPresenter presenter;
    private final HouseholdGanttCanvas canvas;
    private final HouseholdBarAdapter barAdapter;
    private final HouseholdGanttDataLoader dataLoader;
    private final GanttFilterManager filterManager;

    // Cache the container node to avoid rebuilding
    private final VBox containerNode;

    // Filter bar UI components
    private final HBox filterBar;
    private final HBox statusFilterGroup;
    private final HBox categoryFilterGroup;
    private final Map<RoomStatus, Button> statusFilterButtons = new LinkedHashMap<>();
    private final Map<String, Button> categoryFilterButtons = new LinkedHashMap<>();
    private Button allStatusButton;
    private Button allCategoryButton;

    // Track available categories from data
    private Set<String> availableCategories = new LinkedHashSet<>();

    // Track active context menu to allow closing it
    private javafx.scene.control.ContextMenu activeContextMenu;

    // Listener references for cleanup
    private final ListChangeListener<Object> resourceConfigListener;
    private final ListChangeListener<Object> documentLineListener;
    private final ListChangeListener<Object> attendanceGapListener;

    /**
     * Constructor initializes the Canvas-based Gantt view.
     *
     * @param pm The presentation model (provides organization context for queries)
     */
    public HouseholdCanvasGanttView(AccommodationPresentationModel pm) {
        // Initialize presenter (handles business logic - PRESERVED from original)
        this.presenter = new GanttPresenter();

        // Initialize filter manager
        this.filterManager = new GanttFilterManager();

        // Initialize Canvas implementation (pass presenter for expand/collapse state)
        this.canvas = new HouseholdGanttCanvas(pm);

        // Initialize adapter (bridges presenter logic with Canvas format)
        this.barAdapter = new HouseholdBarAdapter(presenter);

        // Initialize data loader (same as original)
        this.dataLoader = new HouseholdGanttDataLoader(pm);

        // Build filter bar UI
        this.statusFilterGroup = new HBox(4);
        this.categoryFilterGroup = new HBox(4);
        this.filterBar = buildFilterBar();

        // Build container with filter bar on top of canvas
        this.containerNode = new VBox();
        containerNode.getChildren().addAll(filterBar, canvas.buildCanvasContainer());
        VBox.setVgrow(containerNode.getChildren().get(1), Priority.ALWAYS);

        // Sync presenter time window with canvas layout time window (one-time initial sync)
        if (pm.getTimeWindowStart() != null && pm.getTimeWindowEnd() != null) {
            presenter.setDateRange(pm.getTimeWindowStart(), pm.getTimeWindowEnd());
        }

        // IMPORTANT: We DON'T add listeners to time window changes because:
        // 1. The data loader has reactive queries that automatically re-execute when time window changes
        // 2. Those reactive queries update the observable lists (resourceConfigurations, documentLines)
        // 3. Our existing listeners on those lists (below) will trigger refreshDisplay() automatically
        // 4. The bidirectional binding already handles layout time window updates
        //
        // Therefore, no additional listeners are needed here - the reactive chain handles everything!

        // Initialize listeners with stored references for cleanup
        this.resourceConfigListener = c -> refreshDisplay();
        this.documentLineListener = c -> refreshDisplay();
        this.attendanceGapListener = c -> refreshDisplay();

        // Listen to data changes and update view
        dataLoader.getResourceConfigurations().addListener(resourceConfigListener);
        dataLoader.getDocumentLines().addListener(documentLineListener);
        dataLoader.getAttendancesForGaps().addListener(attendanceGapListener);

        // Listen to expand/collapse state changes
        presenter.getExpandedRoomIds().addListener((javafx.collections.SetChangeListener<String>) change ->
            refreshDisplay()
        );

        // Add click handler for expand/collapse functionality
        setupClickHandler();

        // Listen to filter changes and update display
        filterManager.getActiveStatusFilters().addListener((SetChangeListener<RoomStatus>) change -> refreshDisplay());
        filterManager.getActiveCategoryFilters().addListener((SetChangeListener<String>) change -> refreshDisplay());

        // Initial display refresh (will be empty until data loads, but sets up the canvas)
        // Subsequent refreshes will be triggered by data change listeners
        refreshDisplay();
    }

    // ============================================================================
    // FILTER BAR UI
    // ============================================================================

    /**
     * Builds the filter bar with status and category toggle buttons.
     * Design: Two-row layout - first row has status filters and buttons, second row has room type filter.
     */
    private HBox buildFilterBar() {
        // Main container - VBox to stack two rows
        VBox container = new VBox(8);
        container.setPadding(new Insets(8, 16, 8, 16));
        container.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
        // Bottom border
        container.setBorder(new Border(new BorderStroke(Color.rgb(222, 226, 230), BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY, new BorderWidths(0, 0, 1, 0))));

        // First row: Status filters + action button
        HBox firstRow = new HBox(24);
        firstRow.setAlignment(Pos.CENTER_LEFT);

        // Status filter section
        HBox statusSection = new HBox(8);
        statusSection.setAlignment(Pos.CENTER_LEFT);
        Label statusLabel = new Label(I18n.getI18nText(HouseholdI18nKeys.Status) + ":");
        statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151; -fx-min-width: 80px;");  // Fixed width for alignment
        buildStatusFilterButtons();
        statusSection.getChildren().addAll(statusLabel, statusFilterGroup);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // "Go to Today" button - prominent accent button with icon
        Button goToTodayButton = createGoToTodayButton();

        firstRow.getChildren().addAll(statusSection, spacer, goToTodayButton);

        // Second row: Room type filter
        HBox secondRow = new HBox(8);
        secondRow.setAlignment(Pos.CENTER_LEFT);

        // Category filter section (dynamic - will be populated when data loads)
        HBox categorySection = new HBox(8);
        categorySection.setAlignment(Pos.CENTER_LEFT);
        Label categoryLabel = new Label("Room type:");
        categoryLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151; -fx-min-width: 80px;");  // Same width for alignment
        categorySection.getChildren().addAll(categoryLabel, categoryFilterGroup);

        secondRow.getChildren().add(categorySection);

        // Add both rows to container
        container.getChildren().addAll(firstRow, secondRow);

        // Wrap in HBox to maintain compatibility with existing code
        HBox wrapper = new HBox(container);
        HBox.setHgrow(container, Priority.ALWAYS);
        return wrapper;
    }

    /**
     * Creates a beautiful "Go to Today" button with calendar icon.
     * Design: Accent-colored pill button with icon, slightly elevated style.
     */
    private Button createGoToTodayButton() {
        // Create button with arrow icon and text
        // Using Unicode arrow ➜ instead of emoji to ensure white color styling works
        Button btn = new Button("➜ Today");

        // Modern pill button style with accent color, subtle shadow, and hover effect
        btn.setStyle(
            "-fx-background-color: #0096D6; " +          // Accent blue
            "-fx-text-fill: white; " +
            "-fx-background-radius: 20; " +              // Pill shape
            "-fx-padding: 6 16 6 16; " +                 // Comfortable padding
            "-fx-font-weight: bold; " +
            "-fx-font-size: 13px; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 1);"  // Subtle shadow
        );

        // Hover effect - slightly darker blue
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: #007AB8; " +          // Darker blue on hover
            "-fx-text-fill: white; " +
            "-fx-background-radius: 20; " +
            "-fx-padding: 6 16 6 16; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 13px; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 5, 0, 0, 2);"  // Slightly stronger shadow on hover
        ));

        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: #0096D6; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 20; " +
            "-fx-padding: 6 16 6 16; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 13px; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 1);"
        ));

        // Action: Reset to 2 days before today
        btn.setOnAction(e -> goToToday());

        return btn;
    }

    /**
     * Resets the Gantt view to show today (with 2 days before for context).
     * This restores the initial view that users see when the page first loads.
     */
    private void goToToday() {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate startDate = today.minusDays(2);  // 2 days before today
        java.time.LocalDate endDate = startDate.plusDays(13); // 14 days total (2 before + today + 11 after)

        // Update the time window in the presentation model (which is bound to the canvas)
        // This will trigger the canvas to scroll/pan to show the new date range
        canvas.getBarsLayout().setTimeWindow(startDate, endDate);
    }

    /**
     * Builds the status filter toggle buttons with colored status dots.
     */
    private void buildStatusFilterButtons() {
        statusFilterGroup.getChildren().clear();
        statusFilterButtons.clear();

        // "All" button
        allStatusButton = createFilterToggleButton(I18n.getI18nText(HouseholdI18nKeys.All), true, null);
        allStatusButton.setOnAction(e -> {
            filterManager.getActiveStatusFilters().clear();
            updateStatusButtonStyles();
        });
        statusFilterGroup.getChildren().add(allStatusButton);

        // Status buttons with their corresponding colors
        Object[][] statusConfigs = {
            {RoomStatus.TO_CLEAN, HouseholdI18nKeys.ToClean, GanttColorScheme.COLOR_ROOM_TO_CLEAN},
            {RoomStatus.TO_INSPECT, HouseholdI18nKeys.ToInspect, GanttColorScheme.COLOR_ROOM_TO_INSPECT},
            {RoomStatus.READY, HouseholdI18nKeys.Ready, GanttColorScheme.COLOR_ROOM_READY},
            {RoomStatus.OCCUPIED, HouseholdI18nKeys.Occupied, GanttColorScheme.COLOR_ROOM_OCCUPIED}
        };

        for (Object[] config : statusConfigs) {
            RoomStatus status = (RoomStatus) config[0];
            String text = I18n.getI18nText(config[1]);
            Color statusColor = (Color) config[2];
            Button btn = createFilterToggleButton(text, false, statusColor);
            btn.setOnAction(e -> {
                filterManager.toggleStatusFilter(status);
                updateStatusButtonStyles();
            });
            statusFilterButtons.put(status, btn);
            statusFilterGroup.getChildren().add(btn);
        }
    }

    /**
     * Updates category filter buttons based on available categories from data.
     */
    private void updateCategoryFilterButtons(Set<String> categories) {
        if (categories.equals(availableCategories)) {
            return; // No change needed
        }
        availableCategories = new LinkedHashSet<>(categories);

        categoryFilterGroup.getChildren().clear();
        categoryFilterButtons.clear();

        // "All" button
        allCategoryButton = createFilterToggleButton(I18n.getI18nText(HouseholdI18nKeys.All), true, null);
        allCategoryButton.setOnAction(e -> {
            filterManager.getActiveCategoryFilters().clear();
            updateCategoryButtonStyles();
        });
        categoryFilterGroup.getChildren().add(allCategoryButton);

        // Category buttons (dynamically from data)
        for (String category : categories) {
            Button btn = createFilterToggleButton(category, false, null);
            btn.setOnAction(e -> {
                filterManager.toggleCategoryFilter(category);
                updateCategoryButtonStyles();
            });
            categoryFilterButtons.put(category, btn);
            categoryFilterGroup.getChildren().add(btn);
        }
    }

    /**
     * Creates a pill-style toggle button for filters.
     * @param text The button label text
     * @param isActive Whether the button should be styled as active
     * @param statusColor Optional color for a status dot (null for no dot)
     */
    private Button createFilterToggleButton(String text, boolean isActive, Color statusColor) {
        Button btn = new Button();
        btn.setPadding(new Insets(4, 12, 4, 12));

        if (statusColor != null) {
            // Create button with colored status dot
            HBox content = new HBox(6);
            content.setAlignment(Pos.CENTER_LEFT);

            // Status dot (circle)
            javafx.scene.shape.Circle dot = new javafx.scene.shape.Circle(5);
            dot.setFill(statusColor);

            Label label = new Label(text);
            content.getChildren().addAll(dot, label);
            btn.setGraphic(content);
        } else {
            btn.setText(text);
        }

        updateFilterButtonStyle(btn, isActive, statusColor);
        return btn;
    }

    /**
     * Updates button style based on active state.
     * @param btn The button to style
     * @param isActive Whether the button is currently active/selected
     * @param statusColor Optional status color (for buttons with colored dots)
     */
    private void updateFilterButtonStyle(Button btn, boolean isActive, Color statusColor) {
        if (isActive) {
            // Active: light grey background, white text
            btn.setStyle("-fx-background-color: #9CA3AF; -fx-text-fill: white; -fx-background-radius: 16; -fx-cursor: hand;");
            // Update label text color inside graphic if present
            if (btn.getGraphic() instanceof HBox hbox) {
                hbox.getChildren().stream()
                    .filter(n -> n instanceof Label)
                    .forEach(n -> ((Label) n).setStyle("-fx-text-fill: white;"));
            }
        } else {
            // Inactive: white background, grey text, subtle border
            btn.setStyle("-fx-background-color: white; -fx-text-fill: #6B7280; -fx-background-radius: 16; " +
                        "-fx-border-color: #D1D5DB; -fx-border-radius: 16; -fx-cursor: hand;");
            // Update label text color inside graphic if present
            if (btn.getGraphic() instanceof HBox hbox) {
                hbox.getChildren().stream()
                    .filter(n -> n instanceof Label)
                    .forEach(n -> ((Label) n).setStyle("-fx-text-fill: #6B7280;"));
            }
        }
    }

    /**
     * Updates status button styles based on current filter state.
     */
    private void updateStatusButtonStyles() {
        boolean noFiltersActive = filterManager.getActiveStatusFilters().isEmpty();
        updateFilterButtonStyle(allStatusButton, noFiltersActive, null);

        for (Map.Entry<RoomStatus, Button> entry : statusFilterButtons.entrySet()) {
            boolean isActive = filterManager.isStatusFilterActive(entry.getKey());
            Color statusColor = getStatusColor(entry.getKey());
            updateFilterButtonStyle(entry.getValue(), isActive, statusColor);
        }
    }

    /**
     * Gets the color for a room status from the color scheme.
     */
    private Color getStatusColor(RoomStatus status) {
        return switch (status) {
            case OCCUPIED -> GanttColorScheme.COLOR_ROOM_OCCUPIED;
            case TO_CLEAN -> GanttColorScheme.COLOR_ROOM_TO_CLEAN;
            case TO_INSPECT -> GanttColorScheme.COLOR_ROOM_TO_INSPECT;
            case READY -> GanttColorScheme.COLOR_ROOM_READY;
        };
    }

    /**
     * Updates category button styles based on current filter state.
     */
    private void updateCategoryButtonStyles() {
        boolean noFiltersActive = filterManager.getActiveCategoryFilters().isEmpty();
        if (allCategoryButton != null) {
            updateFilterButtonStyle(allCategoryButton, noFiltersActive, null);
        }

        for (Map.Entry<String, Button> entry : categoryFilterButtons.entrySet()) {
            boolean isActive = filterManager.isCategoryFilterActive(entry.getKey());
            updateFilterButtonStyle(entry.getValue(), isActive, null);
        }
    }

    /**
     * Clears all active filters.
     */
    private void clearAllFilters() {
        filterManager.clearAllFilters();
        updateStatusButtonStyles();
        updateCategoryButtonStyles();
    }

    // ============================================================================
    // CLICK HANDLERS
    // ============================================================================

    // Fixed positions for room header layout (must match HouseholdGanttCanvas.drawParentRoom)
    private static final double EXPAND_CLICKABLE_END_X = 26;   // Expand area ends at status dot position
    private static final double ACTION_ICON_OFFSET = 16;       // Action icon is at width - 16

    /**
     * Sets up click handler for expand/collapse functionality on room headers.
     */
    private void setupClickHandler() {
        javafx.scene.canvas.Canvas canvasNode = canvas.getBarsDrawer().getCanvas();

        // Add mouse moved handler to change cursor when hovering over multi-bed room headers or booking icons
        canvasNode.setOnMouseMoved(event -> {
            double mouseX = event.getX();
            double mouseY = event.getY();
            double parentHeaderWidth = canvas.getBarsLayout().getParentHeaderWidth();

            // Check if hovering over the room header area
            if (mouseX <= parentHeaderWidth) {
                // Adjust Y coordinate for scroll position using layoutOriginY
                // layoutOriginY is positive when scrolled down, so we add it to get absolute Y
                double adjustedY = mouseY + canvas.getBarsDrawer().getLayoutOriginY();

                // Use built-in method to find parent at this Y position
                dev.webfx.extras.time.layout.gantt.impl.ParentRow<?> parentRow =
                    canvas.getBarsLayout().getParentRowAtY(adjustedY);

                if (parentRow != null) {
                    Object parent = parentRow.getParent();
                    if (parent instanceof GanttParentRow ganttParent) {

                        // Check if hovering over expand/collapse area (only for multi-bed rooms)
                        if (!ganttParent.isBed() && ganttParent.isMultiBedRoom() && mouseX < EXPAND_CLICKABLE_END_X) {
                            canvasNode.setCursor(javafx.scene.Cursor.HAND);
                            return;
                        }

                        // Check if hovering over action icon (⋮) - for all room rows (not bed rows)
                        double actionIconX = parentHeaderWidth - ACTION_ICON_OFFSET;
                        if (!ganttParent.isBed() && mouseX >= actionIconX - 8 && mouseX <= actionIconX + 8) {
                            canvasNode.setCursor(javafx.scene.Cursor.HAND);
                            return;
                        }
                    }
                }
            } else {
                // Check if hovering over a booking bar with an icon
                double adjustedX = mouseX + canvas.getBarsDrawer().getLayoutOriginX();
                double adjustedY = mouseY + canvas.getBarsDrawer().getLayoutOriginY();

                java.util.List<LocalDateBar<HouseholdGanttCanvas.HouseholdBookingBlock>> children = canvas.getBarsLayout().getChildren();
                for (int i = 0; i < children.size(); i++) {
                    LocalDateBar<HouseholdGanttCanvas.HouseholdBookingBlock> bar = children.get(i);
                    dev.webfx.extras.geometry.Bounds bounds = canvas.getBarsLayout().getChildBounds(i);

                    if (bounds != null && bounds.contains(adjustedX, adjustedY)) {
                        // Found a bar - check if hovering over a clickable icon
                        HouseholdGanttCanvas.HouseholdBookingBlock block = bar.getInstance();
                        one.modality.hotel.backoffice.activities.household.gantt.model.BookingPosition position = block.getPosition();

                        // Only show hand cursor when hovering directly over icon positions
                        if (position == one.modality.hotel.backoffice.activities.household.gantt.model.BookingPosition.ARRIVAL ||
                            position == one.modality.hotel.backoffice.activities.household.gantt.model.BookingPosition.SINGLE) {

                            // Calculate icon positions (same as click detection)
                            double dayWidth = canvas.getBarsLayout().getTimeProjector().timeToX(
                                java.time.LocalDate.now().plusDays(1), true, false) -
                                canvas.getBarsLayout().getTimeProjector().timeToX(
                                java.time.LocalDate.now(), true, false);
                            double halfDayWidth = dayWidth / 2;
                            double relativeX = adjustedX - bounds.getMinX();

                            // Icon sizes - person icon scaled to 80% of bar height (14.4px), comment icon at 42%
                            double personIconSize = 18 * 0.8; // BAR_HEIGHT * 0.8
                            double personIconScaledWidth = HouseholdGanttIcons.PERSON_ICON_SVG_WIDTH *
                                (personIconSize / HouseholdGanttIcons.PERSON_ICON_SVG_HEIGHT);
                            double commentIconWidth = HouseholdGanttIcons.MESSAGE_ICON_SVG_WIDTH * 0.42;

                            // Person icon is drawn centered at: adjustedBounds.getMinX() + 8
                            // adjustedBounds.getMinX() = bounds.getMinX() + halfDayWidth
                            // So person icon center is at: bounds.getMinX() + halfDayWidth + 8
                            // In relative terms: halfDayWidth + 8
                            double personIconCenterX = halfDayWidth + 8;
                            double iconPadding = 5; // 5px padding around icon for easier clicking
                            double personIconStart = personIconCenterX - (personIconScaledWidth / 2) - iconPadding;
                            double personIconEnd = personIconCenterX + (personIconScaledWidth / 2) + iconPadding;

                            // Comment icon is rendered centered at halfDayWidth + 3, plus 1/2 day adjustment
                            // Account for centering: icon spans from center - width/2 to center + width/2
                            double commentIconCenter = halfDayWidth + 3 + (dayWidth / 2);
                            double commentIconStart = commentIconCenter - (commentIconWidth / 2) - iconPadding;
                            double commentIconEnd = commentIconCenter + (commentIconWidth / 2) + iconPadding;

                            // Check if hovering over either icon (with padding for easier interaction)
                            if ((block.hasComments() && relativeX >= commentIconStart && relativeX <= commentIconEnd) ||
                                (relativeX >= personIconStart && relativeX <= personIconEnd)) {
                                canvasNode.setCursor(javafx.scene.Cursor.HAND);
                                return;
                            }
                        }
                        break;
                    }
                }
            }
            canvasNode.setCursor(javafx.scene.Cursor.DEFAULT);
        });

        // Add click handler on the canvas to detect clicks on room headers AND booking bar icons
        canvasNode.setOnMouseClicked(event -> {
            // Hide any visible tooltip when clicking (will be shown again if clicking on an icon)
            canvas.getCanvasTooltip().hide();

            // Hide any active context menu when clicking elsewhere on the canvas
            if (activeContextMenu != null && activeContextMenu.isShowing()) {
                activeContextMenu.hide();
                activeContextMenu = null;
            }

            double clickX = event.getX();
            double clickY = event.getY();

            // Calculate total header width (parent header area)
            double parentHeaderWidth = canvas.getBarsLayout().getParentHeaderWidth();

            // Adjust Y coordinate for scroll position (CRITICAL!)
            // layoutOriginY is positive when scrolled down, so we add it to get absolute Y
            double layoutOriginY = canvas.getBarsDrawer().getLayoutOriginY();
            double adjustedY = clickY + layoutOriginY;

            if (clickX > parentHeaderWidth) {
                // Click is in the bars area - check if clicking on an icon
                handleBarIconClick(clickX, adjustedY, event);
                return;
            }

            // Click is in the parent header area (room/bed names on the left)
            // Use built-in method to find the parent row at this Y coordinate
            // This handles all the complex Y-position calculations including category headers
            dev.webfx.extras.time.layout.gantt.impl.ParentRow<?> parentRow =
                canvas.getBarsLayout().getParentRowAtY(adjustedY);

            if (parentRow == null) {
                return;
            }

            // Get the actual parent object (GanttParentRow)
            Object parent = parentRow.getParent();
            if (!(parent instanceof GanttParentRow ganttParent)) {
                return;
            }

            // Check if click is on the action icon (⋮) - for room rows only
            double actionIconX = parentHeaderWidth - ACTION_ICON_OFFSET;
            if (!ganttParent.isBed() && clickX >= actionIconX - 8 && clickX <= actionIconX + 8) {
                // Action icon clicked - show room status menu
                handleActionIconClick(event, ganttParent);
                return;
            }

            // Check if click is in expand/collapse area (left side, before status dot)
            // Only toggle if this is a room row (not a bed row), it's multi-bed, AND click is in expand area
            if (!ganttParent.isBed() && ganttParent.isMultiBedRoom() && clickX < EXPAND_CLICKABLE_END_X) {
                presenter.toggleRoomExpanded(ganttParent.room().getId());
            }
        });
    }

    /**
     * Handles click on the action icon (⋮) in room headers.
     * Shows a context menu for room status changes.
     */
    private void handleActionIconClick(javafx.scene.input.MouseEvent event, GanttParentRow ganttParent) {
        one.modality.hotel.backoffice.activities.household.gantt.model.GanttRoomData room = ganttParent.room();
        if (room == null) {
            return;
        }

        // Hide any existing context menu before showing a new one
        if (activeContextMenu != null && activeContextMenu.isShowing()) {
            activeContextMenu.hide();
        }

        // Create context menu for room actions
        javafx.scene.control.ContextMenu contextMenu = new javafx.scene.control.ContextMenu();

        // Enable auto-hide so menu closes when clicking outside
        contextMenu.setAutoHide(true);

        // Track the active context menu
        activeContextMenu = contextMenu;

        // Get current room status
        one.modality.hotel.backoffice.activities.household.gantt.model.RoomStatus currentStatus = room.getStatus();

        // Add menu items based on UX design status workflow
        // Workflow: OCCUPIED (no action) -> TO_CLEAN -> TO_INSPECT -> READY
        // Current status is already visible via the status dot, so only show available actions
        if (currentStatus == one.modality.hotel.backoffice.activities.household.gantt.model.RoomStatus.OCCUPIED) {
            // OCCUPIED: No actions possible - guests are in the room
            // Don't show menu since there are no actions available
            return;
        } else if (currentStatus == one.modality.hotel.backoffice.activities.household.gantt.model.RoomStatus.TO_CLEAN) {
            // TO_CLEAN: Can mark as cleaned (-> TO_INSPECT) or mark ready (-> READY, skipping inspection)
            javafx.scene.control.MenuItem cleanedItem = new javafx.scene.control.MenuItem("✓ " + I18n.getI18nText(HouseholdI18nKeys.MarkCleaned));
            cleanedItem.setOnAction(e -> updateRoomStatus(room, one.modality.hotel.backoffice.activities.household.gantt.model.RoomStatus.TO_INSPECT));
            javafx.scene.control.MenuItem readyItem = new javafx.scene.control.MenuItem("✓✓ " + I18n.getI18nText(HouseholdI18nKeys.MarkReady));
            readyItem.setOnAction(e -> updateRoomStatus(room, one.modality.hotel.backoffice.activities.household.gantt.model.RoomStatus.READY));
            contextMenu.getItems().addAll(cleanedItem, readyItem);
        } else if (currentStatus == one.modality.hotel.backoffice.activities.household.gantt.model.RoomStatus.TO_INSPECT) {
            // TO_INSPECT: Room cleaned, needs inspection
            javafx.scene.control.MenuItem readyItem = new javafx.scene.control.MenuItem("✓✓ " + I18n.getI18nText(HouseholdI18nKeys.MarkReady));
            readyItem.setOnAction(e -> updateRoomStatus(room, one.modality.hotel.backoffice.activities.household.gantt.model.RoomStatus.READY));
            javafx.scene.control.MenuItem reCleanItem = new javafx.scene.control.MenuItem("🔄 " + I18n.getI18nText(HouseholdI18nKeys.NeedsReClean));
            reCleanItem.setOnAction(e -> updateRoomStatus(room, one.modality.hotel.backoffice.activities.household.gantt.model.RoomStatus.TO_CLEAN));
            contextMenu.getItems().addAll(readyItem, reCleanItem);
        } else if (currentStatus == one.modality.hotel.backoffice.activities.household.gantt.model.RoomStatus.READY) {
            // READY: Room fully ready
            javafx.scene.control.MenuItem toCleanItem = new javafx.scene.control.MenuItem("🧹 " + I18n.getI18nText(HouseholdI18nKeys.MarkToClean));
            toCleanItem.setOnAction(e -> updateRoomStatus(room, one.modality.hotel.backoffice.activities.household.gantt.model.RoomStatus.TO_CLEAN));
            contextMenu.getItems().add(toCleanItem);
        }

        // Show context menu at click position
        javafx.scene.canvas.Canvas canvasNode = canvas.getBarsDrawer().getCanvas();
        contextMenu.show(canvasNode, event.getScreenX(), event.getScreenY());
    }

    /**
     * Updates the room status by setting lastCleaningDate and/or lastInspectionDate on the Resource entity.
     * <p>
     * Status workflow based on Resource dates:
     * - TO_CLEAN: lastCleaningDate is null or not today
     * - TO_INSPECT: lastCleaningDate is today, lastInspectionDate is null or not today
     * - READY: both lastCleaningDate and lastInspectionDate are today
     */
    private void updateRoomStatus(GanttRoomData room,
                                  one.modality.hotel.backoffice.activities.household.gantt.model.RoomStatus newStatus) {
        Resource resource = room.getResource();
        if (resource == null) {
            System.err.println("[HouseholdCanvasGanttView] Cannot update status: Room " + room.getName() + " has no Resource entity");
            return;
        }

        // Debug: Log resource info
        System.out.println("[HouseholdCanvasGanttView] Updating room " + room.getName() +
                " (Resource ID: " + resource.getPrimaryKey() +
                ", current lastCleaningDate: " + resource.getLastCleaningDate() +
                ", current lastInspectionDate: " + resource.getLastInspectionDate() + ")");

        UpdateStore updateStore = UpdateStore.createAbove(resource.getStore());
        Resource r = updateStore.updateEntity(resource);
        LocalDateTime now = LocalDateTime.now();

        switch (newStatus) {
            case TO_CLEAN:
                // Set state to DIRTY - keep date fields for historical tracking
                r.setCleaningState(CleaningState.DIRTY);
                System.out.println("[HouseholdCanvasGanttView] Setting cleaningState=DIRTY for TO_CLEAN");
                break;
            case TO_INSPECT:
                // Set state to TO_INSPECT and update cleaning date
                // Keep lastInspectionDate for historical tracking
                r.setCleaningState(CleaningState.TO_INSPECT);
                r.setLastCleaningDate(now);
                System.out.println("[HouseholdCanvasGanttView] Setting cleaningState=TO_INSPECT, lastCleaningDate=" + now);
                break;
            case READY:
                // Set state to READY
                r.setCleaningState(CleaningState.READY);
                // Only update cleaning date if skipping inspection (TO_CLEAN -> READY)
                // If coming from TO_INSPECT, the cleaning was already done
                if (room.getStatus() == one.modality.hotel.backoffice.activities.household.gantt.model.RoomStatus.TO_CLEAN) {
                    r.setLastCleaningDate(now);
                    System.out.println("[HouseholdCanvasGanttView] Setting cleaningState=READY (skipping inspection), both dates=" + now);
                } else {
                    System.out.println("[HouseholdCanvasGanttView] Setting cleaningState=READY (inspection only), lastInspectionDate=" + now);
                }
                r.setLastInspectionDate(now);
                break;
            case OCCUPIED:
                // Occupied status is determined by bookings, not by these dates
                // This case shouldn't be called, but handle gracefully
                System.out.println("[HouseholdCanvasGanttView] OCCUPIED status is determined by bookings, not manual update");
                return;
        }

        // Debug: Log what we're about to submit
        System.out.println("[HouseholdCanvasGanttView] Updated entity lastCleaningDate: " + r.getLastCleaningDate() +
                ", lastInspectionDate: " + r.getLastInspectionDate());

        // Submit changes to database
        updateStore.submitChanges()
                .onFailure(error -> System.err.println("[HouseholdCanvasGanttView] Failed to update room status: " + error.getMessage()))
                .onSuccess(ignored -> System.out.println("[HouseholdCanvasGanttView] Room " + room.getName() + " status updated to " + newStatus));
    }

    /**
     * Handles clicks on booking bar icons (person icon and special needs icon).
     * Detects which bar and icon was clicked, then shows appropriate tooltip.
     */
    private void handleBarIconClick(double clickX, double adjustedY, javafx.scene.input.MouseEvent event) {
        // Adjust coordinates for scroll offset
        double adjustedX = clickX + canvas.getBarsDrawer().getLayoutOriginX();

        // Icon sizes for hit detection - person icon scaled to 80% of bar height, comment icon at 42%
        double personIconSize = 18 * 0.8; // BAR_HEIGHT * 0.8
        double personIconScaledWidth = HouseholdGanttIcons.PERSON_ICON_SVG_WIDTH *
            (personIconSize / HouseholdGanttIcons.PERSON_ICON_SVG_HEIGHT);
        double commentIconWidth = HouseholdGanttIcons.MESSAGE_ICON_SVG_WIDTH * 0.42;
        double iconPadding = 5; // 5px padding around icon for easier clicking

        // Iterate through all bars to find one that contains this point
        java.util.List<LocalDateBar<HouseholdGanttCanvas.HouseholdBookingBlock>> children = canvas.getBarsLayout().getChildren();

        for (int i = 0; i < children.size(); i++) {
            LocalDateBar<HouseholdGanttCanvas.HouseholdBookingBlock> bar = children.get(i);
            dev.webfx.extras.geometry.Bounds bounds = canvas.getBarsLayout().getChildBounds(i);

            if (bounds != null && bounds.contains(adjustedX, adjustedY)) {
                // Found the clicked bar
                HouseholdGanttCanvas.HouseholdBookingBlock block = bar.getInstance();
                one.modality.hotel.backoffice.activities.household.gantt.model.BookingPosition position = block.getPosition();

                // For ARRIVAL or SINGLE position bars, check which icon was clicked
                if (position == one.modality.hotel.backoffice.activities.household.gantt.model.BookingPosition.ARRIVAL ||
                    position == one.modality.hotel.backoffice.activities.household.gantt.model.BookingPosition.SINGLE) {

                    // Calculate day width for icon positioning
                    double dayWidth = canvas.getBarsLayout().getTimeProjector().timeToX(
                        java.time.LocalDate.now().plusDays(1), true, false) -
                        canvas.getBarsLayout().getTimeProjector().timeToX(
                        java.time.LocalDate.now(), true, false);
                    double halfDayWidth = dayWidth / 2;

                    // Calculate relative X position within the bar
                    double relativeX = adjustedX - bounds.getMinX();

                    // Person icon is drawn centered at: adjustedBounds.getMinX() + 8
                    // adjustedBounds.getMinX() = bounds.getMinX() + halfDayWidth
                    // So person icon center is at: bounds.getMinX() + halfDayWidth + 8
                    // In relative terms: halfDayWidth + 8
                    double personIconCenterX = halfDayWidth + 8;
                    double personIconStart = personIconCenterX - (personIconScaledWidth / 2) - iconPadding;
                    double personIconEnd = personIconCenterX + (personIconScaledWidth / 2) + iconPadding;

                    // Comment icon is rendered centered at halfDayWidth + 3, plus 1/2 day adjustment
                    // Account for centering: icon spans from center - width/2 to center + width/2
                    double commentIconCenter = halfDayWidth + 3 + (dayWidth / 2);
                    double commentIconStart = commentIconCenter - (commentIconWidth / 2) - iconPadding;
                    double commentIconEnd = commentIconCenter + (commentIconWidth / 2) + iconPadding;

                    // Check which icon was clicked (prioritize comment icon if both present)
                    if (block.hasComments() && relativeX >= commentIconStart && relativeX <= commentIconEnd) {
                        // Click is within comment icon bounds
                        showSpecialNeedsTooltip(event, block.getBookingData());
                        return;
                    } else if (relativeX >= personIconStart && relativeX <= personIconEnd) {
                        // Click is within person icon bounds
                        showGuestInfoTooltip(event, block.getBookingData());
                        return;
                    }
                }
                // MIDDLE position - comment icon only (old logic, kept for compatibility)
                else if (block.hasComments() &&
                         position == one.modality.hotel.backoffice.activities.household.gantt.model.BookingPosition.MIDDLE) {
                    showSpecialNeedsTooltip(event, block.getBookingData());
                    return;
                }

                return;  // Found the bar, stop searching
            }
        }
    }

    /**
     * Shows a tooltip with guest information when person icon is clicked.
     * Uses canvas-based tooltip (WebFX compatible) instead of JavaFX Tooltip.
     */
    private void showGuestInfoTooltip(javafx.scene.input.MouseEvent event,
                                      one.modality.hotel.backoffice.activities.household.gantt.model.GanttBookingData booking) {
        if (booking == null) {
            return;
        }

        // Build tooltip content
        StringBuilder body = new StringBuilder();

        if (booking.getFirstName() != null && booking.getLastName() != null) {
            body.append(I18n.getI18nText(HouseholdI18nKeys.FirstName)).append(": ").append(booking.getFirstName()).append("\n");
            body.append(I18n.getI18nText(HouseholdI18nKeys.LastName)).append(": ").append(booking.getLastName()).append("\n");
        } else if (booking.getGuestName() != null) {
            body.append(I18n.getI18nText(HouseholdI18nKeys.Guest)).append(": ").append(booking.getGuestName()).append("\n");
        }

        if (booking.getGender() != null) {
            body.append(I18n.getI18nText(HouseholdI18nKeys.Gender)).append(": ").append(booking.getGender()).append("\n");
        }

        if (booking.getEvent() != null) {
            body.append(I18n.getI18nText(HouseholdI18nKeys.Event)).append(": ").append(booking.getEvent()).append("\n");
        }

        body.append("\n").append(I18n.getI18nText(HouseholdI18nKeys.CheckIn)).append(": ").append(booking.getStartDate());
        body.append("\n").append(I18n.getI18nText(HouseholdI18nKeys.CheckOut)).append(": ").append(booking.getEndDate());

        // Show canvas-based tooltip at mouse position
        CanvasTooltip tooltip = canvas.getCanvasTooltip();
        tooltip.show(event.getX() + 10, event.getY(), I18n.getI18nText(HouseholdI18nKeys.GuestInformation), body.toString());
    }

    /**
     * Shows a tooltip with special needs when comment icon is clicked.
     * Uses canvas-based tooltip (WebFX compatible) instead of JavaFX Tooltip.
     */
    private void showSpecialNeedsTooltip(javafx.scene.input.MouseEvent event,
                                        one.modality.hotel.backoffice.activities.household.gantt.model.GanttBookingData booking) {
        if (booking == null) {
            return;
        }

        // Build tooltip content
        StringBuilder body = new StringBuilder();
        body.append(I18n.getI18nText(HouseholdI18nKeys.Guest)).append(": ").append(booking.getGuestName()).append("\n");
        body.append(booking.getStartDate()).append(" - ").append(booking.getEndDate()).append("\n");

        if (booking.getComments() != null && !booking.getComments().isEmpty()) {
            body.append("\n").append(booking.getComments());
        }

        // Show canvas-based tooltip at mouse position
        CanvasTooltip tooltip = canvas.getCanvasTooltip();
        tooltip.show(event.getX() + 10, event.getY(), I18n.getI18nText(HouseholdI18nKeys.SpecialNeeds), body.toString());
    }

    /**
     * Starts the data loading logic.
     * Must be called after construction to begin loading data.
     *
     * @param mixin The mixin object for reactive chain lifecycle management
     */
    public void startLogic(Object mixin) {
        dataLoader.startLogic(mixin);
    }

    /**
     * Refreshes the display with current data.
     * Called when data changes or expand/collapse state changes.
     */
    private void refreshDisplay() {
        // Step 1: Convert database entities to gantt model using adapter pattern
        // Pass attendances for gap bookings to support split bars where guest doesn't stay certain nights
        List<GanttRoomData> allRooms = EntityDataAdapter.adaptRooms(
            dataLoader.getResourceConfigurations(),
            dataLoader.getDocumentLines(),
            dataLoader.getAttendancesForGaps()
        );

        // Step 1.5: Update category filter buttons dynamically from available data
        Set<String> categories = allRooms.stream()
                .map(GanttRoomData::getCategory)
                .filter(c -> c != null && !c.isEmpty())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        updateCategoryFilterButtons(categories);

        // Step 2: Apply filters to room list
        List<GanttRoomData> filteredRooms = filterManager.applyFilters(allRooms);

        // Step 3: Convert rooms to Canvas parent rows and bars using HouseholdBarAdapter
        // CRITICAL: Adapter creates parent rows AND bars together to ensure they reference same objects
        HouseholdBarAdapter.AdaptedRoomData adapted = barAdapter.adaptAllRoomsWithParents(filteredRooms);

        // Step 4: Update Canvas with parent rows and bars
        try {
            displayRoomsAndBars(adapted.parentRows(), adapted.bars());
        } catch (Exception e) {
            // Catch any rendering errors to prevent UI crash
            System.err.println("[HouseholdCanvasGanttView] Error displaying bars: " + e.getMessage());
        }
    }

    /**
     * Displays rooms and bars in the Canvas.
     * <p>
     * CRITICAL: The GanttLayout needs both parents AND children to work correctly.
     * - Parents: Room/bed headers displayed on the left (GanttParentRow objects)
     * - Children: Booking bars displayed in the timeline
     * <p>
     * Parent rows are created by the adapter to ensure bars reference the correct parent objects.
     *
     * @param parentRows The parent rows (rooms and/or beds)
     * @param bars The booking bars (children)
     */
    private void displayRoomsAndBars(List<GanttParentRow> parentRows, List<LocalDateBar<HouseholdGanttCanvas.HouseholdBookingBlock>> bars) {
        // STEP 1: Set the parent rows in the layout
        canvas.getBarsLayout().getParents().setAll(parentRows);

        // STEP 2: Set the booking bars as children
        canvas.getBarsLayout().getChildren().clear();
        canvas.getBarsLayout().getChildren().addAll(bars);

        // STEP 3: Mark layout as dirty to trigger redraw
        canvas.getBarsLayout().markLayoutAsDirty();

        // STEP 4: Mark the drawer as dirty to ensure canvas is redrawn
        canvas.getBarsDrawer().markDrawAreaAsDirty();
    }

    /**
     * Returns the root container node for embedding in the UI.
     *
     * @return The root JavaFX Node containing the entire gantt chart UI
     */
    public Node getNode() {
        return containerNode;
    }

    /**
     * Gets the presenter (for advanced customization if needed).
     *
     * @return The GanttPresenter instance managing business logic
     */
    public GanttPresenter getPresenter() {
        return presenter;
    }

    /**
     * Cleanup method to remove listeners and prevent memory leaks.
     */
    public void cleanup() {
        // Remove listeners from data loader's observable lists
        if (resourceConfigListener != null) {
            dataLoader.getResourceConfigurations().removeListener(resourceConfigListener);
        }
        if (documentLineListener != null) {
            dataLoader.getDocumentLines().removeListener(documentLineListener);
        }
        if (attendanceGapListener != null) {
            dataLoader.getAttendancesForGaps().removeListener(attendanceGapListener);
        }
    }
}
