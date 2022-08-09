// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.client.aggregates {

    // Direct dependencies modules
    requires java.base;
    requires modality.base.shared.entities;
    requires webfx.platform.async;
    requires webfx.platform.console;
    requires webfx.platform.util;
    requires webfx.stack.com.bus;
    requires webfx.stack.com.websocket.bus;
    requires webfx.stack.db.query;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.entity;

    // Exported packages
    exports org.modality_project.base.client.aggregates.cart;
    exports org.modality_project.base.client.aggregates.event;
    exports org.modality_project.base.client.aggregates.person;

}