// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.server.application.min {

  // Direct dependencies modules
  requires webfx.stack.conf;
  requires webfx.stack.conf.env.java;
  requires webfx.stack.conf.file.java;

  // Provided services
  provides dev.webfx.stack.conf.spi.ConfigurationSupplier with
      one.modality.base.server.application.min.conf.ModalityServerEnvironmentConfigurationSupplier,
      one.modality.base.server.application.min.conf.ModalityServerDirectoryConfigurationSupplier;
}
