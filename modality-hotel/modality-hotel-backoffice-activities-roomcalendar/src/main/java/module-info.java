// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.hotel.backoffice.activities.roomcalendar {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.activity;
    requires modality.base.client.gantt.fx;
    requires modality.base.shared.entities;
    requires modality.crm.backoffice.organization.fx;
    requires webfx.extras.bounds;
    requires webfx.extras.theme;
    requires webfx.extras.timelayout;
    requires webfx.extras.timelayout.gantt;
    requires webfx.extras.util.layout;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.reactive.entities;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.operation;
    requires webfx.stack.ui.operation.action;

    // Exported packages
    exports one.modality.hotel.backoffice.activities.roomcalendar;
    exports one.modality.hotel.backoffice.activities.roomcalendar.routing;
    exports one.modality.hotel.backoffice.operations.routes.roomcalendar;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.hotel.backoffice.activities.roomcalendar.RoomCalendarUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.hotel.backoffice.activities.roomcalendar.RouteToRoomCalendarRequestEmitter;

}