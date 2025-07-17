// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.event.frontoffice.activity.book {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.brand;
    requires modality.base.client.i18n;
    requires modality.base.client.mainframe.fx;
    requires modality.base.client.util;
    requires modality.base.frontoffice.mainframe.fx;
    requires modality.base.frontoffice.utility;
    requires modality.base.shared.entities;
    requires modality.crm.backoffice.organization.fx;
    requires modality.crm.client.authn.fx;
    requires modality.crm.client.i18n;
    requires modality.crm.frontoffice.activity.orders.plugin;
    requires modality.ecommerce.client.i18n;
    requires modality.ecommerce.client.workingbooking;
    requires modality.ecommerce.document.service;
    requires modality.ecommerce.frontoffice.bookingform;
    requires modality.ecommerce.payment;
    requires modality.ecommerce.payment.client;
    requires modality.event.client.event.fx;
    requires modality.event.client.lifecycle;
    requires modality.event.frontoffice.eventheader;
    requires webfx.extras.controlfactory;
    requires webfx.extras.i18n;
    requires webfx.extras.i18n.controls;
    requires webfx.extras.imagestore;
    requires webfx.extras.operation;
    requires webfx.extras.panes;
    requires webfx.extras.styles.bootstrap;
    requires webfx.extras.util.animation;
    requires webfx.extras.util.control;
    requires webfx.extras.util.dialog;
    requires webfx.extras.util.layout;
    requires webfx.extras.util.scene;
    requires webfx.extras.validation;
    requires webfx.extras.webtext;
    requires webfx.kit.util;
    requires webfx.platform.async;
    requires webfx.platform.conf;
    requires webfx.platform.console;
    requires webfx.platform.service;
    requires webfx.platform.uischeduler;
    requires webfx.platform.util;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.entity;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.event.frontoffice.activities.book;
    exports one.modality.event.frontoffice.activities.book.account;
    exports one.modality.event.frontoffice.activities.book.event;
    exports one.modality.event.frontoffice.activities.book.event.slides;
    exports one.modality.event.frontoffice.activities.book.fx;

    // Used services
    uses one.modality.ecommerce.frontoffice.bookingform.BookingFormProvider;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.event.frontoffice.activities.book.event.BookEventRouting.BookEventUiRoute;

}