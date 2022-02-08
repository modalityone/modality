// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.frontoffice.activities.contactus {

    // Direct dependencies modules
    requires javafx.controls;
    requires javafx.graphics;
    requires mongoose.client.activity;
    requires mongoose.client.util;
    requires mongoose.client.validation;
    requires mongoose.frontoffice.activities.cart.routing;
    requires mongoose.shared.entities;
    requires webfx.framework.client.action;
    requires webfx.framework.client.activity;
    requires webfx.framework.client.orm.domainmodel.activity;
    requires webfx.framework.client.uirouter;
    requires webfx.framework.client.util;
    requires webfx.framework.shared.orm.entity;
    requires webfx.platform.client.uischeduler;
    requires webfx.platform.client.windowhistory;
    requires webfx.platform.client.windowlocation;
    requires webfx.platform.shared.log;
    requires webfx.platform.shared.submit;
    requires webfx.platform.shared.util;

    // Exported packages
    exports mongoose.frontoffice.activities.contactus;
    exports mongoose.frontoffice.activities.contactus.routing;
    exports mongoose.frontoffice.operations.contactus;

    // Provided services
    provides dev.webfx.framework.client.ui.uirouter.UiRoute with mongoose.frontoffice.activities.contactus.ContactUsUiRoute;

}