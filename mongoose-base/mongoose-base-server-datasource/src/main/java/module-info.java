// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.base.server.datasource {

    // Direct dependencies modules
    requires webfx.framework.shared.orm.datasourcemodelservice;
    requires webfx.framework.shared.orm.domainmodel;
    requires webfx.framework.shared.orm.dql;
    requires webfx.platform.shared.datasource;
    requires webfx.platform.shared.json;
    requires webfx.platform.shared.log;
    requires webfx.platform.shared.resource;
    requires webfx.platform.shared.util;

    // Exported packages
    exports mongoose.base.server.services.datasource;

    // Resources packages
    opens mongoose.base.server.datasource.MDS;

    // Provided services
    provides dev.webfx.platform.shared.services.datasource.spi.LocalDataSourceProvider with mongoose.base.server.services.datasource.MongooseLocalDataSourceProvider;

}