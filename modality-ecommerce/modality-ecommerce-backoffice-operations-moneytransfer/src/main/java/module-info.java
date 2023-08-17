// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * Defines the different operations that a back-office user can possibly execute on money transfers.
 */
module modality.ecommerce.backoffice.operations.moneytransfer {

    // Direct dependencies modules
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.web;
    requires modality.base.shared.entities;
    requires modality.ecommerce.payment.custom;
    requires webfx.extras.util.layout;
    requires webfx.platform.async;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.ui.controls;
    requires webfx.stack.ui.operation;

    // Exported packages
    exports one.modality.ecommerce.backoffice.operations.entities.moneytransfer;

}