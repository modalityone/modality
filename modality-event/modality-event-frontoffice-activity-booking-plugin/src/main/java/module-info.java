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
    requires modality.base.client.css;
    requires modality.base.client.i18n;
    requires modality.base.client.mainframe.fx;
    requires modality.base.client.tile;
    requires modality.base.frontoffice.utility;
    requires modality.base.shared.entities;
    requires modality.crm.backoffice.organization.fx;
    requires modality.ecommerce.frontoffice.bookingform;
    requires modality.event.frontoffice.activity.book;
    requires webfx.extras.controlfactory;
    requires webfx.extras.i18n;
    requires webfx.extras.imagestore;
    requires webfx.extras.operation;
    requires webfx.extras.panes;
    requires webfx.extras.util.layout;
    requires webfx.extras.util.scene;
    requires webfx.extras.webview.pane;
    requires webfx.kit.launcher;
    requires webfx.kit.util;
    requires webfx.platform.conf;
    requires webfx.platform.console;
    requires webfx.platform.os;
    requires webfx.platform.resource;
    requires webfx.platform.useragent;
    requires webfx.platform.util;
    requires webfx.platform.windowhistory;
    requires webfx.stack.cache.client;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.orm.reactive.entities;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.event.frontoffice.activities.booking;
    exports one.modality.event.frontoffice.activities.booking.fx;
    exports one.modality.event.frontoffice.activities.booking.map;
    exports one.modality.event.frontoffice.activities.booking.views;

    // Resources packages
    opens one.modality.event.frontoffice.activities.booking.map;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.event.frontoffice.activities.booking.BookingRouting.BookingUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.event.frontoffice.activities.booking.BookingRouting.RouteToBookingRequestEmitter;

}