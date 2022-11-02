// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.client.aggregates {

    // Direct dependencies modules
    requires java.base;
    requires modality.base.shared.entities;
    requires transitive webfx.platform.async;
    requires webfx.platform.console;
    requires webfx.platform.util;
    requires webfx.stack.com.bus;
    requires webfx.stack.com.websocket.bus;
    requires webfx.stack.db.query;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.entity;

    // Exported packages
    exports one.modality.base.client.aggregates.cart;
    exports one.modality.base.client.aggregates.event;
    exports one.modality.base.client.aggregates.person;

}