// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * A button in the back-office header to allow managers to create events.
 */
module modality.event.backoffice.header.eventcreator.plugin {

    // Direct dependencies modules
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.backoffice.mainframe.fx;
    requires modality.base.backoffice.mainframe.headernode;
    requires modality.base.client.i18n;
    requires modality.base.client.mainframe.fx;
    requires modality.crm.backoffice.organization.fx;
    requires webfx.extras.styles.bootstrap;
    requires webfx.extras.styles.materialdesign;
    requires webfx.kit.util;
    requires webfx.platform.util;
    requires webfx.stack.i18n;
    requires webfx.stack.i18n.controls;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.entity;
    requires webfx.stack.ui.controls;
    requires webfx.stack.ui.dialog;
    requires webfx.stack.ui.validation;

    // Exported packages
    exports one.modality.event.backoffice.events.eventcreator;

    // Provided services
    provides one.modality.base.backoffice.mainframe.headernode.MainFrameHeaderNodeProvider with one.modality.event.backoffice.events.eventcreator.MainFrameHeaderEventCreatorProvider;

}