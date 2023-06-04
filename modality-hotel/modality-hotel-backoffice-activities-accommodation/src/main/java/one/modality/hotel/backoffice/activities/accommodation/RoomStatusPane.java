package one.modality.hotel.backoffice.activities.accommodation;

import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class RoomStatusPane extends VBox {

    private TextField roomNameTextField;
    private ComboBox<Integer> bedsInRoomComboBox;
    private CheckBox specialRateCheckBox;
    private TextField rateTextField;
    private TextField specificPriceTextField;

    private Button deleteButton;
    private Button closeButton;
    private Button saveButton;

    public RoomStatusPane(RoomsAlterationView roomsAlterationView) {
        HBox topRow = new HBox(createHeadingLabel("Details"), createDetailsGrid());


        deleteButton = new Button("Delete room");
        closeButton = new Button("Close");
        closeButton.setOnAction(e -> roomsAlterationView.showRoomList());
        saveButton = new Button("Save");
        HBox buttonPane = new HBox(deleteButton, closeButton, saveButton);

        getChildren().addAll(topRow, buttonPane);

        roomsAlterationView.selectedRoomProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                roomNameTextField.setText(newValue.getName());
            } else {
                roomNameTextField.setText(null);
            }
        });
    }

    private GridPane createDetailsGrid() {
        roomNameTextField = new TextField();
        bedsInRoomComboBox = createBedsInRoomComboBox();
        specialRateCheckBox = new CheckBox();
        rateTextField = new TextField();
        rateTextField.setPromptText("Enter the name of your rule here");
        specificPriceTextField = new TextField();

        GridPane detailsGridPane = new GridPane();
        detailsGridPane.add(createLabel("Product"), 0, 0);
        detailsGridPane.add(createRoomTypeComboBox(), 1, 0);
        detailsGridPane.add(createLabel("Name"), 0, 1);
        detailsGridPane.add(roomNameTextField, 1, 1);
        detailsGridPane.add(createLabel("Beds in the room"), 0, 2);
        detailsGridPane.add(bedsInRoomComboBox, 1, 2);
        detailsGridPane.add(createLabel("Eligibility for booking"), 0, 3);
        detailsGridPane.add(createLabel("<TODO>"), 1, 3);
        detailsGridPane.add(createLabel("Special rate"), 0, 4);
        detailsGridPane.add(specialRateCheckBox, 1, 4);
        detailsGridPane.add(createLabel("Rate"), 0, 5);
        detailsGridPane.add(rateTextField, 1, 5);
        detailsGridPane.add(createLabel("From / To"), 0, 6);
        detailsGridPane.add(createLabel("Specific Price\n(per night)"), 0, 7);
        detailsGridPane.add(specificPriceTextField, 1, 7);
        return detailsGridPane;
    }

    private ComboBox<String> createRoomTypeComboBox() {
        return new ComboBox<>();
    }

    private ComboBox<Integer> createBedsInRoomComboBox() {
        final int maxBedsInRoom = 50;
        List<Integer> items = new ArrayList<>();
        for (int i = 1; i <= maxBedsInRoom; i++) {
            items.add(i);
        }
        return new ComboBox<>(FXCollections.observableList(items));
    }

    private Label createLabel(String text) {
        return new Label(text);
    }

    private Label createHeadingLabel(String text) {
        return new Label(text);
    }
}
