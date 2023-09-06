package one.modality.base.backoffice.ganttcanvas.spi;

import javafx.scene.layout.Region;

public interface MainFrameGanttCanvasProvider {

    Region getCanvasContainer();

    void setupFXBindingsAndStartLogic(Object mixin);

}
