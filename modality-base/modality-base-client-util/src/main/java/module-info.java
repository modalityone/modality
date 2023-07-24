// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.client.util {

    // Direct dependencies modules
    requires modality.base.client.entities;
    requires webfx.extras.type;
    requires webfx.platform.console;
    requires webfx.platform.util;
    requires webfx.stack.i18n;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.expression;

    // Exported packages
    exports one.modality.base.client.util.functions;
    exports one.modality.base.client.util.log;
    exports one.modality.base.client.util.routing;
}
