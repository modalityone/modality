// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The UI displayed to the user for initiating an embedded payment before calling the embedded payment API.
 */
module modality.ecommerce.payment.client {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.shared.entities;
    requires modality.ecommerce.payment;
    requires webfx.extras.panes;
    requires webfx.extras.webview.pane;
    requires webfx.platform.async;
    requires webfx.platform.browser;
    requires webfx.platform.conf;
    requires webfx.platform.console;
    requires webfx.platform.scheduler;
    requires webfx.platform.shutdown;
    requires webfx.platform.uischeduler;
    requires webfx.platform.util;
    requires webfx.platform.windowlocation;

    // Exported packages
    exports one.modality.ecommerce.payment.client;

}