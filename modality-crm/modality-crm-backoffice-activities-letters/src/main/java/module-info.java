// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.crm.backoffice.activities.letters {

    // Direct dependencies modules
    requires javafx.graphics;
    requires modality.base.client.activity;
    requires modality.base.client.util;
    requires modality.crm.backoffice.activities.letter;
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
    exports org.modality_project.crm.backoffice.activities.letters;
    exports org.modality_project.crm.backoffice.activities.letters.routing;
    exports org.modality_project.crm.backoffice.operations.routes.letters;

    // Provided services
    provides dev.webfx.stack.framework.client.operations.route.RouteRequestEmitter with org.modality_project.crm.backoffice.activities.letters.RouteToLettersRequestEmitter;
    provides dev.webfx.stack.framework.client.ui.uirouter.UiRoute with org.modality_project.crm.backoffice.activities.letters.LettersUiRoute;

}