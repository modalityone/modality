// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * Java implementation of the CSS module (using JavaFX CSS).
 */
module modality.base.client.css {

    // Direct dependencies modules
    requires javafx.graphics;
    requires webfx.platform.boot;

    // Exported packages
    exports one.modality.base.client.fonts;

    // Resources packages
    opens one.modality.base.client.css;
    opens one.modality.base.client.fonts;

    // Provided services
    provides dev.webfx.platform.boot.spi.ApplicationJob with one.modality.base.client.fonts.ModalityFontsLoader;

}