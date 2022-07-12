// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.client.activity {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.aggregates;
    requires modality.base.client.presentationmodel;
    requires modality.ecommerce.client.businesslogic;
    requires webfx.extras.visual;
    requires webfx.extras.visual.grid;
    requires webfx.kit.util;
    requires webfx.platform.console;
    requires webfx.platform.util;
    requires webfx.stack.async;
    requires webfx.stack.db.query;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.reactive.dql;
    requires webfx.stack.orm.reactive.visual;
    requires webfx.stack.platform.json;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.controls;
    requires webfx.stack.ui.util;

    // Exported packages
    exports org.modality_project.base.client.activity;
    exports org.modality_project.base.client.activity.eventdependent;
    exports org.modality_project.base.client.activity.organizationdependent;
    exports org.modality_project.base.client.activity.table;
    exports org.modality_project.base.client.activity.themes;

}