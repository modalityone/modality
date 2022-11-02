// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.event.client.bookingcalendar {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.graphics;
    requires modality.base.client.aggregates;
    requires modality.base.client.util;
    requires modality.base.shared.entities;
    requires modality.ecommerce.client.businesslogic;
    requires modality.event.client.calendar;
    requires modality.hotel.shared.time;
    requires webfx.platform.console;
    requires webfx.platform.uischeduler;
    requires webfx.stack.orm.entity;

    // Exported packages
    exports one.modality.event.client.controls.bookingcalendar;

}