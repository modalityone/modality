// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.client.entities {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.presentationmodel;
    requires modality.base.shared.entities;
    requires webfx.kit.util;
    requires webfx.platform.util;
    requires webfx.stack.i18n;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.orm.reactive.dql;
    requires webfx.stack.ui.controls;
    requires webfx.stack.ui.util;

    // Exported packages
    exports org.modality_project.base.client.entities.util;
    exports org.modality_project.base.client.entities.util.filters;

}