// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.server.datasource {

    // Direct dependencies modules
    requires webfx.framework.shared.orm.datasourcemodelservice;
    requires webfx.framework.shared.orm.domainmodel;
    requires webfx.framework.shared.orm.dql;
    requires webfx.platform.console;
    requires webfx.platform.resource;
    requires webfx.stack.db.datasource;
    requires webfx.stack.platform.json;

    // Exported packages
    exports org.modality_project.base.server.services.datasource;

    // Resources packages
    opens org.modality_project.base.server.datasource.MDS;

    // Provided services
    provides dev.webfx.stack.db.datasource.spi.LocalDataSourceProvider with org.modality_project.base.server.services.datasource.ModalityLocalDataSourceProvider;

}