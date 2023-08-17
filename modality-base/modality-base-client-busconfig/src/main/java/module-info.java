// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * Module that defines the default client bus configuration (with configuration variables and default values).
 */
module modality.base.client.busconfig {

    // Direct dependencies modules
    requires webfx.stack.conf;
    requires webfx.stack.conf.resource;

    // Exported packages
    exports one.modality.base.client.busconfig;

    // Resources packages
    opens one.modality.base.client.busconfig;

    // Provided services
    provides dev.webfx.stack.conf.spi.ConfigurationSupplier with one.modality.base.client.busconfig.ModalityClientBusOptionsConfigurationSupplier;

}