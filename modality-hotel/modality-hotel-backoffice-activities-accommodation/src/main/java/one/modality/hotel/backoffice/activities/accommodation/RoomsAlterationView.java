package one.modality.hotel.backoffice.activities.accommodation;

import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.util.layout.LayoutUtil;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.FontWeight;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.hotel.backoffice.accommodation.AccommodationPresentationModel;
import one.modality.hotel.backoffice.accommodation.ResourceConfigurationLoader;

/**
 * @author Bruno Salmon
 */
public class RoomsAlterationView {

    private static final int NUM_COLS = 7;
    private static final FontDef ROOM_NAME_FONT = FontDef.font(FontWeight.BOLD, 15);

    private final ResourceConfigurationLoader resourceConfigurationLoader;

    public RoomsAlterationView(AccommodationPresentationModel pm) {
        resourceConfigurationLoader = ResourceConfigurationLoader.getOrCreate(pm);
    }

    public Node buildView() {
        //VBox vBox = new VBox(10);
        GridPane gridPane = new GridPane();
        gridPane.setVgap(4);
        gridPane.setHgap(4);
        for (int i = 0; i < NUM_COLS; i++) {
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setPercentWidth(100.0 / NUM_COLS);
            gridPane.getColumnConstraints().add(columnConstraints);
        }
        //ObservableLists.bindConverted(vBox.getChildren(), resourceConfigurationLoader.getResourceConfigurations(), this::createRoomNode);
        resourceConfigurationLoader.getResourceConfigurations().addListener((ListChangeListener<? super ResourceConfiguration>) change -> {
            Platform.runLater(() -> gridPane.getChildren().clear());
            int columnIndex = 0, rowIndex = 0;
            for (ResourceConfiguration rc : resourceConfigurationLoader.getResourceConfigurations()) {
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
        });
        return LayoutUtil.createVerticalScrollPane(gridPane);
    }

    private Node createRoomNode(ResourceConfiguration rc) {
        Node icon = createIcon();
        Item item = rc.getItem();
        Label roomTypeLabel = new Label(item.getName());
        HBox topRow = new HBox(icon, roomTypeLabel);
        topRow.setAlignment(Pos.CENTER);
        Label roomNameLabel = new Label(rc.getName());
        roomNameLabel.setAlignment(Pos.CENTER);
        TextTheme.createPrimaryTextFacet(roomNameLabel)
                .requestedFont(ROOM_NAME_FONT)
                .style();
        VBox vBox = new VBox(topRow, roomNameLabel);
        vBox.setStyle("-fx-background-color: white");
        vBox.setAlignment(Pos.CENTER);
        return vBox;
    }

    private Node createIcon() {
        return new Label("<icon>");
    }

    public void startLogic(Object mixin) {
        resourceConfigurationLoader.startLogic(mixin);
    }
}
