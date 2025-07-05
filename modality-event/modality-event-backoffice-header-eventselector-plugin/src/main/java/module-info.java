// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * A JavaFX property holder of the current Event viewed by the user in the back-office.
 */
module modality.event.backoffice.header.eventselector.plugin {

    // Direct dependencies modules
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.backoffice.mainframe.fx;
    requires modality.base.backoffice.mainframe.headernode;
    requires modality.base.shared.entities;
    requires modality.crm.backoffice.organization.fx;
    requires modality.event.client.event.fx;
    requires webfx.extras.controlfactory;
    requires webfx.extras.util.layout;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.controls;

    // Exported packages
    exports one.modality.event.backoffice.events.eventselector;

    // Provided services
    provides one.modality.base.backoffice.mainframe.headernode.MainFrameHeaderNodeProvider with one.modality.event.backoffice.events.eventselector.MainFrameHeaderEventSelectorProvider;

}