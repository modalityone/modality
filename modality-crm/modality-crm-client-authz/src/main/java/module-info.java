// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.crm.client.authz {

    // Direct dependencies modules
    requires modality.crm.client.authn;
    requires webfx.framework.shared.authz;
    requires webfx.framework.shared.orm.datasourcemodelservice;
    requires webfx.framework.shared.orm.domainmodel;
    requires webfx.framework.shared.orm.entity;
    requires webfx.framework.shared.router;
    requires webfx.platform.shared.log;
    requires webfx.platform.shared.util;
    requires webfx.stack.async;

    // Exported packages
    exports org.modality_project.crm.client.services.authz;

    // Provided services
    provides dev.webfx.stack.framework.shared.services.authz.spi.AuthorizationServiceProvider with org.modality_project.crm.client.services.authz.ModalityAuthorizationServiceProvider;

}