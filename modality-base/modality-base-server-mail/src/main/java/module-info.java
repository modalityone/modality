// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.server.mail {

    // Direct dependencies modules
    requires modality.base.shared.context;
    requires modality.base.shared.entities;
    requires webfx.platform.async;
    requires webfx.stack.mail;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.entity;

    // Exported packages
    exports one.modality.base.server.mail;

    // Provided services
    provides dev.webfx.stack.mail.spi.MailServiceProvider with one.modality.base.server.mail.ModalityServerMailServiceProvider;

}