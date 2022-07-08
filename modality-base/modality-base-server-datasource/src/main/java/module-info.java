// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.server.datasource {

    // Direct dependencies modules
    requires webfx.framework.shared.orm.datasourcemodelservice;
    requires webfx.framework.shared.orm.domainmodel;
    requires webfx.framework.shared.orm.dql;
    requires webfx.platform.shared.datasource;
    requires webfx.platform.shared.json;
    requires webfx.platform.shared.log;
    requires webfx.platform.shared.resource;

    // Exported packages
    exports org.modality_project.base.server.services.datasource;

    // Resources packages
    opens org.modality_project.base.server.datasource.MDS;

    // Provided services
    provides dev.webfx.platform.shared.services.datasource.spi.LocalDataSourceProvider with org.modality_project.base.server.services.datasource.MongooseLocalDataSourceProvider;

}