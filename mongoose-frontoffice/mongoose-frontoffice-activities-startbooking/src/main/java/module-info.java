// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.frontoffice.activities.startbooking {

    // Direct dependencies modules
    requires javafx.controls;
    requires javafx.graphics;
    requires mongoose.client.bookingprocess;
    requires mongoose.client.entities;
    requires mongoose.client.icons;
    requires mongoose.client.util;
    requires mongoose.frontoffice.activities.fees;
    requires mongoose.frontoffice.activities.options;
    requires mongoose.frontoffice.activities.program;
    requires mongoose.frontoffice.activities.terms;
    requires mongoose.shared.entities;
    requires webfx.extras.imagestore;
    requires webfx.framework.client.action;
    requires webfx.framework.client.activity;
    requires webfx.framework.client.orm.domainmodel.activity;
    requires webfx.framework.client.uirouter;
    requires webfx.framework.client.util;
    requires webfx.kit.util;
    requires webfx.platform.client.uischeduler;
    requires webfx.platform.client.windowhistory;
    requires webfx.platform.shared.util;

    // Exported packages
    exports mongoose.frontoffice.activities.startbooking;
    exports mongoose.frontoffice.activities.startbooking.routing;
    exports mongoose.frontoffice.operations.startbooking;

    // Provided services
    provides dev.webfx.framework.client.ui.uirouter.UiRoute with mongoose.frontoffice.activities.startbooking.StartBookingUiRoute;

}