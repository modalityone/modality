// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.ecommerce.frontoffice.activities.cart {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires mongoose.base.client.aggregates;
    requires mongoose.base.client.util;
    requires mongoose.base.shared.domainmodel;
    requires mongoose.base.shared.entities;
    requires mongoose.ecommerce.client.bookingoptionspanel;
    requires mongoose.ecommerce.client.businesslogic;
    requires mongoose.ecommerce.frontoffice.activities.cart.routing;
    requires mongoose.ecommerce.frontoffice.activities.contactus;
    requires mongoose.ecommerce.frontoffice.activities.payment;
    requires mongoose.event.client.sectionpanel;
    requires mongoose.event.frontoffice.activities.options;
    requires mongoose.event.frontoffice.activities.startbooking;
    requires webfx.extras.flexbox;
    requires webfx.extras.type;
    requires webfx.extras.visual.base;
    requires webfx.extras.visual.controls.grid;
    requires webfx.framework.client.action;
    requires webfx.framework.client.activity;
    requires webfx.framework.client.controls;
    requires webfx.framework.client.i18n;
    requires webfx.framework.client.orm.domainmodel.activity;
    requires webfx.framework.client.orm.reactive.visual;
    requires webfx.framework.client.uirouter;
    requires webfx.framework.client.util;
    requires webfx.framework.shared.orm.domainmodel;
    requires webfx.framework.shared.orm.entity;
    requires webfx.framework.shared.orm.expression;
    requires webfx.platform.client.uischeduler;
    requires webfx.platform.shared.async;
    requires webfx.platform.shared.log;
    requires webfx.platform.shared.submit;
    requires webfx.platform.shared.util;

    // Exported packages
    exports mongoose.ecommerce.frontoffice.activities.cart;

    // Provided services
    provides dev.webfx.framework.client.ui.uirouter.UiRoute with mongoose.ecommerce.frontoffice.activities.cart.CartUiRoute;

}