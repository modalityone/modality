// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The front-office booking activity.
 */
module modality.event.frontoffice.activity.booking.plugin {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.web;
    requires jdk.jsobject;
    requires modality.base.client.brand;
    requires modality.base.client.i18n;
    requires modality.base.client.icons;
    requires modality.base.client.mainframe.fx;
    requires modality.base.client.tile;
    requires modality.base.client.util;
    requires modality.base.client.validation;
    requires modality.base.frontoffice.mainframe.fx;
    requires modality.base.frontoffice.utility;
    requires modality.base.shared.entities;
    requires modality.crm.backoffice.organization.fx;
    requires modality.crm.client.authn.fx;
    requires modality.crm.client.i18n;
    requires modality.ecommerce.client.i18n;
    requires modality.ecommerce.document.service;
    requires modality.ecommerce.payment;
    requires modality.ecommerce.payment.client;
    requires modality.event.client.event.fx;
    requires modality.event.client.recurringevents;
    requires webfx.extras.imagestore;
    requires webfx.extras.panes;
    requires webfx.extras.styles.bootstrap;
    requires webfx.extras.util.animation;
    requires webfx.extras.util.control;
    requires webfx.extras.util.layout;
    requires webfx.extras.util.scene;
    requires webfx.extras.webtext;
    requires webfx.extras.webview.pane;
    requires webfx.kit.launcher;
    requires webfx.kit.util;
    requires webfx.platform.async;
    requires webfx.platform.conf;
    requires webfx.platform.console;
    requires webfx.platform.os;
    requires webfx.platform.resource;
    requires webfx.platform.uischeduler;
    requires webfx.platform.useragent;
    requires webfx.platform.util;
    requires webfx.platform.windowhistory;
    requires webfx.stack.cache.client;
    requires webfx.stack.cloud.image;
    requires webfx.stack.cloud.image.client;
    requires webfx.stack.i18n;
    requires webfx.stack.i18n.controls;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.orm.reactive.entities;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.controls;
    requires webfx.stack.ui.operation;

    // Exported packages
    exports one.modality.event.frontoffice.activities.booking;
    exports one.modality.event.frontoffice.activities.booking.fx;
    exports one.modality.event.frontoffice.activities.booking.map;
    exports one.modality.event.frontoffice.activities.booking.process;
    exports one.modality.event.frontoffice.activities.booking.process.account;
    exports one.modality.event.frontoffice.activities.booking.process.event;
    exports one.modality.event.frontoffice.activities.booking.process.event.slides;
    exports one.modality.event.frontoffice.activities.booking.views;

    // Resources packages
    opens one.modality.event.frontoffice.activities.booking.map;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.event.frontoffice.activities.booking.BookingRouting.BookingUiRoute, one.modality.event.frontoffice.activities.booking.process.event.BookEventRouting.BookEventUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.event.frontoffice.activities.booking.BookingRouting.RouteToBookingRequestEmitter;

}