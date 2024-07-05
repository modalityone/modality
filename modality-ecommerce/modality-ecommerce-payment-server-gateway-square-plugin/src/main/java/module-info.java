// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The SPI payment gateway implementation for making embedded payments with Square.
 */
module modality.ecommerce.payment.server.gateway.square.plugin {

    // Direct dependencies modules
    requires io.vertx.core;
    requires io.vertx.web;
    requires modality.ecommerce.payment;
    requires modality.ecommerce.payment.server.gateway;
    requires square;
    requires webfx.platform.async;
    requires webfx.platform.boot;
    requires webfx.platform.console;
    requires webfx.platform.resource;
    requires webfx.platform.util;
    requires webfx.platform.vertx.common;
    requires io.netty.codec.http;

    // Exported packages
    exports one.modality.ecommerce.payment.server.gateway.impl.square;

    // Resources packages
    opens one.modality.ecommerce.payment.server.gateway.impl.square;

    // Provided services
    provides dev.webfx.platform.boot.spi.ApplicationJob with one.modality.ecommerce.payment.server.gateway.impl.square.SquareRestApiStarterJob;
    provides one.modality.ecommerce.payment.server.gateway.PaymentGateway with one.modality.ecommerce.payment.server.gateway.impl.square.SquarePaymentGateway;

}