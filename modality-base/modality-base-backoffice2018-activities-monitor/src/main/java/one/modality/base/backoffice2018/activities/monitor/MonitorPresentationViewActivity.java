package one.modality.base.backoffice2018.activities.monitor;

import javafx.scene.Node;
import javafx.scene.layout.VBox;
import dev.webfx.stack.routing.uirouter.activity.presentation.view.impl.PresentationViewActivityImpl;
import dev.webfx.extras.visual.controls.charts.VisualChart;
import dev.webfx.extras.visual.controls.charts.VisualLineChart;

import static dev.webfx.extras.util.layout.LayoutUtil.setVGrowable;

/**
 * @author Bruno Salmon
 */
final class MonitorPresentationViewActivity extends PresentationViewActivityImpl<MonitorPresentationModel> {

    private VisualChart memoryChart;
    private VisualChart cpuChart;

    @Override
    protected void createViewNodes(MonitorPresentationModel pm) {
        memoryChart = setVGrowable(new VisualLineChart());
        cpuChart = setVGrowable(new VisualLineChart());

        memoryChart.visualResultProperty().bind(pm.memoryVisualResultProperty());
        cpuChart.visualResultProperty().bind(pm.cpuVisualResultProperty());
    }

    @Override
    protected Node assemblyViewNodes() {
        return new VBox(memoryChart, cpuChart);
    }
}
