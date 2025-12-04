package one.modality.hotel.backoffice.activities.roomsetup;

import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import one.modality.base.backoffice.mainframe.fx.FXMainFrameHeaderTabs;
import one.modality.base.client.activity.organizationdependent.OrganizationDependentViewDomainActivity;
import one.modality.base.client.tile.TabsBar;
import one.modality.hotel.backoffice.activities.roomsetup.view.BuildingsView;
import one.modality.hotel.backoffice.activities.roomsetup.view.DefaultAllocationView;
import one.modality.hotel.backoffice.activities.roomsetup.view.PoolsView;
import one.modality.hotel.backoffice.activities.roomsetup.view.RoomManagementView;


/**
 * Room Setup Activity - provides tabs for managing rooms, buildings, pools, and default allocations.
 * This is the centralized location for all room configuration functionality.
 *
 * @author David Hello
 * @author Claude Code
 */
final class RoomSetupActivity extends OrganizationDependentViewDomainActivity {

    private final RoomSetupPresentationModel pm = new RoomSetupPresentationModel();

    // Views for the 4 tabs
    private final RoomManagementView roomManagementView = new RoomManagementView(pm);
    private final BuildingsView buildingsView = new BuildingsView(pm);
    private final PoolsView poolsView = new PoolsView(pm);
    private final DefaultAllocationView defaultAllocationView = new DefaultAllocationView(pm);

    final BorderPane container = new BorderPane();
    private final TabsBar<Node> headerTabsBar = new TabsBar<>(this, container::setCenter);

    public RoomSetupActivity() {
        pm.doFXBindings();
    }

    @Override
    public Node buildUi() {
        // Creating the tabs buttons that will appear in the main frame header tabs bar (see onResume())
        headerTabsBar.setTabs(
                headerTabsBar.createTab("Room config", this::buildRoomManagementView),
                headerTabsBar.createTab("Buildings", this::buildBuildingsView),
                headerTabsBar.createTab("Pools", this::buildPoolsView),
                headerTabsBar.createTab("Default allocation", this::buildDefaultAllocationView)
        );
        // returning the container
        return container;
    }

    private Node buildRoomManagementView() {
        return roomManagementView.buildView();
    }

    private Node buildBuildingsView() {
        return buildingsView.buildView();
    }

    private Node buildPoolsView() {
        return poolsView.buildView();
    }

    private Node buildDefaultAllocationView() {
        return defaultAllocationView.buildView();
    }

    @Override
    public void onResume() {
        super.onResume();
        FXMainFrameHeaderTabs.setHeaderTabs(headerTabsBar.getTabs());
    }

    @Override
    public void onPause() {
        FXMainFrameHeaderTabs.resetToDefault();
        super.onPause();
    }

    /*==================================================================================================================
    =================================================== Logical layer ==================================================
    ==================================================================================================================*/

    @Override
    protected void startLogic() {
        roomManagementView.startLogic(this);
        buildingsView.startLogic(this);
        poolsView.startLogic(this);
        defaultAllocationView.startLogic(this);
    }

    @Override
    public RoomSetupPresentationModel getPresentationModel() {
        return pm;
    }

}
