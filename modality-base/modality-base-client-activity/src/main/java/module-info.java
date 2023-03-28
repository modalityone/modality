// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.client.activity {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires transitive modality.base.client.aggregates;
    requires modality.base.client.presentationmodel;
    requires modality.crm.backoffice.organization.fx;
    requires modality.ecommerce.client.businesslogic;
    requires modality.event.backoffice.event.fx;
    requires webfx.extras.util.background;
    requires webfx.extras.util.border;
    requires webfx.extras.util.scene;
    requires webfx.extras.visual;
    requires webfx.extras.visual.grid;
    requires webfx.kit.util;
    requires transitive webfx.platform.async;
    requires webfx.platform.console;
    requires webfx.platform.json;
    requires webfx.platform.util;
    requires webfx.stack.db.query;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.reactive.dql;
    requires webfx.stack.orm.reactive.visual;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.controls;

    // Exported packages
    exports one.modality.base.client.activity;
    exports one.modality.base.client.activity.eventdependent;
    exports one.modality.base.client.activity.organizationdependent;
    exports one.modality.base.client.activity.table;
    exports one.modality.base.client.activity.themes;

}