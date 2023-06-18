package one.modality.hotel.backoffice.activities.accommodation;

import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.text.TextTheme;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.FontWeight;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.hotel.backoffice.icons.RoomSvgIcon;

public class RoomsAlterationRoomPane extends VBox {

    private static final FontDef ROOM_NAME_FONT = FontDef.font(FontWeight.BOLD, 15);

    public RoomsAlterationRoomPane(ResourceConfiguration rc) {
        Node icon = createIcon();
        Item item = rc.getItem();
        Label roomTypeLabel = new Label(item.getName());
        HBox topRow = new HBox(10, icon, roomTypeLabel);
        topRow.setAlignment(Pos.CENTER);

        Label roomNameLabel = new Label(rc.getName());
        TextTheme.createPrimaryTextFacet(roomNameLabel)
                .requestedFont(ROOM_NAME_FONT)
                .style();

        getChildren().setAll(topRow, roomNameLabel);
        setAlignment(Pos.CENTER);
    }

    private Node createIcon() {
        return RoomSvgIcon.createSVGPath();
    }

}
