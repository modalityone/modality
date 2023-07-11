// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.frontoffice.utility {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.media;
    requires modality.base.shared.entities;
    requires webfx.kit.util;
    requires webfx.platform.json;
    requires webfx.platform.uischeduler;
    requires webfx.platform.util;
    requires webfx.stack.i18n;
    requires webfx.stack.i18n.controls;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.controls;

    // Exported packages
    exports one.modality.base.frontoffice.entities;
    exports one.modality.base.frontoffice.fx;
    exports one.modality.base.frontoffice.states;
    exports one.modality.base.frontoffice.utility;

    // Resources packages
    opens one.modality.base.frontoffice;

}