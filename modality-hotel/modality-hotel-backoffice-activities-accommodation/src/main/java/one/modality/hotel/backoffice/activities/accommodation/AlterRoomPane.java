package one.modality.hotel.backoffice.activities.accommodation;

import dev.webfx.extras.type.PrimType;
import dev.webfx.extras.visual.VisualColumn;
import dev.webfx.extras.visual.VisualResult;
import dev.webfx.extras.visual.VisualResultBuilder;
import dev.webfx.extras.visual.VisualSelection;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.extras.visual.impl.VisualResultImpl;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.*;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelector;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import dev.webfx.stack.routing.activity.impl.elementals.activeproperty.HasActiveProperty;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import dev.webfx.stack.ui.controls.dialog.DialogBuilderUtil;
import dev.webfx.stack.ui.controls.dialog.DialogContent;
import dev.webfx.stack.ui.dialog.DialogCallback;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
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
import one.modality.base.shared.entities.*;
import one.modality.base.shared.entities.markers.EntityHasDate;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;
import one.modality.hotel.backoffice.accommodation.AccommodationPresentationModel;
import one.modality.hotel.backoffice.accommodation.AttendeeCategory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static dev.webfx.stack.orm.dql.DqlStatement.orderBy;
import static dev.webfx.stack.orm.dql.DqlStatement.where;

public class AlterRoomPane extends VBox {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-uu");

    private final AccommodationPresentationModel pm;
    private final ObjectProperty<ResourceConfiguration> resourceConfigurationProperty = new SimpleObjectProperty<>();
    public ObjectProperty<ResourceConfiguration> resourceConfigurationProperty() { return resourceConfigurationProperty; }
    private final ObjectProperty<ResourceConfiguration> selectedResourceConfigurationProperty = new SimpleObjectProperty<>();
    private final ObservableList<ResourceConfiguration> resourceConfigurations = FXCollections.observableArrayList();

    private final ButtonFactoryMixin mixin;
    private ObservableValue<Boolean> activeProperty;
    private ReactiveVisualMapper<ResourceConfiguration> rvm;
    private UpdateStore updateStore;
    private List<ScheduledItem> scheduledItemsForRoom = Collections.emptyList();

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
            LocalDate tomorrow = LocalDate.now().plusDays(1);
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
        saveButton.setOnAction(e -> confirmSave());
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
        toDateField.setText(rc.getEndDate() != null ? DATE_FORMATTER.format(rc.getEndDate()) : null);
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
        updateStore = UpdateStore.create(previousRc.getStore().getDataSourceModel());
        ResourceConfiguration newRc = updateStore.insertEntity(ResourceConfiguration.class);
        cloneSelectedResourceConfiguration(newRc);
        newRc.setStartDate(previousRc.getEndDate() != null ? previousRc.getEndDate().plusDays(1) : LocalDate.now());
        selectedResourceConfigurationProperty.set(newRc);

