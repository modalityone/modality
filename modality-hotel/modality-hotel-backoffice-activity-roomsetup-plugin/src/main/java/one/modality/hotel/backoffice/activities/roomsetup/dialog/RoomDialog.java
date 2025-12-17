package one.modality.hotel.backoffice.activities.roomsetup.dialog;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.type.PrimType;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.validation.ValidationSupport;
import dev.webfx.extras.visual.VisualColumn;
import dev.webfx.extras.visual.VisualResultBuilder;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.extras.controlfactory.button.ButtonFactoryMixin;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelector;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import javafx.scene.layout.*;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import one.modality.base.shared.entities.Building;
import one.modality.base.shared.entities.BuildingZone;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Resource;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.base.shared.entities.Site;
import one.modality.base.shared.entities.SiteItemFamily;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.crm.backoffice.organization.fx.FXOrganization;
import one.modality.hotel.backoffice.activities.roomsetup.RoomSetupI18nKeys;
import one.modality.hotel.backoffice.activities.roomsetup.util.UIComponentDecorators;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;

import java.util.stream.IntStream;

/**
 * Dialog for creating and editing rooms.
 * Creates/updates both Resource (physical room) and ResourceConfiguration (room settings with null dates).
 *
 * <p><b>Note:</b> This is a temporary implementation that will be moved elsewhere.
 * Current limitations:
 * <ul>
 *   <li>Uses Console.log for validation errors and debug messages (should use UI notifications)</li>
 *   <li>Inline CSS styling (should use CSS classes and Bootstrap/ModalityStyle helpers)</li>
 *   <li>Hardcoded text (should use I18nControls for translations)</li>
 * </ul>
 *
 * @author Claude Code
 */
public class RoomDialog implements DialogManager.ManagedDialog {

    private final DataSourceModel dataSourceModel;
    private final ResourceConfiguration existingConfig;

    // Form state
    private TextField nameField;
    private EntityButtonSelector<BuildingZone> zoneSelector;
    private EntityButtonSelector<Item> typeSelector;
    private ButtonSelector<Integer> capacitySelector;
    private ToggleGroup genderToggleGroup;
    private TextArea notesArea;

    private UpdateStore updateStore;
    private Runnable onSaveCallback;
    private final ValidationSupport validationSupport = new ValidationSupport();

    // Track if form has changes
    private final BooleanProperty hasChanges = new SimpleBooleanProperty(false);

    // Initial values for change tracking
    private String initialName;
    private BuildingZone initialZone;
    private Item initialType;
    private Integer initialCapacity;
    private String initialGender;
    private String initialNotes;

    public RoomDialog(DataSourceModel dataSourceModel,
                      ResourceConfiguration existingConfig) {
        this.dataSourceModel = dataSourceModel;
        this.existingConfig = existingConfig;
    }

    public Node buildView() {
        VBox container = new VBox();
        container.setSpacing(20);
        // More bottom padding to create space between notes and save button
        container.setPadding(new Insets(24, 24, 32, 24));
        container.setMinWidth(480);
        container.getStyleClass().add(UIComponentDecorators.CSS_DIALOG_CONTAINER);

        // Header
        HBox header = createHeader();

        // Form
        GridPane form = createForm();

        container.getChildren().addAll(header, form);

        // Set up autofill capacity from type (before populateForm so existing values take precedence)
        setupAutoFillCapacity();

        // Initialize with existing data if editing
        if (existingConfig != null) {
            populateForm();
        }

        // Set up change tracking after form is populated
        setupChangeTracking();

        // Set up validation for required fields
        setupValidation();

        return container;
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(12);
        header.setPadding(new Insets(0, 0, 16, 0));
        header.getStyleClass().add(UIComponentDecorators.CSS_DIALOG_HEADER);

        Label iconLabel = new Label(existingConfig != null ? "✏️" : "🏨");

        VBox titleBox = new VBox();
        titleBox.setSpacing(4);
        Label titleLabel = I18nControls.newLabel(existingConfig != null ? RoomSetupI18nKeys.RoomDialogEditTitle : RoomSetupI18nKeys.RoomDialogAddTitle);
        titleLabel.getStyleClass().add(UIComponentDecorators.CSS_TITLE);

        Label subtitleLabel = I18nControls.newLabel(existingConfig != null ?
                RoomSetupI18nKeys.RoomDialogEditSubtitle : RoomSetupI18nKeys.RoomDialogAddSubtitle);
        subtitleLabel.getStyleClass().add(UIComponentDecorators.CSS_SUBTITLE);

        titleBox.getChildren().addAll(titleLabel, subtitleLabel);
        header.getChildren().addAll(iconLabel, titleBox);

        return header;
    }

