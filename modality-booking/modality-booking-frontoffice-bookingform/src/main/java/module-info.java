// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.booking.frontoffice.bookingform {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires transitive modality.base.shared.entities;
    requires modality.booking.client.workingbooking;
    requires modality.ecommerce.document.service;
    requires transitive modality.ecommerce.payment;
    requires webfx.platform.async;

    // Exported packages
    exports one.modality.booking.frontoffice.bookingform;

}