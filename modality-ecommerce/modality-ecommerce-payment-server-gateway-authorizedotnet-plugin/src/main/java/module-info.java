// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The SPI payment gateway implementation for making api payments with Authorize.net.
 */
module modality.ecommerce.payment.server.gateway.authorizedotnet.plugin {

    // Direct dependencies modules
    requires anet.java.sdk;
    requires io.vertx.core;
    requires io.vertx.web;
    requires jakarta.xml.bind;
    requires modality.ecommerce.payment;
    requires modality.ecommerce.payment.server.gateway;
    requires org.apache.httpcomponents.client5.httpclient5;
    requires org.apache.log4j;
    requires org.slf4j;
    requires webfx.platform.ast;
    requires webfx.platform.async;
    requires webfx.platform.boot;
    requires webfx.platform.resource;
    requires webfx.platform.util;
    requires webfx.platform.util.http;
    requires webfx.platform.vertx.common;

    // Exported packages
    exports one.modality.ecommerce.payment.server.gateway.impl.anet;

    // Resources packages
    opens one.modality.ecommerce.payment.server.gateway.impl.anet;

    // Provided services
    provides dev.webfx.platform.boot.spi.ApplicationJob with one.modality.ecommerce.payment.server.gateway.impl.anet.AuthorizeRestApiJob;
    provides one.modality.ecommerce.payment.server.gateway.PaymentGateway with one.modality.ecommerce.payment.server.gateway.impl.anet.AuthorizePaymentGateway;

}