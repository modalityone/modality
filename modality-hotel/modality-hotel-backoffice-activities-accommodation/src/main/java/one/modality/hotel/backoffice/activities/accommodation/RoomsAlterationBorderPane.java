package one.modality.hotel.backoffice.activities.accommodation;

import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.ui.controls.dialog.DialogContent;
import dev.webfx.stack.ui.controls.dialog.DialogUtil;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.ItemFamily;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;
import one.modality.hotel.backoffice.accommodation.AccommodationPresentationModel;

import java.time.LocalDate;

class RoomsAlterationBorderPane {

    private static final String MSG_CLICK_ON_ROOM_TO_EDIT_IT = "Click on a room to edit it.";
    private static final FontDef SELECT_ROOM_TYPE_FONT = FontDef.font(FontWeight.BOLD, 15);

    public static BorderPane createAccommodationBorderPane(RoomsAlterationView roomsAlterationView, AccommodationPresentationModel pm, AccommodationActivity activity) {
        Node body = roomsAlterationView.buildView();
        BorderPane borderPane = new BorderPane(body);

        EntityButtonSelector<Item> roomTypeSelector = new EntityButtonSelector<Item>(
                "{class: 'Item', alias: 'i', where: 'family.code=`acco`'}",
                activity, activity.container, activity.getDataSourceModel()
        )
                .always(FXOrganizationId.organizationIdProperty(), orgId -> DqlStatement.where("exists(select ScheduledResource where configuration.(item=i and resource.site.organization=?))", Entities.getPrimaryKey(orgId)))
                .setAutoOpenOnMouseEntered(true)
                .appendNullEntity(true);
        // Creating the null Entity (the entity the selector will use to display null) to say "<All>"
        EntityStore store = roomTypeSelector.getStore();
        Item allItem = store.createEntity(Item.class);
        allItem.setName("<All>");
        ItemFamily accoFamily = store.createEntity(ItemFamily.class);
        accoFamily.setCode("acco");
        allItem.setFamily(accoFamily);
        roomTypeSelector.setVisualNullEntity(allItem);

        roomsAlterationView.roomTypeProperty().bind(roomTypeSelector.selectedItemProperty());

        Label selectRoomTypeLabel = new Label("Select the room type!");
        TextTheme.createPrimaryTextFacet(selectRoomTypeLabel)
                .requestedFont(SELECT_ROOM_TYPE_FONT)
                .style();

        Label selectedRoomLabel = new Label(MSG_CLICK_ON_ROOM_TO_EDIT_IT);

        Button roomStatusButton = new Button("Room status");
        roomStatusButton.setOnAction(e -> {
            RoomStatusDateSelectionPane roomStatusDateSelectionPane = new RoomStatusDateSelectionPane();
            DialogContent dialogContent = new DialogContent().setContent(roomStatusDateSelectionPane);
            DialogUtil.showModalNodeInGoldLayout(dialogContent, borderPane);
            DialogUtil.armDialogContentButtons(dialogContent, dialogCallback -> {
                dialogCallback.closeDialog();
                showRoomStatusDialog(roomStatusDateSelectionPane, roomsAlterationView);
            });
        });

        HBox bottomBar = new HBox(10, selectRoomTypeLabel, roomTypeSelector.getButton(), selectedRoomLabel, roomStatusButton);
        bottomBar.setBackground(new Background(new BackgroundFill(Color.web("#e0dcdc"), null, null)));
        bottomBar.setAlignment(Pos.CENTER_LEFT);
        borderPane.setBottom(bottomBar);

        return borderPane;
    }

    private static void showRoomStatusDialog(RoomStatusDateSelectionPane roomStatusDateSelectionPane, RoomsAlterationView roomsAlterationView) {
        LocalDate from = roomStatusDateSelectionPane.getFrom();
        LocalDate to = roomStatusDateSelectionPane.getTo();
        roomsAlterationView.showRoomStatusDialog(from, to);
    }
}
