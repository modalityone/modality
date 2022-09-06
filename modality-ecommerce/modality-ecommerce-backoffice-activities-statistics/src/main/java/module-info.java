// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.ecommerce.backoffice.activities.statistics {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.backoffice.masterslave;
    requires modality.base.backoffice.operations.generic;
    requires modality.base.client.activity;
    requires modality.base.client.presentationmodel;
    requires modality.base.client.util;
    requires modality.base.shared.entities;
    requires modality.crm.backoffice.bookingdetailspanel;
    requires modality.ecommerce.backoffice.operations.document;
    requires modality.ecommerce.backoffice.operations.documentline;
    requires webfx.extras.type;
    requires webfx.extras.visual;
    requires webfx.extras.visual.grid;
    requires webfx.platform.util;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.expression;
    requires webfx.stack.orm.reactive.dql;
    requires webfx.stack.orm.reactive.entities;
    requires webfx.stack.orm.reactive.visual;
    requires webfx.stack.routing.activity;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.action;
    requires webfx.stack.ui.operation;
    requires webfx.stack.ui.operation.action;
    requires webfx.stack.ui.util;

    // Exported packages
    exports one.modality.ecommerce.backoffice.activities.statistics;
    exports one.modality.ecommerce.backoffice.activities.statistics.routing;
    exports one.modality.ecommerce.backoffice.operations.routes.statistics;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.ecommerce.backoffice.activities.statistics.StatisticsUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.ecommerce.backoffice.activities.statistics.RouteToStatisticsRequestEmitter;

}