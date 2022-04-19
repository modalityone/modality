// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.ecommerce.frontoffice.activities.contactus {

    // Direct dependencies modules
    requires javafx.controls;
    requires javafx.graphics;
    requires mongoose.base.client.activity;
    requires mongoose.base.client.util;
    requires mongoose.base.client.validation;
    requires mongoose.base.shared.entities;
    requires mongoose.ecommerce.frontoffice.activities.cart.routing;
    requires webfx.framework.client.action;
    requires webfx.framework.client.activity;
    requires webfx.framework.client.orm.domainmodel.activity;
    requires webfx.framework.client.uirouter;
    requires webfx.framework.client.util;
    requires webfx.framework.shared.orm.entity;
    requires webfx.platform.client.uischeduler;
    requires webfx.platform.client.windowhistory;
    requires webfx.platform.client.windowlocation;
    requires webfx.platform.shared.async;
    requires webfx.platform.shared.log;
    requires webfx.platform.shared.submit;
    requires webfx.platform.shared.util;

    // Exported packages
    exports mongoose.ecommerce.frontoffice.activities.contactus;
    exports mongoose.ecommerce.frontoffice.activities.contactus.routing;
    exports mongoose.ecommerce.frontoffice.operations.contactus;

    // Provided services
    provides dev.webfx.framework.client.ui.uirouter.UiRoute with mongoose.ecommerce.frontoffice.activities.contactus.ContactUsUiRoute;

}