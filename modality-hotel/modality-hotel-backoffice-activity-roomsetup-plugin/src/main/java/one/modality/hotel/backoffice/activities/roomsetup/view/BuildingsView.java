package one.modality.hotel.backoffice.activities.roomsetup.view;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.domainmodel.HasDataSourceModel;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import dev.webfx.stack.routing.activity.impl.elementals.activeproperty.HasActiveProperty;
import one.modality.base.shared.entities.Organization;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;
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
import one.modality.base.client.cloud.image.ModalityCloudImageService;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import one.modality.base.client.bootstrap.ModalityStyle;
import one.modality.base.shared.entities.Building;
import one.modality.hotel.backoffice.activities.roomsetup.RoomSetupI18nKeys;
import one.modality.base.shared.entities.BuildingZone;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.hotel.backoffice.activities.roomsetup.RoomSetupPresentationModel;
import one.modality.hotel.backoffice.activities.roomsetup.dialog.BuildingDialog;
import one.modality.hotel.backoffice.activities.roomsetup.dialog.BuildingZoneDialog;
import one.modality.hotel.backoffice.activities.roomsetup.dialog.DialogManager;
import one.modality.hotel.backoffice.activities.roomsetup.util.UIComponentDecorators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static dev.webfx.stack.orm.dql.DqlStatement.orderBy;
import static dev.webfx.stack.orm.dql.DqlStatement.where;

/**
 * Buildings View - displays buildings and their zones in an expandable list format.
 * Based on the mockup design for Buildings tab in Accommodation Activity.
 *
 * @author Claude Code
 */
public class BuildingsView {

    private final RoomSetupPresentationModel pm;
    private ObservableValue<Boolean> activeProperty;
    private DataSourceModel dataSourceModel;


    // Data
    private final ObservableList<Building> buildings = FXCollections.observableArrayList();
    private final ObservableList<BuildingZone> buildingZones = FXCollections.observableArrayList();
    private final ObservableList<ResourceConfiguration> resourceConfigurations = FXCollections.observableArrayList();

    // Performance optimization: Index maps for O(1) lookups
    // These avoid O(n*m) nested stream operations when rendering building list
    private Map<Object, List<BuildingZone>> zonesByBuildingId = new HashMap<>();
    private Map<Object, Long> roomCountByBuildingId = new HashMap<>();
    private Map<Object, Long> roomCountByZoneId = new HashMap<>();

    // Reactive entity mappers
    private ReactiveEntitiesMapper<Building> buildingRem;
    private ReactiveEntitiesMapper<BuildingZone> buildingZoneRem;
    private ReactiveEntitiesMapper<ResourceConfiguration> resourceConfigRem;

    // UI state
    private final ObjectProperty<Building> expandedBuildingProperty = new SimpleObjectProperty<>();
    private Object expandedBuildingId; // Store building ID to preserve expanded state across refreshes

    // Site configuration state
    private final BooleanProperty globalSiteMissingProperty = new SimpleBooleanProperty(false);
    private HBox siteWarningBox;

    private VBox buildingListContainer;

    public BuildingsView(RoomSetupPresentationModel pm) {
        this.pm = pm;
    }

    public Node buildView() {
        // UI components
        VBox mainContainer = new VBox();
        mainContainer.setSpacing(16);
        mainContainer.setPadding(new Insets(20));

        // Header section
        VBox headerSection = createHeaderSection();

        // Site configuration warning (hidden by default)
        siteWarningBox = createSiteWarningBox();
        siteWarningBox.setVisible(false);
        siteWarningBox.setManaged(false);

        // Bind warning visibility to globalSiteMissing property
        globalSiteMissingProperty.addListener((obs, wasVisible, isVisible) -> {
            siteWarningBox.setVisible(isVisible);
            siteWarningBox.setManaged(isVisible);
        });

        // Building list container
        buildingListContainer = new VBox();
        buildingListContainer.setSpacing(12);

        // Wrap building list and info helper in a container for the scroll pane
        VBox scrollContent = new VBox();
        scrollContent.setSpacing(16);
        scrollContent.getChildren().addAll(buildingListContainer, createInfoHelper());

        ScrollPane scrollPane = Controls.createVerticalScrollPane(scrollContent);
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        mainContainer.getChildren().addAll(headerSection, siteWarningBox, scrollPane);

        // Listen for data changes - rebuild index maps before updating UI
        buildings.addListener((ListChangeListener<? super Building>) change -> {
            rebuildBuildingMaps();
            updateBuildingList();
        });
        buildingZones.addListener((ListChangeListener<? super BuildingZone>) change -> {
            rebuildBuildingMaps();
            updateBuildingList();
        });
        resourceConfigurations.addListener((ListChangeListener<? super ResourceConfiguration>) change -> {
            rebuildBuildingMaps();
            updateBuildingList();
        });

        // Initialize building list (in case data already loaded before view was built)
        updateBuildingList();

        return mainContainer;
    }

