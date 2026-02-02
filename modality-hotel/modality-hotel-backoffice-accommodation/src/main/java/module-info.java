// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * A set of classes shared by the Accommodation &amp; Household activities.
 */
module modality.hotel.backoffice.accommodation {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.activity.organizationdependent;
    requires modality.base.client.gantt.fx;
    requires modality.base.client.time;
    requires modality.base.shared.entities;
    requires modality.crm.backoffice.organization.fx;
    requires modality.event.client.theme;
    requires modality.hotel.backoffice.icons;
    requires transitive webfx.extras.canvas.bar;
    requires webfx.extras.canvas.pane;
    requires webfx.extras.geometry;
    requires webfx.extras.theme;
    requires webfx.extras.time.format;
    requires webfx.extras.time.layout;
    requires transitive webfx.extras.time.layout.gantt;
    requires webfx.extras.time.window;
    requires webfx.extras.util.animation;
    requires webfx.extras.util.control;
    requires webfx.kit.util;
    requires webfx.platform.uischeduler;
    requires webfx.platform.util;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.reactive.entities;
    requires webfx.stack.routing.activity;

    // Exported packages
    exports one.modality.hotel.backoffice.accommodation;

}