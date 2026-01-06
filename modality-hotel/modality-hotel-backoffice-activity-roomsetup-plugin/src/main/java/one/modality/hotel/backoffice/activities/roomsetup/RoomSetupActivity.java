package one.modality.hotel.backoffice.activities.roomsetup;

import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import one.modality.base.backoffice.mainframe.fx.FXMainFrameHeaderTabs;
import one.modality.base.client.activity.organizationdependent.OrganizationDependentViewDomainActivity;
import one.modality.base.client.tile.TabsBar;
import one.modality.hotel.backoffice.activities.roomsetup.sitecomparison.view.SiteComparisonView;
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

    // Views for the 5 tabs
    private final RoomManagementView roomManagementView = new RoomManagementView(pm);
    private final BuildingsView buildingsView = new BuildingsView(pm);
    private final PoolsView poolsView = new PoolsView(pm);
    private final DefaultAllocationView defaultAllocationView = new DefaultAllocationView(pm);
    private final SiteComparisonView siteComparisonView = new SiteComparisonView(pm);

    final BorderPane container = new BorderPane();
    private final TabsBar<Node> headerTabsBar = new TabsBar<>(this, container::setCenter);

    // Lazy loading flags - track if logic has been started for each view
    private boolean roomManagementLogicStarted = false;
    private boolean buildingsLogicStarted = false;
    private boolean poolsLogicStarted = false;
    private boolean defaultAllocationLogicStarted = false;
    private boolean siteComparisonLogicStarted = false;

    public RoomSetupActivity() {
        pm.doFXBindings();
    }

    @Override
    public Node buildUi() {
        // Creating the tabs buttons that will appear in the main frame header tabs bar (see onResume())
        // Using onSelected callbacks for lazy loading - view logic starts only when tab is first selected
        headerTabsBar.setTabs(
                headerTabsBar.createTab("Room config", this::buildRoomManagementView, this::onRoomManagementTabSelected),
                headerTabsBar.createTab("Buildings", this::buildBuildingsView, this::onBuildingsTabSelected),
                headerTabsBar.createTab("Pools", this::buildPoolsView, this::onPoolsTabSelected),
                headerTabsBar.createTab("Default allocation", this::buildDefaultAllocationView, this::onDefaultAllocationTabSelected),
                headerTabsBar.createTab("Site comparison", this::buildSiteComparisonView, this::onSiteComparisonTabSelected)
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

    private Node buildSiteComparisonView() {
        return siteComparisonView.buildView();
    }

    // Tab selection callbacks for lazy loading - start view logic only on first selection
    private void onRoomManagementTabSelected() {
        if (!roomManagementLogicStarted) {
            roomManagementView.startLogic(this);
            roomManagementLogicStarted = true;
        }
    }

    private void onBuildingsTabSelected() {
        if (!buildingsLogicStarted) {
            buildingsView.startLogic(this);
            buildingsLogicStarted = true;
        }
    }

    private void onPoolsTabSelected() {
        if (!poolsLogicStarted) {
            poolsView.startLogic(this);
            poolsLogicStarted = true;
        }
    }

    private void onDefaultAllocationTabSelected() {
        if (!defaultAllocationLogicStarted) {
            defaultAllocationView.startLogic(this);
            defaultAllocationLogicStarted = true;
        }
    }

    private void onSiteComparisonTabSelected() {
        if (!siteComparisonLogicStarted) {
            siteComparisonView.startLogic(this);
            siteComparisonLogicStarted = true;
        }
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
        // Lazy loading optimization: Views start logic only when their tab is first selected.
        // This keeps the DOM light and improves browser responsiveness after GWT compilation.
        // The first tab (Room config) is selected automatically by TabsBar.addTabs(),
        // which triggers onRoomManagementTabSelected() and starts that view's logic.
    }

    @Override
    public RoomSetupPresentationModel getPresentationModel() {
        return pm;
    }

}
