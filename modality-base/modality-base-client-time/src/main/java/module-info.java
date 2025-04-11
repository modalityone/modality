// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.client.time {

    // Direct dependencies modules
    requires javafx.graphics;
    requires webfx.extras.cell;
    requires webfx.extras.time.format;
    requires webfx.platform.boot;
    requires webfx.platform.util;

    // Exported packages
    exports one.modality.base.client.time;

    // Provided services
    provides dev.webfx.platform.boot.spi.ApplicationJob with one.modality.base.client.time.TimeRenderersRegistererJob;

}