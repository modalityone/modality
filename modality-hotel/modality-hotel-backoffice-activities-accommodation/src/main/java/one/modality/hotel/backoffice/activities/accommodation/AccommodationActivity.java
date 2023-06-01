package one.modality.hotel.backoffice.activities.accommodation;

import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import one.modality.base.client.activity.organizationdependent.OrganizationDependentViewDomainActivity;
import one.modality.base.client.gantt.fx.visibility.FXGanttVisibility;
import one.modality.base.client.gantt.fx.visibility.GanttVisibility;
import one.modality.hotel.backoffice.accommodation.AccommodationBorderPane;
import one.modality.hotel.backoffice.accommodation.AccommodationPresentationModel;
import one.modality.hotel.backoffice.accommodation.TodayAccommodationStatus;

import java.util.function.Supplier;

final class AccommodationActivity extends OrganizationDependentViewDomainActivity implements
        OperationActionFactoryMixin {

    private final AccommodationPresentationModel pm = new AccommodationPresentationModel();
    private final RoomView roomView = new RoomView(pm);
    private final GuestView guestView = new GuestView(pm);
    private final TodayAccommodationStatus todayAccommodationStatus = TodayAccommodationStatus.getOrCreate(pm);

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
        BorderPane borderPane = new BorderPane(roomView.buildCanvasContainer());
        CheckBox groupBlocksCheckBox = new CheckBox("Group blocks");
        roomView.blocksGroupingProperty.bind(groupBlocksCheckBox.selectedProperty());
        borderPane.setBottom(groupBlocksCheckBox);
        return borderPane;
    }

    private Node buildGuestView() {
        return AccommodationBorderPane.createAccommodationBorderPane(guestView.getAccommodationGantt(), todayAccommodationStatus);
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
        roomView.startLogic(this);
        guestView.startLogic(this);
        todayAccommodationStatus.startLogic(this);
    }

    @Override
    public AccommodationPresentationModel getPresentationModel() {
        return pm; // eventId and organizationId will then be updated from route
    }

}
