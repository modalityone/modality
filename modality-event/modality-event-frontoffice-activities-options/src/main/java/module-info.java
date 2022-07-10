// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.event.frontoffice.activities.options {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.aggregates;
    requires modality.base.client.entities;
    requires modality.base.client.icons;
    requires modality.base.client.util;
    requires modality.base.client.validation;
    requires modality.base.shared.entities;
    requires modality.ecommerce.client.bookingprocess;
    requires modality.ecommerce.client.businesslogic;
    requires modality.ecommerce.frontoffice.activities.person;
    requires modality.event.client.bookingcalendar;
    requires modality.event.client.sectionpanel;
    requires modality.hotel.shared.time;
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
    provides dev.webfx.stack.framework.client.ui.uirouter.UiRoute with org.modality_project.event.frontoffice.activities.options.OptionsUiRoute;

}