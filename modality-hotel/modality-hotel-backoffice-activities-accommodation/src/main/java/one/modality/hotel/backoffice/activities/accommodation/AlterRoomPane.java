package one.modality.hotel.backoffice.activities.accommodation;

import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.stack.db.submit.SubmitResult;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.FontWeight;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;
import one.modality.hotel.backoffice.accommodation.AttendeeCategory;
import one.modality.hotel.backoffice.accommodation.ResourceConfigurationLoader;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AlterRoomPane extends VBox {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-uu");
    private static final FontDef DETAIL_FONT = FontDef.font(FontWeight.NORMAL, 12);

    private EntityButtonSelector<Item> roomTypeSelector;
    private TextField roomNameTextField;
    private ComboBox<Integer> bedsInRoomComboBox;
    private Map<AttendeeCategory, CheckBox> attendeeCategoryCheckBoxMap = new HashMap<>();
    private TextField fromDateField;
    private TextField toDateField;

    private Button createButton;
    private Button updateButton;
    private Button deleteButton;
    private Button deleteRoomButton;

    private ResourceConfiguration rc;

    public AlterRoomPane(ResourceConfiguration rc, AccommodationActivity activity, ResourceConfigurationLoader resourceConfigurationLoader) {
        this.rc = rc;
        HBox detailsRow = new HBox(createHeadingLabel("Details"), createDetailsGrid(rc, activity));
        Label availabilityLabel = createHeadingLabel("Availability");
        GridPane availabilityGrid = createAvailabilityGrid(rc, resourceConfigurationLoader);

        createButton = new Button("Create");
        createButton.setOnAction(e -> create());
        updateButton = new Button("Update");
        deleteButton = new Button("Delete");
        deleteRoomButton = new Button("Delete room");
        HBox buttonPane = new HBox(createButton, updateButton, deleteButton, deleteRoomButton);

        getChildren().addAll(detailsRow, availabilityLabel, availabilityGrid, buttonPane);
    }

    private GridPane createDetailsGrid(ResourceConfiguration rc, AccommodationActivity activity) {
        Button productComboBoxButton = createProductComboBox(rc, activity);
        roomNameTextField = new TextField(rc.getName());
        bedsInRoomComboBox = createBedsInRoomComboBox(rc);
        GridPane eligibilityForBookingGrid = createEligibilityForBookingGrid(rc);
        fromDateField = new TextField();
        fromDateField.setPromptText("e.g. 15-01-22");
        toDateField = new TextField();
        toDateField.setPromptText("e.g. 16-01-22");

        GridPane detailsGridPane = new GridPane();
        detailsGridPane.add(createLabel("Product"), 0, 0);
        detailsGridPane.add(productComboBoxButton, 1, 0);
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

    private Button createProductComboBox(ResourceConfiguration rc, AccommodationActivity activity) {
        roomTypeSelector = new EntityButtonSelector<Item>(
                "{class: 'Item', alias: 'i', where: 'family.code=`acco`'}",
                activity, this, activity.getDataSourceModel()
        )
                .always(FXOrganizationId.organizationIdProperty(), orgId -> DqlStatement.where("exists(select ScheduledResource where configuration.(item=i and resource.site.organization=?))", Entities.getPrimaryKey(orgId)))
                .setAutoOpenOnMouseEntered(true)
                .appendNullEntity(true);
        roomTypeSelector.setSelectedItem(rc.getItem());
        return roomTypeSelector.getButton();
    }

    private ComboBox<Integer> createBedsInRoomComboBox(ResourceConfiguration rc) {
        final int maxBedsInRoom = 50;
        List<Integer> items = new ArrayList<>();
        for (int i = 1; i <= maxBedsInRoom; i++) {
            items.add(i);
        }
        ComboBox<Integer> comboBox = new ComboBox<>(FXCollections.observableList(items));
        Integer max = rc.getMax();
        comboBox.setValue(max);
        return comboBox;
    }

    private GridPane createEligibilityForBookingGrid(ResourceConfiguration rc) {
        GridPane gridPane = new GridPane();
        final int numColumns = 3;
        int columnIndex = 0;
        int rowIndex = 0;
        for (AttendeeCategory attendeeCategory : AttendeeCategory.values()) {
            CheckBox checkBox = new CheckBox(attendeeCategory.getText());
            boolean selected = allowsAttendanceCategory(rc, attendeeCategory);
            checkBox.setSelected(selected);
            attendeeCategoryCheckBoxMap.put(attendeeCategory, checkBox);
            gridPane.add(checkBox, columnIndex, rowIndex);
            columnIndex++;
            if (columnIndex >= numColumns) {
                columnIndex = 0;
                rowIndex++;
            }
        }
        return gridPane;
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

    private GridPane createAvailabilityGrid(ResourceConfiguration rc, ResourceConfigurationLoader resourceConfigurationLoader) {
        List<ResourceConfiguration> historicalResourceConfigurations = resourceConfigurationLoader.getResourceConfigurations().stream()
                .filter(rc2 -> rc2.getResource().equals(rc.getResource()))
                .collect(Collectors.toList());

        GridPane gridPane = new GridPane();
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
        UpdateStore updateStore = UpdateStore.create(rc.getStore().getDataSourceModel());
        ResourceConfiguration newRc = updateStore.createEntity(ResourceConfiguration.class);
        newRc.setResource(rc.getResource());
        newRc.setItem(roomTypeSelector.getSelectedItem());
        newRc.setName(roomNameTextField.getText());
        newRc.setMax(bedsInRoomComboBox.getValue());
        newRc.setAllowsGuest(attendeeCategoryCheckBoxMap.get(AttendeeCategory.GUEST).isSelected());
        newRc.setAllowsResident(attendeeCategoryCheckBoxMap.get(AttendeeCategory.RESIDENT).isSelected());
        newRc.setAllowsResidentFamily(attendeeCategoryCheckBoxMap.get(AttendeeCategory.RESIDENTS_FAMILY).isSelected());
        newRc.setAllowsSpecialGuest(attendeeCategoryCheckBoxMap.get(AttendeeCategory.SPECIAL_GUEST).isSelected());
        newRc.setAllowsVolunteer(attendeeCategoryCheckBoxMap.get(AttendeeCategory.VOLUNTEER).isSelected());
        // TODO set allows female and allows male
        newRc.setStartDate(LocalDate.parse(fromDateField.getText(), DATE_FORMATTER));
        newRc.setEndDate(LocalDate.parse(toDateField.getText(), DATE_FORMATTER));
        updateStore.submitChanges()
                .onFailure(Throwable::printStackTrace)
                .onSuccess(result -> {
                    System.out.println("Success: " + result.getArray().length + " entities inserted.");
                });
    }
}
