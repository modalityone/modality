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
    requires webfx.framework.shared.orm.entity;
    requires webfx.platform.client.uischeduler;
    requires webfx.platform.shared.async;
    requires webfx.platform.shared.log;

    // Exported packages
    exports org.modality_project.event.client.controls.bookingcalendar;

}