// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.crm.backoffice.activities.letter {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.graphics;
    requires mongoose.base.backoffice.multilangeditor;
    requires mongoose.base.client.util;
    requires webfx.framework.client.activity;
    requires webfx.framework.client.controls;
    requires webfx.framework.client.orm.domainmodel.activity;
    requires webfx.framework.client.uirouter;
    requires webfx.kit.util;
    requires webfx.platform.client.windowhistory;
    requires webfx.platform.shared.util;

    // Exported packages
    exports org.modality_project.crm.backoffice.activities.letter;
    exports org.modality_project.crm.backoffice.activities.letter.routing;
    exports org.modality_project.crm.backoffice.operations.routes.letter;

    // Provided services
    provides dev.webfx.framework.client.ui.uirouter.UiRoute with org.modality_project.crm.backoffice.activities.letter.LetterUiRoute;

}