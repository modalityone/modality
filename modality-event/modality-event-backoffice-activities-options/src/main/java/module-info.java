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
    requires webfx.extras.visual.controls.grid;
    requires webfx.framework.client.activity;
    requires webfx.framework.client.controls;
    requires webfx.framework.client.orm.domainmodel.activity;
    requires webfx.framework.client.orm.reactive.visual;
    requires webfx.framework.client.uirouter;
    requires webfx.framework.client.util;
    requires webfx.framework.shared.orm.dql;
    requires webfx.framework.shared.orm.entity;
    requires webfx.kit.util;
    requires webfx.platform.shared.util;
    requires webfx.stack.async;
    requires webfx.stack.db.submit;

    // Exported packages
    exports org.modality_project.event.backoffice.activities.options;

    // Provided services
    provides dev.webfx.stack.framework.client.ui.uirouter.UiRoute with org.modality_project.event.backoffice.activities.options.EditableOptionsUiRoute;

}