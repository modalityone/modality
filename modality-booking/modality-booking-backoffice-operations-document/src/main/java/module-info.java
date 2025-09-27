// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.booking.backoffice.operations.document {

    // Direct dependencies modules
    requires javafx.graphics;
    requires modality.base.client.mainframe.fx;
    requires modality.base.shared.entities;
    requires modality.booking.backoffice.bookingeditor;
    requires modality.ecommerce.backoffice.operations.document;
    requires webfx.extras.i18n;
    requires webfx.extras.operation;
    requires webfx.platform.async;
    requires webfx.stack.orm.entity;

    // Exported packages
    exports one.modality.booking.backoffice.operations.entities.document.registration;

}