// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.base.client.navigationarrows.java {

    // Direct dependencies modules
    requires webfx.framework.client.uirouter;
    requires webfx.framework.shared.router;

    // Exported packages
    exports org.modality_project.base.client.navigationarrows;

    // Provided services
    provides dev.webfx.framework.client.operations.route.RouteRequestEmitter with org.modality_project.base.client.navigationarrows.RouteBackwardRequestEmitter, org.modality_project.base.client.navigationarrows.RouteForwardRequestEmitter;

}