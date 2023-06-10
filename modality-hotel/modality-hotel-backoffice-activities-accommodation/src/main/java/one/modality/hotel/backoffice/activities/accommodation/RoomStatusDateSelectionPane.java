package one.modality.hotel.backoffice.activities.accommodation;

import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.text.TextTheme;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.FontWeight;

public class RoomStatusDateSelectionPane extends VBox {

    private static final FontDef SELECT_PREIOD_FONT = FontDef.font(FontWeight.BOLD, 15);

    public RoomStatusDateSelectionPane(RoomsAlterationView roomsAlterationView) {
        setAlignment(Pos.CENTER);
        setSpacing(16);
        Label selectPeriodLabel = new Label("Select your period");
        TextTheme.createPrimaryTextFacet(selectPeriodLabel)
                .requestedFont(SELECT_PREIOD_FONT)
                .style();

        GridPane fromToGridPane = new GridPane();
        Label fromLabel = new Label("From");
        Label toLabel = new Label("To");
        TextTheme.createDefaultTextFacet(fromLabel)
                .style();
        TextTheme.createDefaultTextFacet(toLabel)
                .style();
        TextField fromTextField = new TextField();
        fromTextField.setPromptText("eg. 01-01-2000");
        TextField toTextField = new TextField();
        toTextField.setPromptText("eg. 31-01-2000");
        fromToGridPane.add(fromLabel, 0, 0);
        fromToGridPane.add(fromTextField, 1, 0);
        fromToGridPane.add(toLabel, 0, 1);
        fromToGridPane.add(toTextField, 1, 1);

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> roomsAlterationView.showRoomList());
        Button acceptButton = new Button("Accept");
        HBox buttonPane = new HBox(closeButton, acceptButton);
        buttonPane.setAlignment(Pos.CENTER);

        getChildren().setAll(selectPeriodLabel, fromToGridPane, buttonPane);
    }

}
