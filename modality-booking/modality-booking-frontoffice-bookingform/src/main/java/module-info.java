// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.booking.frontoffice.bookingform {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.shared.entities;
    requires modality.booking.client.workingbooking;
    requires modality.booking.frontoffice.bookingelements;
    requires modality.ecommerce.document.service;
    requires transitive modality.ecommerce.payment;
    requires webfx.extras.operation;
    requires webfx.extras.panes;
    requires webfx.kit.util;
    requires webfx.platform.async;
    requires webfx.platform.uischeduler;

    // Exported packages
    exports one.modality.booking.frontoffice.bookingform;
    exports one.modality.booking.frontoffice.bookingform.multipages;

}