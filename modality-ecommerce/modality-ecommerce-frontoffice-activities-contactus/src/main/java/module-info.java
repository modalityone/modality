// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.ecommerce.frontoffice.activities.contactus {

    // Direct dependencies modules
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.activity;
    requires modality.base.client.util;
    requires modality.base.client.validation;
    requires modality.base.shared.entities;
    requires modality.ecommerce.frontoffice.activities.cart.routing;
    requires webfx.platform.async;
    requires webfx.platform.console;
    requires webfx.platform.uischeduler;
    requires webfx.platform.util;
    requires webfx.platform.windowhistory;
    requires webfx.platform.windowlocation;
    requires webfx.stack.db.submit;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.entity;
    requires webfx.stack.routing.activity;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.action;
    requires webfx.stack.ui.util;

    // Exported packages
    exports one.modality.ecommerce.frontoffice.activities.contactus;
    exports one.modality.ecommerce.frontoffice.activities.contactus.routing;
    exports one.modality.ecommerce.frontoffice.operations.contactus;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.ecommerce.frontoffice.activities.contactus.ContactUsUiRoute;

}