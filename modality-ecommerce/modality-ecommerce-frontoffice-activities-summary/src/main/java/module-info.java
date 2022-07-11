// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.ecommerce.frontoffice.activities.summary {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.util;
    requires modality.base.client.validation;
    requires modality.base.shared.entities;
    requires modality.crm.client.personaldetails;
    requires modality.ecommerce.client.bookingoptionspanel;
    requires modality.ecommerce.client.bookingprocess;
    requires modality.ecommerce.client.businesslogic;
    requires modality.ecommerce.frontoffice.activities.cart.routing;
    requires modality.event.client.bookingcalendar;
    requires modality.event.client.sectionpanel;
    requires webfx.framework.client.activity;
    requires webfx.framework.client.i18n;
    requires webfx.framework.client.orm.domainmodel.activity;
    requires webfx.framework.client.uirouter;
    requires webfx.kit.util;
    requires webfx.platform.shared.log;
    requires webfx.platform.shared.util;
    requires webfx.stack.async;
    requires webfx.stack.platform.windowhistory;

    // Exported packages
    exports org.modality_project.ecommerce.frontoffice.activities.summary;
    exports org.modality_project.ecommerce.frontoffice.activities.summary.routing;
    exports org.modality_project.ecommerce.frontoffice.operations.summary;

    // Provided services
    provides dev.webfx.stack.framework.client.ui.uirouter.UiRoute with org.modality_project.ecommerce.frontoffice.activities.summary.SummaryUiRoute;

}