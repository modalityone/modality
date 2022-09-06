// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.crm.client.authn {

    // Direct dependencies modules
    requires webfx.platform.async;
    requires webfx.stack.authn;
    requires webfx.stack.db.query;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.orm.domainmodel;

    // Exported packages
    exports one.modality.crm.client.services.authn;

    // Provided services
    provides dev.webfx.stack.authn.spi.AuthenticationServiceProvider with one.modality.crm.client.services.authn.ModalityAuthenticationServiceProvider;

}