// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.client.aggregates {

    // Direct dependencies modules
    requires java.base;
    requires modality.base.shared.entities;
    requires webfx.framework.shared.orm.domainmodel;
    requires webfx.framework.shared.orm.entity;
    requires webfx.platform.client.websocketbus;
    requires webfx.platform.shared.async;
    requires webfx.platform.shared.bus;
    requires webfx.platform.shared.log;
    requires webfx.platform.shared.query;
    requires webfx.platform.shared.util;

    // Exported packages
    exports org.modality_project.base.client.aggregates.cart;
    exports org.modality_project.base.client.aggregates.event;
    exports org.modality_project.base.client.aggregates.person;

}