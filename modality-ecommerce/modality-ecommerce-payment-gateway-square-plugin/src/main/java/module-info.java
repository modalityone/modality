// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The SPI payment gateway implementation for making embedded payments with Square.
 */
module modality.ecommerce.payment.gateway.square.plugin {

    // Direct dependencies modules
    requires io.vertx.core;
    requires io.vertx.web;
    requires modality.ecommerce.payment.gateway;
    requires webfx.platform.async;
    requires webfx.platform.boot;
    requires webfx.platform.resource;
    requires webfx.platform.util;
    requires webfx.platform.vertx.common;

    // Exported packages
    exports one.modality.ecommerce.payment.gateway.impl.square;

    // Resources packages
    opens one.modality.ecommerce.payment.gateway.impl.square;

    // Provided services
    provides dev.webfx.platform.boot.spi.ApplicationJob with one.modality.ecommerce.payment.gateway.impl.square.SquareRestApiStarterJob;
    provides one.modality.ecommerce.payment.gateway.PaymentGateway with one.modality.ecommerce.payment.gateway.impl.square.SquarePaymentGateway;

}