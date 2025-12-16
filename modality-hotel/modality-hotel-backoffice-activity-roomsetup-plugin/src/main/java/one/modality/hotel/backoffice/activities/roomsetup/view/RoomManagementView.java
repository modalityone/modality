package one.modality.hotel.backoffice.activities.roomsetup.view;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.domainmodel.HasDataSourceModel;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import dev.webfx.stack.routing.activity.impl.elementals.activeproperty.HasActiveProperty;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import one.modality.base.client.bootstrap.ModalityStyle;
import one.modality.base.shared.entities.*;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.crm.backoffice.organization.fx.FXOrganization;
import one.modality.hotel.backoffice.activities.roomsetup.RoomSetupI18nKeys;
import one.modality.hotel.backoffice.activities.roomsetup.RoomSetupPresentationModel;
import one.modality.hotel.backoffice.activities.roomsetup.dialog.DialogManager;
import one.modality.hotel.backoffice.activities.roomsetup.dialog.RoomDialog;
import one.modality.hotel.backoffice.activities.roomsetup.util.UIComponentDecorators;

import java.util.*;
import java.util.stream.Collectors;

import static dev.webfx.stack.orm.dql.DqlStatement.orderBy;
import static dev.webfx.stack.orm.dql.DqlStatement.where;

/**
 * Room Management View - displays rooms in a table format with filtering and grouping options.
 * Based on the mockup design for Room Management tab in Accommodation Activity.
 *
 * @author Claude Code
 */
public class RoomManagementView {

    private final RoomSetupPresentationModel pm;
    private DataSourceModel dataSourceModel;
    private ObservableValue<Boolean> activeProperty;

    // Data
    private final ObservableList<ResourceConfiguration> resourceConfigurations = FXCollections.observableArrayList();
    private final ObservableList<Building> buildings = FXCollections.observableArrayList();
    private final ObservableList<BuildingZone> buildingZones = FXCollections.observableArrayList();
    private final ObservableList<Item> accommodationItems = FXCollections.observableArrayList();

    // Reactive entity mappers
    private ReactiveEntitiesMapper<ResourceConfiguration> resourceConfigRem;
    private ReactiveEntitiesMapper<Building> buildingRem;
    private ReactiveEntitiesMapper<BuildingZone> buildingZoneRem;
    private ReactiveEntitiesMapper<Item> itemRem;

    // Filter state
    private final ObjectProperty<Item> filterTypeProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<String> groupByProperty = new SimpleObjectProperty<>("type"); // "type", "building", "zone"

    private VBox roomListContainer;
    private Label roomCountLabel;
    private HBox filterChipsContainer;

    public RoomManagementView(RoomSetupPresentationModel pm) {
        this.pm = pm;
    }

    public Node buildView() {
        // UI components
        VBox mainContainer = new VBox();
        mainContainer.setSpacing(16);
        mainContainer.setPadding(new Insets(20));

        // Header section
        VBox headerSection = createHeaderSection();

        // Filter bar
        HBox filterBar = createFilterBar();

        // Room list container
        roomListContainer = new VBox();
        roomListContainer.setSpacing(16);

        ScrollPane scrollPane = Controls.createVerticalScrollPane(roomListContainer);
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        mainContainer.getChildren().addAll(headerSection, filterBar, scrollPane);

        // Listen for data changes
        resourceConfigurations.addListener((ListChangeListener<? super ResourceConfiguration>) change -> updateRoomList());
        buildings.addListener((ListChangeListener<? super Building>) change -> updateRoomList());
        buildingZones.addListener((ListChangeListener<? super BuildingZone>) change -> updateRoomList());
        accommodationItems.addListener((ListChangeListener<? super Item>) change -> {
            updateFilterChips();
            updateRoomList();
        });

        // Listen for filter and group changes
        filterTypeProperty.addListener((obs, oldVal, newVal) -> {
            updateFilterChips();
            updateRoomList();
        });
        groupByProperty.addListener((obs, oldVal, newVal) -> updateRoomList());

        // Initialize room list (in case data already loaded before view was built)
        updateFilterChips();
        updateRoomList();

        return mainContainer;
    }

