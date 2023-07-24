// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.ecommerce.frontoffice.activities.cart.routing {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.graphics;
    requires modality.base.client.activity;
    requires transitive modality.base.client.aggregates;
    requires modality.base.client.util;
    requires modality.base.shared.entities;
    requires webfx.extras.util.background;
    requires webfx.kit.util;
    requires webfx.platform.json;
    requires webfx.platform.util;
    requires webfx.platform.windowhistory;
    requires webfx.stack.i18n;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.ecommerce.frontoffice.activities.cart.base;
    exports one.modality.ecommerce.frontoffice.activities.cart.routing;
    exports one.modality.ecommerce.frontoffice.operations.cart;
}
