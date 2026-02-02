// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.event.backoffice.eventcreator {

    // Direct dependencies modules
    requires modality.base.shared.entities;
    requires modality.base.shared.knownitems;
    requires modality.crm.backoffice.organization.fx;
    requires modality.event.backoffice.activity.pricing.plugin;
    requires modality.event.client.event.fx;
    requires webfx.platform.async;
    requires webfx.platform.console;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.entity;

    // Exported packages
    exports one.modality.event.backoffice.eventcreator;

}