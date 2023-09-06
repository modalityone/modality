// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.backoffice.gantt.canvas.basic.plugin {

    // Direct dependencies modules
    requires javafx.graphics;
    requires modality.base.backoffice.gantt.canvas;

    // Exported packages
    exports one.modality.base.backoffice.ganttcanvas.spi.impl.basic;

    // Provided services
    provides one.modality.base.backoffice.ganttcanvas.spi.MainFrameGanttCanvasProvider with one.modality.base.backoffice.ganttcanvas.spi.impl.basic.MainFrameBasicGanttCanvasProvider;

}