// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.event.backoffice.activity.pricing.plugin {

    // Direct dependencies modules
    requires javafx.graphics;
    requires modality.base.client.util;
    requires webfx.platform.windowhistory;
    requires webfx.stack.i18n;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.operation;

    // Exported packages
    exports one.modality.event.backoffice.activities.pricing;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.event.backoffice.activities.pricing.EventPricingRouting.EventPricingUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.event.backoffice.activities.pricing.EventPricingRouting.RouteToEventPricingRequestEmitter;

}