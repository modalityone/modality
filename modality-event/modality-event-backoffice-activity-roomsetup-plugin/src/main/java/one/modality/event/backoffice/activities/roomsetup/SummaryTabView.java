package one.modality.event.backoffice.activities.roomsetup;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.routing.activity.impl.elementals.activeproperty.HasActiveProperty;
import one.modality.base.client.bootstrap.ModalityStyle;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import one.modality.base.shared.entities.Building;
import dev.webfx.stack.orm.entity.Entities;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Pool;
import one.modality.base.shared.entities.PoolAllocation;
import one.modality.base.shared.entities.Resource;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.event.client.event.fx.FXEvent;

import java.util.*;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Tab 3: Summary - Review totals and room assignments.
 *
 * This view displays:
 * - Grand totals (beds, rooms, external rooms, overrides)
 * - Breakdown by category pool
 * - Grouped room list with search and grouping options
 * - Notes and observations
 *
 * @author Bruno Salmon
 */
final class SummaryTabView {

    // Shared data model (provided by activity)
    private EventRoomSetupDataModel dataModel;
    private final BooleanProperty activeProperty = new SimpleBooleanProperty();

    // Per-tab loading state
    private final BooleanProperty loadingProperty = new SimpleBooleanProperty(true);

    // UI components
    private final VBox mainContent = new VBox(24);
    private final ScrollPane mainContainer;
    private final StackPane containerWithLoading;
    private final StackPane loadingOverlay;
    private final HBox grandTotalsRow = new HBox(16);
    private final FlowPane categoryBreakdown = new FlowPane(12, 12);
    private final VBox notesSection = new VBox(10);
    private final VBox roomsListSection = new VBox(16);

    // State
    private Event currentEvent;
    private final StringProperty searchText = new SimpleStringProperty("");
    private String groupBy = "category"; // category, type, site, name

    // Group by buttons (stored for style updates)
    private Button byCategoryBtn, byTypeBtn, bySiteBtn, byNameBtn;

    public SummaryTabView() {
        mainContent.setPadding(new Insets(20));
        mainContainer = Controls.createVerticalScrollPane(mainContent);

        // Create loading overlay
        loadingOverlay = new StackPane();
        loadingOverlay.getStyleClass().add("roomsetup-modal-overlay");
        Region spinner = Controls.createPageSizeSpinner();
        VBox loadingBox = new VBox(10);
        loadingBox.setAlignment(Pos.CENTER);
        Label loadingLabel = I18nControls.newLabel(EventRoomSetupI18nKeys.LoadingData);
        loadingLabel.getStyleClass().add("roomsetup-stat-label");
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

        Console.log("SummaryTabView: Starting logic with shared data model");

        // Bind loading state to data model's loading properties
        FXProperties.runNowAndOnPropertiesChange(() -> {
            boolean allLoaded = dataModel.isAllDataLoaded();
            loadingProperty.set(!allLoaded);
            if (allLoaded) {
                Platform.runLater(() -> {
                    loadingOverlay.setVisible(false);
                    loadingOverlay.setManaged(false);
                    refreshUI();
                });
            }
        }, dataModel.sourcePoolsLoadedProperty(),
           dataModel.categoryPoolsLoadedProperty(),
           dataModel.resourcesLoadedProperty(),
           dataModel.roomTypesLoadedProperty(),
           dataModel.permanentConfigsLoadedProperty(),
           dataModel.defaultAllocationsLoadedProperty(),
           dataModel.eventConfigsLoadedProperty(),
           dataModel.eventAllocationsLoadedProperty());

        // Listen to shared list changes (when rooms are assigned/unassigned in other tabs)
        dataModel.getEventAllocations().addListener((ListChangeListener<PoolAllocation>) c -> {
            Console.log("SummaryTabView: Event allocations changed, refreshing UI");
            Platform.runLater(this::refreshUI);
        });
        dataModel.getEventRoomConfigs().addListener((ListChangeListener<ResourceConfiguration>) c -> {
            Console.log("SummaryTabView: Event configs changed, refreshing UI");
            Platform.runLater(this::refreshUI);
        });

        // Listen for event changes
        FXProperties.runNowAndOnPropertiesChange(() -> {
            Event newEvent = FXEvent.getEvent();
            boolean eventChanged = !Objects.equals(
                currentEvent != null ? currentEvent.getPrimaryKey() : null,
                newEvent != null ? newEvent.getPrimaryKey() : null
            );
            currentEvent = newEvent;

            Console.log("SummaryTabView: Event property changed - event=" +
                (currentEvent != null ? currentEvent.getPrimaryKey() : "null") +
                ", active=" + activeProperty.get() + ", changed=" + eventChanged);

            if (activeProperty.get() && currentEvent != null) {
                Platform.runLater(this::refreshUI);
            }
        }, FXEvent.eventProperty());
    }

    void setActive(boolean active) {
        Console.log("SummaryTabView: setActive(" + active + "), currentEvent=" +
            (currentEvent != null ? currentEvent.getPrimaryKey() : "null"));
        activeProperty.set(active);
        if (active && dataModel != null) {
            // Refresh event data when tab becomes active (in case changes were made elsewhere)
            dataModel.refreshEventData();
        }
    }

