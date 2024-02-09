// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The front-office booking activity.
 */
module modality.event.frontoffice.activity.booking.plugin {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.web;
    requires jdk.jsobject;
    requires modality.base.client.mainframe.dialogarea.fx;
    requires modality.base.client.tile;
    requires modality.base.client.util;
    requires modality.base.frontoffice.utility;
    requires modality.base.shared.entities;
    requires modality.crm.backoffice.organization.fx;
    requires webfx.extras.imagestore;
    requires webfx.extras.panes;
    requires webfx.extras.util.control;
    requires webfx.extras.util.layout;
    requires webfx.extras.util.scene;
    requires webfx.kit.launcher;
    requires webfx.kit.util;
    requires webfx.platform.async;
    requires webfx.platform.browser;
    requires webfx.platform.conf;
    requires webfx.platform.console;
    requires webfx.platform.os;
    requires webfx.platform.resource;
    requires webfx.platform.uischeduler;
    requires webfx.platform.useragent;
    requires webfx.platform.util;
    requires webfx.platform.windowhistory;
    requires webfx.stack.cache.client;
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
    requires webfx.stack.ui.dialog;
    requires webfx.stack.ui.operation;

    // Exported packages
    exports one.modality.event.frontoffice.activities.booking;
    exports one.modality.event.frontoffice.activities.booking.browser;
    exports one.modality.event.frontoffice.activities.booking.fx;
    exports one.modality.event.frontoffice.activities.booking.map;
    exports one.modality.event.frontoffice.activities.booking.process;
    exports one.modality.event.frontoffice.activities.booking.process.recurring;
    exports one.modality.event.frontoffice.activities.booking.routing;
    exports one.modality.event.frontoffice.activities.booking.views;
    exports one.modality.event.frontoffice.operations.routes.booking;

    // Resources packages
    opens one.modality.event.frontoffice.activities.booking.map;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.event.frontoffice.activities.booking.BookingUiRoute, one.modality.event.frontoffice.activities.booking.process.recurring.RecurringEventUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.event.frontoffice.activities.booking.RouteToBookingRequestEmitter;

}