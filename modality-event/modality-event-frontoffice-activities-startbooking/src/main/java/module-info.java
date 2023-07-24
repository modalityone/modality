// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.event.frontoffice.activities.startbooking {

    // Direct dependencies modules
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.entities;
    requires modality.base.client.icons;
    requires modality.base.client.util;
    requires modality.base.shared.entities;
    requires modality.ecommerce.client.bookingprocess;
    requires modality.event.frontoffice.activities.fees;
    requires modality.event.frontoffice.activities.options;
    requires modality.event.frontoffice.activities.program;
    requires modality.event.frontoffice.activities.terms;
    requires webfx.extras.imagestore;
    requires webfx.extras.util.animation;
    requires webfx.extras.util.layout;
    requires webfx.kit.util;
    requires webfx.platform.console;
    requires webfx.platform.uischeduler;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.action;

    // Exported packages
    exports one.modality.event.frontoffice.activities.startbooking;
    exports one.modality.event.frontoffice.activities.startbooking.routing;
    exports one.modality.event.frontoffice.operations.startbooking;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with
            one.modality.event.frontoffice.activities.startbooking.StartBookingUiRoute;
}
