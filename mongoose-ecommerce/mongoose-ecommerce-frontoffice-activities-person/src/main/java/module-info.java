// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.ecommerce.frontoffice.activities.person {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires mongoose.base.client.util;
    requires mongoose.crm.client.activities.login;
    requires mongoose.crm.client.personaldetails;
    requires mongoose.ecommerce.client.bookingprocess;
    requires mongoose.ecommerce.client.businesslogic;
    requires mongoose.ecommerce.frontoffice.activities.summary;
    requires webfx.framework.client.action;
    requires webfx.framework.client.activity;
    requires webfx.framework.client.orm.domainmodel.activity;
    requires webfx.framework.client.uirouter;
    requires webfx.framework.client.util;
    requires webfx.kit.util;
    requires webfx.platform.client.windowhistory;
    requires webfx.platform.shared.util;

    // Exported packages
    exports org.modality_project.ecommerce.frontoffice.activities.person;
    exports org.modality_project.ecommerce.frontoffice.activities.person.routing;
    exports org.modality_project.ecommerce.frontoffice.operations.person;

    // Provided services
    provides dev.webfx.framework.client.ui.uirouter.UiRoute with org.modality_project.ecommerce.frontoffice.activities.person.PersonUiRoute;

}