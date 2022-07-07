// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.event.frontoffice.activities.options {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires mongoose.base.client.aggregates;
    requires mongoose.base.client.entities;
    requires mongoose.base.client.icons;
    requires mongoose.base.client.util;
    requires mongoose.base.client.validation;
    requires mongoose.base.shared.entities;
    requires mongoose.ecommerce.client.bookingprocess;
    requires mongoose.ecommerce.client.businesslogic;
    requires mongoose.ecommerce.frontoffice.activities.person;
    requires mongoose.event.client.bookingcalendar;
    requires mongoose.event.client.sectionpanel;
    requires mongoose.hotel.shared.time;
    requires webfx.extras.flexbox;
    requires webfx.extras.imagestore;
    requires webfx.framework.client.activity;
    requires webfx.framework.client.i18n;
    requires webfx.framework.client.orm.domainmodel.activity;
    requires webfx.framework.client.orm.entity.controls;
    requires webfx.framework.client.uirouter;
    requires webfx.framework.client.util;
    requires webfx.framework.shared.orm.entity;
    requires webfx.kit.util;
    requires webfx.platform.client.uischeduler;
    requires webfx.platform.client.windowhistory;
    requires webfx.platform.shared.async;
    requires webfx.platform.shared.log;
    requires webfx.platform.shared.util;

    // Exported packages
    exports org.modality_project.event.frontoffice.activities.options;
    exports org.modality_project.event.frontoffice.activities.options.routing;
    exports org.modality_project.event.frontoffice.operations.options;

    // Provided services
    provides dev.webfx.framework.client.ui.uirouter.UiRoute with org.modality_project.event.frontoffice.activities.options.OptionsUiRoute;

}