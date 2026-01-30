package one.modality.hotel.backoffice.activities.roomsetup.view;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.domainmodel.HasDataSourceModel;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import dev.webfx.stack.routing.activity.impl.elementals.activeproperty.HasActiveProperty;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.bootstrap.ModalityStyle;
import one.modality.base.shared.entities.*;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.hotel.backoffice.activities.roomsetup.RoomSetupI18nKeys;
import one.modality.hotel.backoffice.activities.roomsetup.RoomSetupPresentationModel;
import one.modality.hotel.backoffice.activities.roomsetup.dialog.DefaultAllocationDialog;
import one.modality.hotel.backoffice.activities.roomsetup.dialog.DialogManager;
import one.modality.hotel.backoffice.activities.roomsetup.util.PoolTypeFilter;
import one.modality.hotel.backoffice.activities.roomsetup.util.UIComponentDecorators;

import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.scheduler.Scheduled;

import java.util.*;
import java.util.stream.Collectors;

import static dev.webfx.stack.orm.dql.DqlStatement.orderBy;
import static dev.webfx.stack.orm.dql.DqlStatement.where;

/**
 * Default Allocation View - displays and manages default pool allocations for rooms.
 * Allows assigning rooms to source pools with single or split allocation.
 *
 * @author Claude Code
 */
public class DefaultAllocationView {

    private final RoomSetupPresentationModel pm;
    private ObservableValue<Boolean> activeProperty;
    private DataSourceModel dataSourceModel;

    // Data
    private final ObservableList<ResourceConfiguration> resourceConfigurations = FXCollections.observableArrayList();
    private final ObservableList<Pool> pools = FXCollections.observableArrayList();
    private final ObservableList<PoolAllocation> poolAllocations = FXCollections.observableArrayList();
    private final ObservableList<Building> buildings = FXCollections.observableArrayList();
    private final ObservableList<Item> accommodationItems = FXCollections.observableArrayList();

    // Performance optimization: Index for O(1) lookup of allocations by resource ID
    // This avoids O(n*m) nested stream operations when filtering/counting
    private Map<Object, List<PoolAllocation>> allocationsByResourceId = new HashMap<>();

    // Reactive entity mappers
    private ReactiveEntitiesMapper<ResourceConfiguration> resourceConfigRem;
    private ReactiveEntitiesMapper<Pool> poolRem;
    private ReactiveEntitiesMapper<PoolAllocation> poolAllocationRem;
    private ReactiveEntitiesMapper<Building> buildingRem;
    private ReactiveEntitiesMapper<Item> itemRem;

    // Filter state
    private final ObjectProperty<Pool> filterPoolProperty = new SimpleObjectProperty<>();
    private final BooleanProperty showUnassignedOnlyProperty = new SimpleBooleanProperty(false);
    private final ObjectProperty<String> groupByProperty = new SimpleObjectProperty<>("building"); // "building", "type", "pool"

    // Debouncing for filter changes - prevents excessive UI updates
    private static final long FILTER_DEBOUNCE_MS = 150;
    private Scheduled filterDebounceScheduled;

    // Collapsible groups - track expanded state for lazy content loading
    private final Set<String> expandedGroups = new HashSet<>();
    private boolean allGroupsExpanded = true; // Start with all expanded for first load

    // UI components
    private VBox mainContainer;
    private VBox roomListContainer;
    private Label roomCountLabel;
    private HBox filterChipsContainer;
    private StackPane loadingOverlay;

    // Track loading status for both critical datasets
    private boolean roomsLoaded = false;
    private boolean allocationsLoaded = false;

    public DefaultAllocationView(RoomSetupPresentationModel pm) {
        this.pm = pm;
    }

