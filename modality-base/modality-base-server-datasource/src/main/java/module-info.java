// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.server.datasource {

    // Direct dependencies modules
    requires webfx.platform.console;
    requires webfx.platform.json;
    requires webfx.platform.resource;
    requires webfx.stack.db.datasource;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.dql;

    // Exported packages
    exports one.modality.base.server.services.datasource;

    // Resources packages
    opens one.modality.base.server.datasource.MDS;

    // Provided services
    provides dev.webfx.stack.db.datasource.spi.LocalDataSourceProvider with one.modality.base.server.services.datasource.ModalityLocalDataSourceProvider;

}