// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.ecommerce.client.bookingprocess {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires transitive modality.base.client.activity;
    requires modality.base.client.aggregates;
    requires modality.base.shared.entities;
    requires webfx.kit.util;
    requires webfx.platform.util;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.util;

    // Exported packages
    exports one.modality.ecommerce.client.activity.bookingprocess;

}