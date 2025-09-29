// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.booking.backoffice.bookingeditor.teaching.plugin {

    // Direct dependencies modules
    requires javafx.graphics;
    requires modality.base.shared.knownitems;
    requires modality.booking.backoffice.bookingeditor;
    requires modality.booking.client.scheduleditemsselector;
    requires modality.booking.client.scheduleditemsselector.box;
    requires modality.booking.client.workingbooking;
    requires webfx.extras.styles.bootstrap;
    requires webfx.kit.util;

    // Exported packages
    exports one.modality.booking.backoffice.bookingeditor.teaching;

    // Provided services
    provides one.modality.booking.backoffice.bookingeditor.spi.BookingEditorProvider with one.modality.booking.backoffice.bookingeditor.teaching.TeachingBookingEditorProvider;

}