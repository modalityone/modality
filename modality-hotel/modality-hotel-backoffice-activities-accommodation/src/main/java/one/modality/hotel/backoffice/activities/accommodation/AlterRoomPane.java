package one.modality.hotel.backoffice.activities.accommodation;

import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import dev.webfx.stack.routing.activity.impl.elementals.activeproperty.HasActiveProperty;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.FontWeight;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;
import one.modality.hotel.backoffice.accommodation.AccommodationPresentationModel;
import one.modality.hotel.backoffice.accommodation.AttendeeCategory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.webfx.stack.orm.dql.DqlStatement.orderBy;
import static dev.webfx.stack.orm.dql.DqlStatement.where;

public class AlterRoomPane extends VBox {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-uu");
    private static final FontDef DETAIL_FONT = FontDef.font(FontWeight.NORMAL, 12);

    private final AccommodationPresentationModel pm;
    private final ObjectProperty<ResourceConfiguration> resourceConfigurationProperty = new SimpleObjectProperty<>();
    public ObjectProperty<ResourceConfiguration> resourceConfigurationProperty() { return resourceConfigurationProperty; }
    private final ObjectProperty<ResourceConfiguration>  selectedResourceConfigurationProperty = new SimpleObjectProperty<>();

    private ButtonFactoryMixin mixin;
    private ObservableValue<Boolean> activeProperty;
    private ReactiveVisualMapper<ResourceConfiguration> rvm;

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


    public AlterRoomPane(AccommodationPresentationModel pm) {
        this.pm = pm;
        selectedResourceConfigurationProperty.addListener((observable, oldValue, newValue) -> displayDetails(newValue));
        HBox detailsRow = new HBox(createHeadingLabel("Details"), createDetailsGrid());
        Label availabilityLabel = createHeadingLabel("Availability");
        //VisualGrid availabilityGrid = createAvailabilityGrid(rc, activity);

        table = new VisualGrid();
        createButton = new Button("Create");
        createButton.setOnAction(e -> create());
        updateButton = new Button("Update");
        deleteButton = new Button("Delete");
        deleteRoomButton = new Button("Delete room");
        HBox buttonPane = new HBox(createButton, updateButton, deleteButton, deleteRoomButton);

        getChildren().addAll(detailsRow, availabilityLabel, table, buttonPane);
    }

    private GridPane createDetailsGrid() {
        roomNameTextField = new TextField();
        bedsInRoomComboBox = createBedsInRoomComboBox();
        GridPane eligibilityForBookingGrid = createEligibilityForBookingGrid();
        fromDateField = new TextField();
        fromDateField.setPromptText("e.g. 15-01-22");
        toDateField = new TextField();
        toDateField.setPromptText("e.g. 16-01-22");

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
        return detailsGridPane;
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
            case GUEST: rc.setAllowsGuest(allowed);
            case RESIDENT: rc.setAllowsResident(allowed);
            case RESIDENTS_FAMILY: rc.setAllowsResidentFamily(allowed);
            case SPECIAL_GUEST: rc.setAllowsSpecialGuest(allowed);
            case VOLUNTEER: rc.setAllowsVolunteer(allowed);
        }
    }

    private void displayDetails(ResourceConfiguration rc) {
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

    // TODO change this to return a table
    /*private VisualGrid createAvailabilityGrid(ResourceConfiguration rc, AccommodationActivity activity) {
        AccommodationPresentationModel pm = activity.getPresentationModel();

        /*List<ResourceConfiguration> historicalResourceConfigurations = resourceConfigurationLoader.getResourceConfigurations().stream()
                .filter(rc2 -> rc2.getResource().equals(rc.getResource()))
                .collect(Collectors.toList());*

        rem = ReactiveVisualMapper.<ResourceConfiguration>createPushReactiveChain(activity)
                .always("{class: 'ResourceConfiguration', alias: 'rc', fields: 'name,item.name,max,allowsGuest,allowsResident,allowsResidentFamily,allowsSpecialGuest,allowsVolunteer'}")
                .always(orderBy("item.ord,name"))
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), o -> where("resource.site.(organization=? and event=null)", o))
                // Restricting events to those appearing in the time window
                //.storeEntitiesInto(resourceConfigurations)
                .visualizeResultInto(table)
                // We are now ready to start
                .start();
        //VisualResult visualResult;
        return table;


        /*GridPane gridPane = new GridPane();
        gridPane.setHgap(16);
        int rowIndex = 0;
        for (ResourceConfiguration historicalResourceConfiguration : historicalResourceConfigurations) {
            int numBeds = historicalResourceConfiguration.getMax();
            String numBedsText = numBeds + (numBeds == 1 ? " bed" : " beds");
            String eligible = Stream.of(AttendeeCategory.values())
                    .filter(attendeeCategory -> allowsAttendanceCategory(historicalResourceConfiguration, attendeeCategory))
                    .map(AttendeeCategory::getText)
                    .collect(Collectors.joining(", "));
            if (eligible.isEmpty()) {
                eligible = "<none>";
            }

            gridPane.add(createDetailLabel(historicalResourceConfiguration.getItem().getName()), 0, rowIndex);
            gridPane.add(createDetailLabel(historicalResourceConfiguration.getName()), 1, rowIndex);
            gridPane.add(createDetailLabel(numBedsText), 2, rowIndex);
            gridPane.add(createDetailLabel("Eligible: " + eligible), 3, rowIndex);
            gridPane.add(createDetailLabel("From " + historicalResourceConfiguration.getStartDate()), 4, rowIndex);
            gridPane.add(createDetailLabel("To " + historicalResourceConfiguration.getEndDate()), 5, rowIndex);
            rowIndex++;
        }
        return gridPane;
    }*/

    private void createProductComboBox() {
        roomTypeSelector = new EntityButtonSelector<Item>(
                "{class: 'Item', alias: 'i', where: 'family.code=`acco`'}",
                mixin, this, selectedResourceConfigurationProperty.get().getStore().getDataSourceModel()
        )
                .always(FXOrganizationId.organizationIdProperty(), orgId -> DqlStatement.where("exists(select ScheduledResource where configuration.(item=i and resource.site.organization=?))", Entities.getPrimaryKey(orgId)))
                .setAutoOpenOnMouseEntered(true)
                .appendNullEntity(true);
        detailsGridPane.add(roomTypeSelector.getButton(), 1, 0);
    }

    private Label createDetailLabel(String text) {
        Label label = new Label(text);
        TextTheme.createDefaultTextFacet(label)
                .requestedFont(DETAIL_FONT)
                .style();
        return label;
    }

    private Label createLabel(String text) {
        return new Label(text);
    }

    private Label createHeadingLabel(String text) {
        return new Label(text);
    }

    private void create() {
        UpdateStore updateStore = UpdateStore.create(selectedResourceConfigurationProperty.get().getStore().getDataSourceModel());
        ResourceConfiguration newRc = updateStore.createEntity(ResourceConfiguration.class);
        newRc.setAllowsGuest(false);
        newRc.setAllowsResident(false);
        newRc.setAllowsResidentFamily(false);
        newRc.setAllowsSpecialGuest(false);
        newRc.setAllowsVolunteer(false);
        newRc.setAllowsFemale(false);
        newRc.setAllowsMale(false);
        selectedResourceConfigurationProperty.set(newRc);
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
                    .setSelectedEntityHandler(selectedResourceConfigurationProperty::setValue)
                    .start();

        } else if (activeProperty != null) { // subsequent calls
            rvm.bindActivePropertyTo(activeProperty); // updating the reactive entities mapper active property
        }
    }
}
