// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.client.navigationarrows.java {

    // Direct dependencies modules
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports org.modality_project.base.client.navigationarrows;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with org.modality_project.base.client.navigationarrows.RouteBackwardRequestEmitter, org.modality_project.base.client.navigationarrows.RouteForwardRequestEmitter;

}