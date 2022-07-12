// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.client.entities {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.presentationmodel;
    requires modality.base.shared.entities;
    requires webfx.framework.client.controls;
    requires webfx.framework.client.i18n;
    requires webfx.framework.client.orm.entity.controls;
    requires webfx.framework.client.orm.reactive.dql;
    requires webfx.framework.client.util;
    requires webfx.framework.shared.orm.domainmodel;
    requires webfx.framework.shared.orm.dql;
    requires webfx.kit.util;
    requires webfx.platform.util;

    // Exported packages
    exports org.modality_project.base.client.entities.util;
    exports org.modality_project.base.client.entities.util.filters;

}