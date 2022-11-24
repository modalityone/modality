// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.server.application.vertx {

    // Direct dependencies modules
    requires javafx.base;
    requires modality.base.server.application;
    requires modality.base.shared.domainmodel;
    requires modality.crm.server.authz;
    requires webfx.platform.boot.vertx;
    requires webfx.platform.console.java;
    requires webfx.platform.json.vertx;
    requires webfx.platform.resource.java;
    requires webfx.platform.scheduler.vertx;
    requires webfx.platform.shutdown.java;
    requires webfx.stack.auth.authn.server;
    requires webfx.stack.auth.authn.server.mojoauth;
    requires webfx.stack.auth.login.server.mojoauth;
    requires webfx.stack.com.bus.json.vertx;
    requires webfx.stack.db.querypush.server;
    requires webfx.stack.db.querypush.server.simple;
    requires webfx.stack.db.querysubmit.vertx;
    requires webfx.stack.db.submit.listener;
    requires webfx.stack.orm.dql.query.interceptor;
    requires webfx.stack.orm.dql.querypush.interceptor;
    requires webfx.stack.orm.dql.submit.interceptor;
    requires webfx.stack.push.server.simple;
    requires webfx.stack.routing.router.vertx;
    requires webfx.stack.session.vertx;

}