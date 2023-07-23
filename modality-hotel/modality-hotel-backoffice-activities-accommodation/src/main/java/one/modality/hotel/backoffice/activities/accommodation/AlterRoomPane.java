package one.modality.hotel.backoffice.activities.accommodation;

import dev.webfx.extras.visual.VisualSelection;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import dev.webfx.stack.routing.activity.impl.elementals.activeproperty.HasActiveProperty;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Resource;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;
import one.modality.hotel.backoffice.accommodation.AccommodationPresentationModel;
import one.modality.hotel.backoffice.accommodation.AttendeeCategory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.webfx.stack.orm.dql.DqlStatement.orderBy;
import static dev.webfx.stack.orm.dql.DqlStatement.where;

public class AlterRoomPane extends VBox {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-uu");

    private final AccommodationPresentationModel pm;
    private final ObjectProperty<ResourceConfiguration> resourceConfigurationProperty = new SimpleObjectProperty<>();
    public ObjectProperty<ResourceConfiguration> resourceConfigurationProperty() { return resourceConfigurationProperty; }
    private final ObjectProperty<ResourceConfiguration>  selectedResourceConfigurationProperty = new SimpleObjectProperty<>();
    private final ObservableList<ResourceConfiguration> resourceConfigurations = FXCollections.observableArrayList();

    private ButtonFactoryMixin mixin;
    private ObservableValue<Boolean> activeProperty;
    private ReactiveVisualMapper<ResourceConfiguration> rvm;
    private UpdateStore updateStore;

    private GridPane detailsGridPane;
    private EntityButtonSelector<Item> roomTypeSelector;
    private TextField roomNameTextField;
    private ComboBox<Integer> bedsInRoomComboBox;
    private Map<AttendeeCategory, CheckBox> attendeeCategoryCheckBoxMap = new HashMap<>();
    private CheckBox allowsFemaleCheckBox;
    private CheckBox allowsMaleCheckBox;
    private TextField fromDateField;
    private TextField toDateField;
    private VisualGrid table;

    private Button createButton;
    private Button updateButton;
    private Button deleteButton;
    private Button deleteRoomButton;
    private Button saveButton;
    private Label statusLabel;


    public AlterRoomPane(AccommodationPresentationModel pm) {
        this.pm = pm;

        resourceConfigurations.addListener((ListChangeListener<ResourceConfiguration>) change -> {
            // Select the configuration applicable today
            LocalDate tomorrow = LocalDate.now().plus(1, ChronoUnit.DAYS);
            ResourceConfiguration todayResourceConfiguration = findLatestResourceConfigurationBeforeDate(tomorrow);
            int rowIndex = resourceConfigurations.indexOf(todayResourceConfiguration);
            table.setVisualSelection(VisualSelection.createBuilder().addSelectedRow(rowIndex).build());
        });
        selectedResourceConfigurationProperty.addListener((observable, oldValue, newValue) -> displayDetails(newValue));
        HBox detailsRow = new HBox(createHeadingLabel("Details"), createDetailsGrid());
        Label availabilityLabel = createHeadingLabel("Availability");

        table = new VisualGrid();
        createButton = new Button("Create");
        createButton.setOnAction(e -> create());
        updateButton = new Button("Update");
        updateButton.setOnAction(e -> update());
        deleteButton = new Button("Delete");
        deleteRoomButton = new Button("Delete room");
        saveButton = new Button("Save");
        saveButton.setOnAction(e -> save());
        saveButton.setVisible(false);

        HBox buttonPane = new HBox(createButton, updateButton, deleteButton, deleteRoomButton, saveButton);

        statusLabel = new Label();

        getChildren().addAll(detailsRow, availabilityLabel, table, buttonPane, statusLabel);
    }

