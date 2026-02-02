// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * Define some generic class materials to build Modality client activities.
 */
module modality.base.client.activity {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.presentationmodel;
    requires webfx.extras.controlfactory;
    requires webfx.extras.util.background;
    requires webfx.extras.util.border;
    requires webfx.extras.util.dialog;
    requires webfx.extras.util.scene;
    requires webfx.extras.visual;
    requires webfx.extras.visual.grid;
    requires webfx.kit.util;
    requires webfx.platform.ast;
    requires transitive webfx.platform.async;
    requires webfx.platform.console;
    requires webfx.platform.util;
    requires webfx.stack.db.query;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.reactive.dql;
    requires webfx.stack.orm.reactive.visual;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.base.client.activity;
    exports one.modality.base.client.activity.table;
    exports one.modality.base.client.activity.themes;

}