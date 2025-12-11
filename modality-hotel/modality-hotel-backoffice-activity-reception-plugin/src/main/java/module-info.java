// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.hotel.backoffice.activity.reception.plugin {

    // Direct dependencies modules
    requires javafx.graphics;
    requires webfx.extras.i18n;
    requires webfx.extras.operation;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.hotel.backoffice.activities.reception;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.hotel.backoffice.activities.reception.ReceptionRouting.ReceptionUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.hotel.backoffice.activities.reception.ReceptionRouting.RouteToReceptionRequestEmitter;

}