// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.ecommerce.frontoffice.activities.summary {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires mongoose.base.client.util;
    requires mongoose.base.client.validation;
    requires mongoose.base.shared.entities;
    requires mongoose.crm.client.personaldetails;
    requires mongoose.ecommerce.client.bookingoptionspanel;
    requires mongoose.ecommerce.client.bookingprocess;
    requires mongoose.ecommerce.client.businesslogic;
    requires mongoose.ecommerce.frontoffice.activities.cart.routing;
    requires mongoose.event.client.bookingcalendar;
    requires mongoose.event.client.sectionpanel;
    requires webfx.framework.client.activity;
    requires webfx.framework.client.i18n;
    requires webfx.framework.client.orm.domainmodel.activity;
    requires webfx.framework.client.uirouter;
    requires webfx.kit.util;
    requires webfx.platform.client.windowhistory;
    requires webfx.platform.shared.async;
    requires webfx.platform.shared.log;
    requires webfx.platform.shared.util;

    // Exported packages
    exports mongoose.ecommerce.frontoffice.activities.summary;
    exports mongoose.ecommerce.frontoffice.activities.summary.routing;
    exports mongoose.ecommerce.frontoffice.operations.summary;

    // Provided services
    provides dev.webfx.framework.client.ui.uirouter.UiRoute with mongoose.ecommerce.frontoffice.activities.summary.SummaryUiRoute;

}