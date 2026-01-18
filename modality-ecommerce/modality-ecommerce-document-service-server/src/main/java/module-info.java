// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.ecommerce.document.service.server {

    // Direct dependencies modules
    requires modality.base.shared.entities;
    requires modality.ecommerce.document.service;
    requires modality.ecommerce.history.server;
    requires webfx.platform.ast;
    requires webfx.platform.async;
    requires webfx.platform.scheduler;
    requires webfx.platform.util;
    requires webfx.stack.com.bus;
    requires webfx.stack.com.serial;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.orm.entity;
    requires webfx.stack.push.server;
    requires webfx.stack.session.state;

    // Exported packages
    exports one.modality.ecommerce.document.service.spi.impl.server;

    // Provided services
    provides one.modality.ecommerce.document.service.spi.DocumentServiceProvider with one.modality.ecommerce.document.service.spi.impl.server.ServerDocumentServiceProvider;

}