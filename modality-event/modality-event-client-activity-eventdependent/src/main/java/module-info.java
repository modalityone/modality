// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * A generic event-dependent activity that can be extended.
 */
module modality.event.client.activity.eventdependent {

    // Direct dependencies modules
    requires javafx.base;
    requires modality.base.client.activity;
    requires modality.base.client.activity.organizationdependent;
    requires modality.base.client.presentationmodel;
    requires modality.event.backoffice.event.fx;
    requires webfx.platform.util;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.event.client.activity.eventdependent;

}