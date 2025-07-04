// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.event.frontoffice.bookingform.recurringevent.plugin {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.shared.entities;
    requires modality.ecommerce.client.workingbooking;
    requires modality.event.client.booking;
    requires modality.event.client.recurringevents;
    requires modality.event.frontoffice.activity.booking.plugin;
    requires webfx.extras.controlfactory;
    requires webfx.extras.i18n;
    requires webfx.extras.i18n.controls;
    requires webfx.extras.panes;
    requires webfx.extras.styles.bootstrap;
    requires webfx.extras.webtext;
    requires webfx.platform.util;

    // Exported packages
    exports one.modality.event.frontoffice.bookingforms.recurringevent;

    // Provided services
    provides one.modality.event.frontoffice.activities.booking.process.event.BookingFormProvider with one.modality.event.frontoffice.bookingforms.recurringevent.RecurringEventBookingFormProvider;

}