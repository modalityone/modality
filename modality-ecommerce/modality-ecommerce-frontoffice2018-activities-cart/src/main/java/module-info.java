// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.ecommerce.frontoffice2018.activities.cart {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.util;
    requires modality.base.shared.domainmodel;
    requires modality.base.shared.entities;
    requires modality.ecommerce.client2018.bookingoptionspanel;
    requires modality.ecommerce.client2018.businesslogic;
    requires modality.ecommerce.frontoffice2018.activities.cart.routing;
    requires modality.ecommerce.frontoffice2018.activities.contactus;
    requires modality.ecommerce.frontoffice2018.activities.payment;
    requires modality.event.client2018.sectionpanel;
    requires modality.event.frontoffice2018.activities.options;
    requires modality.event.frontoffice2018.activities.startbooking;
    requires webfx.extras.flexbox;
    requires webfx.extras.type;
    requires webfx.extras.util.layout;
    requires webfx.extras.visual;
    requires webfx.extras.visual.grid;
    requires webfx.platform.async;
    requires webfx.platform.console;
    requires webfx.platform.uischeduler;
    requires webfx.platform.util;
    requires webfx.stack.i18n;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.expression;
    requires webfx.stack.orm.reactive.visual;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.action;
    requires webfx.stack.ui.controls;

    // Exported packages
    exports one.modality.ecommerce.frontoffice.activities.cart;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.ecommerce.frontoffice.activities.cart.CartUiRoute;

}