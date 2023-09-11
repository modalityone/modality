// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * A JavaFX property holder of the current Event viewed by the user in the back-office.
 */
module modality.crm.backoffice.organization.fx {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.backoffice.mainframe.headernode;
    requires modality.base.shared.entities;
    requires webfx.kit.util;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.session;
    requires webfx.stack.session.state.client.fx;
    requires webfx.stack.ui.controls;

    // Exported packages
    exports one.modality.crm.backoffice.organization.fx;
    exports one.modality.crm.backoffice.organization.fx.impl;

    // Provided services
    provides one.modality.base.backoffice.activities.mainframe.headernode.MainFrameHeaderNodeProvider with one.modality.crm.backoffice.organization.fx.impl.MainFrameHeaderOrganizationSelectorProvider;

}