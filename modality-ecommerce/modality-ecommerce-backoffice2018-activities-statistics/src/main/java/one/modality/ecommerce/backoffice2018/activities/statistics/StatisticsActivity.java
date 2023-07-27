package one.modality.ecommerce.backoffice2018.activities.statistics;

import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.scene.Node;
import one.modality.base.client.activity.eventdependent.EventDependentViewDomainActivity;
import one.modality.base.client.gantt.fx.visibility.FXGanttVisibility;
import one.modality.base.client.gantt.fx.visibility.GanttVisibility;

final class StatisticsActivity extends EventDependentViewDomainActivity implements
        OperationActionFactoryMixin {

    private final StatisticsGanttCanvas statisticsGanttCanvas = new StatisticsGanttCanvas();

    @Override
    public Node buildUi() {
        return statisticsGanttCanvas.buildCanvasContainer();
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
        statisticsGanttCanvas.startLogic(this);
    }

}
