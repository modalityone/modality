// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.shared.domainmodel {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires webfx.extras.type;
    requires webfx.framework.shared.orm.datasourcemodelservice;
    requires webfx.framework.shared.orm.domainmodel;
    requires webfx.framework.shared.orm.domainmodelservice;
    requires webfx.framework.shared.orm.dql;
    requires webfx.framework.shared.orm.entity;
    requires webfx.framework.shared.orm.expression;
    requires webfx.platform.resource;
    requires webfx.platform.util;
    requires webfx.stack.async;
    requires webfx.stack.com.serial;
    requires webfx.stack.db.query;
    requires webfx.stack.platform.json;

    // Exported packages
    exports org.modality_project.base.shared.domainmodel.formatters;
    exports org.modality_project.base.shared.domainmodel.functions;
    exports org.modality_project.base.shared.services.datasourcemodel;
    exports org.modality_project.base.shared.services.domainmodel;

    // Resources packages
    opens org.modality_project.base.shared.domainmodel;

    // Provided services
    provides dev.webfx.stack.framework.shared.services.datasourcemodel.spi.DataSourceModelProvider with org.modality_project.base.shared.services.datasourcemodel.ModalityDataSourceModelProvider;
    provides dev.webfx.stack.framework.shared.services.domainmodel.spi.DomainModelProvider with org.modality_project.base.shared.services.domainmodel.ModalityDomainModelProvider;

}