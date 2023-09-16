package one.modality.hotel.backoffice.activities.accommodation;

import dev.webfx.extras.theme.Facet;
import dev.webfx.extras.theme.luminance.LuminanceFacetCategory;
import dev.webfx.extras.util.layout.LayoutUtil;
import dev.webfx.stack.ui.controls.button.ButtonFactoryMixin;
import dev.webfx.stack.ui.dialog.DialogCallback;
import dev.webfx.stack.ui.controls.dialog.DialogContent;
import dev.webfx.stack.ui.controls.dialog.DialogBuilderUtil;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import one.modality.base.backoffice.activities.mainframe.fx.FXMainFrame;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.hotel.backoffice.accommodation.AccommodationPresentationModel;
import one.modality.hotel.backoffice.accommodation.ResourceConfigurationLoader;

import java.time.LocalDate;

/**
 * @author Bruno Salmon
 */
public class RoomsAlterationView {

    private static final int NUM_COLS = 7;

    private final ResourceConfigurationLoader resourceConfigurationLoader;

    private final ObjectProperty<Item> roomTypeProperty = new SimpleObjectProperty<>();
    public ObjectProperty<Item> roomTypeProperty() { return roomTypeProperty; }

    private final RoomStatusView roomStatusView;
    private final AlterRoomPane alterRoomPane;

    private GridPane roomListPane;
    private ScrollPane scrollPane;

    public RoomsAlterationView(AccommodationPresentationModel pm, ButtonFactoryMixin mixin) {
        resourceConfigurationLoader = ResourceConfigurationLoader.getOrCreate(pm);
        roomStatusView = new RoomStatusView(pm);
        alterRoomPane = new AlterRoomPane(pm, mixin);
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

        scrollPane = LayoutUtil.createVerticalScrollPane(roomListPane);
        return scrollPane;
    }

    public void showRoomList() {
        scrollPane.setContent(roomListPane);
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
        RoomsAlterationRoomPane roomsAlterationRoomPane = new RoomsAlterationRoomPane(rc);
        roomsAlterationRoomPane.setOnMouseClicked(e -> {
            alterRoomPane.resourceConfigurationProperty().set(rc);
            DialogContent dialogContent = new DialogContent().setContent(alterRoomPane);
            DialogBuilderUtil.showModalNodeInGoldLayout(dialogContent, FXMainFrame.getDialogArea());
            DialogBuilderUtil.armDialogContentButtons(dialogContent, DialogCallback::closeDialog);
        });
        return createPrimaryPanelFacet(roomsAlterationRoomPane).getContainerNode(); // TODO replace this with a call to a Theme class
    }

    private static Facet createPrimaryPanelFacet(Region panel) {
        return new Facet(LuminanceFacetCategory.PRIMARY_PANEL_FACET, panel)
                .setRounded(true)
                .setBordered(true);
    }

    public void startLogic(Object mixin) {
        resourceConfigurationLoader.startLogic(mixin);
        roomStatusView.startLogic(mixin);
        alterRoomPane.startLogic(mixin);
    }

    public void showRoomStatusDialog(LocalDate from, LocalDate to) {
        roomStatusView.fromProperty().set(from);
        roomStatusView.toProperty().set(to);
        Node roomStatusNode = roomStatusView.buildView();
        DialogContent dialogContent = new DialogContent().setContent(roomStatusNode);
        DialogBuilderUtil.showModalNodeInGoldLayout(dialogContent, (Pane) scrollPane.getParent());
        DialogBuilderUtil.armDialogContentButtons(dialogContent, dialogCallback -> dialogCallback.closeDialog());
    }
}
