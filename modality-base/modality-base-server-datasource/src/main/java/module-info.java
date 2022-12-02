// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.server.datasource {

    // Direct dependencies modules
    requires webfx.platform.async;
    requires webfx.platform.util;
    requires webfx.stack.conf;
    requires webfx.stack.conf.resource;
    requires webfx.stack.db.datasource;
    requires webfx.stack.orm.datasourcemodel.service;

    // Exported packages
    exports one.modality.base.server.services.datasource;

    // Resources packages
    opens one.modality.base.server.services.datasource;

    // Provided services
    provides dev.webfx.stack.conf.spi.ConfigurationConsumer with one.modality.base.server.services.datasource.ModalityLocalDataSourceConfigurationConsumer;
    provides dev.webfx.stack.db.datasource.spi.LocalDataSourceProvider with one.modality.base.server.services.datasource.ModalityLocalDataSourceProvider;

}