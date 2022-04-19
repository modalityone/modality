// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.base.client.navigationarrows.java {

    // Direct dependencies modules
    requires webfx.framework.client.uirouter;
    requires webfx.framework.shared.router;

    // Exported packages
    exports mongoose.base.client.navigationarrows;

    // Provided services
    provides dev.webfx.framework.client.operations.route.RouteRequestEmitter with mongoose.base.client.navigationarrows.RouteBackwardRequestEmitter, mongoose.base.client.navigationarrows.RouteForwardRequestEmitter;

}