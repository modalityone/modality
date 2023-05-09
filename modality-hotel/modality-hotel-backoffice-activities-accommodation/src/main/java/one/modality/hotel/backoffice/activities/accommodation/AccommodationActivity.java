package one.modality.hotel.backoffice.activities.accommodation;

import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import one.modality.base.client.activity.organizationdependent.OrganizationDependentViewDomainActivity;
import one.modality.base.client.gantt.fx.visibility.FXGanttVisibility;
import one.modality.base.client.gantt.fx.visibility.GanttVisibility;

final class AccommodationActivity extends OrganizationDependentViewDomainActivity implements
        OperationActionFactoryMixin {

    private final AccommodationGanttCanvas accommodationGanttCanvas = new AccommodationGanttCanvas();

    @Override
    public Node buildUi() {
        BorderPane borderPane = new BorderPane(accommodationGanttCanvas.buildCanvasContainer());
        return borderPane;
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
        accommodationGanttCanvas.startLogic(this);
    }

    private final AccommodationPresentationModel pm = new AccommodationPresentationModel();

    @Override
    public AccommodationPresentationModel getPresentationModel() {
        super.getPresentationModel();
        return pm; // eventId and organizationId will then be updated from route
    }

}
