// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * Defines some generic operations that a back-office user can execute on any entity (ex: copy to clipboard, set a field, toggle a boolean field).
 */
module modality.base.backoffice.operations.generic {

    // Direct dependencies modules
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.mainframe.dialogarea.fx;
    requires modality.base.shared.entities;
    requires webfx.extras.type;
    requires webfx.platform.async;
    requires webfx.platform.uischeduler;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.orm.expression;
    requires webfx.stack.orm.reactive.entities;
    requires webfx.stack.ui.controls;
    requires webfx.stack.ui.dialog;
    requires webfx.stack.ui.exceptions;
    requires webfx.stack.ui.operation;

    // Exported packages
    exports one.modality.base.backoffice.operations.entities.generic;

}