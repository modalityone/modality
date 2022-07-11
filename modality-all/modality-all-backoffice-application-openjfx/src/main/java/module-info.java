// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.all.backoffice.application.openjfx {

    // Direct dependencies modules
    requires javafx.controls;
    requires modality.all.backoffice.application;
    requires modality.base.shared.domainmodel;
    requires modality.event.backoffice.activities.cloneevent;
    requires modality.event.backoffice.activities.cloneevent.routing;
    requires webfx.framework.client.activity;
    requires webfx.framework.client.orm.domainmodel.activity;
    requires webfx.framework.client.uirouter;
    requires webfx.framework.shared.orm.dql.query.interceptor;
    requires webfx.framework.shared.orm.dql.querypush.interceptor;
    requires webfx.framework.shared.orm.dql.submit.interceptor;
    requires webfx.kit.openjfx;
    requires webfx.platform.java.boot.impl;
    requires webfx.platform.java.resource.impl;
    requires webfx.platform.java.scheduler.impl;
    requires webfx.platform.java.shutdown.impl;
    requires webfx.platform.java.storage.impl;
    requires webfx.platform.shared.log.impl.simple;
    requires webfx.platform.shared.util;
    requires webfx.stack.com.websocket.java;
    requires webfx.stack.db.querysubmit.java.jdbc;
    requires webfx.stack.platform.json.java;
    requires webfx.stack.platform.windowhistory.java;
    requires webfx.stack.platform.windowlocation.java;

    // Exported packages
    exports org.modality_project.all.backoffice.activities.event.clone.openjfx;

}