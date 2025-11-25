package one.modality.hotel.backoffice.activities.household;

import dev.webfx.extras.operation.action.OperationActionFactoryMixin;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import one.modality.base.backoffice.mainframe.fx.FXEventSelector;
import one.modality.base.backoffice.mainframe.fx.FXMainFrameHeaderTabs;
import one.modality.base.client.activity.organizationdependent.OrganizationDependentViewDomainActivity;
import one.modality.base.client.gantt.fx.visibility.FXGanttVisibility;
import one.modality.base.client.tile.TabsBar;
import one.modality.hotel.backoffice.accommodation.AccommodationPresentationModel;
import one.modality.hotel.backoffice.accommodation.TodayAccommodationStatus;
import one.modality.hotel.backoffice.activities.household.dashboard.view.HouseholdDashboardView;
import one.modality.hotel.backoffice.activities.household.gantt.canvas.HouseholdCanvasGanttView;

public final class HouseholdActivity extends OrganizationDependentViewDomainActivity implements
        OperationActionFactoryMixin {

    private final AccommodationPresentationModel pm = new AccommodationPresentationModel();
    private final HouseholdDashboardView householdView = new HouseholdDashboardView(pm, this);
    // CANVAS-BASED: Using new Canvas implementation (EventsGanttCanvas managed by FXGanttVisibility)
    private final HouseholdCanvasGanttView householdGanttView = new HouseholdCanvasGanttView(pm);
    private final TodayAccommodationStatus todayAccommodationStatus = new TodayAccommodationStatus(pm);

    private final BorderPane container = new BorderPane();
    private final TabsBar<Node> headerTabsBar = new TabsBar<>(this, container::setCenter);

    public HouseholdActivity() {
        pm.doFXBindings();
    }

    @Override
    public Node buildUi() {
        headerTabsBar.setTabs(
                headerTabsBar.createTab("Dashboard", this::buildDashboardTab),
                headerTabsBar.createTab("Gantt", this::buildGanttTab));
        return container;
    }

    /**
     * Builds the Dashboard tab and hides EventsGanttCanvas.
     */
    private Node buildDashboardTab() {
        FXGanttVisibility.resetToDefault(); // Hides events gantt
        FXEventSelector.hideEventSelector();
        return householdView.buildUi();
    }

    /**
     * Builds the Gantt tab and shows EventsGanttCanvas.
     */
    private Node buildGanttTab() {
        FXGanttVisibility.showEvents();
        FXEventSelector.showEventSelector();
        return householdGanttView.getNode();
    }

    @Override
    public void onResume() {
        super.onResume();
        FXMainFrameHeaderTabs.setHeaderTabs(headerTabsBar.getTabs());
        // Don't show events here - let tab builders control visibility
    }

    @Override
    public void onPause() {
        FXMainFrameHeaderTabs.resetToDefault();
        FXGanttVisibility.resetToDefault();
        FXEventSelector.resetToDefault();
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
