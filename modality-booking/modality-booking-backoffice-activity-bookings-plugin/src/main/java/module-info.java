// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The bookings activity to view, search, and amend bookings.
 */
module modality.booking.backoffice.activity.bookings.plugin {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.backoffice.activity.home.plugin;
    requires modality.base.backoffice.mainframe.fx;
    requires modality.base.backoffice.masterslave;
    requires modality.base.backoffice.operations.generic;
    requires modality.base.client.entities;
    requires modality.base.client.gantt.fx;
    requires modality.base.client.presentationmodel;
    requires modality.base.client.util;
    requires modality.base.shared.domainmodel;
    requires modality.base.shared.entities;
    requires modality.booking.backoffice.operations.document;
    requires modality.crm.backoffice.bookingdetailspanel;
    requires modality.crm.backoffice.organization.fx;
    requires modality.ecommerce.backoffice.operations.document;
    requires modality.event.client.activity.eventdependent;
    requires modality.event.client.event.fx;
    requires webfx.extras.action;
    requires webfx.extras.i18n;
    requires webfx.extras.operation;
    requires webfx.extras.operation.action;
    requires webfx.extras.time;
    requires webfx.extras.util.control;
    requires webfx.extras.visual;
    requires webfx.extras.visual.grid;
    requires webfx.platform.util;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.expression;
    requires webfx.stack.orm.reactive.dql;
    requires webfx.stack.orm.reactive.visual;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.booking.backoffice.activities.bookings;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.booking.backoffice.activities.bookings.BookingsRouting.BookingsUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.booking.backoffice.activities.bookings.BookingsRouting.RouteToBookingsRequestEmitter;

}