// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * Defines an additional operation (mark bed as cleaned) that a back-office user can execute on an accommodation option.
 */
module modality.hotel.backoffice.operations.documentline {

    // Direct dependencies modules
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.shared.entities;
    requires webfx.platform.async;
    requires webfx.stack.orm.entity;
    requires webfx.stack.ui.controls;
    requires webfx.stack.ui.operation;

    // Exported packages
    exports one.modality.hotel.backoffice.operations.entities.documentline;

}