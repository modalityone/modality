// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * Utility classes for working with entities on client side.
 */
module modality.base.client.entities {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.presentationmodel;
    requires modality.base.client.time;
    requires modality.base.shared.entities;
    requires webfx.extras.controlfactory;
    requires webfx.extras.i18n;
    requires webfx.extras.type;
    requires webfx.extras.util.layout;
    requires webfx.extras.util.scene;
    requires webfx.kit.util;
    requires webfx.kit.util.aria;
    requires webfx.platform.boot;
    requires webfx.platform.util;
    requires webfx.platform.util.time;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.orm.expression;
    requires webfx.stack.orm.reactive.dql;

    // Exported packages
    exports one.modality.base.client.entities;
    exports one.modality.base.client.entities.filters;
    exports one.modality.base.client.entities.functions;

    // Provided services
    provides dev.webfx.platform.boot.spi.ApplicationJob with one.modality.base.client.entities.ClientEntitiesAdditionalRegisteringJob;

}