        setDetailsPaneDisabled(false);
        table.setDisable(true);
        saveButton.setVisible(true);
        cancelButton.setVisible(true);
    }

    private void cloneSelectedResourceConfiguration(ResourceConfiguration newRc) {
        ResourceConfiguration previousRc = selectedResourceConfigurationProperty.get();
        Resource resource = previousRc.getResource();
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
    }

    private void update() {
        ResourceConfiguration updateRc = selectedResourceConfigurationProperty.get();
        updateStore = UpdateStore.createAbove(updateRc.getStore());
        ResourceConfiguration newRc = updateStore.updateEntity(updateRc);
        cloneSelectedResourceConfiguration(newRc);
        newRc.setStartDate(updateRc.getStartDate());
        newRc.setEndDate(updateRc.getEndDate());
        selectedResourceConfigurationProperty.set(newRc);

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

    private void confirmSave() {
        new ValidationQueue(this::save)
                // Perform blocking validations first
                .addValidation(this::validateNotOverlapping)
                .addValidation(this::validateAgainstExistingBookings)
                // Perform non-blocking validations second
                .addValidation(this::validateResourceConfigurationDatesContiguous)
                .run();
    }

    private void validateNotOverlapping(BooleanProperty success) {
        ResourceConfiguration overlappingRc = findFirstOverlappingResourceConfiguration();
        if (overlappingRc != null) {
            showOverlappingConfigurationPopup(overlappingRc);
            success.set(false);
        } else {
            success.set(true);
        }
    }

    private ResourceConfiguration findFirstOverlappingResourceConfiguration() {
        ResourceConfiguration selectedRc = selectedResourceConfigurationProperty.get();
        for (ResourceConfiguration rc : resourceConfigurations) {
            if (rc.equals(selectedRc)) {
                continue;
            }
            if (selectedRc.getEndDate() != null && rc.getStartDate() != null && !rc.getStartDate().isBefore(selectedRc.getEndDate())) {
                continue;
            }
            if (selectedRc.getStartDate() != null && rc.getEndDate() != null && !rc.getEndDate().isAfter(selectedRc.getStartDate())) {
                continue;
            }
            return rc;
        }
        return null;
    }

    private void showOverlappingConfigurationPopup(ResourceConfiguration overlappingRc) {
        Label msgLabel = new Label("Your configuration is overlapping the following existing configuration:");
        int rowIndex = resourceConfigurations.indexOf(overlappingRc);
        int columnCount = table.getVisualResult().getColumnCount();
        Object[] values = new Object[columnCount];
        for (int i = 0; i < columnCount; i++) {
            values[i] = table.getVisualResult().getValue(rowIndex, i);
        }
        VisualResult visualResult = new VisualResultImpl(1, values, table.getVisualResult().getColumns());
        VisualGrid table = new VisualGrid(visualResult);
        VBox overlappingMsgPane = new VBox(msgLabel, table);
        DialogContent dialogContent = new DialogContent().setContent(overlappingMsgPane);
        dialogContent.getOkButton().setVisible(false);
        DialogBuilderUtil.showModalNodeInGoldLayout(dialogContent, this);
        DialogBuilderUtil.armDialogContentButtons(dialogContent, DialogCallback::closeDialog);
    }

    private void showContinueWithSavePopup(String msg, BooleanProperty success) {
        DialogContent dialogContent = new DialogContent().setContentText(msg).setYesNo();
        Platform.runLater(() -> {
            DialogBuilderUtil.showModalNodeInGoldLayout(dialogContent, this);
            DialogBuilderUtil.armDialogContentButtons(dialogContent, dialogCallback -> {
                success.set(true);
                dialogCallback.closeDialog();
            });
            dialogContent.getCancelButton().setOnAction(event -> {
                success.set(false);
                dialogContent.getDialogCallback().closeDialog();
            });
        });
    }

    private void validateResourceConfigurationDatesContiguous(BooleanProperty success) {
        for (ResourceConfiguration rc : resourceConfigurations) {
            if (rc.getStartDate() != null) {
                LocalDate dayBefore = rc.getStartDate().minusDays(1);
                if (dayBefore.equals(selectedResourceConfigurationProperty.get().getEndDate())) {
                    continue;
                }
                if (resourceConfigurations.stream().anyMatch(rc2 -> dayBefore.equals(rc2.getEndDate())
                        && !rc2.equals(selectedResourceConfigurationProperty.get()) )) {
                    continue;
                }
                showContinueWithSavePopup("Configuration dates do not all join. Continue with save?", success);
                return;
            }

            if (rc.getEndDate() != null) {
                LocalDate dayAfter = rc.getEndDate().plusDays(1);
                if (dayAfter.equals(selectedResourceConfigurationProperty.get().getStartDate())) {
                    continue;
                }
                if (resourceConfigurations.stream().anyMatch(rc2 -> dayAfter.equals(rc2.getStartDate())
                        && !rc2.equals(selectedResourceConfigurationProperty.get()))) {
                    continue;
                }
                showContinueWithSavePopup("Configuration dates do not all join. Continue with save?", success);
                return;
            }
        }
        success.set(true);
    }

    private void validateAgainstExistingBookings(BooleanProperty success) {
        DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();
        ResourceConfiguration selectedRc = selectedResourceConfigurationProperty.get();
        EntityId resourceId = selectedRc.getResourceId();
        EntityId organizationId = (EntityId) pm.organizationIdProperty().get();
        // TODO restrict to attendees in time window
        EntityStore.create(dataSourceModel).<Attendance>executeQuery("select date,documentLine.document.(person_guest,person_male,person_resident) from Attendance a where a.documentLine.document.event.organization=" + organizationId.getPrimaryKey() + " and a.scheduledResource.configuration.resource.id=" + resourceId.getPrimaryKey())
                .onFailure(error -> {
                    Console.log("Error while reading attendances", error);
                    success.set(false);
                })
                .onSuccess(attendances -> {
                    if (!selectedRc.allowsMale()) {
                        boolean maleAttendees = attendances.stream()
                                .anyMatch(attendance -> attendance.getDocumentLine().getDocument().isMale());
                        if (maleAttendees) {
                            displayStatus("Unable to save. Male attendees already booked during this period.");
                            success.set(false);
                            return;
                        }
                    }
                    if (!selectedRc.allowsFemale()) {
                        boolean femaleAttendees = attendances.stream()
                                .anyMatch(attendance -> !attendance.getDocumentLine().getDocument().isMale());
                        if (femaleAttendees) {
                            displayStatus("Unable to save. Female attendees already booked during this period.");
                            success.set(false);
                            return;
                        }
                    }
                    if (!selectedRc.allowsGuest()) {
                        boolean guestAttendees = attendances.stream()
                                .anyMatch(attendance -> attendance.getDocumentLine().getDocument().isGuest());
                        if (guestAttendees) {
                            displayStatus("Unable to save. Guest attendees already booked during this period.");
                            success.set(false);
                            return;
                        }
                    }
                    if (!selectedRc.allowsResident()) {
                        boolean residentAttendees = attendances.stream()
                                .anyMatch(attendance -> attendance.getDocumentLine().getDocument().isResident());
                        if (residentAttendees) {
                            displayStatus("Unable to save. Resident attendees already booked during this period.");
                            success.set(false);
                            return;
                        }
                    }
                    success.set(true);
                });
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
            updatedLatestResourceConfiguration.setEndDate(fromDate.minusDays(1));
        }

        Optional<ResourceConfiguration> resourceConfigurationImmediatelyAfterNewOne = resourceConfigurations.stream()
                .filter(rc -> rc.getStartDate() != null && rc.getStartDate().isAfter(fromDate))
                .min(Comparator.comparing(ResourceConfiguration::getStartDate));
        if (resourceConfigurationImmediatelyAfterNewOne.isPresent()) {
            // If there is a resource configuration with a start date later than the one entered then set the end date of this one
            // to the day before it starts
            LocalDate endDate = resourceConfigurationImmediatelyAfterNewOne.get().getStartDate().minusDays(1);
            newRc.setEndDate(endDate);
        }

        new ValidationQueue(() -> {
            // Submit changes
            otherUpdateStore.submitChanges()
                    .onFailure(e -> displayStatus("Not saved. " + e.getMessage()))
                    .onSuccess(b -> updateStore.submitChanges()
                            .onFailure(e -> displayStatus("Not saved. " + e.getMessage()))
                            .onSuccess(b2 -> displayStatus("Saved.")));})
                .addValidation(this::ensureScheduledItemForEachDay)
                .addValidation(this::ensureScheduledResourceForEachDay)
                .run();
    }

    private void ensureScheduledItemForEachDay(BooleanProperty success) {
        ResourceConfiguration selectedRc = selectedResourceConfigurationProperty.get();

        // TODO if start date or end date is null then use site item family start or end date
        LocalDate startDate = getSelectedResourceConfigurationStartDate();
        LocalDate endDate = getSelectedResourceConfigurationEndDate();

        // Find ScheduledItems with this item type
        DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();
        Object itemId = selectedRc.getItem().getPrimaryKey();
        EntityStore.create(dataSourceModel).<ScheduledItem>executeQuery("select id,timeline,site,item,date,startTime,endTime,available,online,resource from ScheduledItem si where si.item.id=? and si.date>=? and si.date<=?", itemId, startDate, endDate)
                .onFailure(error -> {
                    Console.log("Error while reading scheduled items.", error);
                    success.set(false);
                })
                .onSuccess(attendances -> {
                    List<LocalDate> populatedDates = attendances.stream()
                            .map(EntityHasDate::getDate)
                            .collect(Collectors.toList());

                    scheduledItemsForRoom = attendances;

                    Site site = selectedRc.getResource().getSite();
                    // Check that a ScheduledItem exists for each day between the start date and end date
                    for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                        if (!populatedDates.contains(date)) {
                            // If no existing ScheduledItem exists for this item for this date then create one
                            ScheduledItem newScheduledItem = updateStore.insertEntity(ScheduledItem.class);
                            newScheduledItem.setDate(date);
                            newScheduledItem.setItem(selectedRc.getItem());
                            newScheduledItem.setSite(site);
                            scheduledItemsForRoom.add(newScheduledItem);
                        }
                    }
                    success.set(true);
                });
    }

    private LocalDate getSelectedResourceConfigurationStartDate() {
        ResourceConfiguration selectedRc = selectedResourceConfigurationProperty.get();
        if (selectedRc.getStartDate() != null) {
            return selectedRc.getStartDate();
        } else {
            // TODO if start date is null then use site item family start date
            return LocalDate.of(2022, 9, 1);
        }
    }

    private LocalDate getSelectedResourceConfigurationEndDate() {
        ResourceConfiguration selectedRc = selectedResourceConfigurationProperty.get();
        if (selectedRc.getEndDate() != null) {
            return selectedRc.getEndDate();
        } else {
            // TODO if end date is null then use site item family end date
            return LocalDate.of(2023, 8, 31);
        }
    }

    private void ensureScheduledResourceForEachDay(BooleanProperty success) {
        LocalDate startDate = getSelectedResourceConfigurationStartDate();
        LocalDate endDate = getSelectedResourceConfigurationEndDate();

        DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();
        ResourceConfiguration selectedRc = selectedResourceConfigurationProperty.get();
        Object itemId = selectedRc.getItem().getPrimaryKey();

        EntityStore.create(dataSourceModel).<ScheduledResource>executeQuery("select date,sr.configuration.(name,item.name,max,allowsGuest,allowsResident,allowsResidentFamily,allowsSpecialGuest,allowsVolunteer,allowsFemale,allowsMale,startDate,endDate,resource.site) from ScheduledResource sr where sr.scheduledItem.item.id=? and sr.date>=? and sr.date<=?", itemId, startDate, endDate)
                .onFailure(error -> {
                    Console.log("Error while reading scheduled resources.", error);
                    success.set(false);
                })
                .onSuccess(scheduledResources -> {
                    if (hasConfigurationChanged()) {
                        ResourceConfiguration previousResourceConfiguration = getSelectedResourceConfigurationBeforeChanges();
                        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                            final LocalDate finalDate = date;
                            Optional<ScheduledResource> matchingPreviousConfig = scheduledResources.stream()
                                    .filter(sr -> sr.getDate().equals(finalDate) && sr.getResourceConfiguration().equals(previousResourceConfiguration))
                                    .findAny();
                            if (matchingPreviousConfig.isEmpty()) {
                                // If there is no existing ScheduledResource for this date then create it...
                                newScheduledResource(selectedRc, date);
                            } else {
                                // ...else update the existing ScheduledResource for this date to point to the new configuration and Scheduled Item
                                ScheduledResource scheduledResource = matchingPreviousConfig.get();
                                ScheduledResource updatedScheduledResource = updateStore.updateEntity(scheduledResource);
                                updatedScheduledResource.setResourceConfiguration(selectedRc);
                                Entity scheduledItem = scheduledItemForDate(date);
                                updatedScheduledResource.setForeignField("scheduledItem", scheduledItem);
                            }
                        }
                    } else {
                        // Check that a ScheduledResource exists for each day between the start date and end date
                        List<LocalDate> datesWithMatchingConfig = scheduledResources.stream()
                                .filter(sr -> matchesIgnoringDates(sr.getResourceConfiguration(), selectedRc))
                                .map(EntityHasDate::getDate)
                                .collect(Collectors.toList());

                        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                            if (!datesWithMatchingConfig.contains(date)) {
                                newScheduledResource(selectedRc, date);
                            }
                        }
                    }
                    success.set(true);
                });
    }

    private void newScheduledResource(ResourceConfiguration sourceResourceConfiguration, LocalDate date) {
        ScheduledResource newScheduledResource = updateStore.insertEntity(ScheduledResource.class);
        newScheduledResource.setDate(date);
        newScheduledResource.setAvailable(true);
        newScheduledResource.setResourceConfiguration(sourceResourceConfiguration);
        newScheduledResource.setMax(sourceResourceConfiguration.getMax());
        newScheduledResource.setOnline(false);
        Entity scheduledItem = scheduledItemForDate(date);
        newScheduledResource.setForeignField("scheduledItem", scheduledItem);
    }

    private ScheduledItem scheduledItemForDate(LocalDate date) {
        return scheduledItemsForRoom.stream()
                .filter(si -> si.getDate().equals(date))
                .findAny()
                .get();
    }

    private boolean hasConfigurationChanged() {
        ResourceConfiguration before = getSelectedResourceConfigurationBeforeChanges();
        ResourceConfiguration after = selectedResourceConfigurationProperty.get();
        return !matchesIgnoringDates(before, after);
    }

    private boolean matchesIgnoringDates(ResourceConfiguration rc1, ResourceConfiguration rc2) {
        return Objects.equals(rc1.getMax(), rc2.getMax()) &&
                Objects.equals(rc1.allowsGuest(), rc2.allowsGuest()) &&
                Objects.equals(rc1.allowsResident(), rc2.allowsResident()) &&
                Objects.equals(rc1.allowsResidentFamily(), rc2.allowsResidentFamily()) &&
                Objects.equals(rc1.allowsSpecialGuest(), rc2.allowsSpecialGuest()) &&
                Objects.equals(rc1.allowsVolunteer(), rc2.allowsVolunteer()) &&
                Objects.equals(rc1.allowsFemale(), rc2.allowsFemale()) &&
                Objects.equals(rc1.allowsMale(), rc2.allowsMale()) &&
                Objects.equals(rc1.getItem(), rc2.getItem()) &&
                Objects.equals(rc1.getName(), rc2.getName());
    }

    /**
     * Deletes ScheduledResource entities with dates in gaps created by the user bringing the "from" date
     * forward or putting the "to" date back.
     * @param success set to indicate whether transaction succeeds
     */
    private void removeScheduledResourceFromGaps(BooleanProperty success) {
        ResourceConfiguration before = getSelectedResourceConfigurationBeforeChanges();
        ResourceConfiguration after = selectedResourceConfigurationProperty.get();

        // Determine min and max dates for the gap
        LocalDate min = null, max = null;
        if (before.getStartDate().isBefore(after.getStartDate())) {
            min = before.getStartDate();
            max = after.getStartDate();
        }
        if (before.getEndDate().isAfter(after.getEndDate())) {
            if (min == null) {
                min = after.getEndDate();
            }
            max = before.getEndDate();
        }
        if (min == null) {
            // If no time gap has been introduced by changing the "from" and "to" dates the no deletion is necessary
            success.set(true);
            return;
        }

        // Select all ScheduledResources for the item within the time window
        DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();
        Object itemId = selectedResourceConfigurationProperty.get().getItem().getPrimaryKey();
        EntityStore.create(dataSourceModel).<ScheduledResource>executeQuery("select date from ScheduledResource sr where sr.scheduledItem.item.id=? and sr.date>=? and sr.date<=?", itemId, min, max)
                .onFailure(error -> {
                    Console.log("Error while reading scheduled resources.", error);
                    success.set(false);
                })
                .onSuccess(scheduledResources -> {
                    // Delete ScheduledResources in the time gap
                    UpdateStore deleteUpdateStore = UpdateStore.createAbove(scheduledResources.iterator().next().getStore());
                    for (ScheduledResource scheduledResource : scheduledResources) {
                        boolean inGap = scheduledResource.getDate().isBefore(after.getStartDate()) || scheduledResource.getDate().isAfter(after.getEndDate());
                        if (inGap) {
                            deleteUpdateStore.deleteEntity(scheduledResource);
                        }
                    }
                    deleteUpdateStore.submitChanges()
                            .onFailure(e -> displayStatus("Not saved. " + e.getMessage()))
                            .onSuccess(b -> success.set(true));
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
        new ValidationQueue(() -> {
        updateStore.submitChanges()
                .onFailure(e -> displayStatus("Not saved. " + e.getMessage()))
                .onSuccess(result -> {
                    displayStatus("Saved.");
                    cancel();
                });})
                .addValidation(this::removeScheduledResourceFromGaps)
                .addValidation(this::ensureScheduledItemForEachDay)
                .addValidation(this::ensureScheduledResourceForEachDay)
                .run();
    }

    private void cancel() {
        saveButton.setVisible(false);
        cancelButton.setVisible(false);
        setDetailsPaneDisabled(true);
        table.setDisable(false);
        ResourceConfiguration selectedResourceConfiguration = getSelectedResourceConfigurationBeforeChanges();
        selectedResourceConfigurationProperty.set(selectedResourceConfiguration);
        displayStatus(null);
        displayDetails(selectedResourceConfiguration);
    }

    private ResourceConfiguration getSelectedResourceConfigurationBeforeChanges() {
        int selectedRow = table.getVisualSelection().getSelectedRow();
        return resourceConfigurations.get(selectedRow);
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
                    .always("{class: 'ResourceConfiguration', alias: 'rc', fields: 'name,item.name,max,allowsGuest,allowsResident,allowsResidentFamily,allowsSpecialGuest,allowsVolunteer,allowsFemale,allowsMale,startDate,endDate,resource.site'}")
                    .always(orderBy("endDate desc"))
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
