// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.event.frontoffice.bookingform.recurringevent.plugin {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.brand;
    requires modality.base.client.i18n;
    requires modality.base.client.icons;
    requires modality.base.client.time;
    requires modality.base.shared.entities;
    requires modality.ecommerce.client.i18n;
    requires modality.ecommerce.client.scheduleditemsselector;
    requires modality.ecommerce.client.scheduleditemsselector.box;
    requires modality.ecommerce.client.workingbooking;
    requires modality.ecommerce.document.service;
    requires modality.ecommerce.frontoffice.bookingelements;
    requires modality.ecommerce.frontoffice.bookingform;
    requires modality.ecommerce.shared.pricecalculator;
    requires modality.event.frontoffice.activity.book;
    requires modality.event.frontoffice.eventheader;
    requires webfx.extras.controlfactory;
    requires webfx.extras.i18n;
    requires webfx.extras.i18n.controls;
    requires webfx.extras.panes;
    requires webfx.extras.styles.bootstrap;
    requires webfx.extras.time.format;
    requires webfx.extras.util.layout;
    requires webfx.extras.webtext;
    requires webfx.platform.util;

    // Exported packages
    exports one.modality.event.frontoffice.bookingform.recurringevent;

    // Provided services
    provides one.modality.ecommerce.frontoffice.bookingform.BookingFormProvider with one.modality.event.frontoffice.bookingform.recurringevent.RecurringEventBookingFormProvider;

}