// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * A JavaFX property holder of the current Event viewed by the user in the back-office.
 */
module modality.crm.backoffice.organization.fx {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.backoffice.mainframe.headernode;
    requires modality.base.client.i18n;
    requires modality.base.shared.context;
    requires transitive modality.base.shared.entities;
    requires webfx.extras.controlfactory;
    requires webfx.kit.util;
    requires webfx.platform.console;
    requires webfx.platform.uischeduler;
    requires webfx.stack.authn.login.ui;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.session;
    requires webfx.stack.session.state.client.fx;

    // Exported packages
    exports one.modality.crm.backoffice.organization.fx;
    exports one.modality.crm.backoffice.organization.fx.impl;

    // Provided services
    provides one.modality.base.backoffice.mainframe.headernode.MainFrameHeaderNodeProvider with one.modality.crm.backoffice.organization.fx.impl.MainFrameHeaderOrganizationSelectorProvider;

}