// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * Plugin module to declare the backward and forward navigation operations in Java.
 */
module modality.base.client.navigationarrows.java {

    // Direct dependencies modules
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.base.client.navigationarrows;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.base.client.navigationarrows.RouteBackwardRequestEmitter, one.modality.base.client.navigationarrows.RouteForwardRequestEmitter;

}