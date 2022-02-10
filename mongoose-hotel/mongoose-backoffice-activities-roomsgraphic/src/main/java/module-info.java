// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.backoffice.activities.roomsgraphic {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires mongoose.backoffice.masterslave;
    requires mongoose.backoffice.operations.resourceconfiguration;
    requires mongoose.client.activity;
    requires mongoose.client.presentationmodel;
    requires mongoose.client.util;
    requires mongoose.shared.entities;
    requires webfx.extras.flexbox;
    requires webfx.extras.imagestore;
    requires webfx.extras.visual.controls.grid;
    requires webfx.framework.client.action;
    requires webfx.framework.client.activity;
    requires webfx.framework.client.operationaction;
    requires webfx.framework.client.orm.domainmodel.activity;
    requires webfx.framework.client.orm.reactive.entities;
    requires webfx.framework.client.orm.reactive.visual;
    requires webfx.framework.client.uirouter;
    requires webfx.framework.client.util;
    requires webfx.framework.shared.operation;
    requires webfx.framework.shared.orm.dql;
    requires webfx.framework.shared.orm.entity;
    requires webfx.framework.shared.router;
    requires webfx.kit.util;
    requires webfx.platform.client.windowhistory;
    requires webfx.platform.shared.datascope;
    requires webfx.platform.shared.json;
    requires webfx.platform.shared.serial;
    requires webfx.platform.shared.util;

    // Exported packages
    exports mongoose.backoffice.activities.roomsgraphic;
    exports mongoose.backoffice.activities.roomsgraphic.routing;
    exports mongoose.backoffice.operations.routes.roomsgraphic;

    // Provided services
    provides dev.webfx.framework.client.operations.route.RouteRequestEmitter with mongoose.backoffice.activities.roomsgraphic.RouteToRoomsGraphicRequestEmitter;
    provides dev.webfx.framework.client.ui.uirouter.UiRoute with mongoose.backoffice.activities.roomsgraphic.RoomsGraphicUiRoute;

}