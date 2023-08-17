// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * Defines the basic operations that a back-office user can execute on filters.
 */
module modality.base.backoffice.operations.filters {

    // Direct dependencies modules
    requires javafx.graphics;
    requires modality.base.shared.entities;
    requires webfx.platform.async;
    requires webfx.stack.db.submit;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.ui.controls;
    requires webfx.stack.ui.operation;

    // Exported packages
    exports one.modality.base.backoffice.operations.entities.filters;

}