package one.modality.hotel.backoffice.activities.accommodation;

import dev.webfx.extras.operation.action.OperationActionFactoryMixin;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.BorderPane;
import one.modality.base.backoffice.mainframe.fx.FXEventSelector;
import one.modality.base.backoffice.mainframe.fx.FXMainFrameHeaderTabs;
import one.modality.base.client.activity.organizationdependent.OrganizationDependentViewDomainActivity;
import one.modality.base.client.gantt.fx.visibility.FXGanttVisibility;
import one.modality.base.client.tile.TabsBar;
import one.modality.hotel.backoffice.accommodation.AccommodationBorderPane;
import one.modality.hotel.backoffice.accommodation.AccommodationPresentationModel;
import one.modality.hotel.backoffice.accommodation.TodayAccommodationStatus;

final class AccommodationActivity extends OrganizationDependentViewDomainActivity implements
        OperationActionFactoryMixin {

    private final AccommodationPresentationModel pm = new AccommodationPresentationModel();
    private final RoomView roomView = new RoomView(pm);
    private final GuestView guestView = new GuestView(pm);

    private final RoomsAlterationView roomsAlterationView = new RoomsAlterationView(pm, this);
    private final TodayAccommodationStatus todayAccommodationStatus = new TodayAccommodationStatus(pm);
    final BorderPane container = new BorderPane();
    private final TabsBar<Node> headerTabsBar = new TabsBar<>(this, container::setCenter);

    public AccommodationActivity() {
        pm.doFXBindings();
    }

    @Override
    public Node buildUi() {
        // Creating the tabs buttons that will appear in the main frame header tabs bar (see onResume())
        headerTabsBar.setTabs(
                headerTabsBar.createTab("Rooms", this::buildRoomView),
                headerTabsBar.createTab("Guests", this::buildGuestView),
                headerTabsBar.createTab("Rooms alteration", this::buildRoomsAlterationView)
        );
        // returning the container
        return container;
    }

    private Node buildRoomView() {
        BorderPane borderPane = new BorderPane(roomView.buildCanvasContainer());
        CheckBox groupBlocksCheckBox = new CheckBox("Group blocks");
        roomView.blocksGroupingProperty().bind(groupBlocksCheckBox.selectedProperty());
        borderPane.setBottom(groupBlocksCheckBox);
        return borderPane;
    }

    private Node buildGuestView() {
        return AccommodationBorderPane.createAccommodationBorderPane(guestView.getAttendanceGantt(), todayAccommodationStatus);
    }

    private Node buildRoomsAlterationView() {
        return RoomsAlterationBorderPane.createAccommodationBorderPane(roomsAlterationView, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        FXMainFrameHeaderTabs.setHeaderTabs(headerTabsBar.getTabs());
        FXGanttVisibility.showOnsiteAccommodationEvents();
        FXEventSelector.showEventSelector();
    }

    @Override
    public void onPause() {
        FXMainFrameHeaderTabs.resetToDefault();
        FXGanttVisibility.resetToDefault();
        FXEventSelector.resetToDefault();
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
        roomsAlterationView.startLogic(this);
    }

    @Override
    public AccommodationPresentationModel getPresentationModel() {
        return pm;
    }

}