    private GridPane createDetailsGrid() {
        roomNameTextField = new TextField();
        roomNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (selectedResourceConfigurationProperty.get() != null) {
                selectedResourceConfigurationProperty.get().setName(newValue);
            }
        });
        bedsInRoomComboBox = createBedsInRoomComboBox();
        bedsInRoomComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (selectedResourceConfigurationProperty.get() != null && newValue != null) {
                selectedResourceConfigurationProperty.get().setMax(newValue);
            }
        });
        GridPane eligibilityForBookingGrid = createEligibilityForBookingGrid();
        fromDateField = new TextField();
        fromDateField.setPromptText("e.g. 15-01-22");
        fromDateField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (selectedResourceConfigurationProperty.get() != null) {
                LocalDate startDate = dateFromText(newValue);
                selectedResourceConfigurationProperty.get().setStartDate(startDate);
            }
        });
        toDateField = new TextField();
        toDateField.setPromptText("e.g. 16-01-22");
        toDateField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (selectedResourceConfigurationProperty.get() != null) {
                LocalDate endDate = dateFromText(newValue);
                selectedResourceConfigurationProperty.get().setEndDate(endDate);
            }
        });

        detailsGridPane = new GridPane();
        detailsGridPane.add(createLabel("Product"), 0, 0);
        detailsGridPane.add(createLabel("Name"), 0, 1);
        detailsGridPane.add(roomNameTextField, 1, 1);
        detailsGridPane.add(createLabel("Beds in the room"), 0, 2);
        detailsGridPane.add(bedsInRoomComboBox, 1, 2);
        detailsGridPane.add(createLabel("Eligibility for booking"), 0, 3);
        detailsGridPane.add(eligibilityForBookingGrid, 1, 3);
        detailsGridPane.add(createLabel("From / To"), 0, 4);
        detailsGridPane.add(new HBox(fromDateField, toDateField), 1, 4);
        setDetailsPaneDisabled(true);
        return detailsGridPane;
    }

    private static LocalDate dateFromText(String text) {
        try {
            return LocalDate.parse(text, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private ComboBox<Integer> createBedsInRoomComboBox() {
        final int maxBedsInRoom = 50;
        List<Integer> items = new ArrayList<>();
        for (int i = 1; i <= maxBedsInRoom; i++) {
            items.add(i);
        }
        return new ComboBox<>(FXCollections.observableList(items));
    }

    private GridPane createEligibilityForBookingGrid() {
        GridPane gridPane = new GridPane();
        final int numColumns = 3;
        int columnIndex = 0;
        int rowIndex = 0;
        for (AttendeeCategory attendeeCategory : AttendeeCategory.values()) {
            if (columnIndex >= numColumns) {
                columnIndex = 0;
                rowIndex++;
            }
            CheckBox checkBox = new CheckBox(attendeeCategory.getText());
            checkBox.selectedProperty().addListener(e -> setAllowsAttendanceCategory(attendeeCategory, checkBox.isSelected()));
            attendeeCategoryCheckBoxMap.put(attendeeCategory, checkBox);
            gridPane.add(checkBox, columnIndex, rowIndex);
            columnIndex++;
        }
        allowsFemaleCheckBox = new CheckBox("Female");
        allowsMaleCheckBox = new CheckBox("Male");
        allowsFemaleCheckBox.selectedProperty().addListener(e -> selectedResourceConfigurationProperty.get().setAllowsFemale(allowsFemaleCheckBox.isSelected()));
        allowsMaleCheckBox.selectedProperty().addListener(e -> selectedResourceConfigurationProperty.get().setAllowsMale(allowsMaleCheckBox.isSelected()));
        gridPane.add(allowsFemaleCheckBox, 0, rowIndex + 1);
        gridPane.add(allowsMaleCheckBox, 1, rowIndex + 1);
        return gridPane;
    }

    private boolean allowsAttendanceCategory(AttendeeCategory attendeeCategory) {
        return allowsAttendanceCategory(selectedResourceConfigurationProperty.get(), attendeeCategory);
    }

    private boolean allowsAttendanceCategory(ResourceConfiguration rc, AttendeeCategory attendeeCategory) {
        switch (attendeeCategory) {
            case GUEST: return rc.allowsGuest();
            case RESIDENT: return rc.allowsResident();
            case RESIDENTS_FAMILY: return rc.allowsResidentFamily();
            case SPECIAL_GUEST: return rc.allowsSpecialGuest();
            case VOLUNTEER: return rc.allowsVolunteer();
            default: return false;
        }
    }

    private void setAllowsAttendanceCategory(AttendeeCategory attendeeCategory, boolean allowed) {
        ResourceConfiguration rc = selectedResourceConfigurationProperty.get();
        switch (attendeeCategory) {
            case GUEST: rc.setAllowsGuest(allowed); break;
            case RESIDENT: rc.setAllowsResident(allowed); break;
            case RESIDENTS_FAMILY: rc.setAllowsResidentFamily(allowed); break;
            case SPECIAL_GUEST: rc.setAllowsSpecialGuest(allowed); break;
            case VOLUNTEER: rc.setAllowsVolunteer(allowed); break;
        }
    }

    private void displayDetails(ResourceConfiguration rc) {
        setDetailsPaneDisabled(true);
        if (roomTypeSelector == null) {
            // This can be created once we have an entity to get the data source model from
            createProductComboBox();
        }
        roomTypeSelector.setSelectedItem(rc.getItem());
        roomNameTextField.setText(rc.getName());
        bedsInRoomComboBox.setValue(rc.getMax());

        for (AttendeeCategory attendeeCategory : AttendeeCategory.values()) {
            CheckBox checkBox = attendeeCategoryCheckBoxMap.get(attendeeCategory);
            boolean selected = allowsAttendanceCategory(attendeeCategory);
            checkBox.setSelected(selected);
        }
        allowsFemaleCheckBox.setSelected(rc.allowsFemale());
        allowsMaleCheckBox.setSelected(rc.allowsMale());
    }

    private void createProductComboBox() {
        roomTypeSelector = new EntityButtonSelector<Item>(
                "{class: 'Item', alias: 'i', where: 'family.code=`acco`'}",
                mixin, this, selectedResourceConfigurationProperty.get().getStore().getDataSourceModel()
        )
                .always(FXOrganizationId.organizationIdProperty(), orgId -> DqlStatement.where("exists(select ScheduledResource where configuration.(item=i and resource.site.organization=?))", Entities.getPrimaryKey(orgId)))
                .setAutoOpenOnMouseEntered(true)
                .appendNullEntity(true);
        roomTypeSelector.setReadOnly(true);
        detailsGridPane.add(roomTypeSelector.getButton(), 1, 0);
    }

    private Label createLabel(String text) {
        return new Label(text);
    }

    private Label createHeadingLabel(String text) {
        return new Label(text);
    }

    private void create() {
        ResourceConfiguration previousRc = selectedResourceConfigurationProperty.get();
        Resource resource = previousRc.getResource();

        updateStore = UpdateStore.create(previousRc.getStore().getDataSourceModel());
        ResourceConfiguration newRc = updateStore.insertEntity(ResourceConfiguration.class);
        newRc.setResource(resource);
        newRc.setItem(previousRc.getItem());
        newRc.setName(previousRc.getName());
        newRc.setMax(previousRc.getMax());
        newRc.setAllowsGuest(previousRc.allowsGuest());
        newRc.setAllowsResident(previousRc.allowsResident());
        newRc.setAllowsResidentFamily(previousRc.allowsResidentFamily());
        newRc.setAllowsSpecialGuest(previousRc.allowsSpecialGuest());
        newRc.setAllowsVolunteer(previousRc.allowsVolunteer());
        newRc.setAllowsFemale(previousRc.allowsFemale());
        newRc.setAllowsMale(previousRc.allowsMale());
        selectedResourceConfigurationProperty.set(newRc);
        setDetailsPaneDisabled(false);
        saveButton.setVisible(true);
    }

    private void update() {
        setDetailsPaneDisabled(false);
        saveButton.setVisible(true);
    }

    private void save() {
        boolean isNewRecord = ((Integer) selectedResourceConfigurationProperty.get().getPrimaryKey()) < 0;
        if (isNewRecord) {
            saveCreate();
        } else {
            saveUpdate();
        }
    }

    private void saveCreate() {
        ResourceConfiguration newRc = selectedResourceConfigurationProperty.get();
        if (newRc == null) {
            return;
        }
        LocalDate fromDate = newRc.getStartDate();
        if (fromDate == null) {
            displayStatus("Not saved. Please enter from date in the format dd-mm-yy.");
            return;
        }

        ResourceConfiguration latestResourceConfigurationBeforeNewOne = findLatestResourceConfigurationBeforeDate(fromDate);
        LocalDate latestEndDate = latestResourceConfigurationBeforeNewOne.getEndDate();
        if (latestEndDate == null || latestEndDate.until(fromDate, ChronoUnit.DAYS) > 1) {
            // Perform an update if necessary to ensure there are no gaps in dates across all records for this resource
            UpdateStore otherUpdateStore = UpdateStore.createAbove(updateStore);
            ResourceConfiguration updatedLatestResourceConfiguration = otherUpdateStore.updateEntity(latestResourceConfigurationBeforeNewOne);
            updatedLatestResourceConfiguration.setEndDate(fromDate.minus(1, ChronoUnit.DAYS));
            otherUpdateStore.submitChanges()
                    .onFailure(e -> displayStatus("Not saved. " + e.getMessage()))
                    .onSuccess(b -> {
                        updateStore.submitChanges()
                                .onFailure(e -> displayStatus("Not saved. " + e.getMessage()))
                                .onSuccess(b2 -> displayStatus("Saved."));
                    });
        } else {
            // If no update needed then save the new entity
            updateStore.submitChanges()
                    .onFailure(e -> displayStatus("Not saved. " + e.getMessage()))
                    .onSuccess(b2 -> displayStatus("Saved."));
        }
    }

    private void displayStatus(String status) {
        Platform.runLater(() -> statusLabel.setText(status));
    }

    private ResourceConfiguration findLatestResourceConfigurationBeforeDate(LocalDate date) {
        ResourceConfiguration resourceConfigurationWithoutDates = null;
        for (ResourceConfiguration rc : resourceConfigurations) {
            if (rc.getStartDate() != null && rc.getStartDate().isBefore(date)) {
                if (rc.getEndDate() == null || !rc.getEndDate().isBefore(date)) {
                    return rc;
                }
            }

            if (rc.getStartDate() == null && rc.getEndDate() == null) {
                resourceConfigurationWithoutDates = rc;
            }
        }
        return resourceConfigurationWithoutDates;
    }

    private void saveUpdate() {
        // TODO
    }

    private void setDetailsPaneDisabled(boolean disabled) {
        if (roomTypeSelector != null) {
            roomTypeSelector.setReadOnly(disabled);
        }
        roomNameTextField.setDisable(disabled);
        bedsInRoomComboBox.setDisable(disabled);
        attendeeCategoryCheckBoxMap.values().forEach(checkBox -> checkBox.setDisable(disabled));
        allowsFemaleCheckBox.setDisable(disabled);
        allowsMaleCheckBox.setDisable(disabled);
        fromDateField.setDisable(disabled);
        toDateField.setDisable(disabled);
    }

    public void startLogic(Object mixin) { // may be called several times with different mixins (due to workaround)
        if (mixin instanceof ButtonFactoryMixin) {
            this.mixin = (ButtonFactoryMixin) mixin;
        }
        // Updating the active property with a OR => mixin1.active || mixin2.active || mixin3.active ...
        if (mixin instanceof HasActiveProperty) {
            ObservableValue<Boolean> ap = ((HasActiveProperty) mixin).activeProperty();
            if (activeProperty == null)
                activeProperty = ap;
            else
                activeProperty = FXProperties.combine(activeProperty, ap, (a1, a2) -> a1 || a2);
        }
        if (rvm == null) { // first call
            rvm = ReactiveVisualMapper.<ResourceConfiguration>createPushReactiveChain(mixin)
                    .always("{class: 'ResourceConfiguration', alias: 'rc', fields: 'name,item.name,max,allowsGuest,allowsResident,allowsResidentFamily,allowsSpecialGuest,allowsVolunteer,allowsFemale,allowsMale,startDate,endDate'}")
                    .always(orderBy("item.ord,name"))
                    .setEntityColumns("[" +
                            "{label: 'Name', expression: 'name'}," +
                            "{label: 'Product', expression: 'item.name'}," +
                            "{label: 'Beds', expression: 'max'}," +
                            //"{label: 'Eligible', expression: 'name'}," +
                            "{label: 'From', expression: 'startDate'}," +
                            "{label: 'To', expression: 'endDate'}" +
                            "]")
                    .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), o -> where("resource.site.(organization=? and event=null)", o))
                    .ifNotNullOtherwiseEmpty(resourceConfigurationProperty, rc -> where("resource = ?", rc.getResource()))
                    .applyDomainModelRowStyle()
                    .autoSelectSingleRow()
                    .visualizeResultInto(table)
                    .storeEntitiesInto(resourceConfigurations)
                    .setSelectedEntityHandler(selectedResourceConfigurationProperty::setValue)
                    .start();

        } else if (activeProperty != null) { // subsequent calls
            rvm.bindActivePropertyTo(activeProperty); // updating the reactive entities mapper active property
        }
    }
}
