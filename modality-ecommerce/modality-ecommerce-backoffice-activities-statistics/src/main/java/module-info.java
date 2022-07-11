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
    requires webfx.extras.visual.base;
    requires webfx.extras.visual.controls.grid;
    requires webfx.framework.client.action;
    requires webfx.framework.client.activity;
    requires webfx.framework.client.operationaction;
    requires webfx.framework.client.orm.domainmodel.activity;
    requires webfx.framework.client.orm.reactive.dql;
    requires webfx.framework.client.orm.reactive.entities;
    requires webfx.framework.client.orm.reactive.visual;
    requires webfx.framework.client.uirouter;
    requires webfx.framework.client.util;
    requires webfx.framework.shared.operation;
    requires webfx.framework.shared.orm.dql;
    requires webfx.framework.shared.orm.entity;
    requires webfx.framework.shared.orm.expression;
    requires webfx.framework.shared.router;
    requires webfx.platform.shared.util;
    requires webfx.stack.platform.windowhistory;

    // Exported packages
    exports org.modality_project.ecommerce.backoffice.activities.statistics;
    exports org.modality_project.ecommerce.backoffice.activities.statistics.routing;
    exports org.modality_project.ecommerce.backoffice.operations.routes.statistics;

    // Provided services
    provides dev.webfx.stack.framework.client.operations.route.RouteRequestEmitter with org.modality_project.ecommerce.backoffice.activities.statistics.RouteToStatisticsRequestEmitter;
    provides dev.webfx.stack.framework.client.ui.uirouter.UiRoute with org.modality_project.ecommerce.backoffice.activities.statistics.StatisticsUiRoute;

}