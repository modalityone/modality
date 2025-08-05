// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.ecommerce.frontoffice.bookingform {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.shared.entities;
    requires modality.ecommerce.client.workingbooking;
    requires modality.ecommerce.document.service;
    requires modality.ecommerce.frontoffice.bookingelements;
    requires transitive modality.ecommerce.payment;
    requires webfx.extras.operation;
    requires webfx.extras.panes;
    requires webfx.kit.util;
    requires webfx.platform.async;
    requires webfx.platform.uischeduler;

    // Exported packages
    exports one.modality.ecommerce.frontoffice.bookingform;
    exports one.modality.ecommerce.frontoffice.bookingform.multipages;

}