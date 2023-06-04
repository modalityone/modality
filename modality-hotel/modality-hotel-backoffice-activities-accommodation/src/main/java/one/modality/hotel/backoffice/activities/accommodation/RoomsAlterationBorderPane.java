package one.modality.hotel.backoffice.activities.accommodation;

import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.text.TextTheme;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import javafx.util.StringConverter;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.hotel.backoffice.accommodation.AccommodationPresentationModel;
import one.modality.hotel.backoffice.accommodation.ResourceConfigurationLoader;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class RoomsAlterationBorderPane {

    private static final String MSG_CLICK_ON_ROOM_TO_SELECT_IT = "Click on a room to select it.";
    private static final FontDef SELECT_ROOM_TYPE_FONT = FontDef.font(FontWeight.BOLD, 15);

    public static BorderPane createAccommodationBorderPane(RoomsAlterationView roomsAlterationView, AccommodationPresentationModel pm) {
        Node body = roomsAlterationView.buildView();
        BorderPane borderPane = new BorderPane(body);

        ComboBox<String> roomTypeComboBox = createRoomTypeComboBox(pm);
        roomsAlterationView.roomTypeProperty().bind(roomTypeComboBox.valueProperty());

        Label selectRoomTypeLabel = new Label("Select the room type!");
        TextTheme.createPrimaryTextFacet(selectRoomTypeLabel)
                .requestedFont(SELECT_ROOM_TYPE_FONT)
                .style();

        Label selectedRoomLabel = new Label(MSG_CLICK_ON_ROOM_TO_SELECT_IT);
        Button roomStatusButton = new Button("Room status");
        roomStatusButton.setDisable(true);
        roomStatusButton.setOnAction(e -> roomsAlterationView.showRoomStatus());
        roomsAlterationView.selectedRoomProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                selectedRoomLabel.setText(MSG_CLICK_ON_ROOM_TO_SELECT_IT);
            } else {
                selectedRoomLabel.setText(newValue.getName() + " selected.");
            }
            roomStatusButton.setDisable(newValue == null);
        });

        HBox bottomBar = new HBox(10, selectRoomTypeLabel, roomTypeComboBox, selectedRoomLabel, roomStatusButton);
        bottomBar.setBackground(new Background(new BackgroundFill(Color.web("#e0dcdc"), null, null)));
        bottomBar.setAlignment(Pos.CENTER_LEFT);
        borderPane.setBottom(bottomBar);

        return borderPane;
    }

    private static ComboBox<String> createRoomTypeComboBox(AccommodationPresentationModel pm) {
        ComboBox<String> roomTypeComboBox = new ComboBox<>();

        ResourceConfigurationLoader resourceConfigurationLoader = ResourceConfigurationLoader.getOrCreate(pm);
        resourceConfigurationLoader.getResourceConfigurations().addListener((ListChangeListener<? super ResourceConfiguration>) change -> {
            ArrayList<String> roomTypeStrings = resourceConfigurationLoader.getResourceConfigurations().stream()
                    .map(rc -> rc.getItem().getName())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toCollection(ArrayList::new));
            roomTypeStrings.add(0, null);
            roomTypeComboBox.setItems(FXCollections.observableList(roomTypeStrings));
        });

        roomTypeComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(String s) {
                return s != null ? s : "All rooms";
            }

            @Override
            public String fromString(String s) {
                return null;
            }
        });
        return roomTypeComboBox;
    }
}
