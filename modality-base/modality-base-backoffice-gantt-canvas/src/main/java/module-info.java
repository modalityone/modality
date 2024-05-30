// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * A zoomable Gantt canvas that can displays years, months, weeks and days over a time window.
 */
module modality.base.backoffice.gantt.canvas {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.graphics;
    requires modality.base.client.gantt.fx;
    requires modality.base.client.time.theme;
    requires webfx.extras.canvas.bar;
    requires webfx.extras.canvas.layer;
    requires webfx.extras.canvas.pane;
    requires webfx.extras.geometry;
    requires webfx.extras.theme;
    requires webfx.extras.time;
    requires webfx.extras.time.layout;
    requires webfx.extras.time.layout.gantt;
    requires webfx.extras.time.window;
    requires webfx.extras.util.animation;
    requires webfx.kit.launcher;
    requires webfx.kit.util;
    requires webfx.platform.resource;
    requires webfx.platform.service;
    requires webfx.platform.util;
    requires webfx.stack.i18n;

    // Exported packages
    exports one.modality.base.backoffice.ganttcanvas;
    exports one.modality.base.backoffice.ganttcanvas.spi;

    // Resources packages
    opens one.modality.base.backoffice.ganttcanvas.images.s32;

    // Used services
    uses one.modality.base.backoffice.ganttcanvas.spi.MainFrameGanttCanvasProvider;

}