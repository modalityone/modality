// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.frontoffice.activities.cart {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires mongoose.client.aggregates;
    requires mongoose.client.bookingoptionspanel;
    requires mongoose.client.businesslogic;
    requires mongoose.client.sectionpanel;
    requires mongoose.client.util;
    requires mongoose.frontoffice.activities.cart.routing;
    requires mongoose.frontoffice.activities.contactus;
    requires mongoose.frontoffice.activities.options;
    requires mongoose.frontoffice.activities.payment;
    requires mongoose.frontoffice.activities.startbooking;
    requires mongoose.shared.domainmodel;
    requires mongoose.shared.entities;
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
    requires webfx.platform.shared.log;
    requires webfx.platform.shared.submit;
    requires webfx.platform.shared.util;

    // Exported packages
    exports mongoose.frontoffice.activities.cart;

    // Provided services
    provides dev.webfx.framework.client.ui.uirouter.UiRoute with mongoose.frontoffice.activities.cart.CartUiRoute;

}