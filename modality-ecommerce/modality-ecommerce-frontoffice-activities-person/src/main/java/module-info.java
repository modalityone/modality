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
    requires webfx.framework.client.action;
    requires webfx.framework.client.activity;
    requires webfx.framework.client.orm.domainmodel.activity;
    requires webfx.framework.client.uirouter;
    requires webfx.framework.client.util;
    requires webfx.kit.util;
    requires webfx.platform.util;
    requires webfx.stack.platform.windowhistory;

    // Exported packages
    exports org.modality_project.ecommerce.frontoffice.activities.person;
    exports org.modality_project.ecommerce.frontoffice.activities.person.routing;
    exports org.modality_project.ecommerce.frontoffice.operations.person;

    // Provided services
    provides dev.webfx.stack.framework.client.ui.uirouter.UiRoute with org.modality_project.ecommerce.frontoffice.activities.person.PersonUiRoute;

}