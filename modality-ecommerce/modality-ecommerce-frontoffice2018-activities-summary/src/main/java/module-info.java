// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.ecommerce.frontoffice2018.activities.summary {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.util;
    requires modality.base.client.validation;
    requires modality.base.shared.entities;
    requires modality.crm.client2018.personaldetails;
    requires modality.ecommerce.client2018.bookingoptionspanel;
    requires modality.ecommerce.client2018.bookingprocess;
    requires modality.ecommerce.client2018.businesslogic;
    requires modality.ecommerce.frontoffice2018.activities.cart.routing;
    requires modality.event.client2018.bookingcalendar;
    requires modality.event.client2018.sectionpanel;
    requires webfx.kit.util;
    requires webfx.platform.console;
    requires webfx.platform.util;
    requires webfx.platform.windowhistory;
    requires webfx.stack.i18n;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.ecommerce.frontoffice.activities.summary;
    exports one.modality.ecommerce.frontoffice.activities.summary.routing;
    exports one.modality.ecommerce.frontoffice.operations.summary;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.ecommerce.frontoffice.activities.summary.SummaryUiRoute;

}