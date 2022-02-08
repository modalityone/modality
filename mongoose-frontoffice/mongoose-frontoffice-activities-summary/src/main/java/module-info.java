// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.frontoffice.activities.summary {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires mongoose.client.bookingcalendar;
    requires mongoose.client.bookingoptionspanel;
    requires mongoose.client.bookingprocess;
    requires mongoose.client.businesslogic;
    requires mongoose.client.personaldetails;
    requires mongoose.client.sectionpanel;
    requires mongoose.client.util;
    requires mongoose.client.validation;
    requires mongoose.frontoffice.activities.cart.routing;
    requires mongoose.shared.entities;
    requires webfx.framework.client.activity;
    requires webfx.framework.client.i18n;
    requires webfx.framework.client.orm.domainmodel.activity;
    requires webfx.framework.client.uirouter;
    requires webfx.kit.util;
    requires webfx.platform.client.windowhistory;
    requires webfx.platform.shared.log;
    requires webfx.platform.shared.util;

    // Exported packages
    exports mongoose.frontoffice.activities.summary;
    exports mongoose.frontoffice.activities.summary.routing;
    exports mongoose.frontoffice.operations.summary;

    // Provided services
    provides dev.webfx.framework.client.ui.uirouter.UiRoute with mongoose.frontoffice.activities.summary.SummaryUiRoute;

}