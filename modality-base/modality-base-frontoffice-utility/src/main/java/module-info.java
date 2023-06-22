// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.frontoffice.utility {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.shared.entities;
    requires webfx.stack.i18n.controls;

    // Exported packages
    exports one.modality.base.frontoffice.states;
    exports one.modality.base.frontoffice.utility;

    // Resources packages
    opens one.modality.base.frontoffice;

}