    private VBox createHeaderSection() {
        VBox header = new VBox();
        header.setSpacing(16);
        header.setPadding(new Insets(0, 0, 16, 0));

        // Title
        VBox titleBox = new VBox();
        titleBox.setSpacing(6);
        Label titleLabel = I18nControls.newLabel(RoomSetupI18nKeys.BuildingsAndLocations);
        titleLabel.getStyleClass().add(UIComponentDecorators.CSS_TITLE);
        Label subtitleLabel = I18nControls.newLabel(RoomSetupI18nKeys.BuildingsSubtitle);
        subtitleLabel.getStyleClass().add(UIComponentDecorators.CSS_SUBTITLE);
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);

        // Buildings count and Add button row
        HBox controlRow = new HBox();
        controlRow.setAlignment(Pos.CENTER_LEFT);
        controlRow.setSpacing(16);

        Label countLabel = new Label("BUILDINGS (" + buildings.size() + ")");
        countLabel.getStyleClass().add(UIComponentDecorators.CSS_CAPTION);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBuildingButton = Bootstrap.primaryButton(I18nControls.newButton(RoomSetupI18nKeys.AddBuilding));
        addBuildingButton.setOnAction(e -> openBuildingDialog(null));

        controlRow.getChildren().addAll(countLabel, spacer, addBuildingButton);

        header.getChildren().addAll(titleBox, controlRow);

