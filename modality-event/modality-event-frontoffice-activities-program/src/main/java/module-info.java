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
    requires webfx.extras.util.layout;
    requires webfx.platform.console;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.event.frontoffice.activities.program;
    exports one.modality.event.frontoffice.activities.program.routing;
    exports one.modality.event.frontoffice.operations.program;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with
            one.modality.event.frontoffice.activities.program.ProgramUiRoute;
}
