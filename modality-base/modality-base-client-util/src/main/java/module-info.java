// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * A set of client-side utility classes (translate function &amp; route interpolator).
 */
module modality.base.client.util {

    // Direct dependencies modules
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.entities;
    requires modality.base.client.i18n;
    requires modality.base.client.mainframe.fx;
    requires webfx.extras.async;
    requires webfx.extras.i18n;
    requires webfx.extras.i18n.controls;
    requires webfx.extras.styles.bootstrap;
    requires webfx.extras.type;
    requires webfx.extras.util.dialog;
    requires webfx.extras.util.masterslave;
    requires webfx.platform.async;
    requires webfx.platform.console;
    requires webfx.platform.util;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.expression;

    // Exported packages
    exports one.modality.base.client.util.dialog;
    exports one.modality.base.client.util.functions;
    exports one.modality.base.client.util.log;
    exports one.modality.base.client.util.masterslave;
    exports one.modality.base.client.util.routing;

}