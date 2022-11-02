// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.ecommerce.frontoffice.activities.summary {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.util;
    requires modality.base.client.validation;
    requires modality.base.shared.entities;
    requires modality.crm.client.personaldetails;
    requires modality.ecommerce.client.bookingoptionspanel;
    requires modality.ecommerce.client.bookingprocess;
    requires modality.ecommerce.client.businesslogic;
    requires modality.ecommerce.frontoffice.activities.cart.routing;
    requires modality.event.client.bookingcalendar;
    requires modality.event.client.sectionpanel;
    requires webfx.kit.util;
    requires webfx.platform.console;
    requires webfx.platform.util;
    requires webfx.platform.windowhistory;
    requires webfx.stack.i18n;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.routing.activity;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.ecommerce.frontoffice.activities.summary;
    exports one.modality.ecommerce.frontoffice.activities.summary.routing;
    exports one.modality.ecommerce.frontoffice.operations.summary;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.ecommerce.frontoffice.activities.summary.SummaryUiRoute;

}