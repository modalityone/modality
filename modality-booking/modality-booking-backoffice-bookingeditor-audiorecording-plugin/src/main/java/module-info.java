// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.booking.backoffice.bookingeditor.audiorecording.plugin {

    // Direct dependencies modules
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.i18n;
    requires modality.base.shared.entities;
    requires modality.base.shared.knownitems;
    requires modality.booking.backoffice.bookingeditor;
    requires modality.booking.client.workingbooking;
    requires webfx.kit.util;
    requires webfx.platform.util;

    // Exported packages
    exports one.modality.booking.backoffice.bookingeditor.audiorecording;

    // Provided services
    provides one.modality.booking.backoffice.bookingeditor.spi.BookingEditorProvider with one.modality.booking.backoffice.bookingeditor.audiorecording.AudioRecordingBookingEditorProvider;

}