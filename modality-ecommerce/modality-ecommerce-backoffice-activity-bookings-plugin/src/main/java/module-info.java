// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The bookings activity to view, search, and amend bookings.
 */
module modality.ecommerce.backoffice.activity.bookings.plugin {

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
    requires modality.crm.backoffice.bookingdetailspanel;
    requires modality.ecommerce.backoffice.operations.document;
    requires modality.event.client.activity.eventdependent;
    requires webfx.extras.time;
    requires webfx.extras.util.control;
    requires webfx.extras.visual;
    requires webfx.extras.visual.grid;
    requires webfx.platform.windowhistory;
    requires webfx.stack.cache.client;
    requires webfx.stack.i18n;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.expression;
    requires webfx.stack.orm.reactive.dql;
    requires webfx.stack.orm.reactive.visual;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.action;
    requires webfx.stack.ui.operation;
    requires webfx.stack.ui.operation.action;

    // Exported packages
    exports one.modality.ecommerce.backoffice.activities.bookings;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.ecommerce.backoffice.activities.bookings.BookingsRouting.BookingsUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.ecommerce.backoffice.activities.bookings.BookingsRouting.RouteToBookingsRequestEmitter;

}