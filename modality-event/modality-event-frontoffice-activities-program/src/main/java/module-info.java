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
    requires webfx.framework.client.activity;
    requires webfx.framework.client.orm.domainmodel.activity;
    requires webfx.framework.client.uirouter;
    requires webfx.framework.client.util;
    requires webfx.platform.shared.log;
    requires webfx.platform.shared.util;
    requires webfx.stack.async;
    requires webfx.stack.platform.windowhistory;

    // Exported packages
    exports org.modality_project.event.frontoffice.activities.program;
    exports org.modality_project.event.frontoffice.activities.program.routing;
    exports org.modality_project.event.frontoffice.operations.program;

    // Provided services
    provides dev.webfx.stack.framework.client.ui.uirouter.UiRoute with org.modality_project.event.frontoffice.activities.program.ProgramUiRoute;

}