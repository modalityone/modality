package one.modality.hotel.backoffice.activities.household;

import dev.webfx.extras.operation.action.OperationActionFactoryMixin;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import one.modality.base.backoffice.mainframe.fx.FXMainFrameHeaderTabs;
import one.modality.base.client.activity.organizationdependent.OrganizationDependentViewDomainActivity;
import one.modality.base.client.tile.TabsBar;
import one.modality.hotel.backoffice.accommodation.AccommodationPresentationModel;
import one.modality.hotel.backoffice.accommodation.TodayAccommodationStatus;
import one.modality.hotel.backoffice.activities.household.dashboard.view.HouseholdDashboardView;
import one.modality.hotel.backoffice.activities.household.gantt.view.HouseholdGanttView;

public final class HouseholdActivity extends OrganizationDependentViewDomainActivity implements
        OperationActionFactoryMixin {

    private final AccommodationPresentationModel pm = new AccommodationPresentationModel();
    private final HouseholdDashboardView householdView = new HouseholdDashboardView(pm, this);
    private final HouseholdGanttView householdGanttView = new HouseholdGanttView(pm);
    private final TodayAccommodationStatus todayAccommodationStatus = new TodayAccommodationStatus(pm);

    private final BorderPane container = new BorderPane();
    private final TabsBar<Node> headerTabsBar = new TabsBar<>(this, container::setCenter);

    public HouseholdActivity() {
        pm.doFXBindings();
    }

    @Override
    public Node buildUi() {
        headerTabsBar.setTabs(
                headerTabsBar.createTab("Dashboard", householdView::buildUi),
                headerTabsBar.createTab("Gantt", householdGanttView::getNode));
        return container;
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

    /*
     * =============================================================================
     * =====================================
     * =================================================== Logical layer
     * ==================================================
     * =============================================================================
     * =====================================
     */

    @Override
    protected void startLogic() {
        householdView.startLogic(this);
        householdGanttView.startLogic(this);
        todayAccommodationStatus.startLogic(this);
    }

    @Override
    public AccommodationPresentationModel getPresentationModel() {
        return pm;
    }

}
