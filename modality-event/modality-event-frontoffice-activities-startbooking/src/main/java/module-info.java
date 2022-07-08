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
    requires webfx.framework.client.action;
    requires webfx.framework.client.activity;
    requires webfx.framework.client.orm.domainmodel.activity;
    requires webfx.framework.client.uirouter;
    requires webfx.framework.client.util;
    requires webfx.kit.util;
    requires webfx.platform.client.uischeduler;
    requires webfx.platform.client.windowhistory;
    requires webfx.platform.shared.async;
    requires webfx.platform.shared.util;

    // Exported packages
    exports org.modality_project.event.frontoffice.activities.startbooking;
    exports org.modality_project.event.frontoffice.activities.startbooking.routing;
    exports org.modality_project.event.frontoffice.operations.startbooking;

    // Provided services
    provides dev.webfx.framework.client.ui.uirouter.UiRoute with org.modality_project.event.frontoffice.activities.startbooking.StartBookingUiRoute;

}