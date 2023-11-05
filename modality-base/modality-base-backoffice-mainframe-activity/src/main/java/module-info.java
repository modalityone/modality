// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.backoffice.mainframe.activity {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.backoffice.gantt.canvas;
    requires modality.base.backoffice.mainframe.headernode;
    requires modality.base.backoffice.mainframe.headertabs.fx;
    requires modality.base.backoffice.tile;
    requires modality.base.client.application;
    requires modality.base.client.gantt.fx;
    requires modality.base.client.mainframe.dialogarea.fx;
    requires modality.base.client.profile.fx;
    requires webfx.extras.canvas.pane;
    requires webfx.extras.panes;
    requires webfx.extras.theme;
    requires webfx.extras.util.animation;
    requires webfx.kit.util;
    requires webfx.platform.conf;
    requires webfx.platform.resource;
    requires webfx.platform.scheduler;
    requires webfx.platform.uischeduler;
    requires webfx.platform.util;
    requires webfx.stack.com.bus;
    requires webfx.stack.com.bus.call;
    requires webfx.stack.com.bus.client;
    requires webfx.stack.i18n;
    requires webfx.stack.session.state.client.fx;

    // Exported packages
    exports one.modality.base.backoffice.activities.mainframe;

    // Resources packages
    opens one.modality.base.backoffice.activities.mainframe;

}