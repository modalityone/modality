// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * Client-side module responsible for checking if the user is authorised to access a route or execute an operation.
 */
module modality.crm.client.authz.required.plugin {

    // Direct dependencies modules
    requires webfx.platform.boot;
    requires webfx.platform.util;
    requires webfx.stack.authz.client;
    requires webfx.stack.com.bus.call;
    requires webfx.stack.db.query;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.session.state.client.fx;

    // Exported packages
    exports one.modality.crm.client.services.authz;

    // Provided services
    provides dev.webfx.platform.boot.spi.ApplicationModuleBooter with one.modality.crm.client.services.authz.ModalityAuthorizationClientModuleBooter;
    provides dev.webfx.stack.authz.client.spi.AuthorizationClientServiceProvider with one.modality.crm.client.services.authz.ModalityAuthorizationClientServiceProvider;

}