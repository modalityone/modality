// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.event.frontoffice.activities.program {

    // Direct dependencies modules
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.icons;
    requires modality.base.client.util;
    requires modality.ecommerce.client.bookingprocess;
    requires modality.ecommerce.client.businesslogic;
    requires modality.event.client.bookingcalendar;
    requires modality.event.client.sectionpanel;
    requires webfx.platform.console;
    requires webfx.platform.util;
    requires webfx.stack.async;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.platform.windowhistory;
    requires webfx.stack.routing.activity;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.util;

    // Exported packages
    exports org.modality_project.event.frontoffice.activities.program;
    exports org.modality_project.event.frontoffice.activities.program.routing;
    exports org.modality_project.event.frontoffice.operations.program;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with org.modality_project.event.frontoffice.activities.program.ProgramUiRoute;

}