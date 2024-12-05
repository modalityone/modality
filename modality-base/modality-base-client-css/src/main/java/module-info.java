// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.client.css {

    // Direct dependencies modules
    requires webfx.platform.boot;

    // Exported packages
    exports one.modality.base.client.css;

    // Resources packages
    opens dev.webfx.kit.css.fonts.montserrat;
    opens dev.webfx.kit.css.fonts.poppins;
    opens dev.webfx.kit.css.fonts.roboto;

    // Provided services
    provides dev.webfx.platform.boot.spi.ApplicationJob with one.modality.base.client.css.CssModuleDevLoaderJob;

}