    // Convenience accessors to shared data
    private ObservableList<Pool> getCategoryPools() { return dataModel.getCategoryPools(); }
    private ObservableList<Resource> getResources() { return dataModel.getResources(); }
    private ObservableList<PoolAllocation> getPoolAllocations() { return dataModel.getEventAllocations(); }
    private ObservableList<ResourceConfiguration> getBaseResourceConfigurations() { return dataModel.getPermanentRoomConfigs(); }
    private ObservableList<ResourceConfiguration> getEventResourceConfigurations() { return dataModel.getEventRoomConfigs(); }

    /**
     * Builds the initial UI structure.
     */
    private void buildUI() {
        mainContent.getChildren().clear();

        // Title
        Label title = I18nControls.newLabel(EventRoomSetupI18nKeys.SummaryTabTitle);
        TextTheme.createPrimaryTextFacet(title).style();
        title.getStyleClass().add(Bootstrap.H3);
        title.setPadding(new Insets(0, 0, 10, 0));

        // Grand totals row
        grandTotalsRow.setAlignment(Pos.CENTER_LEFT);

        // Category breakdown
        categoryBreakdown.setPadding(new Insets(0));

        // Notes section
        notesSection.getStyleClass().add("roomsetup-notes-section");
        notesSection.setPadding(new Insets(20));

        // Toolbar with search and group by
        HBox toolbar = createToolbar();

        // Rooms list section
        roomsListSection.setPadding(new Insets(0));

        mainContent.getChildren().addAll(title, grandTotalsRow, categoryBreakdown, notesSection, toolbar, roomsListSection);
    }

    /**
     * Creates the toolbar with search and group by options.
     */
    private HBox createToolbar() {
        HBox toolbar = new HBox(16);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(0));

        // Search field
        TextField searchField = new TextField();
        searchField.setPromptText("Search rooms...");
        searchField.setPrefWidth(250);
        searchField.getStyleClass().add("roomsetup-search-input");
        searchField.textProperty().bindBidirectional(searchText);
        searchField.textProperty().addListener((obs, old, newVal) -> refreshRoomsList());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Group by label
        Label groupByLabel = I18nControls.newLabel(EventRoomSetupI18nKeys.GroupBy);
        groupByLabel.getStyleClass().add("roomsetup-pool-title");

        // Group by buttons
        HBox groupByButtons = new HBox(4);
        groupByButtons.setAlignment(Pos.CENTER_LEFT);

        byCategoryBtn = createGroupByButton(EventRoomSetupI18nKeys.ByCategory, "category");
        byTypeBtn = createGroupByButton(EventRoomSetupI18nKeys.ByType, "type");
        bySiteBtn = createGroupByButton(EventRoomSetupI18nKeys.BySite, "site");
        byNameBtn = createGroupByButton(EventRoomSetupI18nKeys.ByName, "name");

        groupByButtons.getChildren().addAll(byCategoryBtn, byTypeBtn, bySiteBtn, byNameBtn);

