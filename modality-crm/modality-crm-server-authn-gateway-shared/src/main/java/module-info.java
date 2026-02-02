// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.crm.server.authn.gateway.shared {

    // Direct dependencies modules
    requires modality.base.server.mail;
    requires modality.base.shared.context;
    requires modality.base.shared.entities;
    requires modality.base.shared.util;
    requires modality.crm.shared.authn;
    requires webfx.platform.async;
    requires webfx.platform.util;
    requires webfx.stack.authn;
    requires webfx.stack.mail;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.entity;
    requires webfx.stack.session.state;

    // Exported packages
    exports one.modality.crm.server.authn.gateway.shared;

}