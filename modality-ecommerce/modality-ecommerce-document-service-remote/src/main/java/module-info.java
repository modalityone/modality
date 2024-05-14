// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.ecommerce.document.service.remote {

    // Direct dependencies modules
    requires modality.ecommerce.document.service;
    requires modality.ecommerce.document.service.buscall;
    requires webfx.platform.async;
    requires webfx.stack.com.bus.call;

    // Exported packages
    exports one.modality.ecommerce.document.service.spi.impl.remote;

    // Provided services
    provides one.modality.ecommerce.document.service.spi.DocumentServiceProvider with one.modality.ecommerce.document.service.spi.impl.remote.RemoteDocumentServiceProvider;

}