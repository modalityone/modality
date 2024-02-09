// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The Gantt canvas that displays events over the current Gantt time window.
 */
module modality.event.backoffice.events.ganttcanvas.plugin {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.graphics;
    requires modality.base.backoffice.gantt.canvas;
    requires modality.base.client.gantt.fx;
    requires modality.base.shared.entities;
    requires modality.crm.backoffice.organization.fx;
    requires modality.event.backoffice.event.fx;
    requires modality.event.backoffice.events.pm;
    requires modality.event.client.theme;
    requires webfx.extras.canvas.bar;
    requires webfx.extras.geometry;
    requires webfx.extras.theme;
    requires webfx.extras.time.layout.gantt;
    requires webfx.stack.cache.client;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.reactive.entities;

    // Exported packages
    exports one.modality.event.backoffice.events.ganttcanvas;
    exports one.modality.event.backoffice.events.ganttcanvas.spi.impl.event;

    // Provided services
    provides one.modality.base.backoffice.ganttcanvas.spi.MainFrameGanttCanvasProvider with one.modality.event.backoffice.events.ganttcanvas.spi.impl.event.MainFrameEventsGanttCanvasProvider;

}