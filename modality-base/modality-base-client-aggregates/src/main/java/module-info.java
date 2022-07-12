// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.client.aggregates {

    // Direct dependencies modules
    requires java.base;
    requires modality.base.shared.entities;
    requires webfx.framework.shared.orm.domainmodel;
    requires webfx.framework.shared.orm.entity;
    requires webfx.platform.console;
    requires webfx.platform.util;
    requires webfx.stack.async;
    requires webfx.stack.com.bus;
    requires webfx.stack.com.websocketbus;
    requires webfx.stack.db.query;

    // Exported packages
    exports org.modality_project.base.client.aggregates.cart;
    exports org.modality_project.base.client.aggregates.event;
    exports org.modality_project.base.client.aggregates.person;

}