    private VBox createHeaderSection() {
        VBox header = new VBox();
        header.setSpacing(8);
        header.setPadding(new Insets(0, 0, 16, 0));
        header.getStyleClass().add(UIComponentDecorators.CSS_DIALOG_HEADER);

        // Page title with organization name (using Bootstrap for primary blue color)
        Label organizationLabel = Bootstrap.textPrimary(Bootstrap.h2(new Label()));
        // Bind to organization name
        FXOrganization.organizationProperty().addListener((obs, oldOrg, newOrg) -> organizationLabel.setText(newOrg != null ? newOrg.getName() : ""));
        Organization currentOrg = FXOrganization.getOrganization();
        if (currentOrg != null) {
            organizationLabel.setText(currentOrg.getName());
        }

        Label siteSubtitleLabel = I18nControls.newLabel(RoomSetupI18nKeys.RoomManagementSubtitle);
        siteSubtitleLabel.getStyleClass().add(UIComponentDecorators.CSS_SUBTITLE);

        VBox pageTitleBox = new VBox();
        pageTitleBox.setSpacing(2);
        pageTitleBox.setPadding(new Insets(0, 0, 16, 0));
        pageTitleBox.getChildren().addAll(organizationLabel, siteSubtitleLabel);

        // Title row
        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);
        titleRow.setSpacing(16);

        VBox titleBox = new VBox();
        titleBox.setSpacing(4);
        Label titleLabel = I18nControls.newLabel(RoomSetupI18nKeys.RoomManagement);
        titleLabel.getStyleClass().add(UIComponentDecorators.CSS_TITLE);

        roomCountLabel = new Label(I18n.getI18nText(RoomSetupI18nKeys.RoomCountAndCapacity, 0, 0));
        roomCountLabel.getStyleClass().add(UIComponentDecorators.CSS_SUBTITLE);

        titleBox.getChildren().addAll(titleLabel, roomCountLabel);

        // Add Room button
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addRoomButton = Bootstrap.primaryButton(I18nControls.newButton(RoomSetupI18nKeys.AddRoom));
        addRoomButton.setOnAction(e -> openRoomDialog(null));

        titleRow.getChildren().addAll(titleBox, spacer, addRoomButton);
        header.getChildren().addAll(pageTitleBox, titleRow);

