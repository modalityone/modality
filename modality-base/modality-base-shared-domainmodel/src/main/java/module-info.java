// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * Module that loads the domain model in memory (on both client &amp; server).
 */
module modality.base.shared.domainmodel {

    // Direct dependencies modules
    requires webfx.extras.type;
    requires webfx.platform.ast;
    requires webfx.platform.ast.json.plugin;
    requires webfx.platform.async;
    requires webfx.platform.resource;
    requires webfx.platform.util;
    requires webfx.platform.util.time;
    requires webfx.stack.com.serial;
    requires webfx.stack.db.query;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.domainmodel.service;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.expression;

    // Exported packages
    exports one.modality.base.shared.domainmodel.formatters;
    exports one.modality.base.shared.domainmodel.functions;
    exports one.modality.base.shared.services.datasourcemodel;
    exports one.modality.base.shared.services.domainmodel;

    // Resources packages
    opens one.modality.base.shared.domainmodel;

    // Provided services
    provides dev.webfx.stack.orm.datasourcemodel.service.spi.DataSourceModelProvider with one.modality.base.shared.services.datasourcemodel.ModalityDataSourceModelProvider;
    provides dev.webfx.stack.orm.domainmodel.service.spi.DomainModelProvider with one.modality.base.shared.services.domainmodel.ModalityDomainModelProvider;

}