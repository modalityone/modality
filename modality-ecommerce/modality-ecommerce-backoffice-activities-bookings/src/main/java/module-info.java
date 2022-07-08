// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.ecommerce.backoffice.activities.bookings {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.backoffice.masterslave;
    requires modality.base.backoffice.operations.generic;
    requires modality.base.client.activity;
    requires modality.base.client.aggregates;
    requires modality.base.client.presentationmodel;
    requires modality.base.client.util;
    requires modality.base.shared.domainmodel;
    requires modality.base.shared.entities;
    requires modality.crm.backoffice.bookingdetailspanel;
    requires modality.ecommerce.backoffice.operations.document;
    requires modality.event.backoffice.activities.cloneevent.routing;
    requires modality.event.frontoffice.activities.fees;
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
    requires webfx.platform.shared.async;
    requires webfx.platform.shared.util;

    // Exported packages
    exports org.modality_project.ecommerce.backoffice.activities.bookings;
    exports org.modality_project.ecommerce.backoffice.activities.bookings.routing;
    exports org.modality_project.ecommerce.backoffice.operations.routes.bookings;

    // Provided services
    provides dev.webfx.framework.client.operations.route.RouteRequestEmitter with org.modality_project.ecommerce.backoffice.activities.bookings.RouteToBookingsRequestEmitter;
    provides dev.webfx.framework.client.ui.uirouter.UiRoute with org.modality_project.ecommerce.backoffice.activities.bookings.BookingsUiRoute;

}