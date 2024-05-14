// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.ecommerce.document.service.server {

    // Direct dependencies modules
    requires modality.ecommerce.document.service;
    requires webfx.platform.async;
    requires webfx.stack.db.query;
    requires webfx.stack.orm.datasourcemodel.service;

    // Exported packages
    exports one.modality.ecommerce.document.service.spi.impl.server;

    // Provided services
    provides one.modality.ecommerce.document.service.spi.DocumentServiceProvider with one.modality.ecommerce.document.service.spi.impl.server.ServerDocumentServiceProvider;

}