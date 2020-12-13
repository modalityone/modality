package mongoose.backend.activities.monitor;

import javafx.scene.Node;
import javafx.scene.layout.VBox;
import dev.webfx.framework.client.activity.impl.elementals.presentation.view.impl.PresentationViewActivityImpl;
import dev.webfx.extras.visual.controls.charts.VisualChart;
import dev.webfx.extras.visual.controls.charts.VisualLineChart;

import static dev.webfx.framework.client.ui.util.layout.LayoutUtil.setVGrowable;

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
