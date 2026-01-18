package one.modality.event.backoffice.activities.roomsetup;

import dev.webfx.extras.controlfactory.button.ButtonFactoryMixin;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelector;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.routing.activity.impl.elementals.activeproperty.HasActiveProperty;
import one.modality.base.client.bootstrap.ModalityStyle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.DialogUtil;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.application.Platform;
import dev.webfx.stack.orm.entity.Entities;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Pool;
import one.modality.base.shared.entities.PoolAllocation;
import one.modality.base.shared.entities.Resource;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.crm.backoffice.organization.fx.FXOrganization;
import one.modality.event.client.event.fx.FXEvent;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Tab 2: Customize - Event-specific room overrides.
 *
 * This view allows editing:
 * - Bed count overrides
 * - Gender restrictions (allowsMale, allowsFemale)
 * - Comments/notes
 *
 * Only shows rooms that have been assigned to category pools.
 *
 * @author Bruno Salmon
 */
final class CustomizeTabView {

    // Shared data model (provided by activity)
    private EventRoomSetupDataModel dataModel;
    private final BooleanProperty activeProperty = new SimpleBooleanProperty();
    private DataSourceModel dataSourceModel;

    // Per-tab loading state
    private final BooleanProperty loadingProperty = new SimpleBooleanProperty(true);

    // UI components
    private final VBox mainContent = new VBox(20);
    private final ScrollPane mainContainer;
    private final StackPane containerWithLoading;
    private final StackPane loadingOverlay;
    private final StackPane noEventSelectedOverlay;
    private final HBox statsRow = new HBox(12);
    private final VBox roomsSection = new VBox(16);

    // State
    private Event currentEvent;
    private Resource selectedRoom; // Currently selected room for editing

    public CustomizeTabView() {
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

        // Create "no event selected" overlay with styled message
        noEventSelectedOverlay = createNoEventSelectedOverlay();
        noEventSelectedOverlay.setVisible(false);
        noEventSelectedOverlay.setManaged(false);

        // Container with loading and no-event overlays
        containerWithLoading = new StackPane(mainContainer, loadingOverlay, noEventSelectedOverlay);
    }

    /**
     * Creates a styled overlay for when no event is selected.
     * Uses Bootstrap alert-warning style with an icon and description.
     */
    private StackPane createNoEventSelectedOverlay() {
        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("roomsetup-modal-overlay");

        // Create message container
        VBox messageBox = new VBox(16);
        messageBox.setAlignment(Pos.CENTER);
        messageBox.setMaxWidth(400);
        messageBox.setPadding(new Insets(32));

        // Apply warning alert style from Bootstrap
        Bootstrap.alertWarning(messageBox);

        // Warning icon (using Unicode warning triangle)
        Label iconLabel = new Label("\u26A0"); // ⚠ symbol
        iconLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: #f59e0b;");

        // Title
        Label titleLabel = I18nControls.newLabel(EventRoomSetupI18nKeys.SelectEventRequired);
        TextTheme.createPrimaryTextFacet(titleLabel).style();
        Bootstrap.h4(titleLabel);

        // Description
        Label descLabel = I18nControls.newLabel(EventRoomSetupI18nKeys.SelectEventRequiredDescription);
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-text-fill: #78716c; -fx-text-alignment: center;");

        messageBox.getChildren().addAll(iconLabel, titleLabel, descLabel);
        overlay.getChildren().add(messageBox);

        return overlay;
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
        this.dataSourceModel = dataModel.getDataSourceModel();

        Console.log("CustomizeTabView: Starting logic with shared data model");

        // Bind loading/overlay state to data model's loading properties and event selection
        FXProperties.runNowAndOnPropertiesChange(() -> {
            Platform.runLater(this::updateOverlayState);
        }, dataModel.sourcePoolsLoadedProperty(),
           dataModel.categoryPoolsLoadedProperty(),
           dataModel.resourcesLoadedProperty(),
           dataModel.roomTypesLoadedProperty(),
           dataModel.permanentConfigsLoadedProperty(),
           dataModel.defaultAllocationsLoadedProperty(),
           dataModel.eventConfigsLoadedProperty(),
           dataModel.eventAllocationsLoadedProperty(),
           FXEvent.eventProperty());

        // Listen to shared list changes (when rooms are assigned/unassigned in other tabs)
        dataModel.getEventAllocations().addListener((ListChangeListener<PoolAllocation>) c -> {
            Console.log("CustomizeTabView: Event allocations changed, refreshing UI");
            Platform.runLater(this::refreshUI);
        });
        dataModel.getEventRoomConfigs().addListener((ListChangeListener<ResourceConfiguration>) c -> {
            Console.log("CustomizeTabView: Event configs changed, refreshing UI");
            Platform.runLater(this::refreshUI);
        });

        // Listen for bed configuration changes (ensures refresh happens after data arrives)
        FXProperties.runOnPropertyChange(() -> {
            Console.log("CustomizeTabView: Bed configuration changed, refreshing UI");
            Platform.runLater(this::refreshUI);
        }, dataModel.bedConfigurationVersionProperty());

        // Listen for event changes
        FXProperties.runNowAndOnPropertiesChange(() -> {
            currentEvent = FXEvent.getEvent();
            if (currentEvent != null && activeProperty.get()) {
                Console.log("CustomizeTabView: Event changed to: " + currentEvent.getName());
            }
            Platform.runLater(this::updateOverlayState);
        }, FXEvent.eventProperty());
    }

