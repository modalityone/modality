// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.event.backoffice.activities.options {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires mongoose.base.backoffice.multilangeditor;
    requires mongoose.base.client.aggregates;
    requires mongoose.base.client.icons;
    requires mongoose.base.shared.entities;
    requires mongoose.ecommerce.client.businesslogic;
    requires mongoose.event.client.bookingcalendar;
    requires mongoose.event.client.calendar;
    requires mongoose.event.frontoffice.activities.options;
    requires mongoose.hotel.shared.time;
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
    requires webfx.platform.shared.async;
    requires webfx.platform.shared.submit;
    requires webfx.platform.shared.util;

    // Exported packages
    exports org.modality_project.event.backoffice.activities.options;

    // Provided services
    provides dev.webfx.framework.client.ui.uirouter.UiRoute with org.modality_project.event.backoffice.activities.options.EditableOptionsUiRoute;

}