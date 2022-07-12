// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.crm.client.authz {

    // Direct dependencies modules
    requires modality.crm.client.authn;
    requires webfx.platform.console;
    requires webfx.platform.util;
    requires webfx.stack.async;
    requires webfx.stack.authz;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.entity;
    requires webfx.stack.routing.router;

    // Exported packages
    exports org.modality_project.crm.client.services.authz;

    // Provided services
    provides dev.webfx.stack.authz.spi.AuthorizationServiceProvider with org.modality_project.crm.client.services.authz.ModalityAuthorizationServiceProvider;

}