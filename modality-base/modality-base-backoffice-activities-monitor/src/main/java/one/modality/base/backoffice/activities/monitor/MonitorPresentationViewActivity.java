package one.modality.base.backoffice.activities.monitor;

import static dev.webfx.extras.util.layout.LayoutUtil.setVGrowable;

import dev.webfx.extras.visual.controls.charts.VisualChart;
import dev.webfx.extras.visual.controls.charts.VisualLineChart;
import dev.webfx.stack.routing.uirouter.activity.presentation.view.impl.PresentationViewActivityImpl;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

/**
 * @author Bruno Salmon
 */
final class MonitorPresentationViewActivity
    extends PresentationViewActivityImpl<MonitorPresentationModel> {

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
