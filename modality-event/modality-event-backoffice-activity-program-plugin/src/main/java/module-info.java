// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.event.backoffice.activity.program.plugin {

    // Direct dependencies modules
    requires javafx.graphics;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.controls;
    requires webfx.stack.ui.operation;

    // Exported packages
    exports one.modality.event.backoffice.activities.program;
    exports one.modality.event.backoffice.activities.program.routing;
    exports one.modality.event.backoffice.operations.routes.program;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.event.backoffice.activities.program.ProgramUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.event.backoffice.activities.program.RouteToProgramRequestEmitter;

}