        return header;
    }

    private HBox createFilterBar() {
        HBox filterBar = new HBox();
        filterBar.setSpacing(24);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(0, 0, 16, 0));

        // Filter by type section
        VBox filterSection = new VBox();
        filterSection.setSpacing(10);
        Label filterLabel = I18nControls.newLabel(RoomSetupI18nKeys.FilterByType);
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

        Button typeBtn = createGroupButton(RoomSetupI18nKeys.GroupByType, "type");
        Button buildingBtn = createGroupButton(RoomSetupI18nKeys.GroupByBuilding, "building");
        Button zoneBtn = createGroupButton(RoomSetupI18nKeys.GroupByZone, "zone");

        groupButtons.getChildren().addAll(typeBtn, buildingBtn, zoneBtn);
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
                        String btnGroupBy = (String) b.getUserData();
                        if (btnGroupBy != null) {
                            updateGroupButtonStyle(b, btnGroupBy);
                        }
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
            filterChipsContainer.getChildren().clear();

            // All button
            Button allBtn = new Button(I18n.getI18nText(RoomSetupI18nKeys.AllTypes) + " (" + resourceConfigurations.size() + ")");
            boolean isAllSelected = filterTypeProperty.get() == null;
            ModalityStyle.setChipButtonPrimarySelected(allBtn, isAllSelected);
            allBtn.setOnAction(e -> filterTypeProperty.set(null));
            filterChipsContainer.getChildren().add(allBtn);

            // Type filter buttons
            for (Item item : accommodationItems) {
                long count = resourceConfigurations.stream()
                        .filter(rc -> rc.getItem() != null && rc.getItem().equals(item))
                        .count();
                if (count > 0) {
                    Button typeBtn = new Button(item.getName() + " (" + count + ")");
                    boolean isSelected = item.equals(filterTypeProperty.get());
                    ModalityStyle.setChipButtonPrimarySelected(typeBtn, isSelected);
                    typeBtn.setOnAction(e -> filterTypeProperty.set(isSelected ? null : item));
                    filterChipsContainer.getChildren().add(typeBtn);
                }
            }
        });
    }

    private void updateRoomList() {
        Platform.runLater(() -> {
            roomListContainer.getChildren().clear();

            // Filter rooms
            List<ResourceConfiguration> filteredRooms = resourceConfigurations.stream()
                    .filter(rc -> filterTypeProperty.get() == null || (rc.getItem() != null && rc.getItem().equals(filterTypeProperty.get())))
                    .collect(Collectors.toList());

            // Update count
            int totalCapacity = filteredRooms.stream()
                    .mapToInt(rc -> rc.getMax() != null ? rc.getMax() : 0)
                    .sum();
            roomCountLabel.setText(I18n.getI18nText(RoomSetupI18nKeys.RoomCountAndCapacity, filteredRooms.size(), totalCapacity));

            // Group rooms
            Map<String, List<ResourceConfiguration>> groupedRooms = groupRooms(filteredRooms);

            // Create group panels
            for (Map.Entry<String, List<ResourceConfiguration>> entry : groupedRooms.entrySet()) {
                VBox groupPanel = createGroupPanel(entry.getKey(), entry.getValue());
                roomListContainer.getChildren().add(groupPanel);
            }
        });
    }

    private Map<String, List<ResourceConfiguration>> groupRooms(List<ResourceConfiguration> rooms) {
        String groupBy = groupByProperty.get();
        Map<String, List<ResourceConfiguration>> grouped = new LinkedHashMap<>();

        for (ResourceConfiguration rc : rooms) {
            String key;
            switch (groupBy) {
                case "building":
                    Resource resource = rc.getResource();
                    Building building = findBuildingForResource(resource);
                    key = building != null ? building.getName() : I18n.getI18nText(RoomSetupI18nKeys.UnknownBuilding);
                    break;
                case "zone":
                    Resource res = rc.getResource();
                    BuildingZone zone = findZoneForResource(res);
                    if (zone != null) {
                        Building b = zone.getBuilding();
                        key = (b != null ? b.getName() + " — " : "") + zone.getName();
                    } else {
                        key = I18n.getI18nText(RoomSetupI18nKeys.UnknownBuilding);
                    }
                    break;
                case "type":
                default:
                    Item item = rc.getItem();
                    key = item != null ? item.getName() : I18n.getI18nText(RoomSetupI18nKeys.UnknownType);
                    break;
            }
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(rc);
        }

        return grouped;
    }

    private Building findBuildingForResource(Resource resource) {
        if (resource == null) return null;
        return resource.getBuilding();
    }

    private BuildingZone findZoneForResource(Resource resource) {
        if (resource == null) return null;
        return resource.getBuildingZone();
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

        int totalCapacity = rooms.stream().mapToInt(rc -> rc.getMax() != null ? rc.getMax() : 0).sum();
        Label statsLabel = ModalityStyle.badgeLightInfo(new Label(rooms.size() + " rooms • capacity: " + totalCapacity));

        header.getChildren().addAll(groupLabel, statsLabel);

        // Table
        GridPane table = new GridPane();
        table.setHgap(0);
        table.setVgap(0);

        // Table header
        Object[] headerKeys = {RoomSetupI18nKeys.TableHeaderRoom, RoomSetupI18nKeys.TableHeaderType, RoomSetupI18nKeys.TableHeaderLocation, RoomSetupI18nKeys.TableHeaderCapacity, RoomSetupI18nKeys.TableHeaderGender, RoomSetupI18nKeys.TableHeaderNotes};
        double[] widths = {15, 20, 20, 10, 10, 25};
        Pos[] alignments = {Pos.CENTER_LEFT, Pos.CENTER_LEFT, Pos.CENTER_LEFT, Pos.CENTER, Pos.CENTER, Pos.CENTER_LEFT};
        for (int i = 0; i < headerKeys.length; i++) {
            HBox headerCell = new HBox();
            headerCell.setAlignment(alignments[i]);
            headerCell.setPadding(new Insets(12, 20, 12, 20));
            headerCell.setMaxWidth(Double.MAX_VALUE);
            Label headerLabel = I18nControls.newLabel(headerKeys[i]);
            headerLabel.getStyleClass().add(UIComponentDecorators.CSS_SMALL);
            headerCell.getChildren().add(headerLabel);
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(widths[i]);
            cc.setFillWidth(true);
            table.getColumnConstraints().add(cc);
            GridPane.setFillWidth(headerCell, true);
            table.add(headerCell, i, 0);
        }

        // Table rows
        int row = 1;
        for (ResourceConfiguration rc : rooms) {
            addRoomRow(table, row++, rc);
        }

        panel.getChildren().addAll(header, table);
        return panel;
    }

    private void addRoomRow(GridPane table, int row, ResourceConfiguration rc) {
        Insets cellPadding = new Insets(12, 20, 12, 20);

        // Room name
        Resource resource = rc.getResource();
        HBox roomCell = new HBox();
        roomCell.setAlignment(Pos.CENTER_LEFT);
        roomCell.setPadding(cellPadding);
        roomCell.getStyleClass().addAll(UIComponentDecorators.CSS_CELL, UIComponentDecorators.CSS_CLICKABLE);
        roomCell.setMaxWidth(Double.MAX_VALUE);
        Label roomName = new Label(resource != null ? resource.getName() : "—");
        roomName.getStyleClass().add(UIComponentDecorators.CSS_BODY_BOLD);
        roomCell.getChildren().add(roomName);
        roomCell.setOnMouseClicked(e -> openRoomDialog(rc));
        GridPane.setFillWidth(roomCell, true);
        table.add(roomCell, 0, row);

        // Type
        Item item = rc.getItem();
        HBox typeCell = new HBox();
        typeCell.setAlignment(Pos.CENTER_LEFT);
        typeCell.setPadding(cellPadding);
        typeCell.getStyleClass().addAll(UIComponentDecorators.CSS_CELL, UIComponentDecorators.CSS_CLICKABLE);
        typeCell.setMaxWidth(Double.MAX_VALUE);
        Label typeLabel = new Label(item != null ? item.getName() : "—");
        typeLabel.getStyleClass().add(UIComponentDecorators.CSS_BODY);
        typeCell.getChildren().add(typeLabel);
        typeCell.setOnMouseClicked(e -> openRoomDialog(rc));
        GridPane.setFillWidth(typeCell, true);
        table.add(typeCell, 1, row);

        // Location - show building zone name (with building name if available)
        String locationText = getString(resource);
        HBox locationCell = new HBox();
        locationCell.setAlignment(Pos.CENTER_LEFT);
        locationCell.setPadding(cellPadding);
        locationCell.getStyleClass().addAll(UIComponentDecorators.CSS_CELL, UIComponentDecorators.CSS_CLICKABLE);
        locationCell.setMaxWidth(Double.MAX_VALUE);
        Label locationLabel = new Label(locationText);
        locationLabel.getStyleClass().add(UIComponentDecorators.CSS_BODY);
        locationCell.getChildren().add(locationLabel);
        locationCell.setOnMouseClicked(e -> openRoomDialog(rc));
        GridPane.setFillWidth(locationCell, true);
        table.add(locationCell, 2, row);

        // Capacity - centered in cell
        HBox capacityCell = new HBox();
        capacityCell.setAlignment(Pos.CENTER);
        capacityCell.setPadding(cellPadding);
        capacityCell.getStyleClass().addAll(UIComponentDecorators.CSS_CELL, UIComponentDecorators.CSS_CLICKABLE);
        capacityCell.setMaxWidth(Double.MAX_VALUE);
        Label capacityLabel = ModalityStyle.badgeLightInfo(new Label(rc.getMax() != null ? String.valueOf(rc.getMax()) : "—"));
        capacityCell.getChildren().add(capacityLabel);
        capacityCell.setOnMouseClicked(e -> openRoomDialog(rc));
        GridPane.setFillWidth(capacityCell, true);
        table.add(capacityCell, 3, row);

        // Gender - centered in cell
        String gender = "—";
        Boolean allowsMale = rc.allowsMale();
        Boolean allowsFemale = rc.allowsFemale();
        if (Boolean.TRUE.equals(allowsMale) && Boolean.TRUE.equals(allowsFemale)) {
            gender = "◐"; // Mixed
        } else if (Boolean.TRUE.equals(allowsMale)) {
            gender = "♂";
        } else if (Boolean.TRUE.equals(allowsFemale)) {
            gender = "♀";
        }
        HBox genderCell = new HBox();
        genderCell.setAlignment(Pos.CENTER);
        genderCell.setPadding(cellPadding);
        genderCell.getStyleClass().addAll(UIComponentDecorators.CSS_CELL, UIComponentDecorators.CSS_CLICKABLE);
        genderCell.setMaxWidth(Double.MAX_VALUE);
        Label genderLabel = new Label(gender);
        genderCell.getChildren().add(genderLabel);
        genderCell.setOnMouseClicked(e -> openRoomDialog(rc));
        GridPane.setFillWidth(genderCell, true);
        table.add(genderCell, 4, row);

        // Notes
        HBox notesCell = new HBox();
        notesCell.setAlignment(Pos.CENTER_LEFT);
        notesCell.setPadding(cellPadding);
        notesCell.getStyleClass().addAll(UIComponentDecorators.CSS_CELL, UIComponentDecorators.CSS_CLICKABLE);
        notesCell.setMaxWidth(Double.MAX_VALUE);
        String comment = rc.getComment();
        Label notesLabel = new Label(comment != null && !comment.isEmpty() ? comment : "—");
        notesLabel.getStyleClass().add(UIComponentDecorators.CSS_BODY);
        notesLabel.setMaxWidth(200);
        notesCell.getChildren().add(notesLabel);
        notesCell.setOnMouseClicked(e -> openRoomDialog(rc));
        GridPane.setFillWidth(notesCell, true);
        table.add(notesCell, 5, row);
    }

    private static String getString(Resource resource) {
        String locationText = "—";
        if (resource != null) {
            BuildingZone zone = resource.getBuildingZone();
            Building building = resource.getBuilding();
            if (zone != null) {
                locationText = zone.getName();
                if (building != null) {
                    locationText = building.getName() + " — " + locationText;
                }
            } else if (building != null) {
                locationText = building.getName();
            }
        }
        return locationText;
    }

    private void openRoomDialog(ResourceConfiguration rc) {
        RoomDialog dialog = new RoomDialog(dataSourceModel, rc);
        DialogManager.openDialog(dialog, () -> {
            // Refresh the mapper to re-query the database with updated data
            if (resourceConfigRem != null) {
                resourceConfigRem.refreshWhenActive();
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
            // Load ResourceConfigurations (rooms with null dates = main site config)
            resourceConfigRem = ReactiveEntitiesMapper.<ResourceConfiguration>createPushReactiveChain(mixin)
                    .always("{class: 'ResourceConfiguration', alias: 'rc', fields: 'name,item.name,item.family.code,max,allowsGuest,allowsResident,allowsResidentFamily,allowsSpecialGuest,allowsVolunteer,allowsMale,allowsFemale,comment,resource.(name,site,building,buildingZone,building.name,buildingZone.name)'}")
                    .always(where("startDate is null and endDate is null"))
                    .always(orderBy("item.ord,resource.name"))
                    .always(pm.organizationIdProperty(), o -> where("resource.site=(select globalSite from Organization where id=?)",o))
                    .storeEntitiesInto(resourceConfigurations)
                    .addEntitiesHandler(entities -> {
                        // Force UI update when entities are received (after refresh)
                        Platform.runLater(() -> {
                            updateFilterChips();
                            updateRoomList();
                        });
                    })
                    .start();

            // Load Buildings
            buildingRem = ReactiveEntitiesMapper.<Building>createPushReactiveChain(mixin)
                    .always("{class: 'Building', fields: 'name,site'}")
                    .always(orderBy("name"))
                    .always(pm.organizationIdProperty(), o -> where("site.organization=?", o))
                    .storeEntitiesInto(buildings)
                    .setResultCacheEntry("modality/hotel/roomsetup/buildings")
                    .start();

            // Load BuildingZones
            buildingZoneRem = ReactiveEntitiesMapper.<BuildingZone>createPushReactiveChain(mixin)
                    .always("{class: 'BuildingZone', fields: 'name,building.name,building.site'}")
                    .always(orderBy("building.name,name"))
                    .always(pm.organizationIdProperty(), o -> where("building.site.organization=?", o))
                    .storeEntitiesInto(buildingZones)
                    .setResultCacheEntry("modality/hotel/roomsetup/building-zones")
                    .start();

            // Load accommodation Items (accommodation family, not deprecated, for current organization)
            itemRem = ReactiveEntitiesMapper.<Item>createPushReactiveChain(mixin)
                    .always("{class: 'Item', fields: 'name,code,ord,family.code'}")
                    .always(where("family.code=?", KnownItemFamily.ACCOMMODATION.getCode()))
                    .always(where("(deprecated is null or deprecated=false)"))
                    .always(pm.organizationIdProperty(), o -> where("organization=?", o))
                    .always(orderBy("ord,name"))
                    .storeEntitiesInto(accommodationItems)
                    .setResultCacheEntry("modality/hotel/roomsetup/items")
                    .start();

            // Bind to active property so refreshWhenActive() works
        }
        if (activeProperty != null) {
            resourceConfigRem.bindActivePropertyTo(activeProperty);
            buildingRem.bindActivePropertyTo(activeProperty);
            buildingZoneRem.bindActivePropertyTo(activeProperty);
            itemRem.bindActivePropertyTo(activeProperty);
        }
    }

    public ObservableList<Building> getBuildings() {
        return buildings;
    }

}
