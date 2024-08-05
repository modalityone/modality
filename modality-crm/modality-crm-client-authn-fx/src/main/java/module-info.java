// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * A JavaFX property holder of an authenticated Modality user (ModalityUserPrincipal).
 */
module modality.crm.client.authn.fx {

    // Direct dependencies modules
    requires javafx.base;
    requires modality.base.shared.entities;
    requires transitive modality.crm.shared.authn;
    requires webfx.kit.util;
    requires webfx.platform.console;
    requires webfx.stack.authn;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.entity;
    requires webfx.stack.session.state.client.fx;
    requires webfx.platform.uischeduler;

    // Exported packages
    exports one.modality.crm.shared.services.authn.fx;

}