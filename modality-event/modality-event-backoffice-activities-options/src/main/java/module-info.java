// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.event.backoffice.activities.options {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.backoffice.multilangeditor;
    requires modality.base.client.aggregates;
    requires modality.base.client.icons;
    requires modality.base.shared.entities;
    requires modality.ecommerce.client.businesslogic;
    requires modality.event.client.bookingcalendar;
    requires modality.event.client.calendar;
    requires modality.event.frontoffice.activities.options;
    requires modality.hotel.shared.time;
    requires webfx.extras.visual.grid;
    requires webfx.kit.util;
    requires webfx.platform.util;
    requires webfx.stack.db.submit;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.reactive.visual;
    requires webfx.stack.routing.activity;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.controls;
    requires webfx.stack.ui.util;

    // Exported packages
    exports one.modality.event.backoffice.activities.options;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.event.backoffice.activities.options.EditableOptionsUiRoute;

}