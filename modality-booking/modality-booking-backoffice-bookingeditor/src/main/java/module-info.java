// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.booking.backoffice.bookingeditor {

    // Direct dependencies modules
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.shared.entities;
    requires modality.base.shared.knownitems;
    requires modality.booking.client.workingbooking;
    requires webfx.extras.async;
    requires webfx.extras.util.dialog;
    requires webfx.platform.async;
    requires webfx.platform.service;
    requires webfx.platform.util;

    // Exported packages
    exports one.modality.booking.backoffice.bookingeditor;
    exports one.modality.booking.backoffice.bookingeditor.family;
    exports one.modality.booking.backoffice.bookingeditor.spi;
    exports one.modality.booking.backoffice.bookingeditor.multi;

    // Used services
    uses one.modality.booking.backoffice.bookingeditor.spi.BookingEditorProvider;

}