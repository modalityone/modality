package one.modality.hotel.backoffice.activities.roomsetup.view;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.domainmodel.HasDataSourceModel;
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
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.bootstrap.ModalityStyle;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import one.modality.base.shared.entities.*;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.hotel.backoffice.activities.roomsetup.RoomSetupI18nKeys;
import one.modality.hotel.backoffice.activities.roomsetup.RoomSetupPresentationModel;
import one.modality.hotel.backoffice.activities.roomsetup.dialog.DefaultAllocationDialog;
import one.modality.hotel.backoffice.activities.roomsetup.dialog.DialogManager;
import one.modality.hotel.backoffice.activities.roomsetup.util.PoolTypeFilter;
import one.modality.hotel.backoffice.activities.roomsetup.util.UIComponentDecorators;

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

    // UI components
    private VBox mainContainer;
    private VBox roomListContainer;
    private Label roomCountLabel;
    private HBox filterChipsContainer;
    private StackPane loadingOverlay;
    private boolean dataLoaded = false;

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
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(50, 50);
        VBox loadingBox = new VBox(10);
        loadingBox.setAlignment(Pos.CENTER);
        Label loadingLabel = I18nControls.newLabel(RoomSetupI18nKeys.LoadingAllocations);
        loadingLabel.getStyleClass().add(UIComponentDecorators.CSS_SUBTITLE);
        loadingBox.getChildren().addAll(progressIndicator, loadingLabel);
        loadingOverlay.getChildren().add(loadingBox);

        // Use StackPane to overlay loading indicator on scroll content
        StackPane contentWithLoading = new StackPane();
        contentWithLoading.getChildren().addAll(scrollPane, loadingOverlay);
        VBox.setVgrow(contentWithLoading, Priority.ALWAYS);

        mainContainer.getChildren().addAll(headerSection, filterBar, contentWithLoading);

        // Listen for data changes
        resourceConfigurations.addListener((ListChangeListener<? super ResourceConfiguration>) change -> updateRoomList());
        pools.addListener((ListChangeListener<? super Pool>) change -> {
            updateFilterChips();
            updateRoomList();
        });
        poolAllocations.addListener((ListChangeListener<? super PoolAllocation>) change -> updateRoomList());
        buildings.addListener((ListChangeListener<? super Building>) change -> updateRoomList());
        accommodationItems.addListener((ListChangeListener<? super Item>) change -> updateRoomList());

        // Listen for filter and group changes
        filterPoolProperty.addListener((obs, oldVal, newVal) -> {
            updateFilterChips();
            updateRoomList();
        });
        showUnassignedOnlyProperty.addListener((obs, oldVal, newVal) -> {
            updateFilterChips();
            updateRoomList();
        });
        groupByProperty.addListener((obs, oldVal, newVal) -> updateRoomList());

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
            // Use special styling for unassigned
            if (isUnassignedSelected) {
                unassignedBtn.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #92400e; " +
                        "-fx-padding: 8 14; -fx-background-radius: 8; -fx-border-color: #92400e; -fx-border-width: 2; -fx-border-radius: 8; -fx-cursor: hand; -fx-font-weight: 600;");
            } else {
                unassignedBtn.setStyle("-fx-background-color: " + (unassignedCount > 0 ? "#fef3c7" : "white") + "; " +
                        "-fx-text-fill: " + (unassignedCount > 0 ? "#92400e" : "#78716c") + "; " +
                        "-fx-padding: 8 14; -fx-background-radius: 8; -fx-border-color: #e5e5e5; -fx-border-radius: 8; -fx-cursor: hand;");
            }
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

                poolBtn.setStyle(UIComponentDecorators.getPoolFilterChipStyle(color, isSelected));

                poolBtn.setOnAction(e -> {
                    showUnassignedOnlyProperty.set(false);
                    filterPoolProperty.set(isSelected ? null : pool);
                });
                filterChipsContainer.getChildren().add(poolBtn);
            }
        });
    }

    private long countUnassignedRooms() {
        return resourceConfigurations.stream()
                .filter(rc -> {
                    Resource resource = rc.getResource();
                    if (resource == null) return true;
                    return poolAllocations.stream()
                            .noneMatch(pa -> pa.getResource() != null && pa.getResource().equals(resource) && pa.getEvent() == null);
                })
                .count();
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

            // Hide loading overlay once we have data
            if (dataLoaded && loadingOverlay != null) {
                loadingOverlay.setVisible(false);
                loadingOverlay.setManaged(false);
            }

            roomListContainer.getChildren().clear();

            // Filter rooms
            List<ResourceConfiguration> filteredRooms = resourceConfigurations.stream()
                    .filter(rc -> {
                        // Check for unassigned filter
                        if (showUnassignedOnlyProperty.get()) {
                            Resource resource = rc.getResource();
                            if (resource == null) return true;
                            return poolAllocations.stream()
                                    .noneMatch(pa -> pa.getResource() != null && pa.getResource().equals(resource) && pa.getEvent() == null);
                        }

                        // Check for pool filter
                        Pool filterPool = filterPoolProperty.get();
                        if (filterPool == null) return true;

                        Resource resource = rc.getResource();
                        if (resource == null) return false;

                        return poolAllocations.stream()
                                .anyMatch(pa -> pa.getResource() != null && pa.getResource().equals(resource)
                                        && pa.getPool() != null && pa.getPool().equals(filterPool)
                                        && pa.getEvent() == null);
                    })
                    .collect(Collectors.toList());

            // Update count
            long assignedCount = filteredRooms.stream()
                    .filter(rc -> {
                        Resource resource = rc.getResource();
                        if (resource == null) return false;
                        return poolAllocations.stream()
                                .anyMatch(pa -> pa.getResource() != null && pa.getResource().equals(resource) && pa.getEvent() == null);
                    })
                    .count();
            roomCountLabel.setText(I18n.getI18nText(RoomSetupI18nKeys.RoomStats, filteredRooms.size(), assignedCount, filteredRooms.size() - assignedCount));

            // Group rooms
            Map<String, List<ResourceConfiguration>> groupedRooms = groupRooms(filteredRooms);

            // Create group panels
            for (Map.Entry<String, List<ResourceConfiguration>> entry : groupedRooms.entrySet()) {
                VBox groupPanel = createGroupPanel(entry.getKey(), entry.getValue());
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
                    if (resource != null) {
                        List<String> poolNames = poolAllocations.stream()
                                .filter(pa -> pa.getResource() != null && pa.getResource().equals(resource) && pa.getEvent() == null)
                                .map(pa -> pa.getPool() != null ? pa.getPool().getName() : I18n.getI18nText(RoomSetupI18nKeys.UnknownType))
                                .distinct()
                                .collect(Collectors.toList());
                        key = poolNames.isEmpty() ? I18n.getI18nText(RoomSetupI18nKeys.UnassignedWarning) : String.join(", ", poolNames);
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

    private VBox createGroupPanel(String groupName, List<ResourceConfiguration> rooms) {
        VBox panel = new VBox();
        panel.getStyleClass().add(UIComponentDecorators.CSS_CARD);

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(10);
        header.setPadding(new Insets(14, 20, 14, 20));
        header.getStyleClass().add(UIComponentDecorators.CSS_CARD_HEADER);

        Label groupLabel = new Label(groupName);
        groupLabel.getStyleClass().add(UIComponentDecorators.CSS_BODY_BOLD);

        Label countLabel = ModalityStyle.badgeLightInfo(new Label(rooms.size() + " rooms"));

        header.getChildren().addAll(groupLabel, countLabel);

        // Room cards
        FlowPane roomGrid = new FlowPane();
        roomGrid.setHgap(12);
        roomGrid.setVgap(12);
        roomGrid.setPadding(new Insets(16, 20, 16, 20));

        for (ResourceConfiguration rc : rooms) {
            HBox roomCard = createRoomCard(rc);
            roomGrid.getChildren().add(roomCard);
        }

        panel.getChildren().addAll(header, roomGrid);
        return panel;
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

        // Determine pool allocation for styling
        Resource resource = rc.getResource();
        List<PoolAllocation> roomAllocations = poolAllocations.stream()
                .filter(pa -> pa.getResource() != null && resource != null && pa.getResource().equals(resource) && pa.getEvent() == null)
                .collect(Collectors.toList());

        boolean isAssigned = !roomAllocations.isEmpty();
        String borderColor = isAssigned ? "#10b981" : "#f59e0b";

        // Dynamic border color still needs setStyle (CSS can't handle dynamic colors)
        card.setStyle("-fx-border-color: " + borderColor + "; -fx-border-width: 2; -fx-border-radius: 10;");

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
                    .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), o -> where("resource.site.organization=?", o))
                    .storeEntitiesInto(resourceConfigurations)
                    .addEntitiesHandler(entities -> {
                        dataLoaded = true;
                        Platform.runLater(this::updateRoomList);
                    })
                    .setResultCacheEntry("modality/hotel/roomsetup/default-alloc-rooms")
                    .start();

            // Load Pools
            poolRem = ReactiveEntitiesMapper.<Pool>createPushReactiveChain(mixin)
                    .always("{class: 'Pool', fields: 'name,graphic,webColor,eventPool,eventType'}")
                    .always(orderBy("name"))
                    .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), o -> where("eventType.organization=?", o))
                    .storeEntitiesInto(pools)
                    .addEntitiesHandler(entities -> {
                        Platform.runLater(this::updateFilterChips);
                        Platform.runLater(this::updateRoomList);
                    })
                    .setResultCacheEntry("modality/hotel/roomsetup/default-alloc-pools")
                    .start();

            // Load PoolAllocations (default only - where event is null)
            poolAllocationRem = ReactiveEntitiesMapper.<PoolAllocation>createPushReactiveChain(mixin)
                    .always("{class: 'PoolAllocation', fields: 'pool.name,pool.graphic,pool.webColor,resource.name,quantity,event'}")
                    .always(where("event is null"))
                    .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), o -> where("resource.site.organization=?", o))
                    .storeEntitiesInto(poolAllocations)
                    .addEntitiesHandler(entities -> Platform.runLater(this::updateRoomList))
                    .setResultCacheEntry("modality/hotel/roomsetup/default-alloc-allocations")
                    .start();

            // Load Buildings
            buildingRem = ReactiveEntitiesMapper.<Building>createPushReactiveChain(mixin)
                    .always("{class: 'Building', fields: 'name,site'}")
                    .always(orderBy("name"))
                    .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), o -> where("site.organization=?", o))
                    .storeEntitiesInto(buildings)
                    .setResultCacheEntry("modality/hotel/roomsetup/default-alloc-buildings")
                    .start();

            // Load accommodation Items
            itemRem = ReactiveEntitiesMapper.<Item>createPushReactiveChain(mixin)
                    .always("{class: 'Item', fields: 'name,code,ord,family.code'}")
                    .always(where("family.code=?", KnownItemFamily.ACCOMMODATION.getCode()))
                    .always(where("(deprecated is null or deprecated=false)"))
                    .always(orderBy("ord,name"))
                    .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), o -> where("organization=?", o))
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
