// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.client.util {

    // Direct dependencies modules
    requires modality.base.client.entities;
    requires webfx.extras.type;
    requires webfx.framework.client.i18n;
    requires webfx.framework.shared.orm.entity;
    requires webfx.framework.shared.orm.expression;
    requires webfx.platform.console;
    requires webfx.platform.util;

    // Exported packages
    exports org.modality_project.base.client.util.functions;
    exports org.modality_project.base.client.util.log;
    exports org.modality_project.base.client.util.routing;

}