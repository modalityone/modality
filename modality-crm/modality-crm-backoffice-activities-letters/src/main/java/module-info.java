// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.crm.backoffice.activities.letters {

    // Direct dependencies modules
    requires javafx.graphics;
    requires modality.base.client.activity;
    requires modality.base.client.util;
    requires modality.crm.backoffice.activities.letter;
    requires webfx.platform.async;
    requires webfx.platform.util;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.reactive.visual;
    requires webfx.stack.routing.activity;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.operation;

    // Exported packages
    exports org.modality_project.crm.backoffice.activities.letters;
    exports org.modality_project.crm.backoffice.activities.letters.routing;
    exports org.modality_project.crm.backoffice.operations.routes.letters;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with org.modality_project.crm.backoffice.activities.letters.LettersUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with org.modality_project.crm.backoffice.activities.letters.RouteToLettersRequestEmitter;

}