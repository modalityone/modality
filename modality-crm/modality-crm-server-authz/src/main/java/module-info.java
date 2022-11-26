// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.crm.server.authz {

    // Direct dependencies modules
    requires webfx.platform.async;
    requires webfx.stack.authn;
    requires webfx.stack.authz.server;
    requires webfx.stack.com.bus;
    requires webfx.stack.db.query;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.push.server;
    requires webfx.stack.session.state;

    // Exported packages
    exports one.modality.crm.server.services.authz;

    // Provided services
    provides dev.webfx.stack.authz.server.spi.AuthorizationServerServiceProvider with one.modality.crm.server.services.authz.ModalityAuthorizationServerServiceProvider;

}