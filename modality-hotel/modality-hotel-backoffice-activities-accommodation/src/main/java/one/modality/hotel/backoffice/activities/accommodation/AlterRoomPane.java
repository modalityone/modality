package one.modality.hotel.backoffice.activities.accommodation;

import dev.webfx.extras.type.PrimType;
import dev.webfx.extras.visual.VisualColumn;
import dev.webfx.extras.visual.VisualResultBuilder;
import dev.webfx.extras.visual.VisualSelection;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelector;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import dev.webfx.stack.routing.activity.impl.elementals.activeproperty.HasActiveProperty;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import dev.webfx.stack.ui.dialog.DialogCallback;
import dev.webfx.stack.ui.controls.dialog.DialogContent;
import dev.webfx.stack.ui.controls.dialog.DialogBuilderUtil;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

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
    private ButtonSelector<Integer> bedsInRoomButtonSelector;
    private final Map<AttendeeCategory, CheckBox> attendeeCategoryCheckBoxMap = new HashMap<>();
    private CheckBox allowsFemaleCheckBox;
    private CheckBox allowsMaleCheckBox;
    private TextField fromDateField;
    private TextField toDateField;
    private VisualGrid table;

    private final Button createButton;
    private final Button updateButton;
    private final Button deleteButton;
    private final Button deleteRoomButton;
    private final Button saveButton;
    private final Button cancelButton;
    private final Label statusLabel;

    public AlterRoomPane(AccommodationPresentationModel pm, ButtonFactoryMixin mixin) {
        this.pm = pm;
        this.mixin = mixin;

        resourceConfigurations.addListener((ListChangeListener<ResourceConfiguration>) change -> {
            // Select the configuration applicable today
            displayStatus(null);
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
        deleteButton.setOnAction(e -> confirmDelete());
        deleteRoomButton = new Button("Delete room");
        saveButton = new Button("Save");
        saveButton.setOnAction(e -> save());
        saveButton.setVisible(false);
        cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> cancel());
        cancelButton.setVisible(false);

        HBox buttonPane = new HBox(createButton, updateButton, deleteButton, deleteRoomButton, saveButton, cancelButton);

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
        bedsInRoomButtonSelector = createBedsInRoomButtonSelector();
        bedsInRoomButtonSelector.selectedItemProperty().addListener((observable, oldValue, newValue) -> {
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
        detailsGridPane.add(bedsInRoomButtonSelector.getButton(), 1, 2);
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
        } catch (DateTimeParseException | NullPointerException e) {
            return null;
        }
    }

    private ButtonSelector<Integer> createBedsInRoomButtonSelector() {
        final int maxBedsInRoom = 50;
        VisualResultBuilder vrb = VisualResultBuilder.create(maxBedsInRoom, VisualColumn.create(null, PrimType.INTEGER));
        IntStream.range(0, maxBedsInRoom).forEach(i -> vrb.setValue(i, 0, i + 1));
        VisualGrid bedsGrid = new VisualGrid(vrb.build());
        return new ButtonSelector<>(mixin, this) {

            {
                setSearchEnabled(false);
                setShowMode(ShowMode.DROP_DOWN);
                bedsGrid.visualSelectionProperty().addListener((observable, oldValue, newValue) -> {
                    setSelectedItem(newValue.getSelectedRow() + 1);
                    closeDialog();
                });
            }

            @Override
            protected Node getOrCreateButtonContentFromSelectedItem() {
                return new Label(String.valueOf(getSelectedItem()));
            }

            @Override
            protected void startLoading() {}

            @Override
            protected Region getOrCreateDialogContent() {
                return bedsGrid;
            }
        };
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
        bedsInRoomButtonSelector.setSelectedItem(rc.getMax());

        for (AttendeeCategory attendeeCategory : AttendeeCategory.values()) {
            CheckBox checkBox = attendeeCategoryCheckBoxMap.get(attendeeCategory);
            boolean selected = allowsAttendanceCategory(attendeeCategory);
            checkBox.setSelected(selected);
        }
        allowsFemaleCheckBox.setSelected(rc.allowsFemale());
        allowsMaleCheckBox.setSelected(rc.allowsMale());

        fromDateField.setText(rc.getStartDate() != null ? DATE_FORMATTER.format(rc.getStartDate()) : null);
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
        newRc.setStartDate(previousRc.getEndDate() != null ? previousRc.getEndDate().plus(1, ChronoUnit.DAYS) : LocalDate.now());
        selectedResourceConfigurationProperty.set(newRc);
        setDetailsPaneDisabled(false);
        table.setDisable(true);
        saveButton.setVisible(true);
        cancelButton.setVisible(true);
    }

    private void update() {
        setDetailsPaneDisabled(false);
        table.setDisable(true);
        saveButton.setVisible(true);
        cancelButton.setVisible(true);
    }

    private void confirmDelete() {
        if (resourceConfigurations.size() < 2) {
            String msg = "This is the only configuration for this resource. It cannot be deleted.";
            DialogContent dialogContent = new DialogContent().setContentText(msg);
            dialogContent.getCancelButton().setVisible(false);
            DialogBuilderUtil.showModalNodeInGoldLayout(dialogContent, this);
            DialogBuilderUtil.armDialogContentButtons(dialogContent, DialogCallback::closeDialog);
        } else {
            String msg = "Are you sure you wish to delete the selected resource configuration?";
            DialogContent dialogContent = new DialogContent().setContentText(msg).setYesNo();
            DialogBuilderUtil.showModalNodeInGoldLayout(dialogContent, this);
            DialogBuilderUtil.armDialogContentButtons(dialogContent, dialogCallback -> {
                delete();
                dialogCallback.closeDialog();
            });
        }
    }

    private void delete() {
        // Ensure there are no time gaps
        ResourceConfiguration toDelete = selectedResourceConfigurationProperty.get();
        UpdateStore deleteUpdateStore = UpdateStore.createAbove(toDelete.getStore());
        if (resourceConfigurations.size() == 2) {
            // If there would only be one resource configuration left once this is delete then set its start and end dates to null
            int otherIndex = 1 - resourceConfigurations.indexOf(toDelete);
            ResourceConfiguration otherResourceConfiguration = resourceConfigurations.get(otherIndex);
            ResourceConfiguration toUpdate = deleteUpdateStore.updateEntity(otherResourceConfiguration);
            toUpdate.setStartDate(null);
            toUpdate.setEndDate(null);
        } else {
            if (toDelete.getEndDate() == null) {
                // If the configuration to delete has no end date then set the end date of the latest resource before it to null
                ResourceConfiguration latestResourceConfiguration = resourceConfigurations.stream()
                        .filter(rc -> !rc.equals(toDelete))
                        .filter(rc -> rc.getStartDate() != null)
                        .max(Comparator.comparing(ResourceConfiguration::getStartDate))
                        .get();
                ResourceConfiguration toUpdate = deleteUpdateStore.updateEntity(latestResourceConfiguration);
                toUpdate.setEndDate(null);
            }
            if (toDelete.getStartDate() == null) {
                // If the configuration to delete has no start date then set the start date of the earliest resource after it to null
                ResourceConfiguration earliestResourceConfiguration = resourceConfigurations.stream()
                        .filter(rc -> !rc.equals(toDelete))
                        .min(Comparator.comparing(ResourceConfiguration::getStartDate))
                        .get();
                ResourceConfiguration toUpdate = deleteUpdateStore.updateEntity(earliestResourceConfiguration);
                toUpdate.setStartDate(null);
            }
        }

        // Delete the resource configuration
        deleteUpdateStore.deleteEntity(toDelete);
        deleteUpdateStore.submitChanges()
                .onFailure(e -> displayStatus("Not saved. " + e.getMessage()))
                .onSuccess(b -> displayStatus("Saved."));
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
        boolean duplicateStartDate = resourceConfigurations.stream()
                .anyMatch(rc -> rc.getStartDate() != null && rc.getStartDate().equals(fromDate));
        if (duplicateStartDate) {
            displayStatus("Not saved. A resource configuration with this from date already exists.");
            return;
        }

        // Perform updates if necessary to ensure there are no gaps in dates across all configurations for this resource
        UpdateStore otherUpdateStore = UpdateStore.createAbove(updateStore);
        ResourceConfiguration latestResourceConfigurationBeforeNewOne = findLatestResourceConfigurationBeforeDate(fromDate);
        LocalDate latestEndDate = latestResourceConfigurationBeforeNewOne.getEndDate();
        if (latestEndDate == null || latestEndDate.isAfter(fromDate)) {
            // Ensure the resource configuration immediately before the new one ends the day before the new one begins
            ResourceConfiguration updatedLatestResourceConfiguration = otherUpdateStore.updateEntity(latestResourceConfigurationBeforeNewOne);
            updatedLatestResourceConfiguration.setEndDate(fromDate.minus(1, ChronoUnit.DAYS));
        }

        Optional<ResourceConfiguration> resourceConfigurationImmediatelyAfterNewOne = resourceConfigurations.stream()
                .filter(rc -> rc.getStartDate() != null && rc.getStartDate().isAfter(fromDate))
                .min(Comparator.comparing(ResourceConfiguration::getStartDate));
        if (resourceConfigurationImmediatelyAfterNewOne.isPresent()) {
            // If there is a resource configuration with a start date later than the one entered then set the end date of this one
            // to the day before it starts
            LocalDate endDate = resourceConfigurationImmediatelyAfterNewOne.get().getStartDate().minus(1, ChronoUnit.DAYS);
            newRc.setEndDate(endDate);
        }

        // Submit changes
        otherUpdateStore.submitChanges()
                .onFailure(e -> displayStatus("Not saved. " + e.getMessage()))
                .onSuccess(b -> {
                    updateStore.submitChanges()
                            .onFailure(e -> displayStatus("Not saved. " + e.getMessage()))
                            .onSuccess(b2 -> displayStatus("Saved."));
                });
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

    private void cancel() {
        saveButton.setVisible(false);
        cancelButton.setVisible(false);
        setDetailsPaneDisabled(true);
        table.setDisable(false);
        int selectedRow = table.getVisualSelection().getSelectedRow();
        ResourceConfiguration selectedResourceConfiguration = resourceConfigurations.get(selectedRow);
        selectedResourceConfigurationProperty.set(selectedResourceConfiguration);
    }

    private void setDetailsPaneDisabled(boolean disabled) {
        if (roomTypeSelector != null) {
            roomTypeSelector.setReadOnly(disabled);
        }
        roomNameTextField.setDisable(disabled);
        bedsInRoomButtonSelector.getButton().setDisable(disabled);
        attendeeCategoryCheckBoxMap.values().forEach(checkBox -> checkBox.setDisable(disabled));
        allowsFemaleCheckBox.setDisable(disabled);
        allowsMaleCheckBox.setDisable(disabled);
        fromDateField.setDisable(disabled);
        toDateField.setDisable(disabled);
    }

    public void startLogic(Object mixin) { // may be called several times with different mixins (due to workaround)
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
