// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.all.backoffice.application.openjfx {

    // Direct dependencies modules
    requires javafx.controls;
    requires modality.all.backoffice.application;
    requires modality.base.shared.domainmodel;
    requires modality.event.backoffice.activities.cloneevent;
    requires modality.event.backoffice.activities.cloneevent.routing;
    requires webfx.kit.openjfx;
    requires webfx.platform.boot.java;
    requires webfx.platform.console.java;
    requires webfx.platform.json.java;
    requires webfx.platform.resource.java;
    requires webfx.platform.scheduler.java;
    requires webfx.platform.shutdown.java;
    requires webfx.platform.storage.java;
    requires webfx.platform.windowhistory.java;
    requires webfx.platform.windowlocation.java;
    requires webfx.stack.auth.authn.buscall;
    requires webfx.stack.auth.authn.remote;
    requires webfx.stack.com.bus.json.client.websocket.java;
    requires webfx.stack.com.websocket.java;
    requires webfx.stack.db.query.buscall;
    requires webfx.stack.db.querysubmit.java.jdbc;
    requires webfx.stack.db.submit.buscall;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.dql.query.interceptor;
    requires webfx.stack.orm.dql.querypush.interceptor;
    requires webfx.stack.orm.dql.submit.interceptor;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.session.client;
    requires webfx.stack.ui.fxraiser.json;

    // Exported packages
    exports one.modality.all.backoffice.activities.event.clone.openjfx;

}