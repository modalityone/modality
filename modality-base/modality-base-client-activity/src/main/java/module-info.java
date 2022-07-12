// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.client.activity {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.aggregates;
    requires modality.base.client.presentationmodel;
    requires modality.ecommerce.client.businesslogic;
    requires webfx.extras.visual.base;
    requires webfx.extras.visual.controls.grid;
    requires webfx.framework.client.controls;
    requires webfx.framework.client.orm.domainmodel.activity;
    requires webfx.framework.client.orm.reactive.dql;
    requires webfx.framework.client.orm.reactive.visual;
    requires webfx.framework.client.uirouter;
    requires webfx.framework.client.util;
    requires webfx.framework.shared.orm.domainmodel;
    requires webfx.kit.util;
    requires webfx.platform.console;
    requires webfx.platform.util;
    requires webfx.stack.async;
    requires webfx.stack.db.query;
    requires webfx.stack.platform.json;

    // Exported packages
    exports org.modality_project.base.client.activity;
    exports org.modality_project.base.client.activity.eventdependent;
    exports org.modality_project.base.client.activity.organizationdependent;
    exports org.modality_project.base.client.activity.table;
    exports org.modality_project.base.client.activity.themes;

}