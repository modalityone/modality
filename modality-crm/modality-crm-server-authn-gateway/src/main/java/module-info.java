// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.crm.server.authn.gateway {

    // Direct dependencies modules
    requires modality.crm.shared.authn;
    requires webfx.platform.async;
    requires webfx.stack.authn;
    requires webfx.stack.authn.logout.server;
    requires webfx.stack.authn.server.gateway;
    requires webfx.stack.db.query;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.push.server;
    requires webfx.stack.session.state;

    // Exported packages
    exports one.modality.crm.server.authn.gateway;

    // Provided services
    provides dev.webfx.stack.authn.spi.impl.server.gateway.ServerAuthenticationGatewayProvider with one.modality.crm.server.authn.gateway.ModalityUsernamePasswordAuthenticationGatewayProvider;

}