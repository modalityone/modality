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
                headerTabsBar.createTab("Dashboard", this::buildDashboardTab, this::onDashboardTabSelected),
                headerTabsBar.createTab("Gantt", this::buildGanttTab, this::onGanttTabSelected));
        return container;
    }

    /**
     * Builds the Dashboard tab content (called once, then cached).
     */
    private Node buildDashboardTab() {
        return householdView.buildUi();
    }

    /**
     * Called every time the Dashboard tab is selected (including when switching back to it).
     * Hides the EventsGanttCanvas.
     */
    private void onDashboardTabSelected() {
        FXGanttVisibility.resetToDefault(); // Hides events gantt
        FXEventSelector.hideEventSelector();
    }

    /**
     * Builds the Gantt tab content (called once, then cached).
     */
    private Node buildGanttTab() {
        return householdGanttView.getNode();
    }

    /**
     * Called every time the Gantt tab is selected (including when switching back to it).
     * Shows the EventsGanttCanvas.
     */
    private void onGanttTabSelected() {
        FXGanttVisibility.showEvents();
        FXEventSelector.showEventSelector();
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
