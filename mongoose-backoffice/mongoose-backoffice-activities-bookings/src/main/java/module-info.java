// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.backoffice.activities.bookings {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires mongoose.backoffice.activities.cloneevent.routing;
    requires mongoose.backoffice.bookingdetailspanel;
    requires mongoose.backoffice.masterslave;
    requires mongoose.backoffice.operations.document;
    requires mongoose.backoffice.operations.generic;
    requires mongoose.client.activity;
    requires mongoose.client.aggregates;
    requires mongoose.client.presentationmodel;
    requires mongoose.client.util;
    requires mongoose.frontend.activities.fees;
    requires mongoose.shared.domainmodel;
    requires mongoose.shared.entities;
    requires webfx.extras.visual.base;
    requires webfx.extras.visual.controls.grid;
    requires webfx.framework.client.action;
    requires webfx.framework.client.activity;
    requires webfx.framework.client.operationaction;
    requires webfx.framework.client.orm.domainmodel.activity;
    requires webfx.framework.client.orm.reactive.dql;
    requires webfx.framework.client.orm.reactive.visual;
    requires webfx.framework.client.uirouter;
    requires webfx.framework.client.util;
    requires webfx.framework.shared.operation;
    requires webfx.framework.shared.orm.dql;
    requires webfx.framework.shared.orm.expression;
    requires webfx.framework.shared.router;
    requires webfx.platform.client.windowhistory;
    requires webfx.platform.shared.util;

    // Exported packages
    exports mongoose.backoffice.activities.bookings;
    exports mongoose.backoffice.activities.bookings.routing;
    exports mongoose.backoffice.operations.routes.bookings;

    // Provided services
    provides dev.webfx.framework.client.operations.route.RouteRequestEmitter with mongoose.backoffice.activities.bookings.RouteToBookingsRequestEmitter;
    provides dev.webfx.framework.client.ui.uirouter.UiRoute with mongoose.backoffice.activities.bookings.BookingsUiRoute;

}