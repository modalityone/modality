// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The UI displayed to the user for initiating an embedded payment before calling the embedded payment API.
 */
module modality.ecommerce.payment.client {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.error;
    requires modality.base.frontoffice.utility;
    requires modality.base.shared.entities;
    requires modality.crm.client.authn.fx;
    requires modality.ecommerce.payment;
    requires webfx.extras.panes;
    requires webfx.extras.util.control;
    requires webfx.extras.webview.pane;
    requires webfx.kit.util;
    requires webfx.platform.async;
    requires webfx.platform.console;
    requires webfx.platform.scheduler;
    requires webfx.platform.shutdown;
    requires webfx.platform.uischeduler;
    requires webfx.platform.useragent;
    requires webfx.platform.util;
    requires webfx.platform.windowlocation;
    requires webfx.stack.com.origin.client;

    // Exported packages
    exports one.modality.ecommerce.payment.client;

}