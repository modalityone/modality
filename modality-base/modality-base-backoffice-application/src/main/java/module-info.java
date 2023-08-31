// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.backoffice.application {

    // Direct dependencies modules
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.backoffice.gantt.canvas;
    requires modality.base.backoffice.masterslave;
    requires modality.base.client.application;
    requires modality.base.client.gantt.fx;
    requires modality.base.shared.entities;
    requires modality.crm.backoffice.bookingdetailspanel;
    requires modality.crm.backoffice.organization.fx;
    requires modality.event.backoffice.event.fx;
    requires webfx.extras.theme;
    requires webfx.kit.util;
    requires webfx.platform.util;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.controls;

    // Exported packages
    exports one.modality.base.backoffice.application;

    // Provided services
    provides javafx.application.Application with one.modality.base.backoffice.application.ModalityBackOfficeApplication;

}