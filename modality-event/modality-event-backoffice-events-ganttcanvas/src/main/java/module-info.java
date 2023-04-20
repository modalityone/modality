// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.event.backoffice.events.ganttcanvas {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.graphics;
    requires modality.base.client.gantt.canvas;
    requires modality.base.client.gantt.fx;
    requires modality.base.shared.entities;
    requires modality.crm.backoffice.organization.fx;
    requires modality.event.backoffice.event.fx;
    requires modality.event.backoffice.events.pm;
    requires modality.event.client.theme;
    requires webfx.extras.theme;
    requires webfx.extras.timelayout;
    requires webfx.extras.timelayout.gantt;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.reactive.entities;

    // Exported packages
    exports one.modality.event.backoffice.events.ganttcanvas;

}