package one.modality.base.backoffice.ganttcanvas.spi.impl.dated;

import javafx.scene.layout.Region;
import one.modality.base.backoffice.ganttcanvas.DatedGanttCanvas;
import one.modality.base.backoffice.ganttcanvas.spi.MainFrameGanttCanvasProvider;

/**
 * @author Bruno Salmon
 */
public final class MainFrameDatedGanttCanvasProvider implements MainFrameGanttCanvasProvider {

    private final DatedGanttCanvas datedGanttCanvas = new DatedGanttCanvas();
    @Override
    public Region getCanvasContainer() {
        return datedGanttCanvas.getCanvasContainer();
    }

    @Override
    public void setupFXBindingsAndStartLogic(Object mixin) {
        datedGanttCanvas.setupFXBindings();
    }
}
