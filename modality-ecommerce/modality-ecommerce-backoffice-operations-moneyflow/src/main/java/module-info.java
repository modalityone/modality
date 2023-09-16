// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * Defines the different operations that a back-office user can possibly execute on money flows.
 */
module modality.ecommerce.backoffice.operations.moneyflow {

    // Direct dependencies modules
    requires java.base;
    requires javafx.graphics;
    requires modality.base.shared.entities;
    requires webfx.platform.async;
    requires webfx.stack.db.submit;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.ui.controls;
    requires webfx.stack.ui.operation;

    // Exported packages
    exports one.modality.ecommerce.backoffice.operations.entities.moneyaccount;
    exports one.modality.ecommerce.backoffice.operations.entities.moneyflow;

}