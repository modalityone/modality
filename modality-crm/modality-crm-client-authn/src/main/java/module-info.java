// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.crm.client.authn {

    // Direct dependencies modules
    requires webfx.framework.shared.authn;
    requires webfx.framework.shared.orm.datasourcemodelservice;
    requires webfx.framework.shared.orm.domainmodel;
    requires webfx.stack.async;
    requires webfx.stack.db.query;

    // Exported packages
    exports org.modality_project.crm.client.services.authn;

    // Provided services
    provides dev.webfx.stack.framework.shared.services.authn.spi.AuthenticationServiceProvider with org.modality_project.crm.client.services.authn.ModalityAuthenticationServiceProvider;

}