    /**
     * Updates the overlay state based on event selection and data loading status.
     * Shows:
     * - "No event selected" overlay if no event is selected
     * - Loading overlay if event is selected but data is still loading
     * - Main content if event is selected and data is loaded
     */
    private void updateOverlayState() {
        Event event = FXEvent.getEvent();
        boolean hasEvent = event != null;
        boolean allLoaded = dataModel != null && dataModel.isAllDataLoaded();

        Console.log("CustomizeTabView: updateOverlayState - hasEvent=" + hasEvent + ", allLoaded=" + allLoaded);

        if (!hasEvent) {
            // No event selected - show "select event" message
            noEventSelectedOverlay.setVisible(true);
            noEventSelectedOverlay.setManaged(true);
            loadingOverlay.setVisible(false);
            loadingOverlay.setManaged(false);
        } else if (!allLoaded) {
            // Event selected but still loading - show spinner
            noEventSelectedOverlay.setVisible(false);
            noEventSelectedOverlay.setManaged(false);
            loadingOverlay.setVisible(true);
            loadingOverlay.setManaged(true);
        } else {
            // Event selected and data loaded - show content
            noEventSelectedOverlay.setVisible(false);
            noEventSelectedOverlay.setManaged(false);
            loadingOverlay.setVisible(false);
            loadingOverlay.setManaged(false);
            refreshUI();
        }
    }

