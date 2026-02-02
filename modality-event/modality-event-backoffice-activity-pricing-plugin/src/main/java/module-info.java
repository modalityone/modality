// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.event.backoffice.activity.pricing.plugin {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.backoffice.mainframe.fx;
    requires modality.base.client.i18n;
    requires modality.base.client.util;
    requires modality.base.shared.domainmodel;
    requires modality.base.shared.entities;
    requires modality.base.shared.knownitems;
    requires modality.crm.backoffice.organization.fx;
    requires modality.ecommerce.policy.service;
    requires modality.event.client.event.fx;
    requires webfx.extras.async;
    requires webfx.extras.i18n;
    requires webfx.extras.i18n.controls;
    requires webfx.extras.operation;
    requires webfx.extras.panes;
    requires webfx.extras.styles.bootstrap;
    requires webfx.extras.time;
    requires webfx.extras.util.control;
    requires webfx.extras.util.masterslave;
    requires webfx.extras.validation;
    requires webfx.kit.util;
    requires webfx.platform.console;
    requires webfx.platform.util;
    requires webfx.platform.util.time;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.binding;
    requires webfx.stack.orm.reactive.entities;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.event.backoffice.activities.pricing;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.event.backoffice.activities.pricing.EventPricingRouting.EventPricingUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.event.backoffice.activities.pricing.EventPricingRouting.RouteToEventPricingRequestEmitter;

}