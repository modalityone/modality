package one.modality.hotel.backoffice.activities.accommodation;

import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.util.layout.LayoutUtil;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.FontWeight;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.hotel.backoffice.accommodation.AccommodationPresentationModel;
import one.modality.hotel.backoffice.accommodation.ResourceConfigurationLoader;
import one.modality.hotel.backoffice.icons.RoomSvgIcon;

/**
 * @author Bruno Salmon
 */
public class RoomsAlterationView {

    private static final int NUM_COLS = 7;
    private static final FontDef ROOM_NAME_FONT = FontDef.font(FontWeight.BOLD, 15);

    private final ResourceConfigurationLoader resourceConfigurationLoader;

    private final ObjectProperty<Item> roomTypeProperty = new SimpleObjectProperty<>();
    public ObjectProperty<Item> roomTypeProperty() { return roomTypeProperty; }

    private final Property<ResourceConfiguration> selectedRoomProperty = new SimpleObjectProperty<>();
    public Property<ResourceConfiguration> selectedRoomProperty() { return selectedRoomProperty; }

    private GridPane roomListPane;
    private RoomStatusPane roomStatusPane;
    private ScrollPane scrollPane;

    public RoomsAlterationView(AccommodationPresentationModel pm) {
        resourceConfigurationLoader = ResourceConfigurationLoader.getOrCreate(pm);
    }

    public Node buildView() {
        roomListPane = new GridPane();
        roomListPane.setVgap(4);
        roomListPane.setHgap(4);
        for (int i = 0; i < NUM_COLS; i++) {
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setPercentWidth(100.0 / NUM_COLS);
            roomListPane.getColumnConstraints().add(columnConstraints);
        }
        resourceConfigurationLoader.getResourceConfigurations().addListener((ListChangeListener<? super ResourceConfiguration>) change -> addRoomNodes(roomListPane));
        roomTypeProperty.addListener(((observableValue, oldValue, newValue) -> addRoomNodes(roomListPane)));

        roomStatusPane = new RoomStatusPane(this);

        return scrollPane = LayoutUtil.createVerticalScrollPane(roomListPane);
    }

    public void showRoomList() {
        scrollPane.setContent(roomListPane);
    }

    public void showRoomStatus() {
        scrollPane.setContent(roomStatusPane);
    }

    private void addRoomNodes(GridPane gridPane) {
        Platform.runLater(() -> gridPane.getChildren().clear());
        int columnIndex = 0, rowIndex = 0;
        for (ResourceConfiguration rc : resourceConfigurationLoader.getResourceConfigurations()) {
            if (!matchesRoomType(rc)) {
                continue;
            }
            Node roomNode = createRoomNode(rc);
            final int finalColumnIndex = columnIndex;
            final int finalRowIndex = rowIndex;
            Platform.runLater(() -> gridPane.add(roomNode, finalColumnIndex, finalRowIndex));
            columnIndex++;
            if (columnIndex >= NUM_COLS) {
                rowIndex++;
                columnIndex = 0;
            }
        }
    }

    private boolean matchesRoomType(ResourceConfiguration rc) {
        Item requiredRoomType = roomTypeProperty().get();
        return requiredRoomType == null || requiredRoomType.equals(rc.getItem());
    }

    private Node createRoomNode(ResourceConfiguration rc) {
        Node icon = createIcon();
        Item item = rc.getItem();
        Label roomTypeLabel = new Label(item.getName());
        HBox topRow = new HBox(10, icon, roomTypeLabel);
        topRow.setAlignment(Pos.CENTER);
        Label roomNameLabel = new Label(rc.getName());
        roomNameLabel.setAlignment(Pos.CENTER);
        TextTheme.createPrimaryTextFacet(roomNameLabel)
                .requestedFont(ROOM_NAME_FONT)
                .style();
        VBox vBox = new VBox(topRow, roomNameLabel);
        vBox.setStyle("-fx-background-color: white");
        vBox.setAlignment(Pos.CENTER);
        vBox.setOnMouseClicked(e -> selectedRoomProperty.setValue(rc));
        return vBox;
    }

    private Node createIcon() {
        return RoomSvgIcon.createSVGPath();
    }

    public void startLogic(Object mixin) {
        resourceConfigurationLoader.startLogic(mixin);
    }
}