    void setActive(boolean active) {
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
    private ObservableList<ResourceConfiguration> getResourceConfigurations() { return dataModel.getEventRoomConfigs(); }
    private ObservableList<ResourceConfiguration> getPermanentConfigs() { return dataModel.getPermanentRoomConfigs(); }
    private ObservableList<Item> getRoomTypes() { return dataModel.getRoomTypes(); }
    private UpdateStore getUpdateStore() { return dataModel.getUpdateStore(); }

    /**
     * Builds the initial UI structure.
     */
    private void buildUI() {
        mainContent.getChildren().clear();

        // Title
        Label title = I18nControls.newLabel(EventRoomSetupI18nKeys.CustomizeTabTitle);
        TextTheme.createPrimaryTextFacet(title).style();
        title.getStyleClass().add(Bootstrap.H3);
        title.setPadding(new Insets(0, 0, 10, 0));

        // Help tip
        HBox helpTip = createHelpTip();

        // Stats row
        statsRow.setAlignment(Pos.CENTER_LEFT);

        mainContent.getChildren().addAll(title, helpTip, statsRow, roomsSection);
    }

    /**
     * Creates the help tip section.
     */
    private HBox createHelpTip() {
        HBox helpTip = new HBox(12);
        helpTip.getStyleClass().add("roomsetup-help-tip-info");
        helpTip.setPadding(new Insets(14, 18, 14, 18));
        helpTip.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("\uD83D\uDCA1"); // 💡

        VBox textBox = new VBox(2);
        Label adjustmentLabel = I18nControls.newLabel(EventRoomSetupI18nKeys.AdjustmentsEventSpecific);
        adjustmentLabel.getStyleClass().add("roomsetup-pool-title");
        adjustmentLabel.setStyle("-fx-text-fill: #0096D6;"); // Dynamic color override

        Label description = I18nControls.newLabel(EventRoomSetupI18nKeys.ChangesOnlyAffectThisEvent);
        description.setWrapText(true);
        description.getStyleClass().add("roomsetup-help-tip-text");

        textBox.getChildren().addAll(adjustmentLabel, description);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        helpTip.getChildren().addAll(icon, textBox);
        return helpTip;
    }

    /**
     * Refreshes the UI with current data.
     */
    private void refreshUI() {
        refreshStatsRow();
        refreshRoomsSection();
    }

    /**
     * Gets the list of assigned rooms (rooms with pool allocations for this event).
     * Uses permanent configs as the source of room data since they contain complete room information.
     */
    private List<Resource> getAssignedRooms() {
        // Get resource IDs that have event allocations - use Entities.getPrimaryKey() for proper EntityId comparison
        Set<Object> assignedResourceIds = getPoolAllocations().stream()
            .filter(pa -> pa.getResourceId() != null)
            .map(pa -> Entities.getPrimaryKey(pa.getResourceId()))
            .collect(Collectors.toSet());

        // Get rooms from permanent configs that have event allocations
        // Using permanent configs because they have the full resource data loaded
        return getPermanentConfigs().stream()
            .filter(rc -> rc.getResourceId() != null)
            .filter(rc -> assignedResourceIds.contains(Entities.getPrimaryKey(rc.getResourceId())))
            .map(ResourceConfiguration::getResource)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
    }

    /**
     * Refreshes the stats row.
     */
    private void refreshStatsRow() {
        statsRow.getChildren().clear();

        List<Resource> assignedRooms = getAssignedRooms();
        int totalBeds = assignedRooms.stream().mapToInt(this::getBedCount).sum();
        long modifiedCount = getResourceConfigurations().stream()
            .filter(rc -> assignedRooms.stream().anyMatch(r -> Objects.equals(r.getPrimaryKey(), rc.getResourceId())))
            .count();

        // Rooms assigned stat card
        VBox roomsCard = createStatCard(String.valueOf(assignedRooms.size()),
            EventRoomSetupI18nKeys.RoomsAssigned, "#0096D6", currentEvent != null ? "🏨" : "📦");

        // Total beds stat card
        VBox bedsCard = createStatCard(String.valueOf(totalBeds),
            EventRoomSetupI18nKeys.TotalBeds, "#10b981", "🛏️");

        // Modified count stat card
        VBox modifiedCard = createStatCard(String.valueOf(modifiedCount),
            EventRoomSetupI18nKeys.Modified, modifiedCount > 0 ? "#f59e0b" : "#78716c", "✏️");

        statsRow.getChildren().addAll(roomsCard, bedsCard, modifiedCard);
    }

    /**
     * Creates a stat card.
     */
    private VBox createStatCard(String value, Object labelKey, String color, String icon) {
        VBox card = new VBox(4);
        card.getStyleClass().add("roomsetup-stat-card");
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(card, Priority.ALWAYS);

        HBox content = new HBox(14);
        content.setAlignment(Pos.CENTER_LEFT);

        // Icon container
        StackPane iconContainer = new StackPane();
        iconContainer.setMinSize(44, 44);
        iconContainer.setMaxSize(44, 44);
        iconContainer.getStyleClass().add("roomsetup-icon-container");
        // Dynamic color from parameter - use cross-platform helper for alpha background
        applyDynamicBackground(iconContainer, color + "15", new CornerRadii(8));
        Label iconLabel = new Label(icon);
        iconContainer.getChildren().add(iconLabel);

        // Text container
        VBox textContainer = new VBox(2);
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("roomsetup-stat-value-medium");
        // Dynamic color from parameter - use cross-platform helper
        applyDynamicTextFill(valueLabel, color);

        Label descLabel = I18nControls.newLabel(labelKey);
        descLabel.getStyleClass().add("roomsetup-stat-label");

        textContainer.getChildren().addAll(valueLabel, descLabel);

        content.getChildren().addAll(iconContainer, textContainer);
        card.getChildren().add(content);

        return card;
    }

    /**
     * Refreshes the rooms section.
     */
    private void refreshRoomsSection() {
        roomsSection.getChildren().clear();

        List<Resource> assignedRooms = getAssignedRooms();

        if (assignedRooms.isEmpty()) {
            // Empty state
            VBox emptyState = new VBox(16);
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setPadding(new Insets(48));
            emptyState.getStyleClass().add("roomsetup-empty-state");

            Label emptyIcon = new Label("📦");
            emptyIcon.getStyleClass().add("roomsetup-empty-icon");

            Label emptyTitle = I18nControls.newLabel(EventRoomSetupI18nKeys.NoRoomsAssignedYet);
            emptyTitle.getStyleClass().add("roomsetup-empty-title");

            Label emptyDesc = I18nControls.newLabel(EventRoomSetupI18nKeys.AssignRoomsFirst);
            emptyDesc.getStyleClass().add("roomsetup-pool-count");
            emptyDesc.setWrapText(true);

            emptyState.getChildren().addAll(emptyIcon, emptyTitle, emptyDesc);
            roomsSection.getChildren().add(emptyState);
            return;
        }

        // Group rooms by Item type (room type like "Single Ensuite", "Twin Shared", etc.)
        Map<String, List<Resource>> roomsByType = assignedRooms.stream()
            .collect(Collectors.groupingBy(this::getRoomType));

        for (Map.Entry<String, List<Resource>> entry : roomsByType.entrySet()) {
            String typeName = entry.getKey();
            List<Resource> typeRooms = entry.getValue();
            int typeBeds = typeRooms.stream().mapToInt(this::getBedCount).sum();
            long typeModified = typeRooms.stream()
                .filter(this::hasOverride)
                .count();

            VBox typeCard = createRoomTypeCard(typeName, typeRooms, typeBeds, typeModified);
            roomsSection.getChildren().add(typeCard);
        }
    }

    /**
     * Creates a card for a room type group.
     */
    private VBox createRoomTypeCard(String typeName, List<Resource> rooms, int bedCount, long modifiedCount) {
        VBox card = new VBox();
        card.getStyleClass().add("roomsetup-pool-card");

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 20, 14, 20));
        header.getStyleClass().add("roomsetup-pool-header");

        Label typeIcon = new Label("🛏️");

        VBox typeInfo = new VBox(2);
        Label typeLabel = new Label(typeName);
        typeLabel.getStyleClass().add("roomsetup-pool-title");

        Label typeStats = new Label(rooms.size() + " " +
            I18nControls.newLabel(EventRoomSetupI18nKeys.Rooms).getText() + " • " + bedCount + " " +
            I18nControls.newLabel(EventRoomSetupI18nKeys.Beds).getText());
        typeStats.getStyleClass().add("roomsetup-pool-count");

        typeInfo.getChildren().addAll(typeLabel, typeStats);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(typeIcon, typeInfo, spacer);

        if (modifiedCount > 0) {
            Label modifiedBadge = new Label(modifiedCount + " " +
                I18nControls.newLabel(EventRoomSetupI18nKeys.Modified).getText().toLowerCase());
            modifiedBadge.setPadding(new Insets(4, 10, 4, 10));
            modifiedBadge.getStyleClass().add("roomsetup-badge-info");
            header.getChildren().add(modifiedBadge);
        }

        // Rooms flow
        FlowPane roomsFlow = new FlowPane(10, 10);
        roomsFlow.setPadding(new Insets(16, 20, 16, 20));

        for (Resource room : rooms) {
            Node roomChip = createEditableRoomChip(room);
            roomsFlow.getChildren().add(roomChip);
        }

