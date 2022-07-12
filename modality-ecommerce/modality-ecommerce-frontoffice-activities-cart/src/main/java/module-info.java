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
    requires webfx.platform.console;
    requires webfx.platform.uischeduler;
    requires webfx.platform.util;
    requires webfx.stack.async;
    requires webfx.stack.db.submit;

    // Exported packages
    exports org.modality_project.ecommerce.frontoffice.activities.cart;

    // Provided services
    provides dev.webfx.stack.framework.client.ui.uirouter.UiRoute with org.modality_project.ecommerce.frontoffice.activities.cart.CartUiRoute;

}