        toolbar.getChildren().addAll(searchField, spacer, groupByLabel, groupByButtons);
        return toolbar;
    }

    /**
     * Creates a group by button.
     */
    private Button createGroupByButton(Object labelKey, String groupValue) {
        Button btn = I18nControls.newButton(labelKey);
        btn.setPadding(new Insets(6, 12, 6, 12));
        updateGroupByButtonStyle(btn, groupValue);

        btn.setOnAction(e -> {
            groupBy = groupValue;
            updateAllGroupByButtonStyles();
            refreshUI();
        });

        return btn;
    }

    /**
     * Updates all group by button styles based on current selection.
     */
    private void updateAllGroupByButtonStyles() {
        if (byCategoryBtn != null) updateGroupByButtonStyle(byCategoryBtn, "category");
        if (byTypeBtn != null) updateGroupByButtonStyle(byTypeBtn, "type");
        if (bySiteBtn != null) updateGroupByButtonStyle(bySiteBtn, "site");
        if (byNameBtn != null) updateGroupByButtonStyle(byNameBtn, "name");
    }

    /**
     * Updates group by button style based on selection using ModalityStyle chip buttons.
     */
    private void updateGroupByButtonStyle(Button btn, String groupValue) {
        boolean isActive = groupBy.equals(groupValue);
        ModalityStyle.setChipButtonPrimarySelected(btn, isActive);
    }

    /**
     * Refreshes the UI with current data.
     */
    private void refreshUI() {
        refreshGrandTotals();
        refreshCategoryBreakdown();
        refreshNotesSection();
        refreshRoomsList();
    }

    /**
     * Gets the list of assigned rooms (rooms with event-specific pool allocations).
     */
    private List<Resource> getAssignedRooms() {
        // Debug: Log allocation details
        for (PoolAllocation pa : getPoolAllocations()) {
            Pool pool = pa.getPool();
            boolean isCat = isCategoryPool(pool);
            Console.log("SummaryTabView: Allocation - pool=" + (pool != null ? pool.getName() : "null") +
                ", eventPool=" + (pool != null ? pool.isEventPool() : "null") +
                ", isCategoryPool=" + isCat +
                ", resourceId=" + (pa.getResourceId() != null ? Entities.getPrimaryKey(pa.getResourceId()) : "null"));
        }

        // Get resource IDs from event-specific pool allocations (category pools)
        Set<Object> assignedResourceIds = getPoolAllocations().stream()
            .filter(pa -> pa.getResourceId() != null)
            .filter(pa -> isCategoryPool(pa.getPool()))
            .map(pa -> Entities.getPrimaryKey(pa.getResourceId()))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        Console.log("SummaryTabView: Found " + assignedResourceIds.size() + " assigned resource IDs from " + getPoolAllocations().size() + " allocations");

        // Debug: Check if any resources match
        if (!assignedResourceIds.isEmpty()) {
            int matchCount = (int) getResources().stream()
                .filter(r -> assignedResourceIds.contains(Entities.getPrimaryKey(r)))
                .count();
            Console.log("SummaryTabView: " + matchCount + " resources match from " + getResources().size() + " total resources");
        }

        return getResources().stream()
            .filter(r -> assignedResourceIds.contains(Entities.getPrimaryKey(r)))
            .collect(Collectors.toList());
    }

    /**
     * Refreshes the grand totals row.
     */
    private void refreshGrandTotals() {
        grandTotalsRow.getChildren().clear();

        List<Resource> assignedRooms = getAssignedRooms();
        int totalBeds = assignedRooms.stream().mapToInt(this::getBedCount).sum();
        long overrideCount = getEventResourceConfigurations().size();

        Console.log("SummaryTabView: Grand totals - " + assignedRooms.size() + " rooms, " + totalBeds + " beds, " + overrideCount + " overrides");

        // Total beds
        VBox bedsCard = createGrandTotalCard(String.valueOf(totalBeds), EventRoomSetupI18nKeys.TotalBedsAssigned, "#0096D6");

        // Rooms allocated
        VBox roomsCard = createGrandTotalCard(String.valueOf(assignedRooms.size()), EventRoomSetupI18nKeys.RoomsAllocated, "#10b981");

        // Category pools count
        VBox poolsCard = createGrandTotalCard(String.valueOf(getCategoryPools().size()), EventRoomSetupI18nKeys.CategoryPools, "#8b5cf6");

        // Room adjustments
        VBox adjustmentsCard = createGrandTotalCard(String.valueOf(overrideCount), EventRoomSetupI18nKeys.RoomAdjustments, "#f59e0b");

        grandTotalsRow.getChildren().addAll(bedsCard, roomsCard, poolsCard, adjustmentsCard);
    }

    /**
     * Creates a grand total card.
     */
    private VBox createGrandTotalCard(String value, Object labelKey, String color) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("roomsetup-stat-card");
        HBox.setHgrow(card, Priority.ALWAYS);

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("roomsetup-stat-value");
        // Apply dynamic color for the value (color varies per card type) - use cross-platform helper
        applyDynamicTextFill(valueLabel, color);

        Label descLabel = I18nControls.newLabel(labelKey);
        descLabel.getStyleClass().add("roomsetup-stat-label");

        card.getChildren().addAll(valueLabel, descLabel);
        return card;
    }

    /**
     * Refreshes the category breakdown.
     */
    private void refreshCategoryBreakdown() {
        categoryBreakdown.getChildren().clear();

        for (Pool pool : getCategoryPools()) {
            String color = pool.getWebColor() != null ? pool.getWebColor() : "#0096D6";
            String name = pool.getName() != null ? pool.getName() : "Pool";
            String graphic = pool.getGraphic();

            // Count beds for this pool
            int bedCount = getBedCountForPool(pool);

            HBox pill = new HBox(10);
            pill.setAlignment(Pos.CENTER_LEFT);
            pill.setPadding(new Insets(12, 16, 12, 16));
            pill.getStyleClass().add("roomsetup-category-pill");
            // Dynamic color from database - use cross-platform helper
            applyDynamicBackgroundAndBorder(pill, color + "15", color + "30", 1, new CornerRadii(20));

            // Pool icon (SVG or fallback circle)
            StackPane iconPane = createPoolIcon(graphic, color, 24);

            Label nameLabel = new Label(name);
            nameLabel.getStyleClass().add("roomsetup-category-pill-label");
            // Dynamic color from database - use cross-platform helper
            applyDynamicTextFill(nameLabel, color);

            Label countBadge = new Label(String.valueOf(bedCount));
            countBadge.setPadding(new Insets(4, 10, 4, 10));
            countBadge.getStyleClass().add("roomsetup-category-pill-value");
            // Dynamic color from database - use cross-platform helper
            applyDynamicBackground(countBadge, color, new CornerRadii(10));

            pill.getChildren().addAll(iconPane, nameLabel, countBadge);
            categoryBreakdown.getChildren().add(pill);
        }
    }

    /**
     * Refreshes the notes section.
     */
    private void refreshNotesSection() {
        notesSection.getChildren().clear();

        Label notesTitle = I18nControls.newLabel(EventRoomSetupI18nKeys.NotesAndObservations);
        notesTitle.getStyleClass().add("roomsetup-pool-title");
        notesSection.getChildren().add(notesTitle);

        VBox notesList = new VBox(10);

        List<Resource> assignedRooms = getAssignedRooms();
        int totalBeds = assignedRooms.stream().mapToInt(this::getBedCount).sum();

        // Ready for booking note
        if (totalBeds > 0) {
            String eventName = currentEvent != null ? currentEvent.getName() : "this event";
            HBox note = createNoteSuccess("✅", totalBeds + " " +
                I18nControls.newLabel(EventRoomSetupI18nKeys.BedsReadyForBooking).getText() + " " + eventName + ".");
            notesList.getChildren().add(note);
        } else {
            HBox note = createNoteWarning("💡", I18nControls.newLabel(EventRoomSetupI18nKeys.NoRoomsAssignedYet).getText() + ". " +
                I18nControls.newLabel(EventRoomSetupI18nKeys.AssignRoomsFirst).getText());
            notesList.getChildren().add(note);
        }

        // Override note
        if (getEventResourceConfigurations().size() > 0) {
            HBox note = createNoteInfo("✏️", getEventResourceConfigurations().size() + " " +
                I18nControls.newLabel(EventRoomSetupI18nKeys.RoomsHaveBeenModified).getText());
            notesList.getChildren().add(note);
        }

        notesSection.getChildren().add(notesList);
    }

    /**
     * Creates a success note item (green).
     */
    private HBox createNoteSuccess(String icon, String text) {
        return createNote(icon, text, "roomsetup-note-success", "roomsetup-note-text-success");
    }

    /**
     * Creates a warning note item (yellow).
     */
    private HBox createNoteWarning(String icon, String text) {
        return createNote(icon, text, "roomsetup-note-warning", "roomsetup-note-text-warning");
    }

    /**
     * Creates an info note item (amber).
     */
    private HBox createNoteInfo(String icon, String text) {
        return createNote(icon, text, "roomsetup-note-warning", "roomsetup-note-text-warning");
    }

    /**
     * Creates a note item with CSS classes.
     */
    private HBox createNote(String icon, String text, String noteClass, String textClass) {
        HBox note = new HBox(10);
        note.setAlignment(Pos.CENTER_LEFT);
        note.setPadding(new Insets(12, 14, 12, 14));
        note.getStyleClass().addAll("roomsetup-note-item", noteClass);

        Label iconLabel = new Label(icon);

        Label textLabel = new Label(text);
        textLabel.getStyleClass().add(textClass);
        textLabel.setWrapText(true);

        note.getChildren().addAll(iconLabel, textLabel);
        return note;
    }

    /**
     * Refreshes the rooms list.
     */
    private void refreshRoomsList() {
        roomsListSection.getChildren().clear();

        List<Resource> assignedRooms = getAssignedRooms();

        // Filter by search text
        String search = searchText.get().toLowerCase();
        if (!search.isEmpty()) {
            assignedRooms = assignedRooms.stream()
                .filter(r -> {
                    String name = r.getName() != null ? r.getName().toLowerCase() : "";
                    return name.contains(search);
                })
                .collect(Collectors.toList());
        }

        if (assignedRooms.isEmpty()) {
            Label emptyLabel = I18nControls.newLabel(EventRoomSetupI18nKeys.NoRoomsMatchSearch);
            emptyLabel.getStyleClass().add("roomsetup-pool-count");
            emptyLabel.setPadding(new Insets(20));
            roomsListSection.getChildren().add(emptyLabel);
            return;
        }

        // For category grouping, use allocation-based grouping to handle split rooms
        if (groupBy.equals("category")) {
            refreshRoomsListByCategory(assignedRooms, search);
            return;
        }

        // Group rooms based on groupBy setting (type, site, name)
        Map<String, List<Resource>> groupedRooms = groupRooms(assignedRooms);

        for (Map.Entry<String, List<Resource>> entry : groupedRooms.entrySet()) {
            String groupName = entry.getKey();
            List<Resource> groupRooms = entry.getValue();
            int groupBeds = groupRooms.stream().mapToInt(this::getBedCount).sum();

            VBox groupCard = createGroupCard(groupName, groupRooms, groupBeds, null);
            roomsListSection.getChildren().add(groupCard);
        }
    }

    /**
     * Refreshes the rooms list grouped by category, properly handling split allocations.
     * A room with beds split between multiple pools will appear in each pool with its allocated bed count.
     */
    private void refreshRoomsListByCategory(List<Resource> assignedRooms, String search) {
        // Group allocations by pool (not rooms)
        Map<Pool, List<PoolAllocation>> allocationsByPool = new LinkedHashMap<>();

        for (Pool pool : getCategoryPools()) {
            List<PoolAllocation> poolAllocations = getPoolAllocations().stream()
                .filter(pa -> pa.getResourceId() != null && Entities.samePrimaryKey(pa.getPoolId(), pool))
                .filter(pa -> {
                    // Filter by search if needed
                    if (search.isEmpty()) return true;
                    Resource r = getResourceById(pa.getResourceId());
                    String name = r != null && r.getName() != null ? r.getName().toLowerCase() : "";
                    return name.contains(search);
                })
                .collect(Collectors.toList());

            if (!poolAllocations.isEmpty()) {
                allocationsByPool.put(pool, poolAllocations);
            }
        }

        for (Map.Entry<Pool, List<PoolAllocation>> entry : allocationsByPool.entrySet()) {
            Pool pool = entry.getKey();
            List<PoolAllocation> allocations = entry.getValue();

            // Calculate beds from allocations (respects split quantities)
            int groupBeds = allocations.stream()
                .mapToInt(pa -> pa.getQuantity() != null ? pa.getQuantity() : 0)
                .sum();

            VBox groupCard = createGroupCardForPool(pool, allocations, groupBeds);
            roomsListSection.getChildren().add(groupCard);
        }
    }

    /**
     * Gets a Resource by its ID.
     */
    private Resource getResourceById(Object resourceId) {
        if (resourceId == null) return null;
        return getResources().stream()
            .filter(r -> Entities.samePrimaryKey(r, resourceId))
            .findFirst()
            .orElse(null);
    }

    /**
     * Groups rooms based on the groupBy setting (for non-category groupings).
     */
    private Map<String, List<Resource>> groupRooms(List<Resource> rooms) {
        switch (groupBy) {
            case "type":
                return rooms.stream().collect(Collectors.groupingBy(r -> {
                    String typeName = getRoomTypeName(r);
                    return typeName != null ? typeName : "Other";
                }));
            case "site":
                return rooms.stream().collect(Collectors.groupingBy(r -> {
                    Building b = r.getBuilding();
                    return b != null && b.getName() != null ? b.getName() : "Main Site";
                }));
            case "name":
            default:
                // Sort by name, single group
                Map<String, List<Resource>> byName = new LinkedHashMap<>();
                byName.put(I18nControls.newLabel(EventRoomSetupI18nKeys.AllRoomsSortedByName).getText(),
                    rooms.stream()
                        .sorted(Comparator.comparing((Resource r) -> r.getName() != null ? r.getName() : ""))
                        .collect(Collectors.toList()));
                return byName;
        }
    }

    /**
     * Creates a group card for a pool with allocations (handles split allocations correctly).
     */
    private VBox createGroupCardForPool(Pool pool, List<PoolAllocation> allocations, int bedCount) {
        VBox card = new VBox();
        card.getStyleClass().add("roomsetup-pool-card");

        String groupName = pool.getName() != null ? pool.getName() : "Unknown";
        String color = pool.getWebColor() != null ? pool.getWebColor() : "#0096D6";

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 20, 14, 20));
        header.getStyleClass().add("roomsetup-pool-header");

        // Pool icon
        Node iconNode = createPoolIcon(pool.getGraphic(), color, 28);

        Label nameLabel = new Label(groupName);
        nameLabel.getStyleClass().add("roomsetup-pool-title");

        Label statsLabel = new Label(allocations.size() + " " +
            I18nControls.newLabel(EventRoomSetupI18nKeys.Rooms).getText() + " • " + bedCount + " " +
            I18nControls.newLabel(EventRoomSetupI18nKeys.Beds).getText());
        statsLabel.getStyleClass().add("roomsetup-pool-count");

        header.getChildren().addAll(iconNode, nameLabel, statsLabel);

        // Rooms flow
        FlowPane roomsFlow = new FlowPane(10, 10);
        roomsFlow.setPadding(new Insets(16, 20, 16, 20));

        for (PoolAllocation allocation : allocations) {
            Resource room = getResourceById(allocation.getResourceId());
            if (room != null) {
                int bedsInPool = allocation.getQuantity() != null ? allocation.getQuantity() : 0;
                Node roomChip = createSummaryRoomChipWithAllocation(room, pool, bedsInPool);
                roomsFlow.getChildren().add(roomChip);
            }
        }

        card.getChildren().addAll(header, roomsFlow);
        return card;
    }

    /**
     * Creates a group card for non-category groupings (type, site, name).
     * @param pool Optional pool for context (null for non-category groupings)
     */
    private VBox createGroupCard(String groupName, List<Resource> rooms, int bedCount, Pool pool) {
        VBox card = new VBox();
        card.getStyleClass().add("roomsetup-pool-card");

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 20, 14, 20));
        header.getStyleClass().add("roomsetup-pool-header");

        // Create icon based on groupBy mode
        Node iconNode;
        String icon = groupBy.equals("type") ? "🛏️" :
                      groupBy.equals("site") ? "🏛️" : "📋";
        Label iconLabel = new Label(icon);
        iconNode = iconLabel;

        Label nameLabel = new Label(groupName);
        nameLabel.getStyleClass().add("roomsetup-pool-title");

        Label statsLabel = new Label(rooms.size() + " " +
            I18nControls.newLabel(EventRoomSetupI18nKeys.Rooms).getText() + " • " + bedCount + " " +
            I18nControls.newLabel(EventRoomSetupI18nKeys.Beds).getText());
        statsLabel.getStyleClass().add("roomsetup-pool-count");

        header.getChildren().addAll(iconNode, nameLabel, statsLabel);

        // Rooms flow
        FlowPane roomsFlow = new FlowPane(10, 10);
        roomsFlow.setPadding(new Insets(16, 20, 16, 20));

        for (Resource room : rooms) {
            Node roomChip = createSummaryRoomChip(room);
            roomsFlow.getChildren().add(roomChip);
        }

        card.getChildren().addAll(header, roomsFlow);
        return card;
    }

    /**
     * Creates a summary room chip (used for non-category groupings).
     */
    private Node createSummaryRoomChip(Resource room) {
        boolean hasOverride = hasOverride(room);
        String comment = getRoomComment(room);
        boolean hasComment = comment != null && !comment.isEmpty();
        boolean isSplit = hasSplitAllocation(room);

        HBox chip = new HBox(6);
        chip.setPadding(new Insets(10, 14, 10, 14));
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.getStyleClass().add("roomsetup-room-chip");

        String splitColor = "#7c3aed"; // Purple for split rooms

        if (isSplit) {
            // Use cross-platform helper for dynamic styling
            applyDynamicBackgroundAndBorder(chip, "#faf5ff", splitColor, 2, new CornerRadii(8));
        } else if (hasOverride) {
            chip.getStyleClass().add("roomsetup-room-chip-override");
        }

        // Split indicator (lightning bolt emoji)
        if (isSplit) {
            Label splitIcon = new Label("⚡");
            splitIcon.getStyleClass().add("roomsetup-modified-indicator");
            chip.getChildren().add(splitIcon);
        }

        // Room ID
        Label idLabel = new Label(room.getName() != null ? room.getName() : "Room");
        idLabel.getStyleClass().add("roomsetup-room-id");

        // Bed count (total for non-category views)
        int beds = getBedCount(room);
        Label bedsLabel = new Label(String.valueOf(beds));
        bedsLabel.setPadding(new Insets(2, 6, 2, 6));
        bedsLabel.getStyleClass().add("roomsetup-beds-badge");
        if (isSplit) {
            // Use cross-platform helper for dynamic styling
            applyDynamicBackgroundAndTextFill(bedsLabel, splitColor, "#ffffff");
        } else if (hasOverride) {
            bedsLabel.getStyleClass().add("roomsetup-beds-badge-override");
        }

        chip.getChildren().addAll(idLabel, bedsLabel);

        // Override indicator (only if not split, to avoid clutter)
        if (hasOverride && !isSplit) {
            StackPane editIcon = createSvgIcon(EDIT_SVG_PATH, "#0096D6", 14, 0);
            chip.getChildren().add(editIcon);
        }

        // Comment indicator (comment SVG icon)
        if (hasComment) {
            StackPane commentIcon = createSvgIcon(COMMENT_SVG_PATH, "#f59e0b", 14, 0);
            chip.getChildren().add(commentIcon);
        }

        return chip;
    }

    /**
     * Creates a summary room chip with allocation context (for category grouping).
     * Shows beds allocated to THIS pool and split indicator if applicable.
     */
    private Node createSummaryRoomChipWithAllocation(Resource room, Pool pool, int bedsInPool) {
        boolean hasOverride = hasOverride(room);
        String comment = getRoomComment(room);
        boolean hasComment = comment != null && !comment.isEmpty();
        boolean isSplit = hasSplitAllocation(room);
        int totalBeds = getBedCount(room);

        HBox chip = new HBox(6);
        chip.setPadding(new Insets(10, 14, 10, 14));
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.getStyleClass().add("roomsetup-room-chip");

        String splitColor = "#7c3aed"; // Purple for split rooms

        if (isSplit) {
            // Use cross-platform helper for dynamic styling
            applyDynamicBackgroundAndBorder(chip, "#faf5ff", splitColor, 2, new CornerRadii(8));
        } else if (hasOverride) {
            chip.getStyleClass().add("roomsetup-room-chip-override");
        }

        // Split indicator (lightning bolt emoji)
        if (isSplit) {
            Label splitIcon = new Label("⚡");
            splitIcon.getStyleClass().add("roomsetup-modified-indicator");
            chip.getChildren().add(splitIcon);
        }

        // Room ID
        Label idLabel = new Label(room.getName() != null ? room.getName() : "Room");
        idLabel.getStyleClass().add("roomsetup-room-id");

        // For split rooms, show beds allocated to THIS pool; otherwise show total
        String bedText = isSplit ? String.valueOf(bedsInPool) : String.valueOf(totalBeds);
        Label bedsLabel = new Label(bedText);
        bedsLabel.setPadding(new Insets(2, 6, 2, 6));
        bedsLabel.getStyleClass().add("roomsetup-beds-badge");
        if (isSplit) {
            // Use cross-platform helper for dynamic styling
            applyDynamicBackgroundAndTextFill(bedsLabel, splitColor, "#ffffff");
        } else if (hasOverride) {
            bedsLabel.getStyleClass().add("roomsetup-beds-badge-override");
        }

        chip.getChildren().addAll(idLabel, bedsLabel);

        // For split rooms, show fraction indicator (e.g., "/10")
        if (isSplit) {
            Label fractionLabel = new Label("/" + totalBeds);
            fractionLabel.getStyleClass().add("roomsetup-modified-indicator");
            // Use cross-platform helper for dynamic styling
            applyDynamicTextFill(fractionLabel, splitColor);
            chip.getChildren().add(fractionLabel);
        }

        // Override indicator (only if not split, to avoid clutter)
        if (hasOverride && !isSplit) {
            StackPane editIcon = createSvgIcon(EDIT_SVG_PATH, "#0096D6", 14, 0);
            chip.getChildren().add(editIcon);
        }

        // Comment indicator (comment SVG icon)
        if (hasComment) {
            StackPane commentIcon = createSvgIcon(COMMENT_SVG_PATH, "#f59e0b", 14, 0);
            chip.getChildren().add(commentIcon);
        }

        return chip;
    }

    /**
     * Gets all event allocations for a specific room.
     * A room may have multiple allocations if beds are split across pools.
     */
    private List<PoolAllocation> getAllocationsForRoom(Resource room) {
        if (room == null) return Collections.emptyList();
        Object resourceId = Entities.getPrimaryKey(room);
        return getPoolAllocations().stream()
            .filter(pa -> pa.getResourceId() != null &&
                Objects.equals(Entities.getPrimaryKey(pa.getResourceId()), resourceId))
            .collect(Collectors.toList());
    }

    /**
     * Checks if a room has split allocation (allocated to multiple pools).
     */
    private boolean hasSplitAllocation(Resource room) {
        return getAllocationsForRoom(room).size() > 1;
    }

    // SVG path for edit/pen icon (Feather-style pencil, naturally diagonal)
    private static final String EDIT_SVG_PATH = "M17 3a2.83 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5L17 3z";

    // SVG path for comment/chat bubble icon (simple bubble)
    private static final String COMMENT_SVG_PATH = "M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z";

    /**
     * Creates a small SVG icon with optional rotation.
     */
    private StackPane createSvgIcon(String svgPath, String color, double size, double rotation) {
        StackPane iconPane = new StackPane();
        iconPane.setMinSize(size, size);
        iconPane.setMaxSize(size, size);
        iconPane.setAlignment(Pos.CENTER);

        SVGPath svg = new SVGPath();
        svg.setContent(svgPath);
        svg.setFill(Color.TRANSPARENT);
        svg.setStroke(Color.web(color));
        svg.setStrokeWidth(2.5);
        // Scale the icon to fit the desired size (SVG viewbox is 24x24)
        double scale = size / 24.0;
        svg.setScaleX(scale);
        svg.setScaleY(scale);
        if (rotation != 0) {
            svg.setRotate(rotation);
        }
        iconPane.getChildren().add(svg);

        return iconPane;
    }

    /**
     * Gets the comment for a room from event config or base config.
     */
    private String getRoomComment(Resource room) {
        Object roomId = Entities.getPrimaryKey(room);

        // First check event-specific override
        ResourceConfiguration eventConfig = getEventResourceConfigurations().stream()
            .filter(rc -> rc.getResourceId() != null && Objects.equals(Entities.getPrimaryKey(rc.getResourceId()), roomId))
            .findFirst()
            .orElse(null);

        if (eventConfig != null && eventConfig.getComment() != null && !eventConfig.getComment().isEmpty()) {
            return eventConfig.getComment();
        }

        // Fall back to base config
        ResourceConfiguration baseConfig = getBaseResourceConfigurations().stream()
            .filter(rc -> rc.getResourceId() != null && Objects.equals(Entities.getPrimaryKey(rc.getResourceId()), roomId))
            .findFirst()
            .orElse(null);

        if (baseConfig != null && baseConfig.getComment() != null) {
            return baseConfig.getComment();
        }

        return null;
    }

    /**
     * Gets bed count for a pool.
     */
    private int getBedCountForPool(Pool pool) {
        Object poolId = Entities.getPrimaryKey(pool);
        Set<Object> resourceIds = getPoolAllocations().stream()
            .filter(pa -> pa.getPoolId() != null && Objects.equals(Entities.getPrimaryKey(pa.getPoolId()), poolId))
            .filter(pa -> pa.getResourceId() != null)
            .map(pa -> Entities.getPrimaryKey(pa.getResourceId()))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        return getResources().stream()
            .filter(r -> resourceIds.contains(Entities.getPrimaryKey(r)))
            .mapToInt(this::getBedCount)
            .sum();
    }

    /**
     * Gets the bed count for a room, checking event override first, then base config.
     */
    private int getBedCount(Resource room) {
        Object roomId = Entities.getPrimaryKey(room);

        // First check for event-specific override
        ResourceConfiguration eventConfig = getEventResourceConfigurations().stream()
            .filter(rc -> rc.getResourceId() != null && Objects.equals(Entities.getPrimaryKey(rc.getResourceId()), roomId))
            .findFirst()
            .orElse(null);

        if (eventConfig != null && eventConfig.getMax() != null) {
            return eventConfig.getMax();
        }

        // Fall back to base configuration
        ResourceConfiguration baseConfig = getBaseResourceConfigurations().stream()
            .filter(rc -> rc.getResourceId() != null && Objects.equals(Entities.getPrimaryKey(rc.getResourceId()), roomId))
            .findFirst()
            .orElse(null);

        if (baseConfig != null && baseConfig.getMax() != null) {
            return baseConfig.getMax();
        }

        return 2; // Default fallback
    }

    /**
     * Checks if a room has an event-specific override.
     */
    private boolean hasOverride(Resource room) {
        Object roomId = Entities.getPrimaryKey(room);
        return getEventResourceConfigurations().stream()
            .anyMatch(rc -> rc.getResourceId() != null && Objects.equals(Entities.getPrimaryKey(rc.getResourceId()), roomId));
    }

    /**
     * Gets the room type name (Item name) for a resource.
     * First checks event-specific override, then falls back to permanent config.
     */
    private String getRoomTypeName(Resource room) {
        Object roomId = Entities.getPrimaryKey(room);

        // First check for event-specific override
        ResourceConfiguration eventConfig = getEventResourceConfigurations().stream()
            .filter(rc -> rc.getResourceId() != null && Objects.equals(Entities.getPrimaryKey(rc.getResourceId()), roomId))
            .findFirst()
            .orElse(null);

        if (eventConfig != null && eventConfig.getItem() != null && eventConfig.getItem().getName() != null) {
            return eventConfig.getItem().getName();
        }

        // Fall back to base configuration
        ResourceConfiguration baseConfig = getBaseResourceConfigurations().stream()
            .filter(rc -> rc.getResourceId() != null && Objects.equals(Entities.getPrimaryKey(rc.getResourceId()), roomId))
            .findFirst()
            .orElse(null);

        if (baseConfig != null && baseConfig.getItem() != null && baseConfig.getItem().getName() != null) {
            return baseConfig.getItem().getName();
        }

        return null;
    }

    /**
     * Checks if a pool is a category pool.
     */
    private boolean isCategoryPool(Pool pool) {
        return pool != null && Boolean.TRUE.equals(pool.isEventPool());
    }

    /**
     * Creates a pool icon from SVG path or fallback circle.
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

    // ===================================================================================
    // Cross-platform dynamic styling helpers
    // WebFX only translates -fx-* properties in CSS files, NOT in Java setStyle() calls.
    // For dynamic colors (from database), we must use BOTH setStyle() for JavaFX AND
    // programmatic styling (setBackground, setTextFill, etc.) for WebFX.
    // ===================================================================================

    /**
     * Applies a dynamic background color to a Region for both JavaFX (desktop) and WebFX (browser).
     */
    private static void applyDynamicBackground(Region region, String colorHex, CornerRadii radii) {
        // For JavaFX (desktop)
        region.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: " + radiiToCss(radii) + ";");
        // For WebFX (browser) - programmatic styling
        region.setBackground(new Background(new BackgroundFill(Color.web(colorHex), radii, null)));
    }

    /**
     * Applies a dynamic border color to a Region for both JavaFX and WebFX.
     */
    private static void applyDynamicBorder(Region region, String colorHex, double width, CornerRadii radii) {
        // For JavaFX (desktop)
        region.setStyle("-fx-border-color: " + colorHex + "; -fx-border-width: " + width + "; -fx-border-radius: " + radiiToCss(radii) + ";");
        // For WebFX (browser) - programmatic styling
        region.setBorder(new Border(new BorderStroke(Color.web(colorHex), BorderStrokeStyle.SOLID, radii, new BorderWidths(width))));
    }

    /**
     * Applies both dynamic background and border colors to a Region for both JavaFX and WebFX.
     */
    private static void applyDynamicBackgroundAndBorder(Region region, String bgColorHex, String borderColorHex, double borderWidth, CornerRadii radii) {
        // For JavaFX (desktop)
        region.setStyle("-fx-background-color: " + bgColorHex + "; -fx-border-color: " + borderColorHex +
            "; -fx-border-width: " + borderWidth + "; -fx-background-radius: " + radiiToCss(radii) +
            "; -fx-border-radius: " + radiiToCss(radii) + ";");
        // For WebFX (browser) - programmatic styling
        region.setBackground(new Background(new BackgroundFill(Color.web(bgColorHex), radii, null)));
        region.setBorder(new Border(new BorderStroke(Color.web(borderColorHex), BorderStrokeStyle.SOLID, radii, new BorderWidths(borderWidth))));
    }

    /**
     * Applies a dynamic text fill color to a Label for both JavaFX and WebFX.
     */
    private static void applyDynamicTextFill(Label label, String colorHex) {
        // For JavaFX (desktop)
        label.setStyle("-fx-text-fill: " + colorHex + ";");
        // For WebFX (browser) - programmatic styling
        label.setTextFill(Color.web(colorHex));
    }

    /**
     * Applies both dynamic background and text fill colors to a Label for both JavaFX and WebFX.
     */
    private static void applyDynamicBackgroundAndTextFill(Label label, String bgColorHex, String textColorHex) {
        // For JavaFX (desktop)
        label.setStyle("-fx-background-color: " + bgColorHex + "; -fx-text-fill: " + textColorHex + ";");
        // For WebFX (browser) - programmatic styling
        label.setBackground(new Background(new BackgroundFill(Color.web(bgColorHex), new CornerRadii(4), null)));
        label.setTextFill(Color.web(textColorHex));
    }

    /**
     * Converts CornerRadii to CSS string format.
     */
    private static String radiiToCss(CornerRadii radii) {
        if (radii == null || radii == CornerRadii.EMPTY) {
            return "0";
        }
        // Use top-left radius as the value (assuming uniform radii)
        return String.valueOf((int) radii.getTopLeftHorizontalRadius());
    }
}
