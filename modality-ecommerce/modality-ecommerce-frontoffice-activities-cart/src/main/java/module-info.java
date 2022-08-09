// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.ecommerce.frontoffice.activities.cart {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.aggregates;
    requires modality.base.client.util;
    requires modality.base.shared.domainmodel;
    requires modality.base.shared.entities;
    requires modality.ecommerce.client.bookingoptionspanel;
    requires modality.ecommerce.client.businesslogic;
    requires modality.ecommerce.frontoffice.activities.cart.routing;
    requires modality.ecommerce.frontoffice.activities.contactus;
    requires modality.ecommerce.frontoffice.activities.payment;
    requires modality.event.client.sectionpanel;
    requires modality.event.frontoffice.activities.options;
    requires modality.event.frontoffice.activities.startbooking;
    requires webfx.extras.flexbox;
    requires webfx.extras.type;
    requires webfx.extras.visual;
    requires webfx.extras.visual.grid;
    requires webfx.platform.async;
    requires webfx.platform.console;
    requires webfx.platform.uischeduler;
    requires webfx.platform.util;
    requires webfx.stack.db.submit;
    requires webfx.stack.i18n;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.expression;
    requires webfx.stack.orm.reactive.visual;
    requires webfx.stack.routing.activity;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.action;
    requires webfx.stack.ui.controls;
    requires webfx.stack.ui.util;

    // Exported packages
    exports org.modality_project.ecommerce.frontoffice.activities.cart;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with org.modality_project.ecommerce.frontoffice.activities.cart.CartUiRoute;

}