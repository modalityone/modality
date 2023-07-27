// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.crm.backoffice2018.activities.letters {

    // Direct dependencies modules
    requires javafx.graphics;
    requires modality.base.client.activity;
    requires modality.base.client.util;
    requires modality.crm.backoffice2018.activities.letter;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.reactive.visual;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.operation;

    // Exported packages
    exports one.modality.crm.backoffice2018.activities.letters;
    exports one.modality.crm.backoffice2018.activities.letters.routing;
    exports one.modality.crm.backoffice2018.operations.routes.letters;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.crm.backoffice2018.activities.letters.LettersUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.crm.backoffice2018.activities.letters.RouteToLettersRequestEmitter;

}