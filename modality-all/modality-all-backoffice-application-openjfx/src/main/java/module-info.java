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
    requires webfx.platform.resource.java;
    requires webfx.platform.scheduler.java;
    requires webfx.platform.shutdown.java;
    requires webfx.platform.storage.java;
    requires webfx.platform.util;
    requires webfx.stack.com.websocket.java;
    requires webfx.stack.db.querysubmit.java.jdbc;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.dql.query.interceptor;
    requires webfx.stack.orm.dql.submit.interceptor;
    requires webfx.stack.platform.json.java;
    requires webfx.stack.platform.windowhistory.java;
    requires webfx.stack.platform.windowlocation.java;
    requires webfx.stack.routing.activity;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports org.modality_project.all.backoffice.activities.event.clone.openjfx;

}