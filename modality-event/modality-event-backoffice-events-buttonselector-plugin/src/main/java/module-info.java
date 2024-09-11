// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * A JavaFX property holder of the current Event viewed by the user in the back-office.
 */
module modality.event.backoffice.events.buttonselector.plugin {

    // Direct dependencies modules
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.backoffice.mainframe.fx;
    requires modality.base.backoffice.mainframe.headernode;
    requires modality.base.shared.entities;
    requires modality.crm.backoffice.organization.fx;
    requires modality.event.client.event.fx;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.ui.controls;

    // Exported packages
    exports one.modality.event.backoffice.events.buttonselector;

    // Provided services
    provides one.modality.base.backoffice.mainframe.headernode.MainFrameHeaderNodeProvider with one.modality.event.backoffice.events.buttonselector.MainFrameHeaderEventSelectorProvider;

}