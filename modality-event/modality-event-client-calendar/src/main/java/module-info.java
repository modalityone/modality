// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.event.client.calendar {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.hotel.shared.time;
    requires webfx.framework.client.i18n;
    requires webfx.framework.client.util;
    requires webfx.kit.util;
    requires webfx.platform.shared.util;

    // Exported packages
    exports org.modality_project.event.client.businessdata.calendar;
    exports org.modality_project.event.client.businessdata.calendar.impl;
    exports org.modality_project.event.client.controls.calendargraphic;
    exports org.modality_project.event.client.controls.calendargraphic.impl;

}