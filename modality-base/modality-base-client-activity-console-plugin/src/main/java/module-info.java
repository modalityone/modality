// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.client.activity.console.plugin {

    // Direct dependencies modules
    requires javafx.controls;
    requires javafx.graphics;
    requires webfx.platform.console;
    requires webfx.platform.scheduler;
    requires webfx.platform.uischeduler;
    requires webfx.platform.useragent;
    requires webfx.platform.windowhistory;
    requires webfx.stack.i18n;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.operation;

    // Exported packages
    exports one.modality.base.client.activities.console;

    // Provided services
    provides dev.webfx.platform.console.spi.ConsoleProvider with one.modality.base.client.activities.console.BufferedConsoleProvider;
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.base.client.activities.console.ConsoleRouting.ConsoleUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.base.client.activities.console.ConsoleRouting.RouteToConsoleRequestEmitter;

}