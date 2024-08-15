// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * Defines the basic operations that a back-office user can possibly execute on an option booked within a booking.
 */
module modality.ecommerce.backoffice.operations.documentline {

    // Direct dependencies modules
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.backoffice.operations.generic;
    requires modality.base.client.mainframe.fx;
    requires modality.base.shared.entities;
    requires modality.ecommerce.document.service;
    requires webfx.platform.async;
    requires webfx.platform.util;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.ui.operation;

    // Exported packages
    exports one.modality.ecommerce.backoffice.operations.entities.documentline;

}