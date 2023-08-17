// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * A JavaFX property holder of the current Event viewed by the user in the back-office.
 */
module modality.event.backoffice.event.fx {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires modality.base.shared.entities;
    requires webfx.kit.util;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.orm.entity;
    requires webfx.stack.session;
    requires webfx.stack.session.state.client.fx;

    // Exported packages
    exports one.modality.event.backoffice.event.fx;

}