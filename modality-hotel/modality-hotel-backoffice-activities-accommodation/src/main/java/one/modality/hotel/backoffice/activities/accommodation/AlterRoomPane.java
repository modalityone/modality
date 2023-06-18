package one.modality.hotel.backoffice.activities.accommodation;

import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;
import one.modality.hotel.backoffice.accommodation.AttendeeCategory;

import java.util.HashMap;
import java.util.Map;

public class AlterRoomPane extends VBox {

    private TextField roomNameTextField;
    //private ComboBox<Integer> bedsInRoomComboBox;
    private CheckBox specialRateCheckBox;
    private TextField rateTextField;
    private TextField specificPriceTextField;
    private Map<AttendeeCategory, CheckBox> attendeeCategoryCheckBoxMap = new HashMap<>();

    private Button deleteButton;
    private Button saveButton;

    public AlterRoomPane(ResourceConfiguration rc, AccommodationActivity activity) {
        HBox topRow = new HBox(createHeadingLabel("Details"), createDetailsGrid(rc, activity));

        deleteButton = new Button("Delete room");
        saveButton = new Button("Save");
        HBox buttonPane = new HBox(deleteButton, saveButton);

        getChildren().addAll(topRow, buttonPane);
    }

    private GridPane createDetailsGrid(ResourceConfiguration rc, AccommodationActivity activity) {
        Button productComboBoxButton = createProductComboBox(rc, activity);
        roomNameTextField = new TextField(rc.getName());
        //bedsInRoomComboBox = createBedsInRoomComboBox();
        specialRateCheckBox = new CheckBox();
        rateTextField = new TextField();
        rateTextField.setPromptText("Enter the name of your rule here");
        GridPane eligibilityForBookingGrid = createEligibilityForBookingGrid();
        specificPriceTextField = new TextField();

        GridPane detailsGridPane = new GridPane();
        detailsGridPane.add(createLabel("Product"), 0, 0);
        detailsGridPane.add(productComboBoxButton, 1, 0);
        detailsGridPane.add(createLabel("Name"), 0, 1);
        detailsGridPane.add(roomNameTextField, 1, 1);
        detailsGridPane.add(createLabel("Beds in the room"), 0, 2);
        //detailsGridPane.add(bedsInRoomComboBox, 1, 2);
        detailsGridPane.add(createLabel("Eligibility for booking"), 0, 3);
        detailsGridPane.add(eligibilityForBookingGrid, 1, 3);
        detailsGridPane.add(createLabel("Special rate"), 0, 4);
        detailsGridPane.add(specialRateCheckBox, 1, 4);
        detailsGridPane.add(createLabel("Rate"), 0, 5);
        detailsGridPane.add(rateTextField, 1, 5);
        detailsGridPane.add(createLabel("From / To"), 0, 6);
        detailsGridPane.add(createLabel("Specific Price\n(per night)"), 0, 7);
        detailsGridPane.add(specificPriceTextField, 1, 7);
        return detailsGridPane;
    }

    private Button createProductComboBox(ResourceConfiguration rc, AccommodationActivity activity) {
        EntityButtonSelector<Item> roomTypeSelector = new EntityButtonSelector<Item>(
                "{class: 'Item', alias: 'i', where: 'family.code=`acco`'}",
                activity, this, activity.getDataSourceModel()
        )
                .always(FXOrganizationId.organizationIdProperty(), orgId -> DqlStatement.where("exists(select ScheduledResource where configuration.(item=i and resource.site.organization=?))", Entities.getPrimaryKey(orgId)))
                .setAutoOpenOnMouseEntered(true)
                .appendNullEntity(true);
        roomTypeSelector.setSelectedItem(rc.getItem());
        return roomTypeSelector.getButton();
    }

    /*private ComboBox<Integer> createBedsInRoomComboBox() {
        final int maxBedsInRoom = 50;
        List<Integer> items = new ArrayList<>();
        for (int i = 1; i <= maxBedsInRoom; i++) {
            items.add(i);
        }
        return new ComboBox<>(FXCollections.observableList(items));
    }*/

    private GridPane createEligibilityForBookingGrid() {
        GridPane gridPane = new GridPane();
        final int numColumns = 3;
        int columnIndex = 0;
        int rowIndex = 0;
        for (AttendeeCategory attendeeCategory : AttendeeCategory.values()) {
            CheckBox checkBox = new CheckBox(attendeeCategory.getText());
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

    private Label createLabel(String text) {
        return new Label(text);
    }

    private Label createHeadingLabel(String text) {
        return new Label(text);
    }
}
