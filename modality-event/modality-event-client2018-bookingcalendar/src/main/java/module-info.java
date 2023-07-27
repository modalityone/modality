// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.event.client2018.bookingcalendar {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.graphics;
    requires modality.base.client.util;
    requires modality.base.client2018.aggregates;
    requires modality.base.shared.entities;
    requires modality.ecommerce.client2018.businesslogic;
    requires modality.event.client2018.calendar;
    requires modality.hotel.shared2018.time;
    requires webfx.platform.console;
    requires webfx.platform.uischeduler;
    requires webfx.stack.orm.entity;

    // Exported packages
    exports one.modality.event.client2018.controls.bookingcalendar;

}