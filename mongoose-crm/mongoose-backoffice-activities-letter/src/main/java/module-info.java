// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.backoffice.activities.letter {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.graphics;
    requires mongoose.backoffice.multilangeditor;
    requires mongoose.client.util;
    requires webfx.framework.client.activity;
    requires webfx.framework.client.controls;
    requires webfx.framework.client.orm.domainmodel.activity;
    requires webfx.framework.client.uirouter;
    requires webfx.kit.util;
    requires webfx.platform.client.windowhistory;
    requires webfx.platform.shared.util;

    // Exported packages
    exports mongoose.backoffice.activities.letter;
    exports mongoose.backoffice.activities.letter.routing;
    exports mongoose.backoffice.operations.routes.letter;

    // Provided services
    provides dev.webfx.framework.client.ui.uirouter.UiRoute with mongoose.backoffice.activities.letter.LetterUiRoute;

}