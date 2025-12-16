package one.modality.hotel.backoffice.activities.household.gantt.canvas;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.time.layout.bar.LocalDateBar;
import dev.webfx.platform.console.Console;
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
import one.modality.hotel.backoffice.activities.household.gantt.model.PoolInfo;
import one.modality.base.client.gantt.fx.highlight.FXGanttHighlight;
import one.modality.base.shared.entities.Pool;

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

    // Pool filter UI components
    private final HBox poolFilterGroup = new HBox(4);
    private final Map<Object, Button> poolFilterButtons = new LinkedHashMap<>();
    private Button allPoolButton;
    private List<PoolInfo> availablePools = new ArrayList<>();

    // Grouping toggle buttons
    private Button roomTypeGroupingButton;
    private Button zoneGroupingButton;

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
        filterManager.getActivePoolFilters().addListener((SetChangeListener<Object>) change -> refreshDisplay());

        // Listen to pool data changes to update filter buttons
        dataLoader.getSourcePools().addListener((ListChangeListener<Pool>) c -> updatePoolFilterButtons(dataLoader.getSourcePools()));
        dataLoader.getPoolAllocations().addListener((ListChangeListener<Object>) c -> refreshDisplay());

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
        statusLabel.getStyleClass().add("gantt-filter-label");
        statusLabel.setMinWidth(80);  // Fixed width for alignment (set in Java per guidelines)
        buildStatusFilterButtons();
        statusSection.getChildren().addAll(statusLabel, statusFilterGroup);

        // Grouping toggle section
        HBox groupingSection = new HBox(8);
        groupingSection.setAlignment(Pos.CENTER_LEFT);
        Label groupingLabel = new Label(I18n.getI18nText(HouseholdI18nKeys.GroupBy) + ":");
        groupingLabel.getStyleClass().add("gantt-filter-label");
        groupingLabel.setMinWidth(60);
        HBox groupingButtons = buildGroupingToggleButtons();
        groupingSection.getChildren().addAll(groupingLabel, groupingButtons);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Navigation controls group: [◀ Pan Left] [− Zoom Out] [+ Zoom In] [Pan Right ▶] | [Today]
        HBox navigationGroup = createNavigationControls();

        firstRow.getChildren().addAll(statusSection, groupingSection, spacer, navigationGroup);

        // Second row: Room type filter
        HBox secondRow = new HBox(8);
        secondRow.setAlignment(Pos.CENTER_LEFT);

        // Category filter section (dynamic - will be populated when data loads)
        HBox categorySection = new HBox(8);
        categorySection.setAlignment(Pos.CENTER_LEFT);
        Label categoryLabel = new Label("Room type:");
        categoryLabel.getStyleClass().add("gantt-filter-label");
        categoryLabel.setMinWidth(80);  // Same width for alignment (set in Java per guidelines)
        categorySection.getChildren().addAll(categoryLabel, categoryFilterGroup);

        secondRow.getChildren().add(categorySection);

        // Third row: Pool filter
        HBox thirdRow = new HBox(8);
        thirdRow.setAlignment(Pos.CENTER_LEFT);

        // Pool filter section (dynamic - will be populated when data loads)
        HBox poolSection = new HBox(8);
        poolSection.setAlignment(Pos.CENTER_LEFT);
        Label poolLabel = new Label(I18n.getI18nText(HouseholdI18nKeys.Pool) + ":");
        poolLabel.getStyleClass().add("gantt-filter-label");
        poolLabel.setMinWidth(80);  // Same width for alignment (set in Java per guidelines)
        poolSection.getChildren().addAll(poolLabel, poolFilterGroup);

        thirdRow.getChildren().add(poolSection);

        // Add all rows to container
        container.getChildren().addAll(firstRow, secondRow, thirdRow);

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
        btn.getStyleClass().add("gantt-today-btn");
        btn.setPadding(new Insets(6, 16, 6, 16));  // Comfortable padding (set in Java per guidelines)

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
     * Creates the navigation control group with pan, zoom, and "Today" buttons.
     * Modern design with clear visual grouping:
     * - Navigation group: [◂ ▸] for timeline panning
     * - Zoom group: [−] [+] for zoom control
     * - Today button: primary action
     */
    private HBox createNavigationControls() {
        HBox container = new HBox(12);
        container.setAlignment(Pos.CENTER_RIGHT);

        // ═══════════════════════════════════════════════════════════════════
        // NAVIGATION GROUP: Pan through time [◂] [▸]
        // ═══════════════════════════════════════════════════════════════════
        HBox navGroup = new HBox(1);
        navGroup.setAlignment(Pos.CENTER);
        navGroup.getStyleClass().add("gantt-nav-group");

        Button panLeftBtn = createIconButton("‹", "left");
        panLeftBtn.setOnAction(e -> panTimeWindow(-7));

        Button panRightBtn = createIconButton("›", "right");
        panRightBtn.setOnAction(e -> panTimeWindow(7));

        navGroup.getChildren().addAll(panLeftBtn, panRightBtn);

        // ═══════════════════════════════════════════════════════════════════
        // ZOOM GROUP: Adjust visible range [−] [2w] [+]
        // ═══════════════════════════════════════════════════════════════════
        HBox zoomGroup = new HBox(1);
        zoomGroup.setAlignment(Pos.CENTER);
        zoomGroup.getStyleClass().add("gantt-nav-group");

        Button zoomOutBtn = createIconButton("−", "left");
        zoomOutBtn.setOnAction(e -> zoomTimeWindow(1.5));

        Button zoomResetBtn = createZoomResetButton();
        zoomResetBtn.setOnAction(e -> resetZoom());

        Button zoomInBtn = createIconButton("+", "right");
        zoomInBtn.setOnAction(e -> zoomTimeWindow(0.67));

        zoomGroup.getChildren().addAll(zoomOutBtn, zoomResetBtn, zoomInBtn);

        // ═══════════════════════════════════════════════════════════════════
        // TODAY BUTTON: Primary action
        // ═══════════════════════════════════════════════════════════════════
        Button todayBtn = createGoToTodayButton();

        container.getChildren().addAll(navGroup, zoomGroup, todayBtn);
        return container;
    }

    /**
     * Creates an icon button for navigation controls.
     * Clean, minimal design with subtle hover effects.
     */
    private Button createIconButton(String icon, String position) {
        Button btn = new Button(icon);
        btn.getStyleClass().add("gantt-nav-btn");
        btn.setPadding(new Insets(6, 12, 6, 12));  // Padding set in Java per guidelines

        // Position-specific style class for border radius
        if ("left".equals(position)) {
            btn.getStyleClass().add("gantt-nav-btn-left");
        } else if ("right".equals(position)) {
            btn.getStyleClass().add("gantt-nav-btn-right");
        }

        return btn;
    }

    /**
     * Pans the time window by the specified number of days.
     * Positive values pan right (forward in time), negative values pan left (back in time).
     */
    private void panTimeWindow(int days) {
        java.time.LocalDate currentStart = canvas.getBarsLayout().getTimeWindowStart();
        java.time.LocalDate currentEnd = canvas.getBarsLayout().getTimeWindowEnd();

        if (currentStart != null && currentEnd != null) {
            java.time.LocalDate newStart = currentStart.plusDays(days);
            java.time.LocalDate newEnd = currentEnd.plusDays(days);
            canvas.getBarsLayout().setTimeWindow(newStart, newEnd);
        }
    }

    /**
     * Zooms the time window by the specified factor, keeping the center date fixed.
     * Factor > 1 zooms out (shows more days), factor < 1 zooms in (shows fewer days).
     * Minimum visible range is 7 days, maximum is 90 days.
     */
    private void zoomTimeWindow(double factor) {
        java.time.LocalDate currentStart = canvas.getBarsLayout().getTimeWindowStart();
        java.time.LocalDate currentEnd = canvas.getBarsLayout().getTimeWindowEnd();

        if (currentStart != null && currentEnd != null) {
            long currentDays = java.time.temporal.ChronoUnit.DAYS.between(currentStart, currentEnd);
            long newDays = Math.round(currentDays * factor);

            // Clamp to reasonable bounds (7 days minimum, 90 days maximum)
            newDays = Math.max(7, Math.min(90, newDays));

            // Keep the center date fixed
            java.time.LocalDate centerDate = currentStart.plusDays(currentDays / 2);
            java.time.LocalDate newStart = centerDate.minusDays(newDays / 2);
            java.time.LocalDate newEnd = newStart.plusDays(newDays);

            canvas.getBarsLayout().setTimeWindow(newStart, newEnd);
        }
    }

    /**
     * Resets the zoom to the default 14-day view (2 weeks), keeping the center date fixed.
     */
    private void resetZoom() {
        java.time.LocalDate currentStart = canvas.getBarsLayout().getTimeWindowStart();
        java.time.LocalDate currentEnd = canvas.getBarsLayout().getTimeWindowEnd();

        if (currentStart != null && currentEnd != null) {
            long currentDays = java.time.temporal.ChronoUnit.DAYS.between(currentStart, currentEnd);
            java.time.LocalDate centerDate = currentStart.plusDays(currentDays / 2);

            // Reset to 14 days (2 weeks) centered on current view
            java.time.LocalDate newStart = centerDate.minusDays(7);
            java.time.LocalDate newEnd = centerDate.plusDays(7);

            canvas.getBarsLayout().setTimeWindow(newStart, newEnd);
        }
    }

    /**
     * Creates the zoom reset button showing "2w" (2 weeks = default view).
     * Styled to fit in the middle of the zoom button group.
     */
    private Button createZoomResetButton() {
        Button btn = new Button("2w");
        btn.getStyleClass().add("gantt-zoom-reset-btn");
        btn.setPadding(new Insets(6, 8, 6, 8));  // Padding set in Java per guidelines

        return btn;
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
     * Builds the grouping toggle buttons (Room Type / Zone).
     * @return HBox containing the toggle buttons
     */
    private HBox buildGroupingToggleButtons() {
        HBox buttons = new HBox(4);
        buttons.setAlignment(Pos.CENTER_LEFT);

        // Room Type button (default active)
        roomTypeGroupingButton = createFilterToggleButton(I18n.getI18nText(HouseholdI18nKeys.RoomType), true, null);
        roomTypeGroupingButton.setOnAction(e -> {
            canvas.setGrandparentGrouping(HouseholdGanttCanvas.GrandparentGrouping.ROOM_TYPE);
            updateGroupingButtonStyles();
            refreshDisplay();
        });

        // Zone button
        zoneGroupingButton = createFilterToggleButton(I18n.getI18nText(HouseholdI18nKeys.Zone), false, null);
        zoneGroupingButton.setOnAction(e -> {
            canvas.setGrandparentGrouping(HouseholdGanttCanvas.GrandparentGrouping.ZONE);
            updateGroupingButtonStyles();
            refreshDisplay();
        });

        buttons.getChildren().addAll(roomTypeGroupingButton, zoneGroupingButton);
        return buttons;
    }

    /**
     * Updates grouping button styles based on current grouping mode.
     */
    private void updateGroupingButtonStyles() {
        boolean isRoomTypeActive = canvas.getGrandparentGrouping() == HouseholdGanttCanvas.GrandparentGrouping.ROOM_TYPE;
        updateFilterButtonStyle(roomTypeGroupingButton, isRoomTypeActive, null);
        updateFilterButtonStyle(zoneGroupingButton, !isRoomTypeActive, null);
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
        btn.setPadding(new Insets(4, 12, 4, 12));  // Padding set in Java per guidelines
        btn.getStyleClass().add("gantt-filter-btn");

        if (statusColor != null) {
            // Create button with colored status dot
            HBox content = new HBox(6);
            content.setAlignment(Pos.CENTER_LEFT);

            // Status dot (circle)
            javafx.scene.shape.Circle dot = new javafx.scene.shape.Circle(5);
            dot.setFill(statusColor);

            Label label = new Label(text);
            label.getStyleClass().add("gantt-filter-btn-label");
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
     * Uses CSS classes instead of inline styles for cross-platform compatibility.
     * @param btn The button to style
     * @param isActive Whether the button is currently active/selected
     * @param statusColor Optional status color (for buttons with colored dots)
     */
    private void updateFilterButtonStyle(Button btn, boolean isActive, Color statusColor) {
        btn.getStyleClass().removeAll("gantt-filter-btn-active");

        if (isActive) {
            btn.getStyleClass().add("gantt-filter-btn-active");
        }
        // Note: The base styling comes from "gantt-filter-btn" class added in createFilterToggleButton
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
     * Updates pool filter buttons based on available pools from data.
     * Creates buttons with colored dots using Pool.webColor.
     */
    private void updatePoolFilterButtons(List<Pool> pools) {
        // Convert to PoolInfo for comparison and storage
        List<PoolInfo> newPools = new ArrayList<>();
        for (Pool pool : pools) {
            newPools.add(new PoolInfo(pool.getPrimaryKey(), pool.getName(), pool.getWebColor()));
        }

        // Check if pools have changed
        if (newPools.equals(availablePools)) {
            return; // No change needed
        }
        availablePools = newPools;

        poolFilterGroup.getChildren().clear();
        poolFilterButtons.clear();

        if (pools.isEmpty()) {
            return; // No pools to show
        }

        // "All" button
        allPoolButton = createFilterToggleButton(I18n.getI18nText(HouseholdI18nKeys.All), true, null);
        allPoolButton.setOnAction(e -> {
            filterManager.getActivePoolFilters().clear();
            updatePoolButtonStyles();
        });
        poolFilterGroup.getChildren().add(allPoolButton);

        // Pool buttons with colored dots
        for (PoolInfo poolInfo : availablePools) {
            Color poolColor = parsePoolColor(poolInfo.webColor());
            Button btn = createFilterToggleButton(poolInfo.name(), false, poolColor);
            btn.setOnAction(e -> {
                filterManager.togglePoolFilter(poolInfo.id());
                updatePoolButtonStyles();
            });
            poolFilterButtons.put(poolInfo.id(), btn);
            poolFilterGroup.getChildren().add(btn);
        }
    }

    /**
     * Parses a web color string (e.g., "#FF5733") to JavaFX Color.
     * Returns a default color if parsing fails.
     */
    private Color parsePoolColor(String webColor) {
        if (webColor == null || webColor.isEmpty()) {
            return Color.GRAY; // Default color
        }
        try {
            return Color.web(webColor);
        } catch (Exception e) {
            return Color.GRAY; // Fallback on parse error
        }
    }

    /**
     * Updates pool button styles based on current filter state.
     */
    private void updatePoolButtonStyles() {
        boolean noFiltersActive = filterManager.getActivePoolFilters().isEmpty();
        if (allPoolButton != null) {
            updateFilterButtonStyle(allPoolButton, noFiltersActive, null);
        }

        for (Map.Entry<Object, Button> entry : poolFilterButtons.entrySet()) {
            boolean isActive = filterManager.isPoolFilterActive(entry.getKey());
            // Find the pool color for this button
            Color poolColor = null;
            for (PoolInfo poolInfo : availablePools) {
                if (poolInfo.id().equals(entry.getKey())) {
                    poolColor = parsePoolColor(poolInfo.webColor());
                    break;
                }
            }
            updateFilterButtonStyle(entry.getValue(), isActive, poolColor);
        }
    }

    /**
     * Clears all active filters.
     */
    private void clearAllFilters() {
        filterManager.clearAllFilters();
        updateStatusButtonStyles();
        updateCategoryButtonStyles();
        updatePoolButtonStyles();
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

            // Update hover position for row highlighting
            canvas.setHoveredMouseY(mouseY);

            // Update day header highlight in the event Gantt canvas (DatedGanttCanvas)
            // Convert mouse X position to a date and set it as the highlighted day
            java.time.LocalDate hoveredDay = canvas.getBarsLayout().getTimeProjector().xToTime(mouseX);
            FXGanttHighlight.setGanttHighlighted(hoveredDay);

            // Check if hovering over the room header area
            if (mouseX <= parentHeaderWidth) {
                // Adjust Y coordinate for scroll position using layoutOriginY
                // layoutOriginY is positive when scrolled down, so we add it to get absolute Y
                double adjustedY = mouseY + canvas.getBarsDrawer().getOriginLayoutY();

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
                            canvas.getCanvasTooltip().hide();
                            return;
                        }

                        // Check if hovering over room name/comment area (for rooms with comments)
                        // Tooltip shows when hovering anywhere on the room name or comment text
                        if (!ganttParent.isBed() && ganttParent.room() != null) {
                            String roomComment = ganttParent.room().getRoomComments();
                            if (roomComment != null && !roomComment.isEmpty()) {
                                // Get row bounds to calculate text area
                                dev.webfx.extras.geometry.Bounds headerBounds = parentRow.getHeader();
                                if (headerBounds != null) {
                                    double rowTop = headerBounds.getMinY() - canvas.getBarsDrawer().getOriginLayoutY();
                                    double centerY = rowTop + headerBounds.getHeight() / 2;

                                    // When comment exists, room name is at centerY - 6, comment at centerY + 8
                                    // Cover both lines: from room name top to comment bottom
                                    double textAreaYStart = centerY - 14;  // Above room name
                                    double textAreaYEnd = centerY + 16;    // Below comment

                                    // Text X starts after arrow+gap (around 28px from left)
                                    double textAreaXStart = 28;

                                    if (mouseY >= textAreaYStart && mouseY <= textAreaYEnd &&
                                        mouseX >= textAreaXStart && mouseX < parentHeaderWidth - 30) {
                                        // Show tooltip with full comment
                                        canvas.getCanvasTooltip().show(
                                            mouseX + 15, mouseY - 10,
                                            ganttParent.room().getName(),
                                            roomComment);
                                        canvasNode.setCursor(javafx.scene.Cursor.DEFAULT);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
                // Hide tooltip when not hovering over comment
                canvas.getCanvasTooltip().hide();
            } else {
                // Check if hovering over a booking bar with an icon
                double adjustedX = mouseX + canvas.getBarsDrawer().getOriginLayoutX();
                double adjustedY = mouseY + canvas.getBarsDrawer().getOriginLayoutY();

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
                // Hide tooltip when hovering over bars area (not over room header comment)
                canvas.getCanvasTooltip().hide();
            }
            canvasNode.setCursor(javafx.scene.Cursor.DEFAULT);
        });

        // Clear hover highlight when mouse exits the canvas
        canvasNode.setOnMouseExited(event -> {
            canvas.setHoveredMouseY(-1);
            FXGanttHighlight.setGanttHighlighted(null);
            canvas.getCanvasTooltip().hide();
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
            double layoutOriginY = canvas.getBarsDrawer().getOriginLayoutY();
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
            Console.log("[HouseholdCanvasGanttView] Cannot update status: Room " + room.getName() + " has no Resource entity");
            return;
        }

        UpdateStore updateStore = UpdateStore.createAbove(resource.getStore());
        Resource r = updateStore.updateEntity(resource);
        LocalDateTime now = LocalDateTime.now();

        switch (newStatus) {
            case TO_CLEAN:
                // Set state to DIRTY - keep date fields for historical tracking
                r.setCleaningState(CleaningState.DIRTY);
                break;
            case TO_INSPECT:
                // Set state to TO_INSPECT and update cleaning date
                // Keep lastInspectionDate for historical tracking
                r.setCleaningState(CleaningState.TO_INSPECT);
                r.setLastCleaningDate(now);
                break;
            case READY:
                // Set state to READY
                r.setCleaningState(CleaningState.READY);
                // Only update cleaning date if skipping inspection (TO_CLEAN -> READY)
                // If coming from TO_INSPECT, the cleaning was already done
                if (room.getStatus() == one.modality.hotel.backoffice.activities.household.gantt.model.RoomStatus.TO_CLEAN) {
                    r.setLastCleaningDate(now);
                }
                r.setLastInspectionDate(now);
                break;
            case OCCUPIED:
                // Occupied status is determined by bookings, not by these dates
                // This case shouldn't be called, but handle gracefully
                return;
        }

        // Submit changes to database
        updateStore.submitChanges()
                .onFailure(error -> Console.log("[HouseholdCanvasGanttView] Failed to update room status: " + error.getMessage()));
    }

    /**
     * Handles clicks on booking bar icons (person icon and special needs icon).
     * Detects which bar and icon was clicked, then shows appropriate tooltip.
     */
    private void handleBarIconClick(double clickX, double adjustedY, javafx.scene.input.MouseEvent event) {
        // Adjust coordinates for scroll offset
        double adjustedX = clickX + canvas.getBarsDrawer().getOriginLayoutX();

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

        body.append("\n").append(I18n.getI18nText(HouseholdI18nKeys.CheckIn)).append(": ").append(formatDate(booking.getStartDate()));
        // Checkout is the day after the last night stayed (endDate + 1)
        java.time.LocalDate checkoutDate = booking.getEndDate() != null ? booking.getEndDate().plusDays(1) : null;
        body.append("\n").append(I18n.getI18nText(HouseholdI18nKeys.CheckOut)).append(": ").append(formatDate(checkoutDate));

        // Show canvas-based tooltip at mouse position
        CanvasTooltip tooltip = canvas.getCanvasTooltip();
        tooltip.show(event.getX() + 10, event.getY(), I18n.getI18nText(HouseholdI18nKeys.GuestInformation), body.toString());
    }

    /**
     * Formats a date for display in tooltips.
     * Format: "Friday, 28 November 2025"
     */
    private String formatDate(java.time.LocalDate date) {
        if (date == null) {
            return "-";
        }
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern(
            "EEEE, d MMMM yyyy", java.util.Locale.ENGLISH);
        return date.format(formatter);
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
        // Checkout is the day after the last night stayed (endDate + 1)
        java.time.LocalDate checkoutDate = booking.getEndDate() != null ? booking.getEndDate().plusDays(1) : null;
        body.append(formatDate(booking.getStartDate())).append(" - ").append(formatDate(checkoutDate)).append("\n");

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
        // Pass pool allocations to support pool filtering
        List<GanttRoomData> allRooms = EntityDataAdapter.adaptRooms(
            dataLoader.getResourceConfigurations(),
            dataLoader.getDocumentLines(),
            dataLoader.getAttendancesForGaps(),
            dataLoader.getPoolAllocations()
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
            Console.log("[HouseholdCanvasGanttView] Error displaying bars: " + e.getMessage());
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
        // DEBUG: Check room 207 specifically - also log category and zone
        Console.log("[DEBUG-207] displayRoomsAndBars: grouping=" + canvas.getGrandparentGrouping());
        GanttParentRow first207 = null;
        GanttParentRow second207 = null;
        for (GanttParentRow parent : parentRows) {
            if (parent.room() != null && "207".equals(parent.room().getName())) {
                Console.log("[DEBUG-207] displayRoomsAndBars: Found 207 in parentRows" +
                            ", beds=" + parent.room().getBeds().size() +
                            ", isMultiBedRoom=" + parent.isMultiBedRoom() +
                            ", category=" + parent.getCategory() +
                            ", zone=" + parent.getZone() +
                            ", identityHashCode=" + System.identityHashCode(parent) +
                            ", record.hashCode()=" + parent.hashCode());
                if (first207 == null) {
                    first207 = parent;
                } else {
                    second207 = parent;
                }
            }
        }
        // Check if the two 207 records are equal (they shouldn't be!)
        if (first207 != null && second207 != null) {
            Console.log("[DEBUG-207] EQUALITY CHECK: first207.equals(second207)=" + first207.equals(second207) +
                       ", first207.hashCode()=" + first207.hashCode() +
                       ", second207.hashCode()=" + second207.hashCode() +
                       ", room1.equals(room2)=" + first207.room().equals(second207.room()));
        }

        // STEP 1: Set the parent rows in the layout
        canvas.getBarsLayout().getParents().setAll(parentRows);

        // DEBUG: Check what the layout actually contains after setAll
        Console.log("[DEBUG-207] After setAll - layout.getParents().size()=" + canvas.getBarsLayout().getParents().size());
        for (Object p : canvas.getBarsLayout().getParents()) {
            if (p instanceof GanttParentRow gpr && gpr.room() != null && "207".equals(gpr.room().getName())) {
                Console.log("[DEBUG-207] After setAll - Found 207 in layout.getParents(): beds=" + gpr.room().getBeds().size() +
                           ", hashCode=" + System.identityHashCode(gpr));
            }
        }

        // STEP 2: Set the booking bars as children
        canvas.getBarsLayout().getChildren().clear();
        canvas.getBarsLayout().getChildren().addAll(bars);

        // STEP 3: Mark layout as dirty to trigger redraw
        canvas.getBarsLayout().markLayoutAsDirty();

        // DEBUG: Check what getParentRows() returns (the processed internal list used for drawing)
        Console.log("[DEBUG-207] After markLayoutAsDirty - getParentRows().size()=" + canvas.getBarsLayout().getParentRows().size());
        for (var pr : canvas.getBarsLayout().getParentRows()) {
            Object p = pr.getParent();
            if (p instanceof GanttParentRow gpr && gpr.room() != null && "207".equals(gpr.room().getName())) {
                Console.log("[DEBUG-207] After markLayoutAsDirty - Found 207 in getParentRows(): beds=" + gpr.room().getBeds().size() +
                           ", hashCode=" + System.identityHashCode(gpr));
            }
        }

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
     * Forces a redraw of the canvas.
     * Should be called when the Gantt tab becomes visible again to ensure the canvas is properly rendered.
     * This fixes an issue where the canvas may not render after switching away and back to the Gantt tab.
     */
    public void forceRedraw() {
        // Mark both layout and drawer as dirty to ensure full redraw
        canvas.getBarsLayout().markLayoutAsDirty();
        canvas.getBarsDrawer().markDrawAreaAsDirty();
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
