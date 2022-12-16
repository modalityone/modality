// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.all.backoffice.application {

    // Direct dependencies modules
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.backoffice.masterslave;
    requires modality.base.client.application;
    requires modality.base.shared.entities;
    requires modality.crm.backoffice.bookingdetailspanel;
    requires modality.crm.backoffice.organization.fx;
    requires webfx.stack.orm.entity.controls;

    // Exported packages
    exports one.modality.all.backoffice.application;

    // Provided services
    provides javafx.application.Application with one.modality.all.backoffice.application.ModalityBackOfficeApplication;

}