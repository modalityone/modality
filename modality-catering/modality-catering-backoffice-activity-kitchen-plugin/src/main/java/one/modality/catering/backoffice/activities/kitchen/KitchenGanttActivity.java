package one.modality.catering.backoffice.activities.kitchen;

import dev.webfx.extras.operation.action.OperationActionFactoryMixin;
import javafx.scene.Node;
import one.modality.base.backoffice.mainframe.fx.FXEventSelector;
import one.modality.base.backoffice.mainframe.fx.FXMainFrameHeaderTabs;
import one.modality.base.client.gantt.fx.visibility.FXGanttVisibility;
import one.modality.event.client.activity.eventdependent.EventDependentViewDomainActivity;

final class KitchenGanttActivity extends EventDependentViewDomainActivity implements
        OperationActionFactoryMixin {


    private final KitchenGantt statisticsGanttCanvas = new KitchenGantt();

    @Override
    public Node buildUi() {
        // returning the container
        return statisticsGanttCanvas.buildCanvasContainer();
    }

    @Override
    public void onResume() {
        super.onResume();
        FXGanttVisibility.showOnsiteAccommodationEvents();
        FXEventSelector.hideEventSelector();
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
        statisticsGanttCanvas.startLogic(this);
    }

}
