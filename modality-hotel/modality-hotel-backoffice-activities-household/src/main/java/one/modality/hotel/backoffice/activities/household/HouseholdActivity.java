package one.modality.hotel.backoffice.activities.household;

import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.scene.Node;
import one.modality.base.client.activity.organizationdependent.OrganizationDependentViewDomainActivity;
import one.modality.base.client.gantt.fx.visibility.FXGanttVisibility;
import one.modality.base.client.gantt.fx.visibility.GanttVisibility;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.ScheduledResource;
import one.modality.hotel.backoffice.accommodation.AccommodationPresentationModel;
import one.modality.hotel.backoffice.accommodation.AccommodationStatusBar;
import one.modality.hotel.backoffice.accommodation.AccommodationStatusBarUpdater;

import java.util.List;

final class HouseholdActivity extends OrganizationDependentViewDomainActivity implements
        AccommodationStatusBarUpdater,
        OperationActionFactoryMixin {

    private final AccommodationPresentationModel pm = new AccommodationPresentationModel();
    private final HouseholdView householdView = new HouseholdView(pm, this);
    private final AccommodationStatusBar accommodationStatusBar = new AccommodationStatusBar();

    public HouseholdActivity() {
        pm.doFXBindings();
    }

    @Override
    public Node buildUi() {
        return accommodationStatusBar.createAccommodationViewWithStatusBar(householdView);
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
        householdView.startLogic(this);
    }

    @Override
    public AccommodationPresentationModel getPresentationModel() {
        return pm;
    }

}
