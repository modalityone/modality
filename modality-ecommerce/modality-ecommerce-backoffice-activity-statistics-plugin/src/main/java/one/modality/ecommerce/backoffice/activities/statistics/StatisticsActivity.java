package one.modality.ecommerce.backoffice.activities.statistics;

import dev.webfx.extras.operation.action.OperationActionFactoryMixin;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import one.modality.base.backoffice.mainframe.fx.FXEventSelector;
import one.modality.base.backoffice.mainframe.fx.FXMainFrameHeaderTabs;
import one.modality.base.client.gantt.fx.visibility.FXGanttVisibility;
import one.modality.base.client.tile.TabsBar;
import one.modality.event.client.activity.eventdependent.EventDependentViewDomainActivity;

final class StatisticsActivity extends EventDependentViewDomainActivity implements
        OperationActionFactoryMixin {

    final BorderPane container = new BorderPane();
    private final TabsBar<Node> headerTabsBar = new TabsBar<>(this, container::setCenter);

    private final StatisticsGanttCanvas statisticsGanttCanvas = new StatisticsGanttCanvas();

    @Override
    public Node buildUi() {
        // Creating the tabs buttons that will appear in the main frame header tabs bar (see onResume())
        headerTabsBar.setTabs(
                headerTabsBar.createTab("Statistics", this::buildStatisticsView)
        );
        // returning the container
        return container;
    }

    private Node buildStatisticsView() {
        return statisticsGanttCanvas.buildCanvasContainer();
    }

    @Override
    public void onResume() {
        super.onResume();
        FXMainFrameHeaderTabs.setHeaderTabs(headerTabsBar.getTabs());
        FXGanttVisibility.showAllEvents();
        FXEventSelector.showEventSelector();
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
