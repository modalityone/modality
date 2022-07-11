// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.server.application.vertx {

    // Direct dependencies modules
    requires javafx.base;
    requires modality.base.server.application;
    requires modality.base.shared.domainmodel;
    requires webfx.framework.server.push.simple;
    requires webfx.framework.server.querypush.simple;
    requires webfx.framework.shared.orm.dql.query.interceptor;
    requires webfx.framework.shared.orm.dql.querypush.interceptor;
    requires webfx.framework.shared.orm.dql.submit.interceptor;
    requires webfx.platform.boot.vertx;
    requires webfx.platform.java.resource.impl;
    requires webfx.platform.java.shutdown.impl;
    requires webfx.platform.scheduler.vertx;
    requires webfx.platform.shared.log.impl.simple;
    requires webfx.stack.com.bus.vertx;
    requires webfx.stack.db.querysubmit.vertx;
    requires webfx.stack.platform.json.vertx;

}