// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.ecommerce.policy.service {

    // Direct dependencies modules
    requires modality.base.shared.entities;
    requires modality.base.shared.knownitems;
    requires transitive webfx.platform.async;
    requires webfx.platform.console;
    requires webfx.platform.service;
    requires webfx.platform.util;
    requires transitive webfx.stack.db.query;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity;

    // Exported packages
    exports one.modality.ecommerce.policy.service;
    exports one.modality.ecommerce.policy.service.spi;

    // Used services
    uses one.modality.ecommerce.policy.service.spi.PolicyServiceProvider;

}