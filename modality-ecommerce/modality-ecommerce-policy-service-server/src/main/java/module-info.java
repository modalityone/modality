// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.ecommerce.policy.service.server {

    // Direct dependencies modules
    requires modality.base.shared.entities;
    requires modality.ecommerce.policy.service;
    requires webfx.platform.async;
    requires webfx.stack.db.query;
    requires webfx.stack.orm.entity;

    // Exported packages
    exports one.modality.ecommerce.policy.service.spi.impl.server;

    // Provided services
    provides one.modality.ecommerce.policy.service.spi.PolicyServiceProvider with one.modality.ecommerce.policy.service.spi.impl.server.ServerPolicyServiceProvider;

}