    /**
     * Creates the main form layout with all input fields.
     * The form uses a GridPane with two columns: labels (fixed width) and inputs (expanding).
     */
    private GridPane createForm() {
        GridPane form = new GridPane();
        form.setHgap(16);
        form.setVgap(16);

        int row = 0;
        row = addNameField(form, row);
        row = addZoneSelector(form, row);
        row = addTypeSelector(form, row);
        row = addCapacitySelector(form, row);
        row = addGenderSelector(form, row);
        addNotesField(form, row);

        configureFormColumnConstraints(form);
        return form;
    }

    private int addNameField(GridPane form, int row) {
        Label nameLabel = I18nControls.newLabel(RoomSetupI18nKeys.FieldName);
        nameLabel.getStyleClass().add(UIComponentDecorators.CSS_FIELD_LABEL);
        nameField = new TextField();
        nameField.setPromptText(I18n.getI18nText(RoomSetupI18nKeys.NamePlaceholder));
        nameField.getStyleClass().add(UIComponentDecorators.CSS_TEXT_FIELD);
        nameField.setPadding(new Insets(12, 14, 12, 14));
        form.add(nameLabel, 0, row);
        form.add(nameField, 1, row);
        return row + 1;
    }

    /**
     * Adds the zone selector dropdown, filtered by the current organization.
     * Note: Organization filter is embedded directly in the query string because
     * ifNotNullOtherwiseEmpty doesn't work correctly in dialog context.
     */
    private int addZoneSelector(GridPane form, int row) {
        Label zoneLabel = I18nControls.newLabel(RoomSetupI18nKeys.FieldZone);
        zoneLabel.getStyleClass().add(UIComponentDecorators.CSS_FIELD_LABEL);
        Object zoneOrgId = FXOrganization.getOrganization() != null ? FXOrganization.getOrganization().getPrimaryKey() : null;
        // columns: what to display, fields: additional data to load (building.site needed for save logic)
        String zoneQuery = "{class: 'BuildingZone', columns: 'name,building.name', fields: 'building,building.site', where: '" +
                (zoneOrgId != null ? "building.site.organization=" + zoneOrgId : "1=1") + "', orderBy: 'building.name,name'}";
        zoneSelector = new EntityButtonSelector<>(
                zoneQuery,
                new ButtonFactoryMixin() {}, FXMainFrameDialogArea::getDialogArea, dataSourceModel
        );
        zoneSelector.setShowMode(ButtonSelector.ShowMode.DROP_DOWN);
        zoneSelector.setSearchEnabled(false);
        Node zoneSelectorButton = zoneSelector.getButton();
        zoneSelectorButton.getStyleClass().add(UIComponentDecorators.CSS_TEXT_FIELD);
        form.add(zoneLabel, 0, row);
        form.add(zoneSelectorButton, 1, row);
        return row + 1;
    }

    /**
     * Adds the room type selector dropdown.
     * Filters by: accommodation family, not deprecated, current organization.
     * Search is disabled to allow limit of 100 items (search enabled limits to 6).
     */
    private int addTypeSelector(GridPane form, int row) {
        Label typeLabel = I18nControls.newLabel(RoomSetupI18nKeys.FieldRoomType);
        typeLabel.getStyleClass().add(UIComponentDecorators.CSS_FIELD_LABEL);
        Object orgId = FXOrganization.getOrganization() != null ? FXOrganization.getOrganization().getPrimaryKey() : null;
        String typeQuery = "{class: 'Item', fields: 'name,ord', where: 'family.code=`acco` and (deprecated is null or deprecated=false)" +
                (orgId != null ? " and organization=" + orgId : "") + "', orderBy: 'ord,name'}";
        typeSelector = new EntityButtonSelector<>(
                typeQuery,
                new ButtonFactoryMixin() {}, FXMainFrameDialogArea::getDialogArea, dataSourceModel
        );
        typeSelector.setShowMode(ButtonSelector.ShowMode.DROP_DOWN);
        typeSelector.setSearchEnabled(false);
        Node typeSelectorButton = typeSelector.getButton();
        typeSelectorButton.getStyleClass().add(UIComponentDecorators.CSS_TEXT_FIELD);
        form.add(typeLabel, 0, row);
        form.add(typeSelectorButton, 1, row);
        return row + 1;
    }