        card.getChildren().addAll(header, roomsFlow);
        return card;
    }

    /**
     * Creates an editable room chip with styling based on room state.
     * External rooms: purple styling, non-clickable
     * Modified rooms: primary color styling
     * Normal rooms: gray styling
     */
    private Node createEditableRoomChip(Resource room) {
        boolean isExternal = isExternalRoom(room);
        boolean hasOverride = hasOverride(room);
        boolean roomHasNotes = hasNotes(room);
        ResourceConfiguration config = getConfiguration(room);

        HBox chip = new HBox(10);
        chip.setPadding(new Insets(12, 16, 12, 16));
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.getStyleClass().add("roomsetup-room-chip");

        // Apply state-specific styling via CSS classes
        if (isExternal) {
            chip.getStyleClass().add("roomsetup-room-chip-external");
        } else if (hasOverride) {
            chip.getStyleClass().add("roomsetup-room-chip-override");
        }

        // External room indicator
        if (isExternal) {
            Label externalIcon = new Label("🏨");
            chip.getChildren().add(externalIcon);
        }

        // Room ID
        Label idLabel = new Label(room.getName() != null ? room.getName() : "Room");
        idLabel.getStyleClass().add("roomsetup-room-id");
        if (isExternal) {
            idLabel.getStyleClass().add("roomsetup-room-id-external");
        }
        chip.getChildren().add(idLabel);

        // Gender indicator (from event override only)
        Boolean allowsMale = config != null ? config.allowsMale() : null;
        Boolean allowsFemale = config != null ? config.allowsFemale() : null;
        String genderText = "";
        if (Boolean.FALSE.equals(allowsMale) && Boolean.TRUE.equals(allowsFemale)) {
            genderText = "♀";
        } else if (Boolean.TRUE.equals(allowsMale) && Boolean.FALSE.equals(allowsFemale)) {
            genderText = "♂";
        }
        if (!genderText.isEmpty()) {
            Label genderLabel = new Label(genderText);
            chip.getChildren().add(genderLabel);
        }

        // Bed count badge
        int beds = getBedCount(room);
        Label bedsLabel = new Label(String.valueOf(beds));
        bedsLabel.setPadding(new Insets(4, 10, 4, 10));
        bedsLabel.getStyleClass().add("roomsetup-beds-badge");
        if (isExternal) {
            bedsLabel.getStyleClass().add("roomsetup-beds-badge-external");
        } else if (hasOverride) {
            bedsLabel.getStyleClass().add("roomsetup-beds-badge-override");
        }
        chip.getChildren().add(bedsLabel);

        // Override indicator (only for non-external rooms with overrides) - SVG pen icon
        if (hasOverride && !isExternal) {
            StackPane editIcon = createSvgIcon(EDIT_SVG_PATH, "#0096D6", 14);
            chip.getChildren().add(editIcon);
        }

        // Notes indicator - SVG comment icon
        if (roomHasNotes) {
            StackPane notesIcon = createSvgIcon(COMMENT_SVG_PATH, "#f59e0b", 14);
            chip.getChildren().add(notesIcon);
        }

        // Build tooltip
        StringBuilder tooltipText = new StringBuilder();
        if (isExternal) {
            tooltipText.append("🏨 ").append(I18nControls.newLabel(EventRoomSetupI18nKeys.External).getText()).append(" — ");
        }
        tooltipText.append(room.getName() != null ? room.getName() : "Room");
        if (roomHasNotes) {
            tooltipText.append(" — 📝 ").append(getNotes(room));
        }
        Tooltip tooltip = new Tooltip(tooltipText.toString());
        idLabel.setTooltip(tooltip);

        // Click to edit (only for non-external rooms)
        if (!isExternal) {
            chip.setOnMouseClicked(e -> showEditDialog(room));
        }

        return chip;
    }

    /**
     * Shows the edit dialog for a room following the JSX mockup design.
     * Uses GWT-compatible controls and DialogUtil.showModalNodeInGoldLayout() for GWT compatibility.
     */
    private void showEditDialog(Resource room) {
        // Get configurations
        ResourceConfiguration eventConfig = getConfiguration(room);
        ResourceConfiguration permConfig = getPermanentConfig(room);

        // Get base values (from permanent config)
        int baseBeds = permConfig != null && permConfig.getMax() != null ? permConfig.getMax() : 2;
        Item baseItem = permConfig != null ? permConfig.getItem() : null;

        // Get current values (from event override or base)
        final int[] currentBeds = {getBedCount(room)};
        final Item[] currentItem = {eventConfig != null && eventConfig.getItem() != null ? eventConfig.getItem() : baseItem};
        Boolean currentAllowsMale = eventConfig != null ? eventConfig.allowsMale() : true;
        Boolean currentAllowsFemale = eventConfig != null ? eventConfig.allowsFemale() : true;
        String currentComment = eventConfig != null ? eventConfig.getComment() : "";

        // Gender state: null = mixed, "male" = male only, "female" = female only
        final String[] genderState = {null};
        if (Boolean.FALSE.equals(currentAllowsMale)) {
            genderState[0] = "female";
        } else if (Boolean.FALSE.equals(currentAllowsFemale)) {
            genderState[0] = "male";
        }

        // Main container with StackPane wrapper for dropdown positioning
        StackPane dialogArea = new StackPane();
        dialogArea.setMinWidth(450);
        dialogArea.setMinHeight(500);
        VBox content = new VBox(16);
        content.setPadding(new Insets(20));
        content.setMinWidth(450);
        dialogArea.getChildren().add(content);
        StackPane.setAlignment(content, Pos.TOP_LEFT);

        // Header with room name and type
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        Label headerIcon = new Label("✏️");
        headerIcon.setStyle("-fx-font-size: 24px;");
        VBox headerText = new VBox(4);
        // Show room number + room type name (e.g., "206 - Dormitory")
        String roomTypeName = baseItem != null && baseItem.getName() != null ? baseItem.getName() : "";
        String roomDisplayName = (room.getName() != null ? room.getName() : "Room") +
            (roomTypeName.isEmpty() ? "" : " - " + roomTypeName);
        Label roomName = new Label(roomDisplayName);
        roomName.setStyle("-fx-font-size: 17px; -fx-font-weight: 600;");
        String eventName = currentEvent != null ? currentEvent.getName() :
            I18nControls.newLabel(EventRoomSetupI18nKeys.ThisEvent).getText();
        Label eventLabel = new Label(I18nControls.newLabel(EventRoomSetupI18nKeys.OverrideSettingsFor).getText() + " " + eventName);
        eventLabel.getStyleClass().add("roomsetup-stat-label");
        headerText.getChildren().addAll(roomName, eventLabel);
        header.getChildren().addAll(headerIcon, headerText);

        // Info banner
        HBox infoBanner = new HBox(10);
        infoBanner.setPadding(new Insets(12, 16, 12, 16));
        infoBanner.setAlignment(Pos.CENTER_LEFT);
        infoBanner.getStyleClass().add("roomsetup-help-tip-info");
        Label infoIcon = new Label("💡");
        Label infoText = I18nControls.newLabel(EventRoomSetupI18nKeys.ChangesOnlyApplyToThisEvent);
        infoText.getStyleClass().add("roomsetup-help-tip-text");
        infoText.setStyle("-fx-text-fill: #0096D6;"); // Override text color
        infoText.setWrapText(true);
        infoBanner.getChildren().addAll(infoIcon, infoText);

        // Capacity section with +/- buttons (GWT-compatible, no Spinner)
        VBox capacitySection = new VBox(8);
        Label bedsModified = new Label(currentBeds[0] != baseBeds ?
            I18nControls.newLabel(EventRoomSetupI18nKeys.Modified).getText() : "");
        bedsModified.setStyle("-fx-text-fill: #0096D6; -fx-font-size: 11px;");
        HBox capacityHeader = new HBox(8);
        capacityHeader.setAlignment(Pos.CENTER_LEFT);
        Label capacityLabel = I18nControls.newLabel(EventRoomSetupI18nKeys.CapacityForThisEvent);
        capacityLabel.getStyleClass().add("roomsetup-section-label");
        capacityHeader.getChildren().addAll(capacityLabel, bedsModified);

        HBox capacityControls = new HBox(12);
        capacityControls.setAlignment(Pos.CENTER);

        Button minusBtn = new Button("−");
        minusBtn.setMinSize(44, 44);
        minusBtn.setMaxSize(44, 44);
        minusBtn.getStyleClass().add("roomsetup-stepper-button");

        // Larger value display matching JSX mockup (2rem = ~32px)
        Label bedsValue = new Label(String.valueOf(currentBeds[0]));
        bedsValue.setStyle("-fx-font-size: 32px; -fx-font-weight: 700;" +
            (currentBeds[0] != baseBeds ? " -fx-text-fill: #0096D6;" : ""));
        Label bedsUnit = I18nControls.newLabel(EventRoomSetupI18nKeys.Beds);
        bedsUnit.getStyleClass().add("roomsetup-stat-label");
        VBox bedsDisplay = new VBox(2);
        bedsDisplay.setAlignment(Pos.CENTER);
        HBox bedsValueRow = new HBox(6, bedsValue, bedsUnit);
        bedsValueRow.setAlignment(Pos.BASELINE_CENTER);
        bedsDisplay.getChildren().add(bedsValueRow);
        HBox.setHgrow(bedsDisplay, Priority.ALWAYS);

        Button plusBtn = new Button("+");
        plusBtn.setMinSize(44, 44);
        plusBtn.setMaxSize(44, 44);
        plusBtn.getStyleClass().add("roomsetup-stepper-button");

        minusBtn.setOnAction(e -> {
            if (currentBeds[0] > 0) {
                currentBeds[0]--;
                bedsValue.setText(String.valueOf(currentBeds[0]));
                bedsValue.setStyle("-fx-font-size: 32px; -fx-font-weight: 700;" +
                    (currentBeds[0] != baseBeds ? " -fx-text-fill: #0096D6;" : ""));
                bedsModified.setText(currentBeds[0] != baseBeds ?
                    I18nControls.newLabel(EventRoomSetupI18nKeys.Modified).getText() : "");
            }
        });
        plusBtn.setOnAction(e -> {
            currentBeds[0]++;
            bedsValue.setText(String.valueOf(currentBeds[0]));
            bedsValue.setStyle("-fx-font-size: 32px; -fx-font-weight: 700;" +
                (currentBeds[0] != baseBeds ? " -fx-text-fill: #0096D6;" : ""));
            bedsModified.setText(currentBeds[0] != baseBeds ?
                I18nControls.newLabel(EventRoomSetupI18nKeys.Modified).getText() : "");
        });

        capacityControls.getChildren().addAll(minusBtn, bedsDisplay, plusBtn);

        Label baseInfo = new Label(I18nControls.newLabel(EventRoomSetupI18nKeys.Base).getText() + ": " + baseBeds + " " +
            I18nControls.newLabel(EventRoomSetupI18nKeys.Beds).getText().toLowerCase());
        baseInfo.getStyleClass().add("roomsetup-stat-label");
        HBox baseInfoBox = new HBox(baseInfo);
        baseInfoBox.setAlignment(Pos.CENTER);

        capacitySection.getChildren().addAll(capacityHeader, capacityControls, baseInfoBox);

        // Room Type section using EntityButtonSelector (GWT-compatible)
        VBox typeSection = new VBox(8);
        Label typeModified = new Label(!Objects.equals(currentItem[0], baseItem) ? " • " +
            I18nControls.newLabel(EventRoomSetupI18nKeys.Modified).getText() : "");
        typeModified.getStyleClass().add("roomsetup-modified-indicator");
        HBox typeHeader = new HBox(4);
        Label typeLabel = I18nControls.newLabel(EventRoomSetupI18nKeys.RoomType);
        typeLabel.getStyleClass().add("roomsetup-section-label");
        typeHeader.getChildren().addAll(typeLabel, typeModified);

        // Create EntityButtonSelector for room types, filtered by organization
        // Use local dialogArea as drop parent to ensure dropdown appears below button within the dialog
        Object orgId = FXOrganization.getOrganization() != null ? FXOrganization.getOrganization().getPrimaryKey() : null;
        String typeQuery = "{class: 'Item', fields: 'name,ord', where: 'family.code=`acco` and (deprecated is null or deprecated=false)" +
            (orgId != null ? " and organization=" + orgId : "") + "', orderBy: 'ord,name'}";

        EntityButtonSelector<Item> typeSelector = new EntityButtonSelector<>(
            typeQuery,
            new ButtonFactoryMixin() {}, () -> dialogArea, dataSourceModel
        );
        typeSelector.setShowMode(ButtonSelector.ShowMode.DROP_DOWN);
        typeSelector.setSearchEnabled(false);

        // Set initial selection if there's a current item
        if (currentItem[0] != null) {
            typeSelector.setSelectedItem(currentItem[0]);
        }

        // Listen for selection changes to update the currentItem and modified label
        typeSelector.selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            currentItem[0] = newVal;
            typeModified.setText(!Objects.equals(currentItem[0], baseItem) ? " • Modified" : "");
        });

        Node typeSelectorButton = typeSelector.getButton();
        HBox.setHgrow(typeSelectorButton, Priority.ALWAYS);

        typeSection.getChildren().addAll(typeHeader, typeSelectorButton);

        // Gender section with toggle buttons (matching JSX mockup symbols)
        VBox genderSection = new VBox(8);
        Label genderLabel = I18nControls.newLabel(EventRoomSetupI18nKeys.GenderRestriction);
        genderLabel.getStyleClass().add("roomsetup-section-label");

        HBox genderButtons = new HBox(8);
        // Add symbols before text like in JSX mockup: ◐ Mixed, ♀ Female, ♂ Male
        Button mixedBtn = new Button("◐  " + I18nControls.newLabel(EventRoomSetupI18nKeys.Mixed).getText());
        Button femaleBtn = new Button("♀  " + I18nControls.newLabel(EventRoomSetupI18nKeys.Female).getText());
        Button maleBtn = new Button("♂  " + I18nControls.newLabel(EventRoomSetupI18nKeys.Male).getText());

        // Use ModalityStyle chip buttons for toggle behavior
        mixedBtn.setPadding(new Insets(12, 16, 12, 16));
        femaleBtn.setPadding(new Insets(12, 16, 12, 16));
        maleBtn.setPadding(new Insets(12, 16, 12, 16));

        Runnable updateGenderStyles = () -> {
            ModalityStyle.setChipButtonPrimarySelected(mixedBtn, genderState[0] == null);
            ModalityStyle.setChipButtonPrimarySelected(femaleBtn, "female".equals(genderState[0]));
            ModalityStyle.setChipButtonPrimarySelected(maleBtn, "male".equals(genderState[0]));
        };
        updateGenderStyles.run();

        mixedBtn.setOnAction(e -> { genderState[0] = null; updateGenderStyles.run(); });
        femaleBtn.setOnAction(e -> { genderState[0] = "female"; updateGenderStyles.run(); });
        maleBtn.setOnAction(e -> { genderState[0] = "male"; updateGenderStyles.run(); });

        HBox.setHgrow(mixedBtn, Priority.ALWAYS);
        HBox.setHgrow(femaleBtn, Priority.ALWAYS);
        HBox.setHgrow(maleBtn, Priority.ALWAYS);
        mixedBtn.setMaxWidth(Double.MAX_VALUE);
        femaleBtn.setMaxWidth(Double.MAX_VALUE);
        maleBtn.setMaxWidth(Double.MAX_VALUE);

        genderButtons.getChildren().addAll(mixedBtn, femaleBtn, maleBtn);
        genderSection.getChildren().addAll(genderLabel, genderButtons);

        // Notes section
        VBox notesSection = new VBox(8);
        Label notesLabel = I18nControls.newLabel(EventRoomSetupI18nKeys.EventNotes);
        notesLabel.getStyleClass().add("roomsetup-section-label");

        TextArea notesField = new TextArea();
        notesField.setPromptText(I18nControls.newLabel(EventRoomSetupI18nKeys.EventNotesPlaceholder).getText());
        notesField.setText(currentComment != null ? currentComment : "");
        notesField.setPrefRowCount(2);
        notesField.setWrapText(true);
        notesField.getStyleClass().add("roomsetup-form-input");

        notesSection.getChildren().addAll(notesLabel, notesField);

        content.getChildren().addAll(header, infoBanner, capacitySection, typeSection, genderSection, notesSection);

        // Create footer with Reset, Cancel, and Save buttons
        HBox buttonBar = new HBox(12);
        buttonBar.setAlignment(Pos.CENTER_LEFT);
        buttonBar.setPadding(new Insets(16, 20, 20, 20));

        // Reset button - only enabled when an event-specific override exists
        Button resetButton = I18nControls.newButton(EventRoomSetupI18nKeys.ResetToDefault);
        Bootstrap.dangerButton(resetButton);
        resetButton.setPadding(new Insets(10, 24, 10, 24));
        resetButton.setDisable(eventConfig == null);

        // Spacer to push Cancel/Save to the right
        Region buttonSpacer = new Region();
        HBox.setHgrow(buttonSpacer, Priority.ALWAYS);

        Button cancelButton = I18nControls.newButton(EventRoomSetupI18nKeys.Cancel);
        Bootstrap.secondaryButton(cancelButton);
        cancelButton.setPadding(new Insets(10, 24, 10, 24));

        Button saveButton = I18nControls.newButton(EventRoomSetupI18nKeys.Save);
        Bootstrap.primaryButton(saveButton);
        saveButton.setPadding(new Insets(10, 24, 10, 24));

        buttonBar.getChildren().addAll(resetButton, buttonSpacer, cancelButton, saveButton);

        // Wrap in BorderPane for proper layout
        BorderPane dialogPane = new BorderPane();
        dialogPane.setCenter(dialogArea);
        dialogPane.setBottom(buttonBar);
        dialogPane.getStyleClass().add("modal-dialog-pane");
        dialogPane.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(12), null)));
        dialogPane.setBorder(new Border(new BorderStroke(Color.web("#e7e5e4"), BorderStrokeStyle.SOLID, new CornerRadii(12), new BorderWidths(1))));

        // Show dialog using GWT-compatible DialogUtil
        DialogCallback dialogCallback = DialogUtil.showModalNodeInGoldLayout(dialogPane, FXMainFrameDialogArea.getDialogArea());

        // Wire up button actions
        cancelButton.setOnAction(e -> dialogCallback.closeDialog());

        resetButton.setOnAction(e -> {
            deleteOverride(room);
            dialogCallback.closeDialog();
        });

        saveButton.setOnAction(e -> {
            boolean allowsFemale = !"male".equals(genderState[0]);
            boolean allowsMale = !"female".equals(genderState[0]);
            saveOverride(room, currentBeds[0], currentItem[0], allowsFemale, allowsMale, notesField.getText());
            dialogCallback.closeDialog();
        });
    }

    /**
     * Saves a room override.
     */
    private void saveOverride(Resource room, int beds, Item roomType, boolean allowsFemale, boolean allowsMale, String comment) {
        // Find existing configuration or create new - use Entities.samePrimaryKey() for proper EntityId comparison
        ResourceConfiguration config = getResourceConfigurations().stream()
            .filter(rc -> Entities.samePrimaryKey(rc.getResourceId(), room))
            .findFirst()
            .orElse(null);

        if (config == null) {
            config = getUpdateStore().createEntity(ResourceConfiguration.class);
            config.setEvent(currentEvent);
            config.setResource(room);
            // Set start and end dates to match the event dates
            if (currentEvent != null) {
                config.setStartDate(currentEvent.getStartDate());
                config.setEndDate(currentEvent.getEndDate());
            }
        } else {
            config = getUpdateStore().updateEntity(config);
        }

        config.setMax(beds);
        if (roomType != null) {
            config.setItem(roomType);
        }
        config.setAllowsFemale(allowsFemale);
        config.setAllowsMale(allowsMale);
        config.setComment(comment);

        // Track changed entity for targeted refresh
        final Entity changedConfig = config;
        getUpdateStore().submitChanges()
            .onFailure(Console::log)
            .onSuccess(r -> {
                // Refresh only the changed entities - this triggers async data fetch
                // The reactive mapper's entity handler will call notifyBedConfigurationChanged()
                // when the new data arrives, ensuring all tabs refresh with correct data
                dataModel.refreshChangedEntities(Collections.singletonList(changedConfig));
                Platform.runLater(this::refreshUI);
            });
    }

    /**
     * Deletes the event-specific override for a room, restoring it to default configuration.
     *
     * This deletes the ResourceConfiguration from eventRoomConfigs (where event=currentEvent),
     * NOT the permanent configuration (where event is null and no date range).
     * After deletion, the room will use its permanent/default configuration.
     */
    private void deleteOverride(Resource room) {
        // getConfiguration() returns the event-specific override from eventRoomConfigs
        // (NOT the permanent config from permanentRoomConfigs)
        ResourceConfiguration eventOverride = getConfiguration(room);
        if (eventOverride == null) {
            return; // No override to delete
        }

        getUpdateStore().deleteEntity(eventOverride);

        getUpdateStore().submitChanges()
            .onFailure(Console::log)
            .onSuccess(r -> {
                // Refresh event data to remove the deleted override from the list
                dataModel.refreshEventData();
                Platform.runLater(this::refreshUI);
            });
    }

    /**
     * Checks if a room has an override.
     */
    private boolean hasOverride(Resource room) {
        return getResourceConfigurations().stream()
            .anyMatch(rc -> Entities.samePrimaryKey(rc.getResourceId(), room));
    }

    /**
     * Gets the event-specific configuration (override) for a room.
     */
    private ResourceConfiguration getConfiguration(Resource room) {
        return getResourceConfigurations().stream()
            .filter(rc -> Entities.samePrimaryKey(rc.getResourceId(), room))
            .findFirst()
            .orElse(null);
    }

    /**
     * Gets the permanent (default) configuration for a room.
     */
    private ResourceConfiguration getPermanentConfig(Resource room) {
        return getPermanentConfigs().stream()
            .filter(rc -> Entities.samePrimaryKey(rc.getResourceId(), room))
            .findFirst()
            .orElse(null);
    }

    /**
     * Gets the room type (Item name) for a room.
     * First checks for event-specific override, then falls back to permanent config.
     */
    private String getRoomType(Resource room) {
        // First check for event-specific override
        ResourceConfiguration eventConfig = getConfiguration(room);
        if (eventConfig != null && eventConfig.getItem() != null && eventConfig.getItem().getName() != null) {
            return eventConfig.getItem().getName();
        }
        // Fall back to permanent config
        ResourceConfiguration permConfig = getPermanentConfig(room);
        if (permConfig != null && permConfig.getItem() != null && permConfig.getItem().getName() != null) {
            return permConfig.getItem().getName();
        }
        return "Other";
    }

    /**
     * Gets the Item (room type) for a room.
     */
    private Item getRoomItem(Resource room) {
        // First check for event-specific override
        ResourceConfiguration eventConfig = getConfiguration(room);
        if (eventConfig != null && eventConfig.getItem() != null) {
            return eventConfig.getItem();
        }
        // Fall back to permanent config
        ResourceConfiguration permConfig = getPermanentConfig(room);
        if (permConfig != null) {
            return permConfig.getItem();
        }
        return null;
    }

    /**
     * Gets the bed count for a room.
     * First checks for event-specific override, then falls back to permanent config.
     */
    private int getBedCount(Resource room) {
        // First check for event-specific override
        ResourceConfiguration eventConfig = getConfiguration(room);
        if (eventConfig != null && eventConfig.getMax() != null) {
            return eventConfig.getMax();
        }
        // Fall back to permanent config
        ResourceConfiguration permConfig = getPermanentConfig(room);
        if (permConfig != null && permConfig.getMax() != null) {
            return permConfig.getMax();
        }
        return 2; // Last resort default
    }

    /**
     * Checks if a room is from an external site (different from the event's venue).
     */
    private boolean isExternalRoom(Resource room) {
        if (currentEvent == null || room == null || room.getSite() == null) {
            return false;
        }
        Object eventVenueId = currentEvent.getVenueId();
        // Use Entities.samePrimaryKey() for proper EntityId comparison
        return eventVenueId != null && !Entities.samePrimaryKey(room.getSite(), eventVenueId);
    }

    /**
     * Checks if a room has notes/comments.
     */
    private boolean hasNotes(Resource room) {
        // Check event-specific config first
        ResourceConfiguration eventConfig = getConfiguration(room);
        if (eventConfig != null && eventConfig.getComment() != null && !eventConfig.getComment().trim().isEmpty()) {
            return true;
        }
        // Check permanent config
        ResourceConfiguration permConfig = getPermanentConfig(room);
        return permConfig != null && permConfig.getComment() != null && !permConfig.getComment().trim().isEmpty();
    }

    /**
     * Gets the notes/comment for a room (from event config or permanent config).
     */
    private String getNotes(Resource room) {
        // Check event-specific config first
        ResourceConfiguration eventConfig = getConfiguration(room);
        if (eventConfig != null && eventConfig.getComment() != null && !eventConfig.getComment().trim().isEmpty()) {
            return eventConfig.getComment();
        }
        // Check permanent config
        ResourceConfiguration permConfig = getPermanentConfig(room);
        if (permConfig != null && permConfig.getComment() != null && !permConfig.getComment().trim().isEmpty()) {
            return permConfig.getComment();
        }
        return "";
    }

    /**
     * Checks if a pool is a category pool.
     */
    private boolean isCategoryPool(Pool pool) {
        return pool != null && Boolean.TRUE.equals(pool.isEventPool());
    }

    // SVG path for edit/pen icon (Feather-style pencil, naturally diagonal)
    private static final String EDIT_SVG_PATH = "M17 3a2.83 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5L17 3z";

    // SVG path for comment/chat bubble icon (simple bubble)
    private static final String COMMENT_SVG_PATH = "M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z";

    /**
     * Creates a small SVG icon with optional rotation.
     */
    private StackPane createSvgIcon(String svgPath, String color, double size) {
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
        iconPane.getChildren().add(svg);

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
     * Applies a dynamic text fill color to a Label for both JavaFX and WebFX.
     */
    private static void applyDynamicTextFill(Label label, String colorHex) {
        // For JavaFX (desktop)
        label.setStyle("-fx-text-fill: " + colorHex + ";");
        // For WebFX (browser) - programmatic styling
        label.setTextFill(Color.web(colorHex));
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
