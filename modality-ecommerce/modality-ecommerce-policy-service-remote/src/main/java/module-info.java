// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.ecommerce.policy.service.remote {

    // Direct dependencies modules
    requires modality.ecommerce.policy.service;
    requires modality.ecommerce.policy.service.buscall;
    requires webfx.platform.async;
    requires webfx.stack.com.bus.call;

    // Exported packages
    exports one.modality.ecommerce.policy.service.spi.impl.remote;

    // Provided services
    provides one.modality.ecommerce.policy.service.spi.PolicyServiceProvider with one.modality.ecommerce.policy.service.spi.impl.remote.RemotePolicyServiceProvider;

}