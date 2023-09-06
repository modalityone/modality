// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * Some class materials to build master/slaves views, with optionally an additional group view.
 */
module modality.base.backoffice.masterslave {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.entities;
    requires webfx.extras.imagestore;
    requires webfx.extras.type;
    requires webfx.extras.util.layout;
    requires webfx.extras.visual;
    requires webfx.extras.visual.charts;
    requires webfx.extras.visual.controls;
    requires webfx.extras.visual.grid;
    requires webfx.kit.util;
    requires webfx.platform.util;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.expression;
    requires webfx.stack.orm.reactive.dql;
    requires webfx.stack.orm.reactive.entities;
    requires webfx.stack.orm.reactive.visual;
    requires webfx.stack.ui.controls;

    // Exported packages
    exports one.modality.base.backoffice.controls.masterslave;
    exports one.modality.base.backoffice.controls.masterslave.group;

}