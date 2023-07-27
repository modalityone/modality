// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.event.frontoffice2018.activities.options {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.entities;
    requires modality.base.client.icons;
    requires modality.base.client.util;
    requires modality.base.client.validation;
    requires modality.base.shared.entities;
    requires modality.ecommerce.client2018.bookingprocess;
    requires modality.ecommerce.client2018.businesslogic;
    requires modality.ecommerce.frontoffice2018.activities.person;
    requires modality.event.client2018.bookingcalendar;
    requires modality.event.client2018.sectionpanel;
    requires modality.hotel.shared2018.time;
    requires webfx.extras.flexbox;
    requires webfx.extras.imagestore;
    requires webfx.extras.util.layout;
    requires webfx.kit.util;
    requires webfx.platform.console;
    requires webfx.platform.uischeduler;
    requires webfx.platform.util;
    requires webfx.platform.windowhistory;
    requires webfx.stack.i18n;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.event.frontoffice2018.activities.options;
    exports one.modality.event.frontoffice2018.activities.options.routing;
    exports one.modality.event.frontoffice2018.operations.options;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.event.frontoffice2018.activities.options.OptionsUiRoute;

}