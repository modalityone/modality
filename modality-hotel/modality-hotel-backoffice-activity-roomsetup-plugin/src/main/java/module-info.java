// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.hotel.backoffice.activity.roomsetup.plugin {

    // Direct dependencies modules
    requires javafx.graphics;
    requires webfx.extras.i18n;
    requires webfx.extras.operation;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.hotel.backoffice.activities.roomsetup;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.hotel.backoffice.activities.roomsetup.RoomSetupRouting.RoomSetupUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.hotel.backoffice.activities.roomsetup.RoomSetupRouting.RouteToRoomSetupRequestEmitter;

}