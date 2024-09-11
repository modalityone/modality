// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * A set of utility classes for the front-office.
 */
module modality.base.frontoffice.utility {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.web;
    requires modality.base.client.mainframe.fx;
    requires modality.base.frontoffice.mainframe.fx;
    requires modality.base.shared.entities;
    requires webfx.extras.util.scene;
    requires webfx.kit.util;
    requires webfx.platform.browser;
    requires webfx.platform.console;
    requires webfx.platform.util;
    requires webfx.stack.i18n;
    requires webfx.stack.i18n.controls;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.dialog;

    // Exported packages
    exports one.modality.base.frontoffice.browser;
    exports one.modality.base.frontoffice.entities;
    exports one.modality.base.frontoffice.fx;
    exports one.modality.base.frontoffice.states;
    exports one.modality.base.frontoffice.utility;

}