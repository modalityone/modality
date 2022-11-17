// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.crm.client.authz {

    // Direct dependencies modules
    requires modality.crm.client.authn;
    requires webfx.platform.console;
    requires webfx.platform.util;
    requires webfx.stack.auth.authz;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.entity;
    requires webfx.stack.routing.router.client;

    // Exported packages
    exports one.modality.crm.client.services.authz;

    // Provided services
    provides dev.webfx.stack.auth.authz.spi.AuthorizationServiceProvider with one.modality.crm.client.services.authz.ModalityAuthorizationServiceProvider;

}