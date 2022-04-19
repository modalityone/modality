// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.crm.backoffice.activities.letters {

    // Direct dependencies modules
    requires javafx.graphics;
    requires mongoose.base.client.activity;
    requires mongoose.base.client.util;
    requires mongoose.crm.backoffice.activities.letter;
    requires webfx.framework.client.activity;
    requires webfx.framework.client.orm.domainmodel.activity;
    requires webfx.framework.client.orm.reactive.visual;
    requires webfx.framework.client.uirouter;
    requires webfx.framework.shared.operation;
    requires webfx.framework.shared.orm.dql;
    requires webfx.framework.shared.orm.entity;
    requires webfx.framework.shared.router;
    requires webfx.platform.client.windowhistory;
    requires webfx.platform.shared.async;
    requires webfx.platform.shared.util;

    // Exported packages
    exports mongoose.crm.backoffice.activities.letters;
    exports mongoose.crm.backoffice.activities.letters.routing;
    exports mongoose.crm.backoffice.operations.routes.letters;

    // Provided services
    provides dev.webfx.framework.client.operations.route.RouteRequestEmitter with mongoose.crm.backoffice.activities.letters.RouteToLettersRequestEmitter;
    provides dev.webfx.framework.client.ui.uirouter.UiRoute with mongoose.crm.backoffice.activities.letters.LettersUiRoute;

}