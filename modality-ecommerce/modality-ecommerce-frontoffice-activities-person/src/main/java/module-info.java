// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.ecommerce.frontoffice.activities.person {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.util;
    requires modality.crm.client.activities.login;
    requires modality.crm.client.personaldetails;
    requires modality.ecommerce.client.bookingprocess;
    requires modality.ecommerce.client.businesslogic;
    requires modality.ecommerce.frontoffice.activities.summary;
    requires webfx.kit.util;
    requires webfx.platform.util;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.platform.windowhistory;
    requires webfx.stack.routing.activity;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.action;
    requires webfx.stack.ui.util;

    // Exported packages
    exports org.modality_project.ecommerce.frontoffice.activities.person;
    exports org.modality_project.ecommerce.frontoffice.activities.person.routing;
    exports org.modality_project.ecommerce.frontoffice.operations.person;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with org.modality_project.ecommerce.frontoffice.activities.person.PersonUiRoute;

}