        return header;
    }

    private HBox createInfoHelper() {
        HBox infoBox = Bootstrap.infoBox(new HBox());
        infoBox.setSpacing(10);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.setPadding(new Insets(12, 16, 12, 16));
        infoBox.getStyleClass().add(UIComponentDecorators.CSS_INFO_BAR);
        VBox.setMargin(infoBox, new Insets(34, 0, 0, 0)); // Add 50px total space (34 + 16 from scrollContent spacing)

        Label iconLabel = new Label("💡");

        Label textLabel = new Label("How it works: Buildings are the main structures on your site (e.g., \"Priory\", \"North Quad\"). " +
                "Locations are areas within buildings (e.g., \"Ground Floor\", \"East Wing\"). " +
                "Assign rooms to buildings and locations for better organization.");
        textLabel.getStyleClass().add(UIComponentDecorators.CSS_SMALL);
        textLabel.setWrapText(true);

        infoBox.getChildren().addAll(iconLabel, textLabel);

        return infoBox;
    }

    private HBox createSiteWarningBox() {
        HBox warningBox = Bootstrap.alertDanger(new HBox());
        warningBox.setSpacing(10);
        warningBox.setAlignment(Pos.CENTER_LEFT);
        warningBox.setPadding(new Insets(12, 16, 12, 16));

        Label iconLabel = new Label("⚠️");

        Label textLabel = new Label("Site configuration required: This organization does not have a global site defined. " +
                "Buildings cannot be created until a site is configured. Please contact an administrator to set up the site.");
        textLabel.getStyleClass().add(UIComponentDecorators.CSS_SMALL);
        textLabel.setWrapText(true);
        textLabel.setStyle("-fx-text-fill: #721c24;"); // Dark red text for danger alerts

        warningBox.getChildren().addAll(iconLabel, textLabel);

        return warningBox;
    }

    private void updateBuildingList() {
        Platform.runLater(() -> {
            // Guard against being called before view is built
            if (buildingListContainer == null) {
                return;
            }

            // Store expanded building ID before clearing
            Building currentExpanded = expandedBuildingProperty.get();
            if (currentExpanded != null) {
                expandedBuildingId = currentExpanded.getPrimaryKey();
            }

            buildingListContainer.getChildren().clear();

            if (buildings.isEmpty()) {
                buildingListContainer.getChildren().add(createEmptyState());
                return;
            }

            Building buildingToExpand = null;
            for (Building building : buildings) {
                VBox buildingPanel = createBuildingPanel(building);
                buildingListContainer.getChildren().add(buildingPanel);

                // Check if this building should be expanded (by ID match)
                if (expandedBuildingId != null && building.getPrimaryKey() != null
                        && building.getPrimaryKey().equals(expandedBuildingId)) {
                    buildingToExpand = building;
                }
            }

            // Restore expanded state
            if (buildingToExpand != null) {
                expandedBuildingProperty.set(buildingToExpand);
            }
        });
    }

    private VBox createEmptyState() {
        VBox emptyState = new VBox();
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setSpacing(16);
        emptyState.setPadding(new Insets(48));
        emptyState.getStyleClass().add(UIComponentDecorators.CSS_CARD);

        Label iconLabel = new Label("🏛️");

        Label titleLabel = I18nControls.newLabel(RoomSetupI18nKeys.NoBuildings);
        titleLabel.getStyleClass().add(UIComponentDecorators.CSS_TITLE);

        Label descLabel = I18nControls.newLabel(RoomSetupI18nKeys.NoBuildingsDescription);
        descLabel.getStyleClass().add(UIComponentDecorators.CSS_SUBTITLE);

        Button addBtn = Bootstrap.primaryButton(I18nControls.newButton(RoomSetupI18nKeys.AddFirstBuilding));
        addBtn.setOnAction(e -> openBuildingDialog(null));

        emptyState.getChildren().addAll(iconLabel, titleLabel, descLabel, addBtn);

        return emptyState;
    }

    private VBox createBuildingPanel(Building building) {
        VBox panel = new VBox();
        panel.getStyleClass().add(UIComponentDecorators.CSS_CARD);

        // Header row
        HBox header = createBuildingHeader(building);

        // Content container (for zones when expanded)
        VBox contentContainer = new VBox();

        // Check if this building should be initially expanded (by comparing IDs)
        // This is needed because Entity.equals() may return true for different instances with same ID,
        // causing the property change listener not to fire
        boolean initiallyExpanded = expandedBuildingId != null && building.getPrimaryKey() != null
                && building.getPrimaryKey().equals(expandedBuildingId);

        contentContainer.setVisible(initiallyExpanded);
        contentContainer.setManaged(initiallyExpanded);
        if (initiallyExpanded) {
            contentContainer.getChildren().add(createZonesContent(building));
            header.getStyleClass().add(UIComponentDecorators.CSS_BUILDING_HEADER);
        }

        // Listen for expansion changes
        FXProperties.runOnPropertyChange(expandedBuilding -> {
            boolean isExpanded = building.equals(expandedBuilding);
            contentContainer.setVisible(isExpanded);
            contentContainer.setManaged(isExpanded);
            if (isExpanded) {
                contentContainer.getChildren().clear();
                contentContainer.getChildren().add(createZonesContent(building));
            }
            // Update header style class
            header.getStyleClass().remove(UIComponentDecorators.CSS_BUILDING_HEADER);
            if (isExpanded) {
                header.getStyleClass().add(UIComponentDecorators.CSS_BUILDING_HEADER);
            }
        }, expandedBuildingProperty);

        panel.getChildren().addAll(header, contentContainer);

        return panel;
    }

    private HBox createBuildingHeader(Building building) {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(14);
        header.setPadding(new Insets(16, 20, 16, 20));
        header.getStyleClass().add(UIComponentDecorators.CSS_CLICKABLE);

        // Icon - try to load building image
        Node iconNode = createBuildingIcon(building);

        // Name
        VBox infoBox = new VBox();
        infoBox.setSpacing(4);
        Label nameLabel = new Label(building.getName());
        nameLabel.getStyleClass().add(UIComponentDecorators.CSS_BUILDING_NAME);
        infoBox.getChildren().add(nameLabel);

        HBox.setHgrow(infoBox, Priority.ALWAYS);

        // Stats badges - use O(1) lookups instead of stream filtering
        List<BuildingZone> zones = getZonesForBuilding(building);
        long roomCount = countRoomsInBuilding(building);

        Label locationsBadge = new Label(zones.size() + " location" + (zones.size() != 1 ? "s" : ""));
        locationsBadge.getStyleClass().add(UIComponentDecorators.CSS_BADGE_INFO);
        locationsBadge.setPadding(new Insets(4, 10, 4, 10));

        Label roomsBadge = ModalityStyle.badgeLightInfo(new Label(roomCount + " room" + (roomCount != 1 ? "s" : "")));

        // Edit button
        Button editBtn = ModalityStyle.outlineSecondaryButton(I18nControls.newButton(RoomSetupI18nKeys.Edit));
        editBtn.setOnAction(e -> {
            e.consume();
            openBuildingDialog(building);
        });

        // Expand/collapse toggle - reactive to expanded state
        Label toggleLabel = new Label();
        toggleLabel.setMinSize(24, 24);
        toggleLabel.setMaxSize(24, 24);
        toggleLabel.setAlignment(Pos.CENTER);
        toggleLabel.getStyleClass().add(UIComponentDecorators.CSS_EXPAND_ARROW);

        // Update toggle appearance based on expanded state
        // Use ID-based comparison for consistency (Entity.equals may not work across refreshes)
        Runnable updateToggle = () -> {
            boolean isExpanded = (expandedBuildingId != null && building.getPrimaryKey() != null
                    && building.getPrimaryKey().equals(expandedBuildingId))
                    || building.equals(expandedBuildingProperty.get());
            toggleLabel.setText(isExpanded ? "▼" : "▶");
            toggleLabel.getStyleClass().remove(UIComponentDecorators.CSS_EXPAND_ARROW_EXPANDED);
            if (isExpanded) {
                toggleLabel.getStyleClass().add(UIComponentDecorators.CSS_EXPAND_ARROW_EXPANDED);
            }
        };
        updateToggle.run();

        // Listen for expansion state changes
        FXProperties.runOnPropertyChange(expanded -> updateToggle.run(), expandedBuildingProperty);

        header.getChildren().addAll(iconNode, infoBox, locationsBadge, roomsBadge, editBtn, toggleLabel);

        // Click to expand/collapse
        header.setOnMouseClicked(e -> {
            if (building.equals(expandedBuildingProperty.get())) {
                expandedBuildingProperty.set(null);
            } else {
                expandedBuildingProperty.set(building);
            }
        });

        return header;
    }

    private Node createBuildingIcon(Building building) {
        // Container for the building icon
        MonoPane iconContainer = new MonoPane();
        iconContainer.setPrefSize(36, 36);
        iconContainer.setMinSize(36, 36);
        iconContainer.setMaxSize(36, 36);

        // Create placeholder
        Label placeholder = new Label("\uD83C\uDFDB️");
        placeholder.setStyle("-fx-font-size: 24px;");
        iconContainer.setContent(placeholder);

        // Try to load building image from cloud
        String buildingImagePath = ModalityCloudImageService.buildingImagePath(building);
        ModalityCloudImageService.loadHdpiImage(buildingImagePath, 36, 36, iconContainer, () -> {
            Label p = new Label("\uD83C\uDFDB️");
            p.setStyle("-fx-font-size: 24px;");
            return p;
        });

        return iconContainer;
    }

    /**
     * Rebuilds all index maps for O(1) lookups.
     * Called when any of the data lists change.
     * Complexity: O(z + r) where z = zones, r = resource configurations (single pass each)
     */
    private void rebuildBuildingMaps() {
        // Build zone-by-building map
        zonesByBuildingId.clear();
        for (BuildingZone zone : buildingZones) {
            if (zone.getBuilding() != null) {
                Object buildingId = zone.getBuilding().getId().getPrimaryKey();
                zonesByBuildingId.computeIfAbsent(buildingId, k -> new ArrayList<>()).add(zone);
            }
        }

        // Build room counts using single pass with Collectors.groupingBy
        roomCountByBuildingId = resourceConfigurations.stream()
                .filter(rc -> rc.getResource() != null && rc.getResource().getBuilding() != null)
                .collect(Collectors.groupingBy(
                        rc -> rc.getResource().getBuilding().getId().getPrimaryKey(),
                        Collectors.counting()
                ));

        roomCountByZoneId = resourceConfigurations.stream()
                .filter(rc -> rc.getResource() != null && rc.getResource().getBuildingZone() != null)
                .collect(Collectors.groupingBy(
                        rc -> rc.getResource().getBuildingZone().getId().getPrimaryKey(),
                        Collectors.counting()
                ));
    }

    /**
     * Gets zones for a building using O(1) HashMap lookup.
     */
    private List<BuildingZone> getZonesForBuilding(Building building) {
        if (building == null) return Collections.emptyList();
        return zonesByBuildingId.getOrDefault(building.getId().getPrimaryKey(), Collections.emptyList());
    }

    /**
     * Gets room count for a building using O(1) HashMap lookup.
     */
    private long countRoomsInBuilding(Building building) {
        if (building == null) return 0;
        return roomCountByBuildingId.getOrDefault(building.getId().getPrimaryKey(), 0L);
    }

    /**
     * Gets room count for a zone using O(1) HashMap lookup.
     */
    private long countRoomsInZone(BuildingZone zone) {
        if (zone == null) return 0;
        return roomCountByZoneId.getOrDefault(zone.getId().getPrimaryKey(), 0L);
    }

    private VBox createZonesContent(Building building) {
        VBox content = new VBox();
        content.setSpacing(8);
        content.setPadding(new Insets(16, 20, 16, 20));
        content.getStyleClass().add(UIComponentDecorators.CSS_CARD_HEADER);

        // Header row
        HBox headerRow = new HBox();
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.setSpacing(12);

        Label sectionLabel = new Label("LOCATIONS IN " + building.getName().toUpperCase());
        sectionLabel.getStyleClass().add(UIComponentDecorators.CSS_CAPTION);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addZoneBtn = ModalityStyle.outlinePrimaryButton(I18nControls.newButton(RoomSetupI18nKeys.AddLocation));
        addZoneBtn.setOnAction(e -> openZoneDialog(building, null));

        headerRow.getChildren().addAll(sectionLabel, spacer, addZoneBtn);
        content.getChildren().add(headerRow);

        // Zone list - use O(1) lookup instead of stream filtering
        List<BuildingZone> zones = getZonesForBuilding(building);

        if (zones.isEmpty()) {
            Label emptyLabel = I18nControls.newLabel(RoomSetupI18nKeys.NoLocationsYet);
            emptyLabel.getStyleClass().add(UIComponentDecorators.CSS_EMPTY_STATE_TEXT);
            emptyLabel.setPadding(new Insets(20));
            emptyLabel.setMaxWidth(Double.MAX_VALUE);
            emptyLabel.setAlignment(Pos.CENTER);
            content.getChildren().add(emptyLabel);
        } else {
            VBox zoneList = new VBox();
            zoneList.setSpacing(8);
            for (BuildingZone zone : zones) {
                HBox zoneRow = createZoneRow(zone);
                zoneList.getChildren().add(zoneRow);
            }
            content.getChildren().add(zoneList);
        }

        return content;
    }

    private HBox createZoneRow(BuildingZone zone) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setSpacing(12);
        row.setPadding(new Insets(12, 16, 12, 16));
        row.getStyleClass().add(UIComponentDecorators.CSS_ZONE_ROW);

        // Name
        Label nameLabel = new Label(zone.getName());
        nameLabel.getStyleClass().add(UIComponentDecorators.CSS_ZONE_NAME);

        // Room count badge
        long roomCount = countRoomsInZone(zone);
        Label roomsBadge = new Label(roomCount + " room" + (roomCount != 1 ? "s" : ""));
        roomsBadge.getStyleClass().add(UIComponentDecorators.CSS_BADGE_INFO);
        roomsBadge.setPadding(new Insets(3, 8, 3, 8));

        // Spacer to push edit button to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Edit button with SVG icon
        SVGPath editIcon = createEditIcon();
        StackPane editBtn = new StackPane(editIcon);
        editBtn.setPrefSize(28, 28);
        editBtn.setMinSize(28, 28);
        editBtn.setMaxSize(28, 28);
        editBtn.getStyleClass().addAll(UIComponentDecorators.CSS_ACTION_BUTTON, UIComponentDecorators.CSS_CLICKABLE);
        editBtn.setCursor(Cursor.HAND);
        editBtn.setOnMouseClicked(e -> {
            e.consume();
            openZoneDialog(zone.getBuilding(), zone);
        });

        row.getChildren().addAll(nameLabel, roomsBadge, spacer, editBtn);

        return row;
    }

    /**
     * Creates an edit/pencil SVG icon for inline editing.
     */
    private SVGPath createEditIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent("M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z");
        icon.setFill(Color.web("#78716c"));
        icon.setScaleX(0.7);
        icon.setScaleY(0.7);
        return icon;
    }

    private void openBuildingDialog(Building building) {
        BuildingDialog dialog = new BuildingDialog(dataSourceModel, building);
        DialogManager.openDialog(dialog, () -> {
            if (buildingRem != null) {
                buildingRem.refreshWhenActive();
            }
        }, true); // supportsDelete = true
    }

    private void openZoneDialog(Building building, BuildingZone zone) {
        BuildingZoneDialog dialog = new BuildingZoneDialog(dataSourceModel, building, zone);
        DialogManager.openDialog(dialog, () -> {
            // Store the building ID BEFORE refresh to ensure it stays expanded
            if (building != null) {
                expandedBuildingId = building.getPrimaryKey();
            }
            if (buildingZoneRem != null) {
                buildingZoneRem.refreshWhenActive();
            }
        }, true); // supportsDelete = true
    }

    private void checkGlobalSiteConfiguration() {
        if (dataSourceModel == null) return;

        var orgId = FXOrganizationId.getOrganizationId();
        if (orgId == null) return;

        EntityStore entityStore = EntityStore.create(dataSourceModel);
        entityStore.<Organization>executeQuery("select globalSite from Organization where id=?", orgId)
                .onSuccess(organizations -> {
                    Platform.runLater(() -> {
                        if (!organizations.isEmpty()) {
                            Organization org = organizations.get(0);
                            globalSiteMissingProperty.set(org.getGlobalSite() == null);
                        }
                    });
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

        // Check if organization has a globalSite configured
        checkGlobalSiteConfiguration();

        if (buildingRem == null) {
            // Load Buildings
            buildingRem = ReactiveEntitiesMapper.<Building>createPushReactiveChain(mixin)
                    .always("{class: 'Building', fields: 'name,site'}")
                    .always(orderBy("name"))
                    .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), o -> where("site.organization=?", o))
                    .storeEntitiesInto(buildings)
                    .addEntitiesHandler(entities -> {
                        // Force UI update when entities are received (after refresh)
                        Platform.runLater(this::updateBuildingList);
                    })
                    .setResultCacheEntry("modality/hotel/roomsetup/buildings-view")
                    .start();

            // Load BuildingZones
            buildingZoneRem = ReactiveEntitiesMapper.<BuildingZone>createPushReactiveChain(mixin)
                    .always("{class: 'BuildingZone', fields: 'name,building.name,building.site'}")
                    .always(orderBy("building.name,name"))
                    .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), o -> where("building.site.organization=?", o))
                    .storeEntitiesInto(buildingZones)
                    .addEntitiesHandler(entities -> {
                        // Force UI update when entities are received (after refresh)
                        Platform.runLater(this::updateBuildingList);
                    })
                    .setResultCacheEntry("modality/hotel/roomsetup/building-zones-view")
                    .start();

            // Load ResourceConfigurations for room counts
            resourceConfigRem = ReactiveEntitiesMapper.<ResourceConfiguration>createPushReactiveChain(mixin)
                    .always("{class: 'ResourceConfiguration', alias: 'rc', fields: 'resource.name,resource.site,resource.building,resource.buildingZone'}")
                    .always(where("startDate is null and endDate is null"))
                    .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), o -> where("resource.site.organization=?", o))
                    .storeEntitiesInto(resourceConfigurations)
                    .addEntitiesHandler(entities -> {
                        // Force UI update when entities are received (after refresh)
                        Platform.runLater(this::updateBuildingList);
                    })
                    .setResultCacheEntry("modality/hotel/roomsetup/resource-configs-view")
                    .start();

            // Bind to active property so refreshWhenActive() works
            if (activeProperty != null) {
                buildingRem.bindActivePropertyTo(activeProperty);
                buildingZoneRem.bindActivePropertyTo(activeProperty);
                resourceConfigRem.bindActivePropertyTo(activeProperty);
            }
        } else if (activeProperty != null) {
            buildingRem.bindActivePropertyTo(activeProperty);
            buildingZoneRem.bindActivePropertyTo(activeProperty);
            resourceConfigRem.bindActivePropertyTo(activeProperty);
        }
    }

    public ObservableList<Building> getBuildings() {
        return buildings;
    }

}
