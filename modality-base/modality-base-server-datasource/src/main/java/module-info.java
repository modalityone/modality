// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * Module that defines the database connection (configurable variables with default values), and test the connection on server start.
 */
module modality.base.server.datasource {

    // Direct dependencies modules
    requires webfx.platform.boot;
    requires webfx.platform.conf;
    requires webfx.stack.db.datasource;
    requires webfx.stack.db.query;
    requires webfx.stack.orm.datasourcemodel.service;

    // Exported packages
    exports one.modality.base.server.services.datasource;

    // Provided services
    provides dev.webfx.platform.boot.spi.ApplicationModuleBooter with one.modality.base.server.services.datasource.ModalityLocalDataSourceModuleBooter;
    provides dev.webfx.stack.db.datasource.spi.LocalDataSourceProvider with one.modality.base.server.services.datasource.ModalityLocalDataSourceProvider;

}