    private int addCapacitySelector(GridPane form, int row) {
        Label capacityLabel = I18nControls.newLabel(RoomSetupI18nKeys.FieldCapacity);
        capacityLabel.getStyleClass().add(UIComponentDecorators.CSS_FIELD_LABEL);
        capacitySelector = createCapacitySelector();
        Node capacitySelectorButton = capacitySelector.getButton();
        capacitySelectorButton.getStyleClass().add(UIComponentDecorators.CSS_TEXT_FIELD);
        form.add(capacityLabel, 0, row);
        form.add(capacitySelectorButton, 1, row);
        return row + 1;
    }

    private int addGenderSelector(GridPane form, int row) {
        Label genderLabel = I18nControls.newLabel(RoomSetupI18nKeys.FieldGender);
        genderLabel.getStyleClass().add(UIComponentDecorators.CSS_FIELD_LABEL);
        HBox genderBox = createGenderSelector();
        form.add(genderLabel, 0, row);
        form.add(genderBox, 1, row);
        return row + 1;
    }

    private void addNotesField(GridPane form, int row) {
        Label notesLabel = I18nControls.newLabel(RoomSetupI18nKeys.FieldNotes);
        notesLabel.getStyleClass().add(UIComponentDecorators.CSS_FIELD_LABEL);
        notesArea = new TextArea();
        notesArea.setPromptText(I18n.getI18nText(RoomSetupI18nKeys.NotesPlaceholder));
        notesArea.setPrefRowCount(3);
        notesArea.getStyleClass().add(UIComponentDecorators.CSS_TEXT_AREA);
        notesArea.setPadding(new Insets(12, 14, 12, 14));
        form.add(notesLabel, 0, row);
        form.add(notesArea, 1, row);
    }

    private void configureFormColumnConstraints(GridPane form) {
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(120);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(col1, col2);
    }

    private ButtonSelector<Integer> createCapacitySelector() {
        final int maxCapacity = 50;
        VisualResultBuilder vrb = VisualResultBuilder.create(maxCapacity, VisualColumn.create(null, PrimType.INTEGER));
        IntStream.range(0, maxCapacity).forEach(i -> vrb.setValue(i, 0, i + 1));
        VisualGrid capacityGrid = new VisualGrid(vrb.build());

        return new ButtonSelector<>(new ButtonFactoryMixin() {
        }, FXMainFrameDialogArea::getDialogArea) {
            {
                setSearchEnabled(false);
                setShowMode(ShowMode.DROP_DOWN);
                FXProperties.runOnPropertyChange(selection -> {
                    if (selection != null) {
                        setSelectedItem(selection.getSelectedRow() + 1);
                        closeDialog();
                    }
                }, capacityGrid.visualSelectionProperty());
            }

            @Override
            protected Region getOrCreateDialogContent() {
                return capacityGrid;
            }

            @Override
            protected Node getOrCreateButtonContentFromSelectedItem() {
                Integer selectedCapacity = getSelectedItem();
                return new Label(selectedCapacity != null ? String.valueOf(selectedCapacity) : I18n.getI18nText(RoomSetupI18nKeys.SelectCapacity));
            }

            @Override
            protected void startLoading() {
                // No loading needed - data is static
            }
        };
    }

