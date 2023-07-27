// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.event.frontoffice2018.activities.program {

    // Direct dependencies modules
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.icons;
    requires modality.base.client.util;
    requires modality.ecommerce.client2018.bookingprocess;
    requires modality.ecommerce.client2018.businesslogic;
    requires modality.event.client2018.bookingcalendar;
    requires modality.event.client2018.sectionpanel;
    requires webfx.extras.util.layout;
    requires webfx.platform.console;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.event.frontoffice2018.activities.program;
    exports one.modality.event.frontoffice2018.activities.program.routing;
    exports one.modality.event.frontoffice2018.operations.program;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.event.frontoffice2018.activities.program.ProgramUiRoute;

}