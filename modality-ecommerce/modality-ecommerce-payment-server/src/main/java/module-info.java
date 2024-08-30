// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The actual server-side implementation of the embedded payment API, which decides which payment gateway to use and calls it.
 */
module modality.ecommerce.payment.server {

    // Direct dependencies modules
    requires modality.base.shared.entities;
    requires modality.ecommerce.document.service;
    requires modality.ecommerce.history.server;
    requires modality.ecommerce.payment;
    requires modality.ecommerce.payment.server.gateway;
    requires webfx.platform.async;
    requires webfx.platform.console;
    requires webfx.platform.service;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.orm.entity;
    requires webfx.stack.session.state;

    // Exported packages
    exports one.modality.ecommerce.payment.spi.impl.server;

    // Used services
    uses one.modality.ecommerce.payment.server.gateway.PaymentGateway;

    // Provided services
    provides one.modality.ecommerce.payment.spi.PaymentServiceProvider with one.modality.ecommerce.payment.spi.impl.server.ServerPaymentServiceProvider;

}