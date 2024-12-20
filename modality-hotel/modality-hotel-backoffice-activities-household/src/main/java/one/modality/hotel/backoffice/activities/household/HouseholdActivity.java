package one.modality.hotel.backoffice.activities.household;

import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.scene.Node;
import one.modality.base.backoffice.mainframe.fx.FXEventSelector;
import one.modality.base.client.activity.organizationdependent.OrganizationDependentViewDomainActivity;
import one.modality.base.client.gantt.fx.visibility.FXGanttVisibility;
import one.modality.hotel.backoffice.accommodation.AccommodationBorderPane;
import one.modality.hotel.backoffice.accommodation.AccommodationPresentationModel;
import one.modality.hotel.backoffice.accommodation.TodayAccommodationStatus;

final class HouseholdActivity extends OrganizationDependentViewDomainActivity implements
        OperationActionFactoryMixin {

    private final AccommodationPresentationModel pm = new AccommodationPresentationModel();
    private final HouseholdView householdView = new HouseholdView(pm, this);
    private final TodayAccommodationStatus todayAccommodationStatus = new TodayAccommodationStatus(pm);

    public HouseholdActivity() {
        pm.doFXBindings();
    }

    @Override
    public Node buildUi() {
        return AccommodationBorderPane.createAccommodationBorderPane(householdView.getAttendanceGantt(), todayAccommodationStatus);
    }

    @Override
    public void onResume() {
        super.onResume();
        FXGanttVisibility.showEvents();
        FXEventSelector.showEventSelector();
    }

    @Override
    public void onPause() {
        FXGanttVisibility.resetToDefault();
        FXEventSelector.resetToDefault();
        super.onPause();
    }

    /*==================================================================================================================
    =================================================== Logical layer ==================================================
    ==================================================================================================================*/

    @Override
    protected void startLogic() {
        householdView.startLogic(this);
        todayAccommodationStatus.startLogic(this);
    }

    @Override
    public AccommodationPresentationModel getPresentationModel() {
        return pm;
    }

}
