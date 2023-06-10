package one.modality.hotel.backoffice.activities.accommodation;

import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import one.modality.base.shared.entities.Item;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;
import one.modality.hotel.backoffice.accommodation.AccommodationPresentationModel;

class RoomsAlterationBorderPane {

    private static final String MSG_CLICK_ON_ROOM_TO_SELECT_IT = "Click on a room to select it.";
    private static final FontDef SELECT_ROOM_TYPE_FONT = FontDef.font(FontWeight.BOLD, 15);

    public static BorderPane createAccommodationBorderPane(RoomsAlterationView roomsAlterationView, AccommodationPresentationModel pm, AccommodationActivity activity) {
        Node body = roomsAlterationView.buildView();
        BorderPane borderPane = new BorderPane(body);

        EntityButtonSelector<Item> roomTypeSelector = new EntityButtonSelector<>(
                "{class: 'Item', alias: 'i', where: 'family.code=`acco`'}",
                activity, activity.container, activity.getDataSourceModel()
        );
        ReactiveVisualMapper<Item> entityDialogMapper = roomTypeSelector.getEntityDialogMapper();
        entityDialogMapper
                .always(FXOrganizationId.organizationIdProperty(), orgId -> DqlStatement.where("exists(select ScheduledResource where configuration.(item=i and resource.site.organization=?))", Entities.getPrimaryKey(orgId)))
        ;
        roomsAlterationView.roomTypeProperty().bind(roomTypeSelector.selectedItemProperty());

        Label selectRoomTypeLabel = new Label("Select the room type!");
        TextTheme.createPrimaryTextFacet(selectRoomTypeLabel)
                .requestedFont(SELECT_ROOM_TYPE_FONT)
                .style();

        Label selectedRoomLabel = new Label(MSG_CLICK_ON_ROOM_TO_SELECT_IT);

        Button roomStatusButton = new Button("Room status");
        roomStatusButton.setOnAction(e -> roomsAlterationView.showRoomStatusDateSelection());

        Button roomAlterationButton = new Button("Alter Room");
        roomAlterationButton.setDisable(true);
        roomAlterationButton.setOnAction(e -> roomsAlterationView.showAlterRoom());

        roomsAlterationView.selectedRoomProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                selectedRoomLabel.setText(MSG_CLICK_ON_ROOM_TO_SELECT_IT);
            } else {
                selectedRoomLabel.setText(newValue.getName() + " selected.");
            }
            roomAlterationButton.setDisable(newValue == null);
        });

        HBox bottomBar = new HBox(10, selectRoomTypeLabel, roomTypeSelector.getButton(), selectedRoomLabel, roomStatusButton, roomAlterationButton);
        bottomBar.setBackground(new Background(new BackgroundFill(Color.web("#e0dcdc"), null, null)));
        bottomBar.setAlignment(Pos.CENTER_LEFT);
        borderPane.setBottom(bottomBar);

        return borderPane;
    }

}
