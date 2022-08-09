// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.shared.domainmodel {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires webfx.extras.type;
    requires webfx.platform.async;
    requires webfx.platform.json;
    requires webfx.platform.resource;
    requires webfx.platform.util;
    requires webfx.stack.com.serial;
    requires webfx.stack.db.query;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.domainmodel.service;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.expression;

    // Exported packages
    exports org.modality_project.base.shared.domainmodel.formatters;
    exports org.modality_project.base.shared.domainmodel.functions;
    exports org.modality_project.base.shared.services.datasourcemodel;
    exports org.modality_project.base.shared.services.domainmodel;

    // Resources packages
    opens org.modality_project.base.shared.domainmodel;

    // Provided services
    provides dev.webfx.stack.orm.datasourcemodel.service.spi.DataSourceModelProvider with org.modality_project.base.shared.services.datasourcemodel.ModalityDataSourceModelProvider;
    provides dev.webfx.stack.orm.domainmodel.service.spi.DomainModelProvider with org.modality_project.base.shared.services.domainmodel.ModalityDomainModelProvider;

}