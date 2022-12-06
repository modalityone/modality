// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.all.server.application {

    // Direct dependencies modules
    requires webfx.stack.conf;
    requires webfx.stack.conf.env.java;
    requires webfx.stack.conf.file.java;

    // Exported packages
    exports one.modality.all.server.application.conf;

    // Provided services
    provides dev.webfx.stack.conf.spi.ConfigurationSupplier with one.modality.all.server.application.conf.ModalityAllServerEnvironmentConfigurationSupplier, one.modality.all.server.application.conf.ModalityAllServerDirectoryConfigurationSupplier;

}