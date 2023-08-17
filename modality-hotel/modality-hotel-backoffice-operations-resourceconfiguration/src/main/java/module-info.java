// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * Defines the basic operations that a back-office user can execute on a resource configuration (= room configuration here).
 */
module modality.hotel.backoffice.operations.resourceconfiguration {

    // Direct dependencies modules
    requires javafx.graphics;
    requires modality.base.shared.entities;
    requires webfx.platform.async;
    requires webfx.stack.db.submit;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.ui.controls;
    requires webfx.stack.ui.operation;

    // Exported packages
    exports one.modality.hotel.backoffice.operations.entities.resourceconfiguration;

}