    private HBox createGenderSelector() {
        HBox genderBox = new HBox();
        genderBox.setSpacing(8);

        genderToggleGroup = new ToggleGroup();

        ToggleButton mixedBtn = createGenderButton(RoomSetupI18nKeys.GenderMixed, "◐", "mixed");
        ToggleButton femaleBtn = createGenderButton(RoomSetupI18nKeys.GenderFemale, "♀", "female");
        ToggleButton maleBtn = createGenderButton(RoomSetupI18nKeys.GenderMale, "♂", "male");

        mixedBtn.setToggleGroup(genderToggleGroup);
        femaleBtn.setToggleGroup(genderToggleGroup);
        maleBtn.setToggleGroup(genderToggleGroup);

        // Default to mixed and apply selected styling
        mixedBtn.setSelected(true);
        UIComponentDecorators.applyGenderToggleSelectedStyle(mixedBtn);

        genderBox.getChildren().addAll(mixedBtn, femaleBtn, maleBtn);

        return genderBox;
    }

    private ToggleButton createGenderButton(Object i18nKey, String symbol, String userData) {
        ToggleButton btn = new ToggleButton(symbol + " " + I18n.getI18nText(i18nKey));
        btn.setUserData(userData);
        btn.getStyleClass().add(UIComponentDecorators.CSS_GENDER_TOGGLE);

        // Apply initial unselected styling (WebFX compatible)
        UIComponentDecorators.applyGenderToggleUnselectedStyle(btn);

        // Listen for selection changes and update styling dynamically
        btn.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            UIComponentDecorators.applyGenderToggleStyle(btn, isSelected);
        });

        return btn;
    }

    private void populateForm() {
        if (existingConfig == null) return;

        Resource resource = existingConfig.getResource();
        if (resource != null) {
            nameField.setText(resource.getName());

            // Load zone
            BuildingZone zone = resource.getBuildingZone();
            if (zone != null) {
                zoneSelector.setSelectedItem(zone);
            }
        }

        Item item = existingConfig.getItem();
        if (item != null) {
            typeSelector.setSelectedItem(item);
        }

        Integer capacity = existingConfig.getMax();
        if (capacity != null) {
            capacitySelector.setSelectedItem(capacity);
        }

        // Gender
        Boolean allowsMale = existingConfig.allowsMale();
        Boolean allowsFemale = existingConfig.allowsFemale();
        String gender = "mixed";
        if (Boolean.TRUE.equals(allowsMale) && !Boolean.TRUE.equals(allowsFemale)) {
            gender = "male";
        } else if (Boolean.TRUE.equals(allowsFemale) && !Boolean.TRUE.equals(allowsMale)) {
            gender = "female";
        }
        for (Toggle toggle : genderToggleGroup.getToggles()) {
            if (gender.equals(toggle.getUserData())) {
                toggle.setSelected(true);
                break;
            }
        }

        String comment = existingConfig.getComment();
        if (comment != null) {
            notesArea.setText(comment);
        }
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    public BooleanProperty hasChangesProperty() {
        return hasChanges;
    }

    private void setupChangeTracking() {
        // Store initial values
        if (existingConfig != null) {
            Resource resource = existingConfig.getResource();
            initialName = resource != null ? resource.getName() : "";
            initialZone = resource != null ? resource.getBuildingZone() : null;
            initialType = existingConfig.getItem();
            initialCapacity = existingConfig.getMax();
            Boolean allowsMale = existingConfig.allowsMale();
            Boolean allowsFemale = existingConfig.allowsFemale();
            if (Boolean.TRUE.equals(allowsMale) && Boolean.TRUE.equals(allowsFemale)) {
                initialGender = "mixed";
            } else if (Boolean.TRUE.equals(allowsMale)) {
                initialGender = "male";
            } else if (Boolean.TRUE.equals(allowsFemale)) {
                initialGender = "female";
            } else {
                initialGender = "mixed";
            }
            initialNotes = existingConfig.getComment() != null ? existingConfig.getComment() : "";
        } else {
            // New room - any field filled in is a change
            initialName = "";
            initialZone = null;
            initialType = null;
            initialCapacity = null;
            initialGender = "mixed";
            initialNotes = "";
        }

        // Listen to form field changes
        nameField.textProperty().addListener((obs, oldVal, newVal) -> checkForChanges());
        zoneSelector.selectedItemProperty().addListener((obs, oldVal, newVal) -> checkForChanges());
        typeSelector.selectedItemProperty().addListener((obs, oldVal, newVal) -> checkForChanges());
        capacitySelector.selectedItemProperty().addListener((obs, oldVal, newVal) -> checkForChanges());
        genderToggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> checkForChanges());
        notesArea.textProperty().addListener((obs, oldVal, newVal) -> checkForChanges());

        // Initial check
        checkForChanges();
    }

    private void setupValidation() {
        validationSupport.addRequiredInput(nameField);
        validationSupport.addRequiredInput(zoneSelector.selectedItemProperty(), zoneSelector.getButton());
        validationSupport.addRequiredInput(capacitySelector.selectedItemProperty(), capacitySelector.getButton());
    }

    private void checkForChanges() {
        String currentName = nameField.getText() != null ? nameField.getText() : "";
        BuildingZone currentZone = zoneSelector.getSelectedItem();
        Item currentType = typeSelector.getSelectedItem();
        Integer currentCapacity = capacitySelector.getSelectedItem();
        Toggle selectedGender = genderToggleGroup.getSelectedToggle();
        String currentGender = selectedGender != null ? (String) selectedGender.getUserData() : "mixed";
        String currentNotes = notesArea.getText() != null ? notesArea.getText() : "";

        boolean changed = !currentName.equals(initialName)
                || !java.util.Objects.equals(currentZone, initialZone)
                || !java.util.Objects.equals(currentType, initialType)
                || !java.util.Objects.equals(currentCapacity, initialCapacity)
                || !currentGender.equals(initialGender)
                || !currentNotes.equals(initialNotes);

        hasChanges.set(changed);
    }

    public boolean shouldSave() {
        // Always save when OK is clicked (internal buttons were removed, framework provides OK/Cancel)
        return true;
    }

    /**
     * Saves the room configuration to the database.
     *
     * <p>This method handles both creating new rooms and updating existing ones:
     * <ul>
     *   <li><b>New rooms:</b> Requires a zone selection. Queries for an existing SiteItemFamily
     *       for accommodation; creates one if not found. Then creates Resource and ResourceConfiguration.</li>
     *   <li><b>Existing rooms:</b> Updates the Resource and ResourceConfiguration directly
     *       without needing to query SiteItemFamily.</li>
     * </ul>
     *
     * <p>The building and site are derived from the selected zone. For existing rooms,
     * falls back to the existing resource's building/site if the zone's relations aren't loaded.
     *
     * @param dialogCallback Callback to close the dialog on successful save
     */
    public void save(DialogCallback dialogCallback) {
        // Validate required fields first
        if (!validationSupport.isValid()) {
            return;
        }

        try {
            BuildingZone selectedZone = zoneSelector.getSelectedItem();
            Building building = resolveBuildingFromZone(selectedZone);
            Site site = resolveSiteFromBuilding(building);

            // Additional validation for building/site relationships
            if (existingConfig == null) {
                if (building == null || site == null) {
                    Console.log("Zone has no valid building or site");
                    return;
                }
            }
            if (existingConfig != null) {
                performSave(dialogCallback, null, building);
            } else {
                querySiteItemFamilyAndSave(dialogCallback, site, building);
            }
        } catch (Exception e) {
            Console.log("Error in save: " + e.getMessage());
        }
    }

    /**
     * Resolves the building from the selected zone, falling back to existing resource if needed.
     */
    private Building resolveBuildingFromZone(BuildingZone selectedZone) {
        Building building = null;
        if (selectedZone != null) {
            building = selectedZone.getBuilding();
        }
        // Fall back to existing resource's building if zone's building isn't loaded
        if (building == null && existingConfig != null) {
            Resource existingResource = existingConfig.getResource();
            if (existingResource != null) {
                building = existingResource.getBuilding();
            }
        }
        return building;
    }

    /**
     * Resolves the site from the building, falling back to existing resource if needed.
     */
    private Site resolveSiteFromBuilding(Building building) {
        Site site = null;
        if (building != null) {
            site = building.getSite();
        }
        // Fall back to existing resource's site if building's site isn't loaded
        if (site == null && existingConfig != null) {
            Resource existingResource = existingConfig.getResource();
            if (existingResource != null) {
                if (building == null) {
                    building = existingResource.getBuilding();
                    if (building != null) {
                        site = building.getSite();
                    }
                }
                if (site == null) {
                    site = existingResource.getSite();
                }
            }
        }
        return site;
    }

    /**
     * Queries for existing SiteItemFamily and delegates to appropriate save method.
     * Creates a new SiteItemFamily if one doesn't exist for the site's accommodation family.
     */
    private void querySiteItemFamilyAndSave(DialogCallback dialogCallback, Site site, Building building) {
        EntityStore entityStore = EntityStore.create(dataSourceModel);
        entityStore.<SiteItemFamily>executeQuery(
                "select id from SiteItemFamily where site=? and itemFamily.code=?",
                site, KnownItemFamily.ACCOMMODATION.getCode()
        ).onFailure(error -> Console.log("Error querying SiteItemFamily: " + error.getMessage())).onSuccess(siteItemFamilies -> {
            if (siteItemFamilies.isEmpty()) {
                Console.log("No SiteItemFamily found for site and accommodation family. Creating one...");
                performSaveWithNewSiteItemFamily(dialogCallback, site, building);
            } else {
                SiteItemFamily sif = siteItemFamilies.get(0);
                Console.log("Found SiteItemFamily: " + sif.getId());
                performSave(dialogCallback, sif, building);
            }
        });
    }

    /**
     * Creates a new room with a new SiteItemFamily.
     * Used when no existing SiteItemFamily exists for the site's accommodation family.
     *
     * <p>Creates three entities in a single transaction:
     * <ol>
     *   <li>SiteItemFamily - Links the site to the accommodation item family</li>
     *   <li>Resource - The physical room with name, building, and zone</li>
     *   <li>ResourceConfiguration - Room settings (type, capacity, gender) with null dates for permanent config</li>
     * </ol>
     *
     * @param dialogCallback Callback to close dialog on success
     * @param site The site where the room is located
     * @param building The building containing the room
     */
    private void performSaveWithNewSiteItemFamily(DialogCallback dialogCallback, Site site, Building building) {
        updateStore = UpdateStore.create(dataSourceModel);

        // Create SiteItemFamily for accommodation
        SiteItemFamily sif = updateStore.insertEntity(SiteItemFamily.class);
        sif.setSite(site);
        sif.setItemFamily(KnownItemFamily.ACCOMMODATION.getPrimaryKey());

        // Create the resource linked to the new SiteItemFamily
        Resource resource = updateStore.insertEntity(Resource.class);
        resource.setName(nameField.getText());
        resource.setSite(site);
        resource.setSiteItemFamily(sif);
        resource.setBuilding(building);

        BuildingZone selectedZone = zoneSelector.getSelectedItem();
        if (selectedZone != null) {
            resource.setBuildingZone(selectedZone);
        }

        // Create ResourceConfiguration with null dates (permanent configuration)
        ResourceConfiguration config = updateStore.insertEntity(ResourceConfiguration.class);
        config.setResource(resource);
        config.setStartDate(null);
        config.setEndDate(null);

        populateConfig(config);
        submitChangesAndClose(dialogCallback, "Room saved successfully (with new SiteItemFamily)");
    }

    /**
     * Saves a room using an existing SiteItemFamily or updates an existing room.
     *
     * <p>For new rooms: Creates Resource and ResourceConfiguration entities.
     * <p>For existing rooms: Updates the existing Resource and ResourceConfiguration.
     *
     * @param dialogCallback Callback to close dialog on success
     * @param siteItemFamily The SiteItemFamily to link new resources to (null for updates)
     * @param building The building containing the room
     */
    private void performSave(DialogCallback dialogCallback, SiteItemFamily siteItemFamily, Building building) {
        updateStore = UpdateStore.create(dataSourceModel);

        Resource resource;
        ResourceConfiguration config;

        if (existingConfig != null) {
            resource = updateStore.updateEntity(existingConfig.getResource());
            config = updateStore.updateEntity(existingConfig);
        } else {
            resource = updateStore.insertEntity(Resource.class);
            resource.setSiteItemFamily(siteItemFamily);

            config = updateStore.insertEntity(ResourceConfiguration.class);
            config.setResource(resource);
            config.setStartDate(null);
            config.setEndDate(null);
        }

        // Update Resource properties
        resource.setName(nameField.getText());
        if (building != null) {
            resource.setBuilding(building);
            if (building.getSite() != null) {
                resource.setSite(building.getSite());
            }
        }

        BuildingZone selectedZone = zoneSelector.getSelectedItem();
        if (selectedZone != null) {
            resource.setBuildingZone(selectedZone);
        }

        populateConfig(config);
        submitChangesAndClose(dialogCallback, "Room saved successfully");
    }

    /**
     * Submits pending changes to the database and closes the dialog on success.
     */
    private void submitChangesAndClose(DialogCallback dialogCallback, String successMessage) {
        updateStore.submitChanges()
                .onFailure(error -> Console.log("Error saving room: " + error.getMessage()))
                .onSuccess(result -> {
                    Console.log(successMessage);
                    if (onSaveCallback != null) {
                        onSaveCallback.run();
                    }
                    dialogCallback.closeDialog();
                });
    }

    /**
     * Populates the ResourceConfiguration with form field values.
     * Sets room type, capacity, gender restrictions, and notes.
     *
     * @param config The ResourceConfiguration to populate
     */
    private void populateConfig(ResourceConfiguration config) {
        Item selectedType = typeSelector.getSelectedItem();
        if (selectedType != null) {
            config.setItem(selectedType);
        }

        Integer capacity = capacitySelector.getSelectedItem();
        if (capacity != null) {
            config.setMax(capacity);
        }

        applyGenderSettings(config);
        config.setComment(notesArea.getText());
    }

    /**
     * Applies gender restriction settings to the configuration based on toggle selection.
     * Mixed allows both genders, otherwise restricts to selected gender only.
     */
    private void applyGenderSettings(ResourceConfiguration config) {
        Toggle selectedGender = genderToggleGroup.getSelectedToggle();
        if (selectedGender == null) {
            return;
        }

        String gender = (String) selectedGender.getUserData();
        switch (gender) {
            case "mixed":
                config.setAllowsMale(true);
                config.setAllowsFemale(true);
                break;
            case "male":
                config.setAllowsMale(true);
                config.setAllowsFemale(false);
                break;
            case "female":
                config.setAllowsMale(false);
                config.setAllowsFemale(true);
                break;
        }
    }

    /**
     * Sets up autofill of capacity when a room type is selected.
     * Only applies to new rooms (not when editing) and only when capacity hasn't been set yet.
     */
    private void setupAutoFillCapacity() {
        typeSelector.selectedItemProperty().addListener((obs, oldType, newType) -> {
            // Only autofill for new rooms when capacity hasn't been set
            if (existingConfig == null && capacitySelector.getSelectedItem() == null && newType != null) {
                String typeName = newType.getName();
                Integer inferredCapacity = inferCapacityFromTypeName(typeName);
                if (inferredCapacity != null) {
                    capacitySelector.setSelectedItem(inferredCapacity);
                }
            }
        });
    }

    /**
     * Infers the room capacity from the type name by looking for common keywords.
     * Uses case-insensitive matching to handle various naming conventions.
     *
     * <p>Note: This approach may not work well with translated type names.
     * A more robust solution would be to store default capacity on the Item entity.
     *
     * @param typeName The room type name (e.g., "Single Room", "Double", "Twin Deluxe")
     * @return The inferred capacity, or null if no keywords match
     */
    private Integer inferCapacityFromTypeName(String typeName) {
        if (typeName == null) {
            return null;
        }
        String lowerName = typeName.toLowerCase();

        // Check for specific capacity keywords (order matters - check larger capacities first)
        if (lowerName.contains("quad") || lowerName.contains("family") || lowerName.contains("quadruple")) {
            return 4;
        }
        if (lowerName.contains("triple") || lowerName.contains("trio")) {
            return 3;
        }
        if (lowerName.contains("double") || lowerName.contains("twin") || lowerName.contains("duo")) {
            return 2;
        }
        if (lowerName.contains("single") || lowerName.contains("solo")) {
            return 1;
        }

        return null; // No keyword matched - let user select manually
    }
}
