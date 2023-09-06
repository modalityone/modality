// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * A set of Gantt-related JavaFX properties (time window, selection, visibility, etc...).
 */
module modality.base.client.gantt.fx {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.graphics;
    requires webfx.extras.canvas.layer;
    requires webfx.extras.time.window;
    requires webfx.kit.util;
    requires webfx.platform.console;
    requires webfx.platform.scheduler;

    // Exported packages
    exports one.modality.base.client.gantt.fx.highlight;
    exports one.modality.base.client.gantt.fx.interstice;
    exports one.modality.base.client.gantt.fx.selection;
    exports one.modality.base.client.gantt.fx.timewindow;
    exports one.modality.base.client.gantt.fx.today;
    exports one.modality.base.client.gantt.fx.visibility;

}