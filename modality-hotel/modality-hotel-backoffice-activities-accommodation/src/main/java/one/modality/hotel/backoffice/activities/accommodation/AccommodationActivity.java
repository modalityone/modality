package one.modality.hotel.backoffice.activities.accommodation;

import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import one.modality.base.client.activity.organizationdependent.OrganizationDependentViewDomainActivity;
import one.modality.base.client.gantt.fx.visibility.FXGanttVisibility;
import one.modality.base.client.gantt.fx.visibility.GanttVisibility;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.ScheduledResource;
import one.modality.hotel.backoffice.accommodation.AccommodationStatusBarUpdater;
import one.modality.hotel.backoffice.accommodation.AccommodationPresentationModel;
import one.modality.hotel.backoffice.accommodation.AccommodationStatusBar;
import one.modality.hotel.backoffice.accommodation.AttendeeLegend;

import java.util.List;
import java.util.function.Supplier;

final class AccommodationActivity extends OrganizationDependentViewDomainActivity implements
        AccommodationStatusBarUpdater,
        OperationActionFactoryMixin {

    private final AccommodationPresentationModel pm = new AccommodationPresentationModel();
    private final RoomViewGanttCanvas roomViewGanttCanvas = new RoomViewGanttCanvas(pm);
    private final AccommodationGanttCanvas accommodationGanttCanvas = new AccommodationGanttCanvas(pm, this);
    private final AttendeeLegend attendeeLegend = new AttendeeLegend();
    private final AccommodationStatusBar accommodationStatusBar = new AccommodationStatusBar();

    public AccommodationActivity() {
        pm.doFXBindings();
    }

    @Override
    public Node buildUi() {
        TabPane tabPane = new TabPane();
        tabPane.getTabs().setAll(
                createTab("Rooms", this::buildRoomView),
                createTab("Guests", this::buildGuestView)
        );
        return tabPane;
    }

    private Tab createTab(String text, Supplier<Node> nodeSupplier) {
        Tab tab = new Tab(text);
        tab.setContent(nodeSupplier.get());
        tab.setClosable(false);
        return tab;
    }

    private Node buildRoomView() {
        BorderPane borderPane = new BorderPane(roomViewGanttCanvas.buildCanvasContainer());
        CheckBox groupBlocksCheckBox = new CheckBox("Group blocks");
        roomViewGanttCanvas.blocksGroupingProperty.bind(groupBlocksCheckBox.selectedProperty());
        borderPane.setBottom(groupBlocksCheckBox);
        return borderPane;
    }

    private Node buildGuestView() {
        BorderPane borderPane = new BorderPane(accommodationGanttCanvas.buildCanvasContainer());

        CheckBox allRoomsCheckBox = new CheckBox("All rooms");
        allRoomsCheckBox.setSelected(false);
        accommodationGanttCanvas.parentsProvidedProperty().bind(allRoomsCheckBox.selectedProperty());

        CheckBox showKeyCheckBox = new CheckBox("Show Legend");
        showKeyCheckBox.setOnAction(e -> {
            borderPane.setLeft(showKeyCheckBox.isSelected() ? attendeeLegend : null);
        });
        HBox bottomPane = new HBox(10, allRoomsCheckBox, showKeyCheckBox, accommodationStatusBar);
        bottomPane.setBackground(new Background(new BackgroundFill(Color.web("#e0dcdc"), null, null)));
        bottomPane.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(accommodationStatusBar, Priority.ALWAYS);
        borderPane.setBottom(bottomPane);

        return borderPane;
    }

    @Override
    public void setEntities(List<Attendance> attendances) {
        accommodationStatusBar.setEntities(attendances);
    }

    @Override
    public void setAllScheduledResource(List<ScheduledResource> allScheduledResource) {
        accommodationStatusBar.setAllScheduledResource(allScheduledResource);
    }

    @Override
    public void onResume() {
        super.onResume();
        FXGanttVisibility.setGanttVisibility(GanttVisibility.EVENTS);
    }

    @Override
    public void onPause() {
        FXGanttVisibility.setGanttVisibility(GanttVisibility.HIDDEN);
        super.onPause();
    }

    /*==================================================================================================================
    =================================================== Logical layer ==================================================
    ==================================================================================================================*/

    @Override
    protected void startLogic() {
        roomViewGanttCanvas.startLogic(this);
        accommodationGanttCanvas.startLogic(this);
    }

    @Override
    public AccommodationPresentationModel getPresentationModel() {
        return pm; // eventId and organizationId will then be updated from route
    }

}
