// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The Vert.x executable module of the Modality Server.
 */
module modality.all.server.application.vertx {

    // Direct dependencies modules
    requires javafx.base;
    requires modality.all.server.application;
    requires modality.base.shared.domainmodel;
    requires modality.crm.server.authz.required.plugin;
    requires modality.ecommerce.payment.custom.server;
    requires modality.ecommerce.payment.delegated.server;
    requires modality.ecommerce.payment.direct.server;
    requires webfx.platform.ast.factory.vertx;
    requires webfx.platform.blob.java;
    requires webfx.platform.boot.vertx;
    requires webfx.platform.console.java;
    requires webfx.platform.fetch.vertx;
    requires webfx.platform.resource.java;
    requires webfx.platform.scheduler.vertx;
    requires webfx.platform.shutdown.java;
    requires webfx.stack.authn.login.server.portal;
    requires webfx.stack.authn.oauth2.vertx;
    requires webfx.stack.authn.server.portal;
    requires webfx.stack.com.bus.json.vertx;
    requires webfx.stack.db.querypush.server;
    requires webfx.stack.db.querypush.server.simple;
    requires webfx.stack.db.querysubmit.vertx;
    requires webfx.stack.db.submit.listener;
    requires webfx.stack.http.server.vertx.plugin;
    requires webfx.stack.orm.dql.query.interceptor;
    requires webfx.stack.orm.dql.querypush.interceptor;
    requires webfx.stack.orm.dql.submit.interceptor;
    requires webfx.stack.push.server.simple;
    requires webfx.stack.routing.router.vertx;
    requires webfx.stack.session.vertx;

}