package one.modality.event.backoffice.activities.roomsetup;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.DialogUtil;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.routing.activity.impl.elementals.activeproperty.HasActiveProperty;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.bootstrap.ModalityStyle;
import one.modality.base.shared.entities.*;
import one.modality.event.client.event.fx.FXEvent;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Tab 1: Select Rooms - Assign rooms from source pools to booking categories.
 *
 * Data Model:
 * - ResourceConfiguration (event=null, startDate/endDate=null): Defines rooms with default capacity
 * - PoolAllocation (event=null): Default source pool assignment for each room
 * - PoolAllocation (event=X): Category pool assignment for event X
 * - Pool (eventPool=false): Source pools (e.g., "General Guests", "Volunteers")
 * - Pool (eventPool=true): Category pools (e.g., "Staff", "Teachers", "Unavailable")
 *
 * Display Logic:
 * - LEFT: Show rooms grouped by source pool (from default PoolAllocations)
 * - RIGHT: Show category pools with assigned rooms (from event PoolAllocations)
 * - When a room is assigned to a category pool, it disappears from left and appears on right
 *
 * @author Bruno Salmon
 */
final class SelectRoomsTabView {

    // Shared data model (provided by activity)
    private EventRoomSetupDataModel dataModel;
    private ObservableValue<Boolean> activeProperty;

    // Per-tab loading state
    private final BooleanProperty loadingProperty = new SimpleBooleanProperty(true);

    // UI components
    private final VBox mainContent = new VBox(20);
    private final ScrollPane mainContainer;
    private final StackPane containerWithLoading;
    private final StackPane loadingOverlay;
    private final HBox poolsContainer = new HBox(20);
    private final VBox sourcePoolsSection = new VBox(16);
    private final VBox categoryPoolsSection = new VBox(16);

    // State
    private Event currentEvent;

    // Expanded state for room type groups
    private final Map<String, Boolean> expandedTypes = new HashMap<>();

    // Dialog callback for closing modal
    private DialogCallback dialogCallback;

    public SelectRoomsTabView() {
        mainContent.setPadding(new Insets(20));
        mainContainer = Controls.createVerticalScrollPane(mainContent);

        // Create loading overlay
        loadingOverlay = new StackPane();
        loadingOverlay.getStyleClass().add("roomsetup-modal-overlay");
        Region spinner = Controls.createPageSizeSpinner();
        VBox loadingBox = new VBox(10);
        loadingBox.setAlignment(Pos.CENTER);
        Label loadingLabel = I18nControls.newLabel(EventRoomSetupI18nKeys.LoadingData);
        loadingLabel.getStyleClass().add("roomsetup-pool-count");
        loadingBox.getChildren().addAll(spinner, loadingLabel);
        loadingOverlay.getChildren().add(loadingBox);

        // Container with loading overlay
        containerWithLoading = new StackPane(mainContainer, loadingOverlay);
    }

    Node buildContainer() {
        buildUI();
        return containerWithLoading;
    }

    /**
     * Initializes the tab view with the shared data model.
     *
     * @param dataModel Shared data model containing all entities
     * @param hasActiveProperty Activity's active property for lifecycle binding
     */
    void startLogic(EventRoomSetupDataModel dataModel, HasActiveProperty hasActiveProperty) {
        this.dataModel = dataModel;
        this.activeProperty = hasActiveProperty.activeProperty();

        Console.log("SelectRoomsTabView: Starting logic with shared data model");

        // Bind loading state to data model's loading properties
        FXProperties.runNowAndOnPropertiesChange(() -> {
            boolean allLoaded = dataModel.isAllDataLoaded();
            loadingProperty.set(!allLoaded);
            if (allLoaded) {
                Platform.runLater(() -> {
                    loadingOverlay.setVisible(false);
                    loadingOverlay.setManaged(false);
                });
            }
        }, dataModel.sourcePoolsLoadedProperty(),
           dataModel.categoryPoolsLoadedProperty(),
           dataModel.resourcesLoadedProperty(),
           dataModel.roomTypesLoadedProperty(),
           dataModel.permanentConfigsLoadedProperty(),
           dataModel.eventConfigsLoadedProperty(),
           dataModel.defaultAllocationsLoadedProperty(),
           dataModel.eventAllocationsLoadedProperty());

        // Add list change listeners to trigger UI updates (referencing shared lists)
        dataModel.getSourcePools().addListener((ListChangeListener<Pool>) c -> Platform.runLater(this::refreshUI));
        dataModel.getCategoryPools().addListener((ListChangeListener<Pool>) c -> Platform.runLater(this::refreshUI));
        dataModel.getPermanentRoomConfigs().addListener((ListChangeListener<ResourceConfiguration>) c -> Platform.runLater(this::refreshUI));
        dataModel.getEventRoomConfigs().addListener((ListChangeListener<ResourceConfiguration>) c -> Platform.runLater(this::refreshUI));
        dataModel.getDefaultAllocations().addListener((ListChangeListener<PoolAllocation>) c -> Platform.runLater(this::refreshUI));
        dataModel.getEventAllocations().addListener((ListChangeListener<PoolAllocation>) c -> {
            Console.log("SelectRoomsTabView: eventAllocations changed: " + dataModel.getEventAllocations().size() + " allocations");
            Platform.runLater(this::refreshUI);
        });

        // Listen for event changes
        FXProperties.runNowAndOnPropertiesChange(() -> {
            currentEvent = FXEvent.getEvent();
            if (currentEvent != null) {
                Console.log("SelectRoomsTabView: Event changed to: " + currentEvent.getName() + " (id=" + currentEvent.getPrimaryKey() + ")");
            }
        }, FXEvent.eventProperty());
    }

    void setActive(boolean active) {
        // Trigger refresh when tab becomes active
        if (active && dataModel != null) {
            // Refresh event allocations and event configs (overrides may have been updated in Customize tab)
            dataModel.refreshEventData();
        }
    }

    // Convenience accessors to shared data
    private ObservableList<Pool> getSourcePools() { return dataModel.getSourcePools(); }
    private ObservableList<Pool> getCategoryPools() { return dataModel.getCategoryPools(); }
    private ObservableList<ResourceConfiguration> getRoomConfigs() { return dataModel.getPermanentRoomConfigs(); }
    private ObservableList<ResourceConfiguration> getEventConfigs() { return dataModel.getEventRoomConfigs(); }
    private ObservableList<PoolAllocation> getDefaultAllocations() { return dataModel.getDefaultAllocations(); }
    private ObservableList<PoolAllocation> getEventAllocations() { return dataModel.getEventAllocations(); }
    private UpdateStore getUpdateStore() { return dataModel.getUpdateStore(); }

    /**
     * Builds the initial UI structure.
     */
    private void buildUI() {
        mainContent.getChildren().clear();

        // Title
        Label title = I18nControls.newLabel(EventRoomSetupI18nKeys.SelectRoomsTabTitle);
        TextTheme.createPrimaryTextFacet(title).style();
        title.getStyleClass().add(Bootstrap.H3);
        title.setPadding(new Insets(0, 0, 10, 0));

        // Help tip
        HBox helpTip = createHelpTip();

        // Pools container - two columns
        poolsContainer.setFillHeight(true);
        HBox.setHgrow(sourcePoolsSection, Priority.ALWAYS);
        HBox.setHgrow(categoryPoolsSection, Priority.ALWAYS);
        sourcePoolsSection.setMinWidth(350);
        categoryPoolsSection.setMinWidth(350);

        poolsContainer.getChildren().clear();
        poolsContainer.getChildren().addAll(sourcePoolsSection, categoryPoolsSection);

        mainContent.getChildren().addAll(title, helpTip, poolsContainer);
    }

    /**
     * Creates the help tip section.
     */
    private HBox createHelpTip() {
        HBox helpTip = new HBox(12);
        helpTip.getStyleClass().add("roomsetup-help-tip");
        helpTip.setPadding(new Insets(14, 18, 14, 18));
        helpTip.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("\uD83D\uDCA1"); // bulb
        icon.getStyleClass().add("roomsetup-empty-icon");

        VBox textBox = new VBox(4);
        Label howItWorks = I18nControls.newLabel(EventRoomSetupI18nKeys.HowItWorks);
        howItWorks.getStyleClass().add("roomsetup-pool-title");

        Label description = I18nControls.newLabel(EventRoomSetupI18nKeys.ClickRoomToAssign);
        description.setWrapText(true);

        textBox.getChildren().addAll(howItWorks, description);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        helpTip.getChildren().addAll(icon, textBox);
        return helpTip;
    }

    /**
     * Refreshes the UI with current data.
     */
    private void refreshUI() {
        Console.log("refreshUI called - roomConfigs: " + getRoomConfigs().size() + ", eventAllocations: " + getEventAllocations().size());
        refreshSourcePoolsSection();
        refreshCategoryPoolsSection();
    }

    /**
     * Gets the set of resource IDs (primary keys) that are assigned to category pools for current event.
     */
    private Set<Object> getAssignedToEventResourceIds() {
        return getEventAllocations().stream()
            .filter(pa -> pa.getResourceId() != null)
            .map(pa -> Entities.getPrimaryKey(pa.getResourceId()))
            .collect(Collectors.toSet());
    }

    /**
     * Refreshes the source pools section (left side).
     * Shows rooms that are NOT yet assigned to a category pool for this event.
     */
    private void refreshSourcePoolsSection() {
        sourcePoolsSection.getChildren().clear();

        // Get rooms with unassigned beds (either fully unassigned or partially allocated)
        // A room appears here if it has ANY unassigned beds remaining
        List<ResourceConfiguration> availableRooms = getRoomConfigs().stream()
            .filter(rc -> rc.getResource() != null && getUnassignedBeds(rc) > 0)
            .collect(Collectors.toList());
        // Total available beds = sum of unassigned beds for each room
        int totalBeds = availableRooms.stream().mapToInt(this::getUnassignedBeds).sum();

        // Section header with flat icon design
        HBox sectionHeader = new HBox(12);
        sectionHeader.setAlignment(Pos.CENTER_LEFT);
        sectionHeader.setPadding(new Insets(0, 0, 16, 0));
        sectionHeader.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 0 0 2 0;"); // Dynamic border requires setStyle

        // Flat icon box with subtle background
        String availableColor = "#64748b"; // Slate grey
        StackPane iconBox = new StackPane();
        iconBox.setMinSize(40, 40);
        iconBox.setMaxSize(40, 40);
        iconBox.getStyleClass().add("roomsetup-icon-container");
        applyDynamicBackground(iconBox, availableColor + "15", new CornerRadii(10));
        // Inventory/warehouse SVG icon (represents available stock)
        SVGPath availableIcon = new SVGPath();
        availableIcon.setContent("M20 3H4c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H4V5h16v14zM6 7h5v5H6zm7 0h5v2h-5zm0 4h5v2h-5zm0 4h5v2h-5zm-7-2h5v4H6z");
        availableIcon.setFill(Color.web(availableColor));
        availableIcon.setScaleX(0.85);
        availableIcon.setScaleY(0.85);
        iconBox.getChildren().add(availableIcon);

        VBox titleBox = new VBox(2);
        Label sectionTitle = I18nControls.newLabel(EventRoomSetupI18nKeys.AvailableRooms);
        sectionTitle.getStyleClass().add("roomsetup-pool-title");
        Label sectionSubtitle = new Label();
        I18n.bindI18nTextProperty(sectionSubtitle.textProperty(), EventRoomSetupI18nKeys.RoomsAndBeds, availableRooms.size(), totalBeds);
        sectionSubtitle.getStyleClass().add("roomsetup-pool-count");
        titleBox.getChildren().addAll(sectionTitle, sectionSubtitle);

        sectionHeader.getChildren().addAll(iconBox, titleBox);
        sourcePoolsSection.getChildren().add(sectionHeader);

        // Show empty state if no available rooms
        if (availableRooms.isEmpty()) {
            VBox emptyState = new VBox(10);
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setPadding(new Insets(40));
            emptyState.getStyleClass().add("roomsetup-empty-state");

            Label emptyIcon = new Label("🏨");
            emptyIcon.getStyleClass().add("roomsetup-empty-icon");

            Label emptyTitle = I18nControls.newLabel(getRoomConfigs().isEmpty() ? EventRoomSetupI18nKeys.NoRoomsConfigured : EventRoomSetupI18nKeys.AllRoomsAssigned);
            emptyTitle.getStyleClass().add("roomsetup-empty-title");

            Label emptyDesc = I18nControls.newLabel(getRoomConfigs().isEmpty() ? EventRoomSetupI18nKeys.NoRoomsConfiguredDesc : EventRoomSetupI18nKeys.AllRoomsAssignedDesc);
            emptyDesc.getStyleClass().add("roomsetup-pool-count");
            emptyDesc.setWrapText(true);

            emptyState.getChildren().addAll(emptyIcon, emptyTitle, emptyDesc);
            sourcePoolsSection.getChildren().add(emptyState);
            return;
        }

        // Build a map from resource ID (primary key) to its default source pool
        Map<Object, Pool> resourceToSourcePool = new HashMap<>();
        for (PoolAllocation pa : getDefaultAllocations()) {
            if (pa.getResourceId() != null && pa.getPool() != null && !Boolean.TRUE.equals(pa.getPool().isEventPool())) {
                resourceToSourcePool.put(Entities.getPrimaryKey(pa.getResourceId()), pa.getPool());
            }
        }

        // Group available rooms by source pool
        Map<Object, List<ResourceConfiguration>> roomsBySourcePool = new LinkedHashMap<>();
        for (Pool sp : getSourcePools()) {
            roomsBySourcePool.put(sp.getPrimaryKey(), new ArrayList<>());
        }

        // Rooms without default allocation go to "Unassigned" group
        List<ResourceConfiguration> unassignedRooms = new ArrayList<>();

        for (ResourceConfiguration rc : availableRooms) {
            Pool sourcePool = resourceToSourcePool.get(Entities.getPrimaryKey(rc.getResourceId()));
            if (sourcePool != null && roomsBySourcePool.containsKey(sourcePool.getPrimaryKey())) {
                roomsBySourcePool.get(sourcePool.getPrimaryKey()).add(rc);
            } else {
                unassignedRooms.add(rc);
            }
        }

        // Create a card for each source pool
        for (Pool sourcePool : getSourcePools()) {
            List<ResourceConfiguration> poolRooms = roomsBySourcePool.get(sourcePool.getPrimaryKey());
            if (poolRooms == null || poolRooms.isEmpty()) {
                continue;
            }
            VBox poolCard = createSourcePoolCard(sourcePool, poolRooms);
            sourcePoolsSection.getChildren().add(poolCard);
        }

        // Show unassigned rooms if any
        if (!unassignedRooms.isEmpty()) {
            VBox unassignedCard = createUnassignedPoolCard(unassignedRooms);
            sourcePoolsSection.getChildren().add(unassignedCard);
        }
    }

    /**
     * Creates a source pool card with rooms grouped by type.
     */
    private VBox createSourcePoolCard(Pool sourcePool, List<ResourceConfiguration> rooms) {
        String color = sourcePool.getWebColor() != null ? sourcePool.getWebColor() : "#475569";
        String name = sourcePool.getName() != null ? sourcePool.getName() : "Pool";
        String graphic = sourcePool.getGraphic();
        // Use unassigned beds count for rooms with partial allocation
        int totalBeds = rooms.stream().mapToInt(this::getUnassignedBeds).sum();
        String sectionKey = "available_" + sourcePool.getPrimaryKey();

        VBox card = new VBox(0);
        card.getStyleClass().add("roomsetup-pool-card");
        applyDynamicBorder(card, color + "60", 2, new CornerRadii(12));

        // Card header
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 14, 12, 14));
        header.getStyleClass().add("roomsetup-pool-header");
        applyDynamicBackground(header, color + "15", new CornerRadii(11, 11, 0, 0, false));

        // Pool icon
        StackPane iconPane = createPoolIcon(graphic, color, 24);

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("roomsetup-pool-title");
        applyDynamicTextFill(nameLabel, color);

        Label countLabel = new Label();
        I18n.bindI18nTextProperty(countLabel.textProperty(), EventRoomSetupI18nKeys.RoomsAndBeds, rooms.size(), totalBeds);
        countLabel.getStyleClass().add("roomsetup-pool-count");

        // Expand/Collapse button for room types
        Map<String, List<ResourceConfiguration>> roomsByType = groupRoomsByType(rooms);
        Button expandBtn = new Button();
        if (roomsByType.size() > 0) {
            boolean allExpanded = areAllTypesExpanded(sectionKey, roomsByType.keySet());
            expandBtn.setText(allExpanded ? "▼ " + I18n.getI18nText(EventRoomSetupI18nKeys.Collapse) : "▶ " + I18n.getI18nText(EventRoomSetupI18nKeys.Expand));
            expandBtn.setPadding(new Insets(3, 8, 3, 8));
            expandBtn.getStyleClass().add("roomsetup-groupby-btn");
            applyDynamicButtonBorderAndText(expandBtn, color + "40", color);
            expandBtn.setOnAction(e -> {
                toggleAllTypesExpanded(sectionKey, roomsByType.keySet());
                refreshSourcePoolsSection();
            });
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Assign all button
        Button assignAllBtn = I18nControls.newButton(EventRoomSetupI18nKeys.AssignAllArrow);
        assignAllBtn.setPadding(new Insets(6, 14, 6, 14));
        assignAllBtn.getStyleClass().add("roomsetup-assign-all-btn");
        applyDynamicButtonBackground(assignAllBtn, color);
        final List<ResourceConfiguration> roomsCopy = new ArrayList<>(rooms);
        final Pool sourcePoolRef = sourcePool;
        assignAllBtn.setOnAction(e -> showBulkAssignDialog(sourcePoolRef, roomsCopy));

        header.getChildren().addAll(iconPane, nameLabel, countLabel);
        if (roomsByType.size() > 0) {
            header.getChildren().add(expandBtn);
        }
        header.getChildren().addAll(spacer, assignAllBtn);

        // Room type groups
        VBox groupsContainer = new VBox(8);
        groupsContainer.setPadding(new Insets(12));

        for (Map.Entry<String, List<ResourceConfiguration>> entry : roomsByType.entrySet()) {
            String roomType = entry.getKey();
            List<ResourceConfiguration> typeRooms = entry.getValue();
            Node typeGroup = createRoomTypeGroup(sourcePool, sectionKey, roomType, typeRooms, color);
            groupsContainer.getChildren().add(typeGroup);
        }

        card.getChildren().addAll(header, groupsContainer);
        return card;
    }

    /**
     * Creates a card for rooms without default pool allocation.
     */
    private VBox createUnassignedPoolCard(List<ResourceConfiguration> rooms) {
        String color = "#f59e0b"; // Orange for unassigned
        // Use unassigned beds count for rooms with partial allocation
        int totalBeds = rooms.stream().mapToInt(this::getUnassignedBeds).sum();

        VBox card = new VBox(0);
        card.getStyleClass().add("roomsetup-pool-card");
        applyDynamicBorder(card, color + "60", 2, new CornerRadii(12));

        // Card header
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 14, 12, 14));
        header.getStyleClass().add("roomsetup-pool-header");
        applyDynamicBackground(header, color + "15", new CornerRadii(11, 11, 0, 0, false));

        Label warningIcon = new Label("⚠");
        warningIcon.getStyleClass().add("roomsetup-pool-title");
        applyDynamicTextFill(warningIcon, color);

        Label nameLabel = I18nControls.newLabel(EventRoomSetupI18nKeys.UnassignedRooms);
        nameLabel.getStyleClass().add("roomsetup-pool-title");
        applyDynamicTextFill(nameLabel, color);

        Label countLabel = new Label();
        I18n.bindI18nTextProperty(countLabel.textProperty(), EventRoomSetupI18nKeys.RoomsAndBeds, rooms.size(), totalBeds);
        countLabel.getStyleClass().add("roomsetup-pool-count");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Assign all button (use first source pool as default)
        Button assignAllBtn = new Button();
        I18n.bindI18nTextProperty(assignAllBtn.textProperty(), EventRoomSetupI18nKeys.AssignAllBeds, totalBeds);
        assignAllBtn.setPadding(new Insets(6, 12, 6, 12));
        ModalityStyle.outlineWarningButton(assignAllBtn);
        final List<ResourceConfiguration> roomsCopy = new ArrayList<>(rooms);
        assignAllBtn.setOnAction(e -> {
            if (!getSourcePools().isEmpty()) {
                showBulkAssignDialog(getSourcePools().get(0), roomsCopy);
            }
        });

        header.getChildren().addAll(warningIcon, nameLabel, countLabel, spacer, assignAllBtn);

        // Room chips
        FlowPane roomsFlow = new FlowPane(6, 6);
        roomsFlow.setPadding(new Insets(12));

        for (ResourceConfiguration rc : rooms) {
            Node chip = createRoomChip(rc);
            roomsFlow.getChildren().add(chip);
        }

        card.getChildren().addAll(header, roomsFlow);
        return card;
    }

    /**
     * Groups rooms by their permanent item type (for source pools).
     */
    private Map<String, List<ResourceConfiguration>> groupRoomsByType(List<ResourceConfiguration> rooms) {
        Map<String, List<ResourceConfiguration>> grouped = new LinkedHashMap<>();
        for (ResourceConfiguration rc : rooms) {
            String typeName = getRoomTypeName(rc);
            grouped.computeIfAbsent(typeName, k -> new ArrayList<>()).add(rc);
        }
        return grouped;
    }

    /**
     * Groups rooms by their effective type (considering event overrides).
     * Used for category pools where rooms may have overridden types.
     */
    private Map<String, List<ResourceConfiguration>> groupRoomsByEffectiveType(List<ResourceConfiguration> rooms) {
        Map<String, List<ResourceConfiguration>> grouped = new LinkedHashMap<>();
        for (ResourceConfiguration rc : rooms) {
            String typeName = getEffectiveRoomType(rc);
            grouped.computeIfAbsent(typeName, k -> new ArrayList<>()).add(rc);
        }
        return grouped;
    }

    /**
     * Gets the room type name from Item (permanent config only).
     */
    private String getRoomTypeName(ResourceConfiguration rc) {
        Item item = rc.getItem();
        if (item != null && item.getName() != null) {
            return item.getName();
        }
        return I18n.getI18nText(EventRoomSetupI18nKeys.OtherRoomType);
    }

    /**
     * Creates a collapsible room type group.
     */
    private Node createRoomTypeGroup(Pool sourcePool, String sectionKey, String roomType, List<ResourceConfiguration> rooms, String accentColor) {
        String groupKey = sectionKey + "_" + roomType;
        boolean isExpanded = expandedTypes.getOrDefault(groupKey, false);
        int totalBeds = rooms.stream().mapToInt(this::getEffectiveBedCount).sum();

        VBox group = new VBox(0);

        // Header (clickable to expand/collapse)
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 12, 10, 12));
        header.getStyleClass().add(isExpanded ? "roomsetup-type-group-header-expanded" : "roomsetup-type-group-header");
        String headerBgColor = isExpanded ? accentColor + "08" : "#fafaf9";
        String headerBorderColor = isExpanded ? accentColor : "#e7e5e4";
        applyDynamicBackgroundAndBorder(header, headerBgColor, headerBorderColor, 1, new CornerRadii(8));
        header.setCursor(Cursor.HAND);

        // Expand/collapse icon
        VBox expandIcon = new VBox();
        expandIcon.setMinSize(22, 22);
        expandIcon.setMaxSize(22, 22);
        expandIcon.setAlignment(Pos.CENTER);
        expandIcon.getStyleClass().add(isExpanded ? "roomsetup-type-icon-expanded" : "roomsetup-type-icon");
        applyDynamicBackground(expandIcon, isExpanded ? accentColor : "#e5e5e5", new CornerRadii(11));
        Label expandLabel = new Label(isExpanded ? "−" : "+");
        expandLabel.getStyleClass().add("roomsetup-pool-title");
        applyDynamicTextFill(expandLabel, isExpanded ? "white" : "#78716c");
        expandIcon.getChildren().add(expandLabel);

        Label typeLabel = new Label(roomType);
        typeLabel.getStyleClass().add(isExpanded ? "roomsetup-type-label-expanded" : "roomsetup-type-label");
        applyDynamicTextFill(typeLabel, isExpanded ? accentColor : "#1c1917");

        Label countBadge = new Label();
        I18n.bindI18nTextProperty(countBadge.textProperty(), EventRoomSetupI18nKeys.RoomsAndBeds, rooms.size(), totalBeds);
        countBadge.setPadding(new Insets(3, 8, 3, 8));
        countBadge.getStyleClass().add(isExpanded ? "roomsetup-type-count-active" : "roomsetup-type-count");
        String badgeBgColor = isExpanded ? accentColor + "15" : "#f0f0f0";
        String badgeTextColor = isExpanded ? accentColor : "#78716c";
        applyDynamicBackground(countBadge, badgeBgColor, new CornerRadii(4));
        countBadge.setTextFill(Color.web(badgeTextColor));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Assign all button for this room type
        Button assignAllBtn = I18nControls.newButton(EventRoomSetupI18nKeys.AssignAllArrow);
        assignAllBtn.setPadding(new Insets(6, 14, 6, 14));
        assignAllBtn.getStyleClass().add("roomsetup-assign-all-btn");
        applyDynamicButtonBackground(assignAllBtn, accentColor);
        final List<ResourceConfiguration> roomsCopy = new ArrayList<>(rooms);
        final Pool sourcePoolRef = sourcePool;
        assignAllBtn.setOnAction(e -> {
            e.consume();
            showBulkAssignDialog(sourcePoolRef, roomsCopy);
        });

        header.getChildren().addAll(expandIcon, typeLabel, countBadge, spacer, assignAllBtn);

        header.setOnMouseClicked(e -> {
            if (e.getTarget() != assignAllBtn) {
                expandedTypes.put(groupKey, !isExpanded);
                refreshSourcePoolsSection();
            }
        });

        group.getChildren().add(header);

        // Room chips (shown when expanded)
        if (isExpanded) {
            FlowPane roomsFlow = new FlowPane(6, 6);
            roomsFlow.setPadding(new Insets(12));
            roomsFlow.getStyleClass().add("roomsetup-type-group-content");
            applyDynamicBorder(roomsFlow, accentColor, 1, new CornerRadii(0, 0, 8, 8, false));

            for (ResourceConfiguration rc : rooms) {
                Node chip = createRoomChip(rc);
                roomsFlow.getChildren().add(chip);
            }
            group.getChildren().add(roomsFlow);
        }

        return group;
    }

    /**
     * Creates a room chip for available rooms.
     * Shows unassigned beds count and indicates if room has partial allocation.
     */
    private Node createRoomChip(ResourceConfiguration rc) {
        HBox chip = new HBox(6);
        chip.getStyleClass().add("roomsetup-room-chip");
        chip.setPadding(new Insets(8, 12, 8, 12));
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.setCursor(Cursor.HAND);

        Resource resource = rc.getResource();
        String roomName = resource != null && resource.getName() != null ? resource.getName() : "Room";

        // Check if room has partial allocation (some beds already assigned)
        boolean isPartial = hasPartialAllocation(rc);
        int unassignedBeds = getUnassignedBeds(rc);
        int totalBeds = getEffectiveBedCount(rc);

        // Styling: orange for partial allocation, default grey for fully unassigned
        String partialColor = "#f59e0b"; // Amber/orange for partial
        if (isPartial) {
            chip.getStyleClass().add("roomsetup-room-chip-override");
            applyDynamicBackgroundAndBorder(chip, "#fffbeb", partialColor, 2, new CornerRadii(8));
        }

        // Partial allocation indicator
        if (isPartial) {
            Label partialIcon = new Label("◐"); // Half-filled circle to indicate partial
            partialIcon.getStyleClass().add("roomsetup-modified-indicator");
            applyDynamicTextFill(partialIcon, partialColor);
            chip.getChildren().add(partialIcon);
        }

        Label idLabel = new Label(roomName);
        idLabel.getStyleClass().add("roomsetup-room-id");

        // Show unassigned beds count
        Label bedsLabel = new Label(String.valueOf(unassignedBeds));
        bedsLabel.setPadding(new Insets(2, 6, 2, 6));
        if (isPartial) {
            bedsLabel.getStyleClass().add("roomsetup-beds-badge-override");
            applyDynamicBackground(bedsLabel, partialColor, new CornerRadii(4));
        } else {
            bedsLabel.getStyleClass().add("roomsetup-beds-badge");
        }

        chip.getChildren().addAll(idLabel, bedsLabel);

        // For partial allocation, show fraction (e.g., "/10")
        if (isPartial) {
            Label fractionLabel = new Label("/" + totalBeds);
            fractionLabel.getStyleClass().add("roomsetup-modified-indicator");
            applyDynamicTextFill(fractionLabel, partialColor);
            chip.getChildren().add(fractionLabel);
        }

        // Click to assign room
        chip.setOnMouseClicked(e -> {
            e.consume();
            showRoomAssignmentDialog(rc);
        });

        return chip;
    }

    /**
     * Refreshes the category pools section (right side).
     */
    private void refreshCategoryPoolsSection() {
        categoryPoolsSection.getChildren().clear();

        // Calculate total assigned beds for header
        int totalAssignedBeds = calculateTotalAssignedBeds();
        String eventName = currentEvent != null && currentEvent.getName() != null ? currentEvent.getName() : "this event";

        // Section header with flat icon design
        HBox sectionHeader = new HBox(12);
        sectionHeader.setAlignment(Pos.CENTER_LEFT);
        sectionHeader.setPadding(new Insets(0, 0, 16, 0));
        sectionHeader.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 0 0 2 0;"); // Dynamic border requires setStyle

        // Flat icon box with subtle background
        String assignedColor = "#0096D6"; // Primary blue
        StackPane iconBox = new StackPane();
        iconBox.setMinSize(40, 40);
        iconBox.setMaxSize(40, 40);
        iconBox.getStyleClass().add("roomsetup-icon-container");
        applyDynamicBackground(iconBox, assignedColor + "15", new CornerRadii(10));
        // Assignment/clipboard-check SVG icon (represents assigned items)
        SVGPath assignedIcon = new SVGPath();
        assignedIcon.setContent("M19 3h-4.18C14.4 1.84 13.3 1 12 1s-2.4.84-2.82 2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 0c.55 0 1 .45 1 1s-.45 1-1 1-1-.45-1-1 .45-1 1-1zm-2 14l-4-4 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z");
        assignedIcon.setFill(Color.web(assignedColor));
        assignedIcon.setScaleX(0.85);
        assignedIcon.setScaleY(0.85);
        iconBox.getChildren().add(assignedIcon);

        VBox titleBox = new VBox(2);
        Label sectionTitle = new Label();
        I18n.bindI18nTextProperty(sectionTitle.textProperty(), EventRoomSetupI18nKeys.AssignedFor, eventName);
        sectionTitle.getStyleClass().add("roomsetup-pool-title");
        applyDynamicTextFill(sectionTitle, assignedColor);
        Label sectionSubtitle = new Label();
        I18n.bindI18nTextProperty(sectionSubtitle.textProperty(), EventRoomSetupI18nKeys.BedsAllocated, totalAssignedBeds);
        sectionSubtitle.getStyleClass().add("roomsetup-pool-count");
        titleBox.getChildren().addAll(sectionTitle, sectionSubtitle);

        sectionHeader.getChildren().addAll(iconBox, titleBox);
        categoryPoolsSection.getChildren().add(sectionHeader);

        // Show empty state if no category pools defined
        if (getCategoryPools().isEmpty()) {
            VBox emptyState = new VBox(10);
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setPadding(new Insets(40));
            emptyState.getStyleClass().add("roomsetup-empty-state");

            Label emptyIcon = new Label("📦");
            emptyIcon.getStyleClass().add("roomsetup-empty-icon");

            Label emptyTitle = I18nControls.newLabel(EventRoomSetupI18nKeys.NoBookingCategories);
            emptyTitle.getStyleClass().add("roomsetup-empty-title");

            Label emptyDesc = I18nControls.newLabel(EventRoomSetupI18nKeys.NoBookingCategoriesDesc);
            emptyDesc.getStyleClass().add("roomsetup-pool-count");
            emptyDesc.setWrapText(true);

            emptyState.getChildren().addAll(emptyIcon, emptyTitle, emptyDesc);
            categoryPoolsSection.getChildren().add(emptyState);
        } else {
            for (Pool pool : getCategoryPools()) {
                // Get rooms assigned to this pool for current event
                List<ResourceConfiguration> assignedRooms = getRoomsForCategoryPool(pool);
                // Use getTotalBedsInPool() to correctly handle split allocations
                // (sums actual allocated quantities, not total room beds)
                int bedCount = getTotalBedsInPool(pool);

                String color = pool.getWebColor() != null ? pool.getWebColor() : "#0096D6";
                String name = pool.getName() != null ? pool.getName() : "Pool";

                VBox poolCard = createCategoryPoolCard(pool, name, color, assignedRooms, bedCount);
                categoryPoolsSection.getChildren().add(poolCard);
            }
        }
    }

    /**
     * Creates a pool card for category pools with room type grouping.
     */
    private VBox createCategoryPoolCard(Pool pool, String name, String color, List<ResourceConfiguration> assignedRooms, int bedCount) {
        String graphic = pool.getGraphic();
        String sectionKey = "assigned_" + pool.getPrimaryKey();

        VBox card = new VBox(0);
        card.getStyleClass().add("roomsetup-pool-card");
        applyDynamicBorder(card, color, 2, new CornerRadii(12));

        // Header with colored background
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 16, 14, 16));
        header.getStyleClass().add("roomsetup-pool-header");
        if (assignedRooms.isEmpty()) {
            applyDynamicBackground(header, color + "15", new CornerRadii(11, 11, 11, 11, false));
        } else {
            // Header with bottom border when rooms are assigned
            applyDynamicBackgroundAndBorder(header, color + "15", color + "30", 1, new CornerRadii(11, 11, 0, 0, false));
        }

        // Pool icon (SVG or emoji)
        StackPane iconPane = createPoolIcon(graphic, color, 28);

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("roomsetup-pool-title");
        applyDynamicTextFill(nameLabel, color);

        Label countLabel = new Label(bedCount + " beds");
        countLabel.getStyleClass().add("roomsetup-pool-count");

        // Expand/Collapse button for room types - use effective type (considers overrides)
        Map<String, List<ResourceConfiguration>> roomsByType = groupRoomsByEffectiveType(assignedRooms);
        Button expandBtn = new Button();
        if (!assignedRooms.isEmpty() && roomsByType.size() > 0) {
            boolean allExpanded = areAllTypesExpanded(sectionKey, roomsByType.keySet());
            expandBtn.setText(allExpanded ? "▼ " + I18n.getI18nText(EventRoomSetupI18nKeys.Collapse) : "▶ " + I18n.getI18nText(EventRoomSetupI18nKeys.Expand));
            expandBtn.setPadding(new Insets(3, 8, 3, 8));
            expandBtn.getStyleClass().add("roomsetup-groupby-btn");
            applyDynamicButtonBorderAndText(expandBtn, color + "40", color);
            expandBtn.setOnAction(e -> {
                toggleAllTypesExpanded(sectionKey, roomsByType.keySet());
                refreshCategoryPoolsSection();
            });
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(iconPane, nameLabel, countLabel);
        if (!assignedRooms.isEmpty() && roomsByType.size() > 0) {
            header.getChildren().add(expandBtn);
        }
        header.getChildren().add(spacer);

        card.getChildren().add(header);

        // Room type groups or empty state
        if (assignedRooms.isEmpty()) {
            Label emptyLabel = I18nControls.newLabel(EventRoomSetupI18nKeys.NoRoomsAssignedYet);
            emptyLabel.getStyleClass().add("roomsetup-pool-count");
            emptyLabel.setPadding(new Insets(20));
            emptyLabel.setAlignment(Pos.CENTER);
            VBox emptyBox = new VBox(emptyLabel);
            emptyBox.setAlignment(Pos.CENTER);
            card.getChildren().add(emptyBox);
        } else {
            VBox groupsContainer = new VBox(8);
            groupsContainer.setPadding(new Insets(12));

            for (Map.Entry<String, List<ResourceConfiguration>> entry : roomsByType.entrySet()) {
                String roomType = entry.getKey();
                List<ResourceConfiguration> typeRooms = entry.getValue();
                Node typeGroup = createCategoryRoomTypeGroup(pool, sectionKey, roomType, typeRooms, color);
                groupsContainer.getChildren().add(typeGroup);
            }

            card.getChildren().add(groupsContainer);
        }

        return card;
    }

    /**
     * Creates a collapsible room type group for category pools.
     * Uses effective bed counts to account for event-specific overrides.
     */
    private Node createCategoryRoomTypeGroup(Pool pool, String sectionKey, String roomType, List<ResourceConfiguration> rooms, String accentColor) {
        String groupKey = sectionKey + "_" + roomType;
        boolean isExpanded = expandedTypes.getOrDefault(groupKey, false);
        // Use effective bed count (considers event overrides)
        int totalBeds = rooms.stream().mapToInt(this::getEffectiveBedCount).sum();

        VBox group = new VBox(0);

        // Header (clickable to expand/collapse)
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 12, 10, 12));
        header.getStyleClass().add(isExpanded ? "roomsetup-type-group-header-expanded" : "roomsetup-type-group-header");
        String headerBgColor = isExpanded ? accentColor + "08" : "#fafaf9";
        String headerBorderColor = isExpanded ? accentColor : "#e7e5e4";
        applyDynamicBackgroundAndBorder(header, headerBgColor, headerBorderColor, 1, new CornerRadii(8));
        header.setCursor(Cursor.HAND);

        // Expand/collapse icon
        VBox expandIcon = new VBox();
        expandIcon.setMinSize(22, 22);
        expandIcon.setMaxSize(22, 22);
        expandIcon.setAlignment(Pos.CENTER);
        expandIcon.getStyleClass().add(isExpanded ? "roomsetup-type-icon-expanded" : "roomsetup-type-icon");
        applyDynamicBackground(expandIcon, isExpanded ? accentColor : "#e5e5e5", new CornerRadii(11));
        Label expandLabel = new Label(isExpanded ? "−" : "+");
        expandLabel.getStyleClass().add("roomsetup-pool-title");
        applyDynamicTextFill(expandLabel, isExpanded ? "white" : "#78716c");
        expandIcon.getChildren().add(expandLabel);

        Label typeLabel = new Label(roomType);
        typeLabel.getStyleClass().add(isExpanded ? "roomsetup-type-label-expanded" : "roomsetup-type-label");
        applyDynamicTextFill(typeLabel, isExpanded ? accentColor : "#1c1917");

        Label countBadge = new Label();
        I18n.bindI18nTextProperty(countBadge.textProperty(), EventRoomSetupI18nKeys.RoomsAndBeds, rooms.size(), totalBeds);
        countBadge.setPadding(new Insets(3, 8, 3, 8));
        countBadge.getStyleClass().add(isExpanded ? "roomsetup-type-count-active" : "roomsetup-type-count");
        String badgeBgColor = isExpanded ? accentColor + "15" : "#f0f0f0";
        String badgeTextColor = isExpanded ? accentColor : "#78716c";
        applyDynamicBackground(countBadge, badgeBgColor, new CornerRadii(4));
        countBadge.setTextFill(Color.web(badgeTextColor));

        header.getChildren().addAll(expandIcon, typeLabel, countBadge);

        header.setOnMouseClicked(e -> {
            expandedTypes.put(groupKey, !isExpanded);
            refreshCategoryPoolsSection();
        });

        group.getChildren().add(header);

        // Room chips (shown when expanded)
        if (isExpanded) {
            FlowPane roomsFlow = new FlowPane(6, 6);
            roomsFlow.setPadding(new Insets(12));
            roomsFlow.getStyleClass().add("roomsetup-type-group-content");
            applyDynamicBorder(roomsFlow, accentColor, 1, new CornerRadii(0, 0, 8, 8, false));

            for (ResourceConfiguration rc : rooms) {
                Node chip = createAssignedRoomChip(rc, pool);
                roomsFlow.getChildren().add(chip);
            }
            group.getChildren().add(roomsFlow);
        }

        return group;
    }

    /**
     * Checks if all room types in a section are expanded.
     */
    private boolean areAllTypesExpanded(String sectionKey, Set<String> roomTypes) {
        for (String type : roomTypes) {
            if (!expandedTypes.getOrDefault(sectionKey + "_" + type, false)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Toggles expansion state for all room types in a section.
     */
    private void toggleAllTypesExpanded(String sectionKey, Set<String> roomTypes) {
        boolean allExpanded = areAllTypesExpanded(sectionKey, roomTypes);
        for (String type : roomTypes) {
            expandedTypes.put(sectionKey + "_" + type, !allExpanded);
        }
    }

    /**
     * Creates a room chip for assigned rooms in category pools.
     * Uses grey styling for normal chips, primary color for chips with event-specific overrides.
     * Clicking the chip opens the edit dialog for reassignment/unassignment.
     */
    private Node createAssignedRoomChip(ResourceConfiguration rc, Pool pool) {
        HBox chip = new HBox(6);
        chip.getStyleClass().add("roomsetup-room-chip");
        chip.setPadding(new Insets(8, 12, 8, 12));
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.setCursor(Cursor.HAND);

        Resource resource = rc.getResource();
        String roomName = resource != null && resource.getName() != null ? resource.getName() : "Room";

        // Check if this room has an event-specific override
        boolean hasOverride = hasEventOverride(rc);

        // Check if this room has split allocation (multiple pools)
        boolean isSplit = hasSplitAllocation(rc);

        // Styling based on override/split status
        String primaryColor = "#0096D6";
        String splitColor = "#7c3aed"; // Purple for split rooms
        if (isSplit) {
            chip.getStyleClass().add("roomsetup-room-chip-external");
            applyDynamicBackgroundAndBorder(chip, "#faf5ff", splitColor, 2, new CornerRadii(8));
        } else if (hasOverride) {
            chip.getStyleClass().add("roomsetup-room-chip-selected");
            chip.setStyle("-fx-border-width: 2;"); // Override uses primary color from CSS
        }

        // Split indicator (lightning bolt emoji)
        if (isSplit) {
            Label splitIcon = new Label("⚡");
            splitIcon.getStyleClass().add("roomsetup-modified-indicator");
            chip.getChildren().add(splitIcon);
        }
        // Override indicator (only if not split, to avoid clutter)
        else if (hasOverride) {
            Label overrideIcon = new Label("✏️");
            overrideIcon.getStyleClass().add("roomsetup-modified-indicator");
            chip.getChildren().add(overrideIcon);
        }

        Label idLabel = new Label(roomName);
        idLabel.getStyleClass().add("roomsetup-room-id");

        // For split rooms, show the beds allocated to THIS pool; otherwise show total
        int bedsInPool = getBedsAllocatedInPool(rc, pool);
        int totalBeds = getEffectiveBedCount(rc);
        String bedText = isSplit ? String.valueOf(bedsInPool) : String.valueOf(getEffectiveBedCount(rc));
        Label bedsLabel = new Label(bedText);
        bedsLabel.setPadding(new Insets(2, 8, 2, 8));

        // Bed badge styling
        if (isSplit) {
            bedsLabel.getStyleClass().add("roomsetup-beds-badge-external");
            applyDynamicBackground(bedsLabel, splitColor, new CornerRadii(4));
            bedsLabel.setTextFill(Color.WHITE);
        } else if (hasOverride) {
            bedsLabel.getStyleClass().add("roomsetup-beds-badge-override");
        } else {
            bedsLabel.getStyleClass().add("roomsetup-beds-badge");
        }

        // Show room type if overridden (different from permanent type)
        if (hasOverride && !isSplit) {
            String effectiveType = getEffectiveRoomType(rc);
            String permanentType = getRoomTypeName(rc);
            if (!Objects.equals(effectiveType, permanentType)) {
                Label typeLabel = new Label(effectiveType);
                typeLabel.getStyleClass().add("roomsetup-modified-indicator");
                applyDynamicTextFill(typeLabel, primaryColor);
                chip.getChildren().add(typeLabel);
            }
        }

        // For split rooms, show fraction indicator (e.g., "4/10")
        if (isSplit) {
            Label fractionLabel = new Label("/" + totalBeds);
            fractionLabel.getStyleClass().add("roomsetup-modified-indicator");
            applyDynamicTextFill(fractionLabel, splitColor);
            chip.getChildren().addAll(idLabel, bedsLabel, fractionLabel);
        } else {
            chip.getChildren().addAll(idLabel, bedsLabel);
        }

        // Click handler to open edit dialog (replacing the × button)
        chip.setOnMouseClicked(e -> {
            e.consume();
            showRoomEditDialog(rc);
        });

        return chip;
    }

    /**
     * Checks if a room has an event-specific override (ResourceConfiguration for this event).
     */
    private boolean hasEventOverride(ResourceConfiguration rc) {
        return getEventOverride(rc) != null;
    }

    /**
     * Gets the event-specific override for a room, if any.
     */
    private ResourceConfiguration getEventOverride(ResourceConfiguration rc) {
        if (rc == null || rc.getResource() == null) return null;
        Object resourceId = Entities.getPrimaryKey(rc.getResourceId());
        return getEventConfigs().stream()
            .filter(ec -> ec.getResourceId() != null &&
                Objects.equals(Entities.getPrimaryKey(ec.getResourceId()), resourceId))
            .findFirst()
            .orElse(null);
    }

    /**
     * Gets the effective bed count for a room (from override if exists, otherwise from permanent config).
     */
    private int getEffectiveBedCount(ResourceConfiguration rc) {
        ResourceConfiguration override = getEventOverride(rc);
        if (override != null && override.getMax() != null) {
            return override.getMax();
        }
        return getBedCount(rc);
    }

    /**
     * Gets the effective room type name (from override if exists, otherwise from permanent config).
     */
    private String getEffectiveRoomType(ResourceConfiguration rc) {
        ResourceConfiguration override = getEventOverride(rc);
        if (override != null && override.getItem() != null && override.getItem().getName() != null) {
            return override.getItem().getName();
        }
        // Fall back to permanent config
        Item item = rc.getItem();
        if (item != null && item.getName() != null) {
            return item.getName();
        }
        return I18n.getI18nText(EventRoomSetupI18nKeys.OtherRoomType);
    }

    /**
     * Shows the room assignment dialog when clicking on an available room.
     * For dormitories (>2 beds): Shows split UI with +/- controls for each category pool.
     * For regular rooms: Shows simple category selection.
     */
    private void showRoomAssignmentDialog(ResourceConfiguration rc) {
        int totalBeds = getEffectiveBedCount(rc);
        Resource resource = rc.getResource();
        String roomName = resource != null && resource.getName() != null ? resource.getName() : "Room";
        boolean isDorm = isDormitory(rc);
        boolean isPartial = hasPartialAllocation(rc);
        int unassignedBeds = getUnassignedBeds(rc);

        // Track allocation state - pre-populate with current allocations for partial rooms
        Map<Pool, Integer> allocation = new HashMap<>();
        for (Pool cp : getCategoryPools()) {
            allocation.put(cp, getBedsAllocatedInPool(rc, cp));
        }

        VBox dialogContent = new VBox(16);
        dialogContent.setPadding(new Insets(24));
        dialogContent.getStyleClass().add("roomsetup-modal-content");
        dialogContent.setMaxWidth(440);

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = I18nControls.newLabel(isPartial ? EventRoomSetupI18nKeys.EditRoomAssignment : EventRoomSetupI18nKeys.AssignRoom);
        titleLabel.getStyleClass().add("roomsetup-modal-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("×");
        closeBtn.getStyleClass().add("roomsetup-btn-secondary");
        closeBtn.setOnAction(e -> closeModal());

        header.getChildren().addAll(titleLabel, spacer, closeBtn);

        // Room info
        VBox roomInfo = new VBox(4);
        roomInfo.setPadding(new Insets(12));
        roomInfo.getStyleClass().add(isPartial ? "roomsetup-help-tip-warning" : "roomsetup-help-tip");

        Label roomNameLabel = new Label(roomName);
        roomNameLabel.getStyleClass().add("roomsetup-pool-title");

        String partialInfo = isPartial ? " • " + unassignedBeds + " unassigned" : "";
        Label roomDetails = new Label(getRoomTypeName(rc) + " • " + totalBeds + " bed" + (totalBeds != 1 ? "s" : "") +
            (isDorm ? " (dormitory)" : "") + partialInfo);
        roomDetails.getStyleClass().add("roomsetup-pool-count");

        roomInfo.getChildren().addAll(roomNameLabel, roomDetails);

        if (isDorm) {
            // === DORMITORY: Split allocation UI ===
            Label allocateLabel = I18nControls.newLabel(EventRoomSetupI18nKeys.AssignBedsToCategories);
            allocateLabel.getStyleClass().add("roomsetup-section-label");
            allocateLabel.setPadding(new Insets(8, 0, 0, 0));

            VBox poolControls = new VBox(8);

            // Status label showing remaining beds
            Label statusLabel = new Label();
            statusLabel.setStyle("-fx-padding: 10 14; -fx-background-radius: 8; -fx-font-size: 13px;");

            // Function to update status
            Runnable updateStatus = () -> {
                int totalAllocated = allocation.values().stream().mapToInt(Integer::intValue).sum();
                int remaining = totalBeds - totalAllocated;
                if (totalAllocated == 0) {
                    statusLabel.setText(I18n.getI18nText(EventRoomSetupI18nKeys.SelectBedsToAssign));
                    statusLabel.setStyle("-fx-padding: 10 14; -fx-background-radius: 8; -fx-font-size: 13px; -fx-background-color: #fafaf9; -fx-text-fill: #78716c;");
                } else if (remaining > 0) {
                    statusLabel.setText(I18n.getI18nText(EventRoomSetupI18nKeys.BedsWillStayAvailable, remaining));
                    statusLabel.setStyle("-fx-padding: 10 14; -fx-background-radius: 8; -fx-font-size: 13px; -fx-background-color: #fef3c7; -fx-text-fill: #92400e;");
                } else {
                    statusLabel.setText(I18n.getI18nText(EventRoomSetupI18nKeys.AllBedsAssignedStatus, totalBeds));
                    statusLabel.setStyle("-fx-padding: 10 14; -fx-background-radius: 8; -fx-font-size: 13px; -fx-background-color: #e0f4fc; -fx-text-fill: #0096D6;");
                }
            };

            // Create controls for each category pool
            for (Pool cp : getCategoryPools()) {
                String color = cp.getWebColor() != null ? cp.getWebColor() : "#0096D6";
                String graphic = cp.getGraphic();
                int beds = allocation.get(cp);

                HBox poolRow = new HBox(12);
                poolRow.setAlignment(Pos.CENTER_LEFT);
                poolRow.setPadding(new Insets(12, 14, 12, 14));
                String bgColor = beds > 0 ? color + "08" : "#fafaf9";
                String borderColor = beds > 0 ? color : "#e7e5e4";
                applyDynamicBackgroundAndBorder(poolRow, bgColor, borderColor, 1, new CornerRadii(10));

                // Pool icon and name
                StackPane iconPane = createPoolIcon(graphic, color, 24);

                Label poolName = new Label(cp.getName());
                applyDynamicTextFillWithStyle(poolName, beds > 0 ? color : "#1c1917", "-fx-font-weight: 600; -fx-font-size: 14px;");

                Region rowSpacer = new Region();
                HBox.setHgrow(rowSpacer, Priority.ALWAYS);

                // Stepper controls - use SVGPath graphics for GWT compatibility
                Button minusBtn = new Button();
                minusBtn.setMinSize(36, 36);
                minusBtn.setMaxSize(36, 36);

                Label bedCountLabel = new Label(String.valueOf(beds));
                bedCountLabel.setMinWidth(36);
                bedCountLabel.setAlignment(Pos.CENTER);
                applyDynamicTextFillWithStyle(bedCountLabel, beds > 0 ? color : "#d4d4d4", "-fx-font-size: 15px; -fx-font-weight: 700;");

                Button plusBtn = new Button();
                plusBtn.setMinSize(36, 36);
                plusBtn.setMaxSize(36, 36);

                // Update button styles based on state
                Runnable updateButtons = () -> {
                    int currentBeds = allocation.get(cp);
                    int totalAllocated = allocation.values().stream().mapToInt(Integer::intValue).sum();
                    int remaining = totalBeds - totalAllocated;

                    boolean canDecrease = currentBeds > 0;
                    boolean canIncrease = remaining > 0;

                    minusBtn.setGraphic(createMinusIcon(canDecrease ? "#44403c" : "#a1a1aa"));
                    applyDynamicBackgroundAndBorder(minusBtn, canDecrease ? "white" : "#f5f5f4", canDecrease ? "#a8a29e" : "#e7e5e4", 1, new CornerRadii(8));
                    minusBtn.setDisable(!canDecrease);

                    plusBtn.setGraphic(createPlusIcon(canIncrease ? "#44403c" : "#a1a1aa"));
                    applyDynamicBackgroundAndBorder(plusBtn, canIncrease ? "white" : "#f5f5f4", canIncrease ? "#a8a29e" : "#e7e5e4", 1, new CornerRadii(8));
                    plusBtn.setDisable(!canIncrease);

                    bedCountLabel.setText(String.valueOf(currentBeds));
                    applyDynamicTextFillWithStyle(bedCountLabel, currentBeds > 0 ? color : "#d4d4d4", "-fx-font-size: 15px; -fx-font-weight: 700;");

                    applyDynamicBackgroundAndBorder(poolRow, currentBeds > 0 ? color + "08" : "#fafaf9", currentBeds > 0 ? color : "#e7e5e4", 1, new CornerRadii(10));
                    applyDynamicTextFillWithStyle(poolName, currentBeds > 0 ? color : "#1c1917", "-fx-font-weight: 600; -fx-font-size: 14px;");
                };

                // Store pool reference for lambda capture
                final Pool targetPool = cp;

                minusBtn.setOnAction(e -> {
                    int current = allocation.get(targetPool);
                    if (current > 0) {
                        allocation.put(targetPool, current - 1);
                        poolControls.getChildren().forEach(node -> {
                            if (node.getUserData() instanceof Runnable) {
                                ((Runnable) node.getUserData()).run();
                            }
                        });
                        updateStatus.run();
                    }
                });

                plusBtn.setOnAction(e -> {
                    int totalAllocated = allocation.values().stream().mapToInt(Integer::intValue).sum();
                    if (totalAllocated < totalBeds) {
                        allocation.put(targetPool, allocation.get(targetPool) + 1);
                        poolControls.getChildren().forEach(node -> {
                            if (node.getUserData() instanceof Runnable) {
                                ((Runnable) node.getUserData()).run();
                            }
                        });
                        updateStatus.run();
                    }
                });

                HBox stepperBox = new HBox(8, minusBtn, bedCountLabel, plusBtn);
                stepperBox.setAlignment(Pos.CENTER);

                poolRow.getChildren().addAll(iconPane, poolName, rowSpacer, stepperBox);
                poolRow.setUserData(updateButtons);
                poolControls.getChildren().add(poolRow);

                // Initialize button states
                updateButtons.run();
            }

            // Footer buttons
            HBox footer = new HBox(12);
            footer.setAlignment(Pos.CENTER_RIGHT);
            footer.setPadding(new Insets(16, 0, 0, 0));
            footer.setStyle("-fx-border-color: #e7e5e4; -fx-border-width: 1 0 0 0;");

            Button cancelBtn = I18nControls.newButton(EventRoomSetupI18nKeys.Cancel);
            Bootstrap.secondaryButton(cancelBtn);
            cancelBtn.setPadding(new Insets(10, 20, 10, 20));
            cancelBtn.setOnAction(e -> closeModal());

            Button saveBtn = I18nControls.newButton(EventRoomSetupI18nKeys.Assign);
            Bootstrap.primaryButton(saveBtn);
            saveBtn.setPadding(new Insets(10, 24, 10, 24));
            saveBtn.setOnAction(e -> {
                int totalAllocated = allocation.values().stream().mapToInt(Integer::intValue).sum();
                if (totalAllocated > 0) {
                    saveRoomSplitAllocation(rc, allocation);
                }
                closeModal();
            });

            footer.getChildren().addAll(cancelBtn, saveBtn);

            // Initialize status
            updateStatus.run();

            dialogContent.getChildren().addAll(header, roomInfo, allocateLabel, poolControls, statusLabel, footer);

        } else {
            // === REGULAR ROOM: Simple category selection ===
            Label selectLabel = I18nControls.newLabel(EventRoomSetupI18nKeys.SelectCategory);
            selectLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #78716c;");

            VBox categoryOptions = new VBox(8);
            for (Pool cp : getCategoryPools()) {
                String color = cp.getWebColor() != null ? cp.getWebColor() : "#0096D6";
                String graphic = cp.getGraphic();
                String desc = cp.getDescription() != null ? cp.getDescription() : "";

                HBox option = new HBox(12);
                option.setAlignment(Pos.CENTER_LEFT);
                option.setPadding(new Insets(14, 16, 14, 16));
                option.setStyle("-fx-background-color: white; -fx-border-color: #e7e5e4; -fx-border-radius: 10; -fx-background-radius: 10; -fx-cursor: hand;");
                option.setCursor(Cursor.HAND);

                StackPane iconPane = createPoolIcon(graphic, color, 32);

                VBox textBox = new VBox(2);
                Label poolName = new Label(cp.getName());
                applyDynamicTextFillWithStyle(poolName, color, "-fx-font-weight: 600;");
                Label poolDesc = new Label(desc);
                poolDesc.setStyle("-fx-font-size: 12px; -fx-text-fill: #78716c;");
                textBox.getChildren().addAll(poolName, poolDesc);
                HBox.setHgrow(textBox, Priority.ALWAYS);

                Label bedsBadge = new Label();
                I18n.bindI18nTextProperty(bedsBadge.textProperty(), EventRoomSetupI18nKeys.PlusBeds, totalBeds);
                bedsBadge.setPadding(new Insets(4, 10, 4, 10));
                applyDynamicBackground(bedsBadge, color + "15", new CornerRadii(6));
                bedsBadge.setTextFill(Color.web(color));

                option.getChildren().addAll(iconPane, textBox, bedsBadge);

                final Pool targetPool = cp;
                option.setOnMouseClicked(e -> {
                    assignRoomToPool(rc, targetPool);
                    closeModal();
                });

                categoryOptions.getChildren().add(option);
            }

            dialogContent.getChildren().addAll(header, roomInfo, selectLabel, categoryOptions);
        }

        showModal(dialogContent);
    }

    /**
     * Shows the room edit dialog for an already assigned room.
     * For dormitories (>2 beds): Shows split UI with +/- controls for each category pool.
     * For regular rooms: Shows reassignment options and unassign button.
     */
    private void showRoomEditDialog(ResourceConfiguration rc) {
        int totalBeds = getEffectiveBedCount(rc);
        Resource resource = rc.getResource();
        String roomName = resource != null && resource.getName() != null ? resource.getName() : "Room";
        boolean isDorm = isDormitory(rc);

        // Track current allocation state (mutable during dialog interaction)
        Map<Pool, Integer> allocation = new HashMap<>();
        for (Pool cp : getCategoryPools()) {
            allocation.put(cp, getBedsAllocatedInPool(rc, cp));
        }

        VBox dialogContent = new VBox(16);
        dialogContent.setPadding(new Insets(24));
        dialogContent.setStyle("-fx-background-color: white; -fx-background-radius: 16;");
        dialogContent.setMaxWidth(440);

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = I18nControls.newLabel(isDorm ? EventRoomSetupI18nKeys.EditRoomAllocation : EventRoomSetupI18nKeys.EditRoomAssignment);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 600;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("×");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 18px; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> closeModal());

        header.getChildren().addAll(titleLabel, spacer, closeBtn);

        // Room info
        VBox roomInfo = new VBox(4);
        roomInfo.setPadding(new Insets(12));
        roomInfo.setStyle("-fx-background-color: #fafaf9; -fx-background-radius: 8;");

        Label roomNameLabel = new Label(roomName);
        roomNameLabel.setStyle("-fx-font-weight: 600;");

        Label roomDetails = new Label(getRoomTypeName(rc) + " • " + totalBeds + " bed" + (totalBeds != 1 ? "s" : "") +
            (isDorm ? " (dormitory)" : ""));
        roomDetails.setStyle("-fx-font-size: 13px; -fx-text-fill: #78716c;");

        roomInfo.getChildren().addAll(roomNameLabel, roomDetails);

        if (isDorm) {
            // === DORMITORY: Split allocation UI ===
            Label allocateLabel = I18nControls.newLabel(EventRoomSetupI18nKeys.AssignBedsToCategories);
            allocateLabel.getStyleClass().add("roomsetup-section-label");
            allocateLabel.setPadding(new Insets(8, 0, 0, 0));

            VBox poolControls = new VBox(8);

            // Status label showing remaining beds
            Label statusLabel = new Label();
            statusLabel.setStyle("-fx-padding: 10 14; -fx-background-radius: 8; -fx-font-size: 13px;");

            // Function to update status
            Runnable updateStatus = () -> {
                int totalAllocated = allocation.values().stream().mapToInt(Integer::intValue).sum();
                int remaining = totalBeds - totalAllocated;
                if (remaining == totalBeds) {
                    statusLabel.setText(I18n.getI18nText(EventRoomSetupI18nKeys.RoomWillBeUnassigned));
                    statusLabel.setStyle("-fx-padding: 10 14; -fx-background-radius: 8; -fx-font-size: 13px; -fx-background-color: #dcfce7; -fx-text-fill: #166534;");
                } else if (remaining > 0) {
                    statusLabel.setText(I18n.getI18nText(EventRoomSetupI18nKeys.BedsWillStayAvailable, remaining));
                    statusLabel.setStyle("-fx-padding: 10 14; -fx-background-radius: 8; -fx-font-size: 13px; -fx-background-color: #fef3c7; -fx-text-fill: #92400e;");
                } else {
                    statusLabel.setText(I18n.getI18nText(EventRoomSetupI18nKeys.AllBedsAssignedStatus, totalBeds));
                    statusLabel.setStyle("-fx-padding: 10 14; -fx-background-radius: 8; -fx-font-size: 13px; -fx-background-color: #e0f4fc; -fx-text-fill: #0096D6;");
                }
            };

            // Create controls for each category pool
            for (Pool cp : getCategoryPools()) {
                String color = cp.getWebColor() != null ? cp.getWebColor() : "#0096D6";
                String graphic = cp.getGraphic();
                int beds = allocation.get(cp);

                HBox poolRow = new HBox(12);
                poolRow.setAlignment(Pos.CENTER_LEFT);
                poolRow.setPadding(new Insets(12, 14, 12, 14));
                String bgColor = beds > 0 ? color + "08" : "#fafaf9";
                String borderColor = beds > 0 ? color : "#e7e5e4";
                applyDynamicBackgroundAndBorder(poolRow, bgColor, borderColor, 1, new CornerRadii(10));

                // Pool icon and name
                StackPane iconPane = createPoolIcon(graphic, color, 24);

                Label poolName = new Label(cp.getName());
                applyDynamicTextFillWithStyle(poolName, beds > 0 ? color : "#1c1917", "-fx-font-weight: 600; -fx-font-size: 14px;");

                Region rowSpacer = new Region();
                HBox.setHgrow(rowSpacer, Priority.ALWAYS);

                // Stepper controls - use SVGPath graphics for GWT compatibility
                Button minusBtn = new Button();
                minusBtn.setMinSize(36, 36);
                minusBtn.setMaxSize(36, 36);

                Label bedCountLabel = new Label(String.valueOf(beds));
                bedCountLabel.setMinWidth(36);
                bedCountLabel.setAlignment(Pos.CENTER);
                applyDynamicTextFillWithStyle(bedCountLabel, beds > 0 ? color : "#d4d4d4", "-fx-font-size: 15px; -fx-font-weight: 700;");

                Button plusBtn = new Button();
                plusBtn.setMinSize(36, 36);
                plusBtn.setMaxSize(36, 36);

                // Update button styles based on state
                Runnable updateButtons = () -> {
                    int currentBeds = allocation.get(cp);
                    int totalAllocated = allocation.values().stream().mapToInt(Integer::intValue).sum();
                    int remaining = totalBeds - totalAllocated;

                    boolean canDecrease = currentBeds > 0;
                    boolean canIncrease = remaining > 0;

                    minusBtn.setGraphic(createMinusIcon(canDecrease ? "#44403c" : "#a1a1aa"));
                    applyDynamicBackgroundAndBorder(minusBtn, canDecrease ? "white" : "#f5f5f4", canDecrease ? "#a8a29e" : "#e7e5e4", 1, new CornerRadii(8));
                    minusBtn.setDisable(!canDecrease);

                    plusBtn.setGraphic(createPlusIcon(canIncrease ? "#44403c" : "#a1a1aa"));
                    applyDynamicBackgroundAndBorder(plusBtn, canIncrease ? "white" : "#f5f5f4", canIncrease ? "#a8a29e" : "#e7e5e4", 1, new CornerRadii(8));
                    plusBtn.setDisable(!canIncrease);

                    bedCountLabel.setText(String.valueOf(currentBeds));
                    applyDynamicTextFillWithStyle(bedCountLabel, currentBeds > 0 ? color : "#d4d4d4", "-fx-font-size: 15px; -fx-font-weight: 700;");

                    applyDynamicBackgroundAndBorder(poolRow, currentBeds > 0 ? color + "08" : "#fafaf9", currentBeds > 0 ? color : "#e7e5e4", 1, new CornerRadii(10));
                    applyDynamicTextFillWithStyle(poolName, currentBeds > 0 ? color : "#1c1917", "-fx-font-weight: 600; -fx-font-size: 14px;");
                };

                // Store pool reference for lambda capture
                final Pool targetPool = cp;

                minusBtn.setOnAction(e -> {
                    int current = allocation.get(targetPool);
                    if (current > 0) {
                        allocation.put(targetPool, current - 1);
                        // Update all controls
                        poolControls.getChildren().forEach(node -> {
                            if (node.getUserData() instanceof Runnable) {
                                ((Runnable) node.getUserData()).run();
                            }
                        });
                        updateStatus.run();
                    }
                });

                plusBtn.setOnAction(e -> {
                    int totalAllocated = allocation.values().stream().mapToInt(Integer::intValue).sum();
                    if (totalAllocated < totalBeds) {
                        allocation.put(targetPool, allocation.get(targetPool) + 1);
                        // Update all controls
                        poolControls.getChildren().forEach(node -> {
                            if (node.getUserData() instanceof Runnable) {
                                ((Runnable) node.getUserData()).run();
                            }
                        });
                        updateStatus.run();
                    }
                });

                HBox stepperBox = new HBox(8, minusBtn, bedCountLabel, plusBtn);
                stepperBox.setAlignment(Pos.CENTER);

                poolRow.getChildren().addAll(iconPane, poolName, rowSpacer, stepperBox);

                // Store update function for cross-row updates
                poolRow.setUserData(updateButtons);

                poolControls.getChildren().add(poolRow);

                // Initialize button states
                updateButtons.run();
            }

            // Quick action buttons
            HBox quickActions = new HBox(8);
            quickActions.setPadding(new Insets(8, 0, 0, 0));

            Button unassignAllBtn = I18nControls.newButton(EventRoomSetupI18nKeys.UnassignAll);
            unassignAllBtn.setPadding(new Insets(10, 16, 10, 16));
            ModalityStyle.outlineDangerButton(unassignAllBtn);
            unassignAllBtn.setOnAction(e -> {
                for (Pool cp : getCategoryPools()) {
                    allocation.put(cp, 0);
                }
                poolControls.getChildren().forEach(node -> {
                    if (node.getUserData() instanceof Runnable) {
                        ((Runnable) node.getUserData()).run();
                    }
                });
                updateStatus.run();
            });

            quickActions.getChildren().add(unassignAllBtn);
            HBox.setHgrow(quickActions, Priority.ALWAYS);

            // Footer buttons
            HBox footer = new HBox(12);
            footer.setAlignment(Pos.CENTER_RIGHT);
            footer.setPadding(new Insets(16, 0, 0, 0));
            footer.getStyleClass().add("roomsetup-modal-footer");

            Button cancelBtn = I18nControls.newButton(EventRoomSetupI18nKeys.Cancel);
            cancelBtn.setPadding(new Insets(10, 20, 10, 20));
            Bootstrap.secondaryButton(cancelBtn);
            cancelBtn.setOnAction(e -> closeModal());

            Button saveBtn = I18nControls.newButton(EventRoomSetupI18nKeys.Save);
            saveBtn.setPadding(new Insets(10, 24, 10, 24));
            Bootstrap.primaryButton(saveBtn);
            saveBtn.setOnAction(e -> {
                int totalAllocated = allocation.values().stream().mapToInt(Integer::intValue).sum();
                if (totalAllocated == 0) {
                    unassignRoomCompletely(rc);
                } else {
                    saveRoomSplitAllocation(rc, allocation);
                }
                closeModal();
            });

            footer.getChildren().addAll(cancelBtn, saveBtn);

            // Initialize status
            updateStatus.run();

            dialogContent.getChildren().addAll(header, roomInfo, allocateLabel, poolControls, statusLabel, quickActions, footer);

        } else {
            // === REGULAR ROOM: Simple reassignment UI ===
            // Find current pool
            Pool currentPool = null;
            for (Pool cp : getCategoryPools()) {
                if (getBedsAllocatedInPool(rc, cp) > 0) {
                    currentPool = cp;
                    break;
                }
            }

            Label selectLabel = I18nControls.newLabel(EventRoomSetupI18nKeys.ReassignToCategory);
            selectLabel.getStyleClass().add("roomsetup-section-label");

            VBox categoryOptions = new VBox(8);
            for (Pool cp : getCategoryPools()) {
                String color = cp.getWebColor() != null ? cp.getWebColor() : "#0096D6";
                String graphic = cp.getGraphic();
                String desc = cp.getDescription() != null ? cp.getDescription() : "";
                boolean isCurrentPool = Entities.samePrimaryKey(cp, currentPool);

                HBox option = new HBox(12);
                option.setAlignment(Pos.CENTER_LEFT);
                option.setPadding(new Insets(14, 16, 14, 16));
                option.getStyleClass().add(isCurrentPool ? "roomsetup-room-chip-selected" : "roomsetup-room-chip");
                String optBgColor = isCurrentPool ? color + "15" : "white";
                String optBorderColor = isCurrentPool ? color : "#e7e5e4";
                double optBorderWidth = isCurrentPool ? 2 : 1;
                applyDynamicBackgroundAndBorder(option, optBgColor, optBorderColor, optBorderWidth, new CornerRadii(10));
                option.setCursor(Cursor.HAND);

                StackPane iconPane = createPoolIcon(graphic, color, 32);

                VBox textBox = new VBox(2);
                HBox nameRow = new HBox(8);
                nameRow.setAlignment(Pos.CENTER_LEFT);
                Label poolNameLabel = new Label(cp.getName());
                poolNameLabel.getStyleClass().add("roomsetup-pool-title");
                applyDynamicTextFill(poolNameLabel, color);
                nameRow.getChildren().add(poolNameLabel);
                if (isCurrentPool) {
                    Label currentBadge = I18nControls.newLabel(EventRoomSetupI18nKeys.Current);
                    currentBadge.setPadding(new Insets(2, 8, 2, 8));
                    currentBadge.getStyleClass().add("roomsetup-beds-badge-override");
                    applyDynamicBackground(currentBadge, color, new CornerRadii(4));
                    nameRow.getChildren().add(currentBadge);
                }
                Label poolDesc = new Label(desc);
                poolDesc.getStyleClass().add("roomsetup-pool-count");
                textBox.getChildren().addAll(nameRow, poolDesc);
                HBox.setHgrow(textBox, Priority.ALWAYS);

                option.getChildren().addAll(iconPane, textBox);

                final Pool targetPool = cp;
                option.setOnMouseClicked(e -> {
                    if (!isCurrentPool) {
                        // Reassign: delete old allocation, create new one
                        Map<Pool, Integer> newAllocation = new HashMap<>();
                        newAllocation.put(targetPool, totalBeds);
                        saveRoomSplitAllocation(rc, newAllocation);
                    }
                    closeModal();
                });

                categoryOptions.getChildren().add(option);
            }

            // Unassign button
            HBox unassignRow = new HBox();
            unassignRow.setPadding(new Insets(8, 0, 0, 0));

            Button unassignBtn = I18nControls.newButton(EventRoomSetupI18nKeys.UnassignRoom);
            unassignBtn.setPadding(new Insets(10, 16, 10, 16));
            ModalityStyle.outlineDangerButton(unassignBtn);
            unassignBtn.setOnAction(e -> {
                unassignRoomCompletely(rc);
                closeModal();
            });

            unassignRow.getChildren().add(unassignBtn);

            dialogContent.getChildren().addAll(header, roomInfo, selectLabel, categoryOptions, unassignRow);
        }

        showModal(dialogContent);
    }

    /**
     * Shows bulk assign dialog for assigning multiple rooms.
     */
    private void showBulkAssignDialog(Pool sourcePool, List<ResourceConfiguration> rooms) {
        int totalBeds = rooms.stream().mapToInt(this::getEffectiveBedCount).sum();
        String sourceColor = sourcePool != null && sourcePool.getWebColor() != null ? sourcePool.getWebColor() : "#475569";
        String sourceName = sourcePool != null && sourcePool.getName() != null ? sourcePool.getName() : "Rooms";

        VBox dialogContent = new VBox(0);
        dialogContent.getStyleClass().add("roomsetup-modal-content");
        dialogContent.setMaxWidth(440);

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 24, 20, 24));
        header.getStyleClass().add("roomsetup-modal-header");

        VBox headerText = new VBox(4);
        Label titleLabel = I18nControls.newLabel(EventRoomSetupI18nKeys.AssignAllRooms);
        titleLabel.getStyleClass().add("roomsetup-modal-title");
        Label subtitleLabel = I18nControls.newLabel(EventRoomSetupI18nKeys.SelectCategoryForRooms);
        subtitleLabel.getStyleClass().add("roomsetup-pool-count");
        headerText.getChildren().addAll(titleLabel, subtitleLabel);
        HBox.setHgrow(headerText, Priority.ALWAYS);

        Button closeBtn = new Button("×");
        closeBtn.getStyleClass().add("roomsetup-btn-secondary");
        closeBtn.setOnAction(e -> closeModal());

        header.getChildren().addAll(headerText, closeBtn);

        // Source info
        HBox sourceInfo = new HBox(12);
        sourceInfo.setAlignment(Pos.CENTER_LEFT);
        sourceInfo.setPadding(new Insets(16, 24, 16, 24));
        sourceInfo.getStyleClass().add("roomsetup-pool-header");
        applyDynamicBackground(sourceInfo, sourceColor + "10", CornerRadii.EMPTY);

        // Create source pool icon with SVG
        StackPane sourceIcon = sourcePool != null ? createPoolIcon(sourcePool.getGraphic(), sourceColor, 44) : new StackPane();
        sourceIcon.getStyleClass().add("roomsetup-icon-container");
        applyDynamicBackground(sourceIcon, sourceColor, new CornerRadii(10));

        VBox sourceText = new VBox(4);
        Label fromLabel = new Label();
        I18n.bindI18nTextProperty(fromLabel.textProperty(), EventRoomSetupI18nKeys.FromPool, sourceName);
        fromLabel.getStyleClass().add("roomsetup-pool-title");
        applyDynamicTextFill(fromLabel, sourceColor);
        Label roomsLabel = new Label();
        I18n.bindI18nTextProperty(roomsLabel.textProperty(), EventRoomSetupI18nKeys.RoomsSlashBeds, rooms.size(), totalBeds);
        roomsLabel.getStyleClass().add("roomsetup-pool-count");
        sourceText.getChildren().addAll(fromLabel, roomsLabel);

        sourceInfo.getChildren().addAll(sourceIcon, sourceText);

        // Category options
        VBox categorySection = new VBox(10);
        categorySection.setPadding(new Insets(20, 24, 20, 24));

        Label assignLabel = I18nControls.newLabel(EventRoomSetupI18nKeys.AssignToCategory);
        assignLabel.getStyleClass().add("roomsetup-section-label");

        VBox categoryOptions = new VBox(10);
        for (Pool cp : getCategoryPools()) {
            String color = cp.getWebColor() != null ? cp.getWebColor() : "#0096D6";
            String graphic = cp.getGraphic();
            String desc = cp.getDescription() != null ? cp.getDescription() : "";

            HBox option = new HBox(14);
            option.setAlignment(Pos.CENTER_LEFT);
            option.setPadding(new Insets(16));
            option.getStyleClass().add("roomsetup-room-chip");
            option.setStyle("-fx-border-width: 2;"); // Use CSS class for structure
            option.setCursor(Cursor.HAND);

            // Create icon with SVG
            StackPane iconPane = createPoolIcon(graphic, color, 42);
            iconPane.getStyleClass().add("roomsetup-icon-container");
            applyDynamicBackground(iconPane, color + "15", new CornerRadii(10));

            VBox textBox = new VBox(2);
            Label poolName = new Label(cp.getName());
            poolName.getStyleClass().add("roomsetup-pool-title");
            applyDynamicTextFill(poolName, color);
            Label poolDesc = new Label(desc);
            poolDesc.getStyleClass().add("roomsetup-pool-count");
            textBox.getChildren().addAll(poolName, poolDesc);
            HBox.setHgrow(textBox, Priority.ALWAYS);

            Label bedsBadge = new Label();
            I18n.bindI18nTextProperty(bedsBadge.textProperty(), EventRoomSetupI18nKeys.PlusBeds, totalBeds);
            bedsBadge.setPadding(new Insets(6, 12, 6, 12));
            bedsBadge.getStyleClass().add("roomsetup-beds-badge");
            applyDynamicBackground(bedsBadge, color + "15", new CornerRadii(10));
            bedsBadge.setTextFill(Color.web(color));

            option.getChildren().addAll(iconPane, textBox, bedsBadge);

            // Click to assign all
            final Pool targetPool = cp;
            final List<ResourceConfiguration> roomsCopy = new ArrayList<>(rooms);
            option.setOnMouseClicked(e -> {
                for (ResourceConfiguration room : roomsCopy) {
                    assignRoomToPoolSilent(room, targetPool);
                }
                submitChangesAndRefresh();
                closeModal();
            });

            categoryOptions.getChildren().add(option);
        }

        categorySection.getChildren().addAll(assignLabel, categoryOptions);

        // Footer
        HBox footer = new HBox();
        footer.setPadding(new Insets(16, 24, 16, 24));
        footer.getStyleClass().add("roomsetup-modal-footer");

        Button cancelBtn = I18nControls.newButton(EventRoomSetupI18nKeys.Cancel);
        cancelBtn.setPadding(new Insets(12, 16, 12, 16));
        Bootstrap.secondaryButton(cancelBtn);
        cancelBtn.setMaxWidth(Double.MAX_VALUE);
        cancelBtn.setOnAction(e -> closeModal());
        HBox.setHgrow(cancelBtn, Priority.ALWAYS);

        footer.getChildren().add(cancelBtn);

        dialogContent.getChildren().addAll(header, sourceInfo, categorySection, footer);

        showModal(dialogContent);
    }

    /**
     * Shows a modal dialog with the given content.
     */
    private void showModal(Region content) {
        dialogCallback = DialogUtil.showModalNodeInGoldLayout(
            content,
            FXMainFrameDialogArea.getDialogArea()
        );
    }

    /**
     * Closes the modal dialog.
     */
    private void closeModal() {
        if (dialogCallback != null) {
            dialogCallback.closeDialog();
            dialogCallback = null;
        }
    }

    /**
     * Assigns a room to a category pool for this event.
     */
    private void assignRoomToPool(ResourceConfiguration rc, Pool pool) {
        Resource resource = rc.getResource();
        Console.log("assignRoomToPool: room=" + (resource != null ? resource.getName() : "?") +
                   ", pool=" + pool.getName() +
                   ", event=" + (currentEvent != null ? currentEvent.getPrimaryKey() : "null"));

        if (currentEvent == null) {
            Console.log("ERROR: No current event selected");
            return;
        }

        // Create a new PoolAllocation for this event
        PoolAllocation allocation = getUpdateStore().insertEntity(PoolAllocation.class);
        allocation.setEvent(currentEvent);
        allocation.setResource(resource);
        allocation.setPool(pool);
        allocation.setQuantity(getEffectiveBedCount(rc));

        // Submit changes and trigger reactive refresh
        getUpdateStore().submitChanges()
            .onFailure(error -> Console.log("ERROR saving allocation: " + error))
            .onSuccess(result -> {
                Console.log("SUCCESS: PoolAllocation saved to database");
                dataModel.refreshEventData();
            });
    }

    /**
     * Assigns a room to a category pool without submitting (for bulk operations).
     */
    private void assignRoomToPoolSilent(ResourceConfiguration rc, Pool pool) {
        Resource resource = rc.getResource();
        if (currentEvent == null || resource == null) return;

        PoolAllocation allocation = getUpdateStore().insertEntity(PoolAllocation.class);
        allocation.setEvent(currentEvent);
        allocation.setResource(resource);
        allocation.setPool(pool);
        allocation.setQuantity(getEffectiveBedCount(rc));
    }

    /**
     * Submits pending changes and triggers reactive refresh.
     */
    private void submitChangesAndRefresh() {
        Console.log("submitChangesAndRefresh: Submitting bulk changes...");
        getUpdateStore().submitChanges()
            .onFailure(error -> Console.log("ERROR in bulk submit: " + error))
            .onSuccess(result -> {
                Console.log("SUCCESS: Bulk changes saved to database");
                dataModel.refreshEventData();
            });
    }

    /**
     * Unassigns a room from a category pool.
     */
    private void unassignRoomFromPool(ResourceConfiguration rc, Pool pool) {
        Resource resource = rc.getResource();
        Console.log("unassignRoomFromPool: room=" + (resource != null ? resource.getName() : "?") + ", pool=" + pool.getName());

        // Find and delete the allocation - use Entities.samePrimaryKey() for proper EntityId comparison
        PoolAllocation allocationToDelete = getEventAllocations().stream()
            .filter(pa -> Entities.samePrimaryKey(pa.getResourceId(), rc.getResourceId()) &&
                         Entities.samePrimaryKey(pa.getPoolId(), pool))
            .findFirst()
            .orElse(null);

        if (allocationToDelete != null) {
            Console.log("Found allocation to delete: id=" + allocationToDelete.getPrimaryKey());
            getUpdateStore().deleteEntity(allocationToDelete);
            getUpdateStore().submitChanges()
                .onFailure(error -> Console.log("ERROR deleting allocation: " + error))
                .onSuccess(result -> {
                    Console.log("SUCCESS: Allocation deleted");
                    dataModel.refreshEventData();
                });
        } else {
            Console.log("No allocation found to delete");
        }
    }

    /**
     * Gets rooms assigned to a specific category pool for current event.
     * Note: For split allocations, a room may appear in multiple pools.
     */
    private List<ResourceConfiguration> getRoomsForCategoryPool(Pool pool) {
        // Use Entities.samePrimaryKey() for proper EntityId comparison
        Set<Object> resourceIds = getEventAllocations().stream()
            .filter(pa -> Entities.samePrimaryKey(pa.getPoolId(), pool))
            .map(pa -> Entities.getPrimaryKey(pa.getResourceId()))
            .collect(Collectors.toSet());

        return getRoomConfigs().stream()
            .filter(rc -> resourceIds.contains(Entities.getPrimaryKey(rc.getResourceId())))
            .collect(Collectors.toList());
    }

    /**
     * Gets the total beds allocated to a specific pool across all rooms.
     * This correctly handles split allocations by using the quantity from PoolAllocations.
     */
    private int getTotalBedsInPool(Pool pool) {
        return getEventAllocations().stream()
            .filter(pa -> Entities.samePrimaryKey(pa.getPoolId(), pool))
            .mapToInt(pa -> pa.getQuantity() != null ? pa.getQuantity() : 0)
            .sum();
    }

    /**
     * Gets the bed count for a room from ResourceConfiguration.
     */
    private int getBedCount(ResourceConfiguration rc) {
        Integer max = rc.getMax();
        return max != null ? max : 2; // Default to 2 if not set
    }

    /**
     * Calculates total assigned beds across all category pools.
     * Uses actual allocated quantities from PoolAllocations to correctly handle split allocations.
     */
    private int calculateTotalAssignedBeds() {
        // Sum all allocated quantities from event allocations
        // This correctly handles split allocations where a room's beds are distributed across pools
        return getEventAllocations().stream()
            .mapToInt(pa -> pa.getQuantity() != null ? pa.getQuantity() : 0)
            .sum();
    }

    /**
     * Creates a pool icon from SVG path or falls back to a colored circle.
     */
    private StackPane createPoolIcon(String svgPath, String color, double size) {
        StackPane iconPane = new StackPane();
        iconPane.setMinSize(size, size);
        iconPane.setMaxSize(size, size);
        iconPane.setAlignment(Pos.CENTER);

        if (svgPath != null && !svgPath.isEmpty()) {
            SVGPath svg = new SVGPath();
            svg.setContent(svgPath);
            svg.setFill(Color.web(color));
            double scale = size / 24.0 * 0.6;
            svg.setScaleX(scale);
            svg.setScaleY(scale);
            iconPane.getChildren().add(svg);
        } else {
            Circle circle = new Circle(size / 3, Color.web(color));
            iconPane.getChildren().add(circle);
        }

        return iconPane;
    }

    /**
     * Creates a plus icon using SVGPath for GWT compatibility.
     */
    private Node createPlusIcon(String color) {
        SVGPath svg = new SVGPath();
        // Plus sign: horizontal bar + vertical bar
        svg.setContent("M4 9h8M8 5v8");
        svg.setStroke(Color.web(color));
        svg.setStrokeWidth(2);
        svg.setFill(Color.TRANSPARENT);
        return svg;
    }

    /**
     * Creates a minus icon using SVGPath for GWT compatibility.
     */
    private Node createMinusIcon(String color) {
        SVGPath svg = new SVGPath();
        // Minus sign: just a horizontal bar
        svg.setContent("M4 9h8");
        svg.setStroke(Color.web(color));
        svg.setStrokeWidth(2);
        svg.setFill(Color.TRANSPARENT);
        return svg;
    }

    // ============================================================================
    // SPLIT ALLOCATION SUPPORT
    // ============================================================================

    /**
     * Checks if a room is a dormitory (more than 2 beds), which supports split allocation.
     * Uses effective bed count to respect event-specific capacity overrides.
     */
    private boolean isDormitory(ResourceConfiguration rc) {
        return getEffectiveBedCount(rc) > 2;
    }

    /**
     * Gets all event allocations for a specific room.
     * A room may have multiple allocations if beds are split across pools.
     */
    private List<PoolAllocation> getAllocationsForRoom(ResourceConfiguration rc) {
        if (rc == null || rc.getResource() == null) return Collections.emptyList();
        Object resourceId = Entities.getPrimaryKey(rc.getResourceId());
        return getEventAllocations().stream()
            .filter(pa -> pa.getResourceId() != null &&
                Objects.equals(Entities.getPrimaryKey(pa.getResourceId()), resourceId))
            .collect(Collectors.toList());
    }

    /**
     * Gets the number of beds allocated to a specific pool for a room.
     * Returns 0 if no allocation exists.
     */
    private int getBedsAllocatedInPool(ResourceConfiguration rc, Pool pool) {
        if (rc == null || pool == null) return 0;
        Object resourceId = Entities.getPrimaryKey(rc.getResourceId());
        return getEventAllocations().stream()
            .filter(pa -> pa.getResourceId() != null &&
                Objects.equals(Entities.getPrimaryKey(pa.getResourceId()), resourceId) &&
                Entities.samePrimaryKey(pa.getPoolId(), pool))
            .mapToInt(pa -> pa.getQuantity() != null ? pa.getQuantity() : 0)
            .sum();
    }

    /**
     * Gets total beds allocated across all pools for a room.
     */
    private int getTotalBedsAllocated(ResourceConfiguration rc) {
        return getAllocationsForRoom(rc).stream()
            .mapToInt(pa -> pa.getQuantity() != null ? pa.getQuantity() : 0)
            .sum();
    }

    /**
     * Gets the number of unassigned beds for a room.
     * This is the effective bed count minus the beds already allocated to pools.
     * Uses effective bed count to respect event-specific capacity overrides.
     */
    private int getUnassignedBeds(ResourceConfiguration rc) {
        int totalBeds = getEffectiveBedCount(rc);
        int allocatedBeds = getTotalBedsAllocated(rc);
        return Math.max(0, totalBeds - allocatedBeds);
    }

    /**
     * Checks if a room has partial allocation (some beds assigned, some not).
     * Uses effective bed count to respect event-specific capacity overrides.
     */
    private boolean hasPartialAllocation(ResourceConfiguration rc) {
        int allocated = getTotalBedsAllocated(rc);
        return allocated > 0 && allocated < getEffectiveBedCount(rc);
    }

    /**
     * Checks if a room has split allocation (allocated to multiple pools).
     */
    private boolean hasSplitAllocation(ResourceConfiguration rc) {
        return getAllocationsForRoom(rc).size() > 1;
    }

    /**
     * Saves a split allocation for a room. Deletes existing allocations and creates
     * new ones for each pool with beds > 0.
     *
     * @param rc The room configuration
     * @param allocation Map of Pool to bed count (only entries with beds > 0 will be saved)
     */
    private void saveRoomSplitAllocation(ResourceConfiguration rc, Map<Pool, Integer> allocation) {
        Resource resource = rc.getResource();
        Console.log("saveRoomSplitAllocation: room=" + (resource != null ? resource.getName() : "?") +
                   ", allocation=" + allocation.size() + " pools");

        if (currentEvent == null || resource == null) {
            Console.log("ERROR: No current event or resource");
            return;
        }

        // Delete all existing allocations for this room
        List<PoolAllocation> existingAllocations = getAllocationsForRoom(rc);
        for (PoolAllocation pa : existingAllocations) {
            getUpdateStore().deleteEntity(pa);
        }

        // Create new allocations for each pool with beds > 0
        for (Map.Entry<Pool, Integer> entry : allocation.entrySet()) {
            Pool pool = entry.getKey();
            int beds = entry.getValue();
            if (beds > 0) {
                PoolAllocation newAllocation = getUpdateStore().insertEntity(PoolAllocation.class);
                newAllocation.setEvent(currentEvent);
                newAllocation.setResource(resource);
                newAllocation.setPool(pool);
                newAllocation.setQuantity(beds);
            }
        }

        // Submit changes
        getUpdateStore().submitChanges()
            .onFailure(error -> Console.log("ERROR saving split allocation: " + error))
            .onSuccess(result -> {
                Console.log("SUCCESS: Split allocation saved");
                dataModel.refreshEventData();
            });
    }

    /**
     * Unassigns all beds of a room from all pools.
     */
    private void unassignRoomCompletely(ResourceConfiguration rc) {
        Resource resource = rc.getResource();
        Console.log("unassignRoomCompletely: room=" + (resource != null ? resource.getName() : "?"));

        // Delete all existing allocations for this room
        List<PoolAllocation> existingAllocations = getAllocationsForRoom(rc);
        for (PoolAllocation pa : existingAllocations) {
            getUpdateStore().deleteEntity(pa);
        }

        getUpdateStore().submitChanges()
            .onFailure(error -> Console.log("ERROR unassigning room: " + error))
            .onSuccess(result -> {
                Console.log("SUCCESS: Room unassigned");
                dataModel.refreshEventData();
            });
    }

    // ============================================================================
    // CROSS-PLATFORM DYNAMIC STYLING HELPERS
    // ============================================================================
    // WebFX doesn't translate setStyle() calls - only CSS files are translated.
    // For dynamic colors (from database), we must use BOTH setStyle() for JavaFX
    // AND programmatic styling (setBackground/setBorder/setTextFill) for WebFX.

    /**
     * Applies a dynamic background color to a region (works on both JavaFX and WebFX).
     * @param region The region to style
     * @param colorHex The color in hex format (e.g., "#475569" or "#47556915" with alpha)
     * @param radii The corner radii (use CornerRadii.EMPTY for no rounding)
     */
    private static void applyDynamicBackground(Region region, String colorHex, CornerRadii radii) {
        // For JavaFX (desktop)
        region.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: " + radiiToCss(radii) + ";");
        // For WebFX (browser) - programmatic styling
        region.setBackground(new Background(new BackgroundFill(Color.web(colorHex), radii, null)));
    }

    /**
     * Applies a dynamic border color to a region (works on both JavaFX and WebFX).
     * @param region The region to style
     * @param colorHex The border color in hex format
     * @param width The border width in pixels
     * @param radii The corner radii
     */
    private static void applyDynamicBorder(Region region, String colorHex, double width, CornerRadii radii) {
        // For JavaFX (desktop)
        region.setStyle("-fx-border-color: " + colorHex + "; -fx-border-width: " + width + "; -fx-border-radius: " + radiiToCss(radii) + ";");
        // For WebFX (browser) - programmatic styling
        region.setBorder(new Border(new BorderStroke(Color.web(colorHex), BorderStrokeStyle.SOLID, radii, new BorderWidths(width))));
    }

    /**
     * Applies both dynamic background and border to a region (works on both JavaFX and WebFX).
     * @param region The region to style
     * @param bgColorHex The background color in hex format (can include alpha like "#47556915")
     * @param borderColorHex The border color in hex format
     * @param borderWidth The border width in pixels
     * @param radii The corner radii
     */
    private static void applyDynamicBackgroundAndBorder(Region region, String bgColorHex, String borderColorHex, double borderWidth, CornerRadii radii) {
        // For JavaFX (desktop)
        region.setStyle("-fx-background-color: " + bgColorHex + "; -fx-background-radius: " + radiiToCss(radii) +
                       "; -fx-border-color: " + borderColorHex + "; -fx-border-width: " + borderWidth +
                       "; -fx-border-radius: " + radiiToCss(radii) + ";");
        // For WebFX (browser) - programmatic styling
        region.setBackground(new Background(new BackgroundFill(Color.web(bgColorHex), radii, null)));
        region.setBorder(new Border(new BorderStroke(Color.web(borderColorHex), BorderStrokeStyle.SOLID, radii, new BorderWidths(borderWidth))));
    }

    /**
     * Applies a dynamic text fill color to a label (works on both JavaFX and WebFX).
     * @param label The label to style
     * @param colorHex The text color in hex format
     */
    private static void applyDynamicTextFill(Label label, String colorHex) {
        // For JavaFX (desktop)
        label.setStyle("-fx-text-fill: " + colorHex + ";");
        // For WebFX (browser) - programmatic styling
        label.setTextFill(Color.web(colorHex));
    }

    /**
     * Applies dynamic text fill with additional font styling to a label.
     * @param label The label to style
     * @param colorHex The text color in hex format
     * @param additionalStyle Additional CSS style properties (e.g., "-fx-font-weight: bold;")
     */
    private static void applyDynamicTextFillWithStyle(Label label, String colorHex, String additionalStyle) {
        // For JavaFX (desktop)
        label.setStyle("-fx-text-fill: " + colorHex + "; " + additionalStyle);
        // For WebFX (browser) - programmatic styling for text color
        label.setTextFill(Color.web(colorHex));
    }

    /**
     * Applies dynamic button styling with background color (works on both JavaFX and WebFX).
     * @param button The button to style
     * @param bgColorHex The background color in hex format
     */
    private static void applyDynamicButtonBackground(Region button, String bgColorHex) {
        // For JavaFX (desktop)
        button.setStyle("-fx-background-color: " + bgColorHex + ";");
        // For WebFX (browser) - programmatic styling
        button.setBackground(new Background(new BackgroundFill(Color.web(bgColorHex), new CornerRadii(6), null)));
    }

    /**
     * Applies dynamic button styling with border (works on both JavaFX and WebFX).
     * @param button The button to style
     * @param borderColorHex The border color in hex format
     * @param textColorHex The text color (if button has text - use applyDynamicTextFill for Label children)
     */
    private static void applyDynamicButtonBorderAndText(Button button, String borderColorHex, String textColorHex) {
        // For JavaFX (desktop)
        button.setStyle("-fx-border-color: " + borderColorHex + "; -fx-text-fill: " + textColorHex + ";");
        // For WebFX (browser) - programmatic styling
        button.setBorder(new Border(new BorderStroke(Color.web(borderColorHex), BorderStrokeStyle.SOLID, new CornerRadii(4), new BorderWidths(1))));
        button.setTextFill(Color.web(textColorHex));
    }

    /**
     * Helper to convert CornerRadii to CSS string.
     */
    private static String radiiToCss(CornerRadii radii) {
        if (radii == null || radii == CornerRadii.EMPTY) return "0";
        // For simplicity, use the top-left radius (assuming uniform radii)
        return String.valueOf((int) radii.getTopLeftHorizontalRadius());
    }
}