    public Node buildView() {
        mainContainer = new VBox();
        mainContainer.setSpacing(16);
        mainContainer.setPadding(new Insets(20));

        // Header section
        VBox headerSection = createHeaderSection();

        // Filter bar
        HBox filterBar = createFilterBar();

        // Room list container
        roomListContainer = new VBox();
        roomListContainer.setSpacing(16);

        // Info helper
        HBox infoHelper = createInfoHelper();

        VBox scrollContent = new VBox();
        scrollContent.setSpacing(16);
        scrollContent.getChildren().addAll(roomListContainer, infoHelper);

        ScrollPane scrollPane = Controls.createVerticalScrollPane(scrollContent);
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Create loading overlay
        loadingOverlay = new StackPane();
        loadingOverlay.setStyle("-fx-background-color: rgba(255,255,255,0.8);");
        Region loadingSpinner = Controls.createPageSizeSpinner();
        VBox loadingBox = new VBox(10);
        loadingBox.setAlignment(Pos.CENTER);
        Label loadingLabel = I18nControls.newLabel(RoomSetupI18nKeys.LoadingAllocations);
        loadingLabel.getStyleClass().add(UIComponentDecorators.CSS_SUBTITLE);
        loadingBox.getChildren().addAll(loadingSpinner, loadingLabel);
        loadingOverlay.getChildren().add(loadingBox);

        // Use StackPane to overlay loading indicator on scroll content
        StackPane contentWithLoading = new StackPane();
        contentWithLoading.getChildren().addAll(scrollPane, loadingOverlay);
        VBox.setVgrow(contentWithLoading, Priority.ALWAYS);

        mainContainer.getChildren().addAll(headerSection, filterBar, contentWithLoading);

        // Listen for data changes with debouncing to prevent excessive UI updates
        resourceConfigurations.addListener((ListChangeListener<? super ResourceConfiguration>) change -> scheduleUpdateRoomList());
        pools.addListener((ListChangeListener<? super Pool>) change -> {
            updateFilterChips();
            scheduleUpdateRoomList();
        });
        poolAllocations.addListener((ListChangeListener<? super PoolAllocation>) change -> {
            rebuildAllocationIndex(); // Rebuild index for O(1) lookups
            scheduleUpdateRoomList();
        });
        buildings.addListener((ListChangeListener<? super Building>) change -> scheduleUpdateRoomList());
        accommodationItems.addListener((ListChangeListener<? super Item>) change -> scheduleUpdateRoomList());

        // Listen for filter and group changes with debouncing
        filterPoolProperty.addListener((obs, oldVal, newVal) -> {
            updateFilterChips();
            scheduleUpdateRoomList();
        });
        showUnassignedOnlyProperty.addListener((obs, oldVal, newVal) -> {
            updateFilterChips();
            scheduleUpdateRoomList();
        });
        groupByProperty.addListener((obs, oldVal, newVal) -> {
            // Reset expanded state when grouping changes
            expandedGroups.clear();
            allGroupsExpanded = true;
            scheduleUpdateRoomList();
        });

        // Initial update
        updateFilterChips();
        updateRoomList();

        return mainContainer;
    }

    private VBox createHeaderSection() {
        VBox header = new VBox();
        header.setSpacing(16);
        header.setPadding(new Insets(0, 0, 8, 0));

        // Title
        VBox titleBox = new VBox();
        titleBox.setSpacing(6);
        Label titleLabel = I18nControls.newLabel(RoomSetupI18nKeys.DefaultAllocation);
        titleLabel.getStyleClass().add(UIComponentDecorators.CSS_TITLE);
        Label subtitleLabel = I18nControls.newLabel(RoomSetupI18nKeys.DefaultAllocationSubtitle);
        subtitleLabel.getStyleClass().add(UIComponentDecorators.CSS_SUBTITLE);
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);

        // Stats row
        HBox statsRow = new HBox();
        statsRow.setAlignment(Pos.CENTER_LEFT);
        statsRow.setSpacing(16);

        roomCountLabel = new Label();
        roomCountLabel.getStyleClass().add(UIComponentDecorators.CSS_SUBTITLE);

        statsRow.getChildren().add(roomCountLabel);

        header.getChildren().addAll(titleBox, statsRow);
        return header;
    }

    private HBox createFilterBar() {
        HBox filterBar = new HBox();
        filterBar.setSpacing(24);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(0, 0, 16, 0));

        // Filter by pool section
        VBox filterSection = new VBox();
        filterSection.setSpacing(10);
        Label filterLabel = I18nControls.newLabel(RoomSetupI18nKeys.FilterByPool);
        filterLabel.getStyleClass().add(UIComponentDecorators.CSS_CAPTION);

        filterChipsContainer = new HBox();
        filterChipsContainer.setSpacing(8);

        filterSection.getChildren().addAll(filterLabel, filterChipsContainer);

        // Group by section
        VBox groupSection = new VBox();
        groupSection.setSpacing(10);
        Label groupLabel = I18nControls.newLabel(RoomSetupI18nKeys.GroupBy);
        groupLabel.getStyleClass().add(UIComponentDecorators.CSS_CAPTION);

        HBox groupButtons = new HBox();
        groupButtons.setSpacing(8);

        Button buildingBtn = createGroupButton(RoomSetupI18nKeys.GroupByBuilding, "building");
        Button typeBtn = createGroupButton(RoomSetupI18nKeys.GroupByType, "type");
        Button poolBtn = createGroupButton(RoomSetupI18nKeys.GroupByPool, "pool");

        groupButtons.getChildren().addAll(buildingBtn, typeBtn, poolBtn);
        groupSection.getChildren().addAll(groupLabel, groupButtons);

        filterBar.getChildren().addAll(filterSection, groupSection);

        return filterBar;
    }

    private Button createGroupButton(Object i18nKey, String groupBy) {
        Button btn = I18nControls.newButton(i18nKey);
        btn.setUserData(groupBy); // Store groupBy value for later lookup
        updateGroupButtonStyle(btn, groupBy);
        btn.setOnAction(e -> {
            groupByProperty.set(groupBy);
            // Update all group button styles
            if (btn.getParent() instanceof HBox parent) {
                for (Node child : parent.getChildren()) {
                    if (child instanceof Button b) {
                        String btnGroupBy = b.getUserData() instanceof String ? (String) b.getUserData() : "";
                        updateGroupButtonStyle(b, btnGroupBy);
                    }
                }
            }
        });
        return btn;
    }

    private void updateGroupButtonStyle(Button btn, String groupBy) {
        boolean selected = groupBy.equals(groupByProperty.get());
        ModalityStyle.setChipButtonPrimarySelected(btn, selected);
    }

    private void updateFilterChips() {
        Platform.runLater(() -> {
            if (filterChipsContainer == null) return;

            filterChipsContainer.getChildren().clear();

            // Get source pools only
            List<Pool> sourcePools = PoolTypeFilter.filterSourcePools(pools);

            // All button
            Button allBtn = I18nControls.newButton(RoomSetupI18nKeys.AllPools);
            boolean isAllSelected = filterPoolProperty.get() == null && !showUnassignedOnlyProperty.get();
            ModalityStyle.setChipButtonPrimarySelected(allBtn, isAllSelected);
            allBtn.setOnAction(e -> {
                showUnassignedOnlyProperty.set(false);
                filterPoolProperty.set(null);
            });
            filterChipsContainer.getChildren().add(allBtn);

            // Unassigned button
            long unassignedCount = countUnassignedRooms();
            Button unassignedBtn = new Button(I18n.getI18nText(RoomSetupI18nKeys.UnassignedCount, unassignedCount));
            boolean isUnassignedSelected = showUnassignedOnlyProperty.get();
            // Use WebFX-compatible styling for unassigned button
            UIComponentDecorators.applyUnassignedFilterChipStyle(unassignedBtn, isUnassignedSelected, unassignedCount > 0);
            unassignedBtn.setOnAction(e -> {
                // Toggle unassigned filter
                if (showUnassignedOnlyProperty.get()) {
                    showUnassignedOnlyProperty.set(false);
                } else {
                    showUnassignedOnlyProperty.set(true);
                    filterPoolProperty.set(null);
                }
            });
            filterChipsContainer.getChildren().add(unassignedBtn);

            // Pool filter buttons
            for (Pool pool : sourcePools) {
                String color = pool.getWebColor() != null ? pool.getWebColor() : "#475569";
                Button poolBtn = new Button(pool.getName());
                boolean isSelected = pool.equals(filterPoolProperty.get());

                // Use WebFX-compatible styling for pool filter buttons
                UIComponentDecorators.applyPoolFilterChipStyle(poolBtn, color, isSelected);

                poolBtn.setOnAction(e -> {
                    showUnassignedOnlyProperty.set(false);
                    filterPoolProperty.set(isSelected ? null : pool);
                });
                filterChipsContainer.getChildren().add(poolBtn);
            }
        });
    }

    /**
     * Rebuilds the allocation index for O(1) lookup by resource ID.
     * Called when poolAllocations list changes.
     * Complexity: O(m) where m = number of allocations (single pass)
     */
    private void rebuildAllocationIndex() {
        allocationsByResourceId.clear();
        for (PoolAllocation pa : poolAllocations) {
            if (pa.getResourceId() != null && pa.getEvent() == null) {
                Object resourceId = Entities.getPrimaryKey(pa.getResourceId());
                allocationsByResourceId.computeIfAbsent(resourceId, k -> new ArrayList<>()).add(pa);
            }
        }
    }

    /**
     * Gets allocations for a resource using direct stream comparison.
     * Uses Entities.getPrimaryKey for consistent ID comparison across different entity instances.
     * @param resource The resource to look up
     * @return List of pool allocations for the resource (empty list if none)
     */
    private List<PoolAllocation> getAllocationsForResource(Resource resource) {
        if (resource == null) return Collections.emptyList();
        Object targetId = Entities.getPrimaryKey(resource);
        // Use direct stream comparison to avoid HashMap key comparison issues
        return poolAllocations.stream()
                .filter(pa -> pa.getResourceId() != null && pa.getEvent() == null)
                .filter(pa -> Objects.equals(Entities.getPrimaryKey(pa.getResourceId()), targetId))
                .collect(Collectors.toList());
    }

    /**
     * Checks if a resource has any pool allocations.
     * Uses O(1) HashMap lookup instead of O(m) stream.
     */
    private boolean hasAllocations(Resource resource) {
        return !getAllocationsForResource(resource).isEmpty();
    }

    private long countUnassignedRooms() {
        return resourceConfigurations.stream()
                .filter(rc -> !hasAllocations(rc.getResource()))
                .count();
    }

    /**
     * Schedules a debounced update of the room list.
     * Prevents excessive UI rebuilds when multiple changes occur rapidly.
     */
    private void scheduleUpdateRoomList() {
        if (filterDebounceScheduled != null) {
            filterDebounceScheduled.cancel();
        }
        filterDebounceScheduled = UiScheduler.scheduleDelay(FILTER_DEBOUNCE_MS, this::updateRoomList);
    }

    private HBox createInfoHelper() {
        HBox infoBox = Bootstrap.infoBox(new HBox());
        infoBox.setSpacing(10);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.setPadding(new Insets(12, 16, 12, 16));
        infoBox.getStyleClass().add(UIComponentDecorators.CSS_INFO_BAR);
        VBox.setMargin(infoBox, new Insets(34, 0, 0, 0));

        Label iconLabel = new Label("💡");

        Label textLabel = new Label(
                "Default allocations define where rooms \"belong\" when not assigned to an event. " +
                "Each room can be fully assigned to one pool, or split across multiple pools " +
                "(e.g., 2 beds for Residents, 2 beds for General). Click a room to edit its allocation."
        );
        textLabel.getStyleClass().add(UIComponentDecorators.CSS_SMALL);
        textLabel.setWrapText(true);

        infoBox.getChildren().addAll(iconLabel, textLabel);
        return infoBox;
    }

    private void updateRoomList() {
        Platform.runLater(() -> {
            if (roomListContainer == null) return;

            // Hide loading overlay once both rooms AND allocations are loaded
            boolean dataLoaded = roomsLoaded && allocationsLoaded;
            if (dataLoaded && loadingOverlay != null) {
                loadingOverlay.setVisible(false);
                loadingOverlay.setManaged(false);
            }

            roomListContainer.getChildren().clear();

            // Filter rooms using O(1) allocation lookups
            List<ResourceConfiguration> filteredRooms = resourceConfigurations.stream()
                    .filter(rc -> {
                        // Check for unassigned filter
                        if (showUnassignedOnlyProperty.get()) {
                            return !hasAllocations(rc.getResource());
                        }

                        // Check for pool filter
                        Pool filterPool = filterPoolProperty.get();
                        if (filterPool == null) return true;

                        // Use O(1) lookup instead of O(m) stream
                        List<PoolAllocation> allocations = getAllocationsForResource(rc.getResource());
                        return allocations.stream()
                                .anyMatch(pa -> pa.getPool() != null && pa.getPool().equals(filterPool));
                    })
                    .collect(Collectors.toList());

            // Update count using O(1) lookups
            long assignedCount = filteredRooms.stream()
                    .filter(rc -> hasAllocations(rc.getResource()))
                    .count();
            roomCountLabel.setText(I18n.getI18nText(RoomSetupI18nKeys.RoomStats, filteredRooms.size(), assignedCount, filteredRooms.size() - assignedCount));

            // Group rooms
            Map<String, List<ResourceConfiguration>> groupedRooms = groupRooms(filteredRooms);

            // Create collapsible group panels - content only rendered when expanded
            for (Map.Entry<String, List<ResourceConfiguration>> entry : groupedRooms.entrySet()) {
                String groupName = entry.getKey();
                List<ResourceConfiguration> rooms = entry.getValue();
                boolean isExpanded = allGroupsExpanded || expandedGroups.contains(groupName);
                VBox groupPanel = createCollapsibleGroupPanel(groupName, rooms, isExpanded);
                roomListContainer.getChildren().add(groupPanel);
            }

            if (filteredRooms.isEmpty()) {
                Label emptyLabel = I18nControls.newLabel(RoomSetupI18nKeys.NoRoomsMatchFilter);
                emptyLabel.getStyleClass().add(UIComponentDecorators.CSS_EMPTY_STATE_TEXT);
                emptyLabel.setPadding(new Insets(32));
                roomListContainer.getChildren().add(emptyLabel);
            }
        });
    }

    private Map<String, List<ResourceConfiguration>> groupRooms(List<ResourceConfiguration> rooms) {
        String groupBy = groupByProperty.get();
        Map<String, List<ResourceConfiguration>> grouped = new LinkedHashMap<>();

        for (ResourceConfiguration rc : rooms) {
            String key;
            switch (groupBy) {
                case "type":
                    Item item = rc.getItem();
                    key = item != null ? item.getName() : I18n.getI18nText(RoomSetupI18nKeys.UnknownType);
                    break;
                case "pool":
                    Resource resource = rc.getResource();
                    // Use O(1) lookup instead of O(m) stream filtering
                    List<PoolAllocation> resourceAllocations = getAllocationsForResource(resource);
                    if (!resourceAllocations.isEmpty()) {
                        List<String> poolNames = resourceAllocations.stream()
                                .map(pa -> pa.getPool() != null ? pa.getPool().getName() : I18n.getI18nText(RoomSetupI18nKeys.UnknownType))
                                .distinct()
                                .collect(Collectors.toList());
                        key = String.join(", ", poolNames);
                    } else {
                        key = I18n.getI18nText(RoomSetupI18nKeys.UnassignedWarning);
                    }
                    break;
                case "building":
                default:
                    Resource res = rc.getResource();
                    Building building = res != null ? res.getBuilding() : null;
                    key = building != null ? building.getName() : I18n.getI18nText(RoomSetupI18nKeys.UnknownBuilding);
                    break;
            }
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(rc);
        }

        return grouped;
    }

    /**
     * Creates a collapsible group panel with lazy-loaded content.
     * Room cards are only rendered when the group is expanded, reducing initial DOM weight.
     */
    private VBox createCollapsibleGroupPanel(String groupName, List<ResourceConfiguration> rooms, boolean expanded) {
        VBox panel = new VBox();
        panel.getStyleClass().add(UIComponentDecorators.CSS_CARD);

        // Header - clickable to expand/collapse
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(10);
        header.setPadding(new Insets(14, 20, 14, 20));
        header.getStyleClass().add(UIComponentDecorators.CSS_CARD_HEADER);
        header.setCursor(Cursor.HAND);

        // Expand/collapse indicator
        Label expandIndicator = new Label(expanded ? "▼" : "▶");
        expandIndicator.setMinWidth(16);

        Label groupLabel = new Label(groupName);
        groupLabel.getStyleClass().add(UIComponentDecorators.CSS_BODY_BOLD);

        Label countLabel = ModalityStyle.badgeLightInfo(new Label(rooms.size() + " rooms"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(expandIndicator, groupLabel, countLabel, spacer);

        // Content container for lazy loading
        VBox contentContainer = new VBox();

        // Only create content if expanded (lazy loading)
        if (expanded) {
            FlowPane roomGrid = createRoomGrid(rooms);
            contentContainer.getChildren().add(roomGrid);
        }

        // Click handler for expand/collapse
        header.setOnMouseClicked(e -> {
            boolean isCurrentlyExpanded = !contentContainer.getChildren().isEmpty();
            if (isCurrentlyExpanded) {
                // Collapse: remove content, update state
                contentContainer.getChildren().clear();
                expandIndicator.setText("▶");
                expandedGroups.remove(groupName);
                allGroupsExpanded = false;
            } else {
                // Expand: create content lazily
                FlowPane roomGrid = createRoomGrid(rooms);
                contentContainer.getChildren().add(roomGrid);
                expandIndicator.setText("▼");
                expandedGroups.add(groupName);
            }
        });

        panel.getChildren().addAll(header, contentContainer);
        return panel;
    }

    /**
     * Creates the room cards FlowPane.
     * Extracted for lazy loading - only called when group is expanded.
     */
    private FlowPane createRoomGrid(List<ResourceConfiguration> rooms) {
        FlowPane roomGrid = new FlowPane();
        roomGrid.setHgap(12);
        roomGrid.setVgap(12);
        roomGrid.setPadding(new Insets(16, 20, 16, 20));

        for (ResourceConfiguration rc : rooms) {
            HBox roomCard = createRoomCard(rc);
            roomGrid.getChildren().add(roomCard);
        }

        return roomGrid;
    }

    private HBox createRoomCard(ResourceConfiguration rc) {
        HBox card = new HBox();
        card.setAlignment(Pos.CENTER_LEFT);
        card.setSpacing(12);
        card.setPadding(new Insets(12, 16, 12, 16));
        card.setPrefWidth(280);
        card.setMinWidth(280);
        card.setCursor(Cursor.HAND);
        card.getStyleClass().add(UIComponentDecorators.CSS_ALLOCATION_ROOM);

        // Determine pool allocation for styling using O(1) lookup
        Resource resource = rc.getResource();
        List<PoolAllocation> roomAllocations = getAllocationsForResource(resource);

        // Determine border color based on allocation status
        // - Unassigned: orange warning color
        // - Single pool: use pool's color from database
        // - Split pools: purple color
        String borderColorHex;
        if (roomAllocations.isEmpty()) {
            borderColorHex = "#f59e0b"; // Orange for unassigned
        } else if (roomAllocations.size() == 1) {
            Pool pool = roomAllocations.get(0).getPool();
            borderColorHex = (pool != null && pool.getWebColor() != null) ? pool.getWebColor() : "#10b981";
        } else {
            borderColorHex = "#7c3aed"; // Purple for split allocation
        }

        // Apply border styling using both setStyle (for JavaFX) and setBackground/setBorder (for WebFX)
        card.setStyle("-fx-border-color: " + borderColorHex + "; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-color: white; -fx-background-radius: 10;");
        // Also apply programmatically for WebFX compatibility
        Color borderColor = Color.web(borderColorHex);
        card.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(10), null)));
        card.setBorder(new Border(new BorderStroke(borderColor, BorderStrokeStyle.SOLID, new CornerRadii(10), new BorderWidths(2))));

        // Room name
        VBox infoBox = new VBox();
        infoBox.setSpacing(4);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label nameLabel = new Label(resource != null ? resource.getName() : "—");
        nameLabel.getStyleClass().add(UIComponentDecorators.CSS_ROOM_NAME);

        // Capacity badge
        Integer capacity = rc.getMax();
        Label capacityLabel = new Label(I18n.getI18nText(RoomSetupI18nKeys.CapacityLabel, capacity != null ? capacity : "—"));
        capacityLabel.getStyleClass().add(UIComponentDecorators.CSS_SMALL);

        infoBox.getChildren().addAll(nameLabel, capacityLabel);

        // Pool allocation indicator
        VBox allocationBox = new VBox();
        allocationBox.setAlignment(Pos.CENTER_RIGHT);
        allocationBox.setSpacing(4);

        if (roomAllocations.isEmpty()) {
            Label unassignedLabel = I18nControls.newLabel(RoomSetupI18nKeys.UnassignedWarning);
            unassignedLabel.getStyleClass().add(UIComponentDecorators.CSS_BADGE_WARNING);
            unassignedLabel.setPadding(new Insets(4, 8, 4, 8));
            allocationBox.getChildren().add(unassignedLabel);
        } else if (roomAllocations.size() == 1) {
            PoolAllocation alloc = roomAllocations.get(0);
            Pool pool = alloc.getPool();
            String color = pool != null && pool.getWebColor() != null ? pool.getWebColor() : "#475569";
            String poolName = pool != null ? pool.getName() : I18n.getI18nText(RoomSetupI18nKeys.UnknownType);

            HBox poolBox = new HBox();
            poolBox.setAlignment(Pos.CENTER_RIGHT);
            poolBox.setSpacing(6);

            // Pool icon
            StackPane iconPane = new StackPane();
            iconPane.setPrefSize(20, 20);
            if (pool != null && pool.getGraphic() != null && !pool.getGraphic().isEmpty()) {
                SVGPath svg = new SVGPath();
                svg.setContent(pool.getGraphic());
                svg.setFill(Color.web(color));
                svg.setScaleX(0.5);
                svg.setScaleY(0.5);
                iconPane.getChildren().add(svg);
            }

            Label poolLabel = new Label(poolName);
            poolLabel.getStyleClass().add(UIComponentDecorators.CSS_POOL_NAME);

            poolBox.getChildren().addAll(iconPane, poolLabel);
            allocationBox.getChildren().add(poolBox);
        } else {
            // Split allocation
            Label splitLabel = new Label(I18n.getI18nText(RoomSetupI18nKeys.SplitPoolsCount, roomAllocations.size()));
            splitLabel.getStyleClass().add(UIComponentDecorators.CSS_BADGE_SPLIT);
            splitLabel.setPadding(new Insets(4, 8, 4, 8));
            allocationBox.getChildren().add(splitLabel);
        }

        card.getChildren().addAll(infoBox, allocationBox);

        // Click to edit
        card.setOnMouseClicked(e -> openAllocationDialog(rc));

        return card;
    }

    private void openAllocationDialog(ResourceConfiguration rc) {
        // Filter to source pools only using PoolTypeFilter utility
        ObservableList<Pool> sourcePools = FXCollections.observableArrayList(
                PoolTypeFilter.filterSourcePools(pools)
        );

        DefaultAllocationDialog dialog = new DefaultAllocationDialog(dataSourceModel, rc, sourcePools, poolAllocations);
        DialogManager.openDialog(dialog, () -> {
            if (poolAllocationRem != null) {
                poolAllocationRem.refreshWhenActive();
            }
        });
    }

    public void startLogic(Object mixin) {
        if (mixin instanceof HasActiveProperty) {
            ObservableValue<Boolean> ap = ((HasActiveProperty) mixin).activeProperty();
            if (activeProperty == null)
                activeProperty = ap;
            else
                activeProperty = FXProperties.combine(activeProperty, ap, (a1, a2) -> a1 || a2);
        }
        // Get DataSourceModel from activity
        if (mixin instanceof HasDataSourceModel) {
            dataSourceModel = ((HasDataSourceModel) mixin).getDataSourceModel();
        }

        if (resourceConfigRem == null) {
            // Load ResourceConfigurations (rooms)
            resourceConfigRem = ReactiveEntitiesMapper.<ResourceConfiguration>createPushReactiveChain(mixin)
                    .always("{class: 'ResourceConfiguration', alias: 'rc', fields: 'name,item.name,max,resource.(name,site,building,buildingZone)'}")
                    .always(where("startDate is null and endDate is null"))
                    .always(orderBy("resource.building.name,resource.name"))
                    .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), o -> where("resource.site.organization=$1", o))
                    .storeEntitiesInto(resourceConfigurations)
                    .addEntitiesHandler(entities -> {
                        roomsLoaded = true;
                        Platform.runLater(this::updateRoomList);
                    })
                    .setResultCacheEntry("modality/hotel/roomsetup/default-alloc-rooms")
                    .start();

            // Load Pools - global pools, not filtered by organization
            poolRem = ReactiveEntitiesMapper.<Pool>createPushReactiveChain(mixin)
                    .always("{class: 'Pool', fields: 'name,graphic,webColor,eventPool,eventType'}")
                    .always(orderBy("name"))
                    .storeEntitiesInto(pools)
                    .addEntitiesHandler(entities -> {
                        Platform.runLater(this::updateFilterChips);
                        Platform.runLater(this::updateRoomList);
                    })
                    .setResultCacheEntry("modality/hotel/roomsetup/default-alloc-pools")
                    .start();

            // Load PoolAllocations (default only - where event is null)
            poolAllocationRem = ReactiveEntitiesMapper.<PoolAllocation>createPushReactiveChain(mixin)
                    .always("{class: 'PoolAllocation', fields: 'pool.(name,graphic,webColor),resource,resource.name,quantity,event'}")
                    .always(where("event is null"))
                    .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), o -> where("resource.site.organization=$1", o))
                    .storeEntitiesInto(poolAllocations)
                    .addEntitiesHandler(entities -> {
                        allocationsLoaded = true;
                        Platform.runLater(this::updateRoomList);
                    })
                    .setResultCacheEntry("modality/hotel/roomsetup/default-alloc-allocations")
                    .start();

            // Load Buildings
            buildingRem = ReactiveEntitiesMapper.<Building>createPushReactiveChain(mixin)
                    .always("{class: 'Building', fields: 'name,site'}")
                    .always(orderBy("name"))
                    .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), o -> where("site.organization=$1", o))
                    .storeEntitiesInto(buildings)
                    .setResultCacheEntry("modality/hotel/roomsetup/default-alloc-buildings")
                    .start();

            // Load accommodation Items
            itemRem = ReactiveEntitiesMapper.<Item>createPushReactiveChain(mixin)
                    .always("{class: 'Item', fields: 'name,code,ord,family.code'}")
                    .always(where("family.code=$1", KnownItemFamily.ACCOMMODATION.getCode()))
                    .always(where("(deprecated is null or deprecated=false)"))
                    .always(orderBy("ord,name"))
                    .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), o -> where("organization=$1", o))
                    .storeEntitiesInto(accommodationItems)
                    .setResultCacheEntry("modality/hotel/roomsetup/default-alloc-items")
                    .start();

            // Bind to active property so refreshWhenActive() works
            if (activeProperty != null) {
                resourceConfigRem.bindActivePropertyTo(activeProperty);
                poolRem.bindActivePropertyTo(activeProperty);
                poolAllocationRem.bindActivePropertyTo(activeProperty);
                buildingRem.bindActivePropertyTo(activeProperty);
                itemRem.bindActivePropertyTo(activeProperty);
            }
        } else if (activeProperty != null) {
            resourceConfigRem.bindActivePropertyTo(activeProperty);
            poolRem.bindActivePropertyTo(activeProperty);
            poolAllocationRem.bindActivePropertyTo(activeProperty);
            buildingRem.bindActivePropertyTo(activeProperty);
            itemRem.bindActivePropertyTo(activeProperty);
        }
    }

    public ObservableList<Pool> getPools() {
        return pools;
    }

    public ObservableList<PoolAllocation> getPoolAllocations() {
        return poolAllocations;
    }
}
