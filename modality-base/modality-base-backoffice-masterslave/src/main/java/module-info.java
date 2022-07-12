// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.backoffice.masterslave {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.entities;
    requires webfx.extras.imagestore;
    requires webfx.extras.type;
    requires webfx.extras.visual.base;
    requires webfx.extras.visual.controls;
    requires webfx.extras.visual.controls.charts;
    requires webfx.extras.visual.controls.grid;
    requires webfx.framework.client.controls;
    requires webfx.framework.client.orm.reactive.dql;
    requires webfx.framework.client.orm.reactive.entities;
    requires webfx.framework.client.orm.reactive.visual;
    requires webfx.framework.client.util;
    requires webfx.framework.shared.orm.domainmodel;
    requires webfx.framework.shared.orm.dql;
    requires webfx.framework.shared.orm.entity;
    requires webfx.framework.shared.orm.expression;
    requires webfx.kit.util;
    requires webfx.platform.util;

    // Exported packages
    exports org.modality_project.base.backoffice.controls.masterslave;
    exports org.modality_project.base.backoffice.controls.masterslave.group;

}