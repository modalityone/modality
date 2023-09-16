// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.backoffice.mainframe.activity {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.graphics;
    requires modality.base.backoffice.gantt.canvas;
    requires modality.base.backoffice.mainframe.fx;
    requires modality.base.backoffice.mainframe.headernode;
    requires modality.base.backoffice.tile;
    requires modality.base.client.application;
    requires modality.base.client.gantt.fx;
    requires webfx.extras.canvas.pane;
    requires webfx.extras.theme;
    requires webfx.extras.util.animation;
    requires webfx.extras.util.pane;
    requires webfx.kit.util;
    requires webfx.platform.conf;
    requires webfx.platform.resource;
    requires webfx.platform.uischeduler;
    requires webfx.platform.util;

    // Exported packages
    exports one.modality.base.backoffice.activities.mainframe;

    // Resources packages
    opens one.modality.base.backoffice.activities.mainframe;

}