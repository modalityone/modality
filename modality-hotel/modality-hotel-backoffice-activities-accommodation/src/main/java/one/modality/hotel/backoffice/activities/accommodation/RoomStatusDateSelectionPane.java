package one.modality.hotel.backoffice.activities.accommodation;

import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.text.TextTheme;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.FontWeight;

public class RoomStatusDateSelectionPane extends VBox {

    private static final FontDef SELECT_PREIOD_FONT = FontDef.font(FontWeight.BOLD, 15);

    public RoomStatusDateSelectionPane(RoomsAlterationView roomsAlterationView) {
        Label selectPeriodLabel = new Label("Select your period");
        TextTheme.createPrimaryTextFacet(selectPeriodLabel)
                .requestedFont(SELECT_PREIOD_FONT)
                .style();

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> roomsAlterationView.showRoomList());
        Button acceptButton = new Button("Accept");
        HBox buttonPane = new HBox(closeButton, acceptButton);

        getChildren().setAll(selectPeriodLabel, buttonPane);
    }
}
