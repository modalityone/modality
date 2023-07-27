// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.client2018.aggregates {

    // Direct dependencies modules
    requires java.base;
    requires transitive modality.base.shared.entities;
    requires transitive webfx.platform.async;
    requires webfx.platform.console;
    requires webfx.platform.util;
    requires webfx.stack.com.bus;
    requires webfx.stack.com.bus.json.client.websocket;
    requires webfx.stack.db.query;
    requires webfx.stack.orm.domainmodel;
    requires transitive webfx.stack.orm.entity;

    // Exported packages
    exports one.modality.base.client2018.aggregates.cart;
    exports one.modality.base.client2018.aggregates.event;
    exports one.modality.base.client2018.aggregates.person;

}