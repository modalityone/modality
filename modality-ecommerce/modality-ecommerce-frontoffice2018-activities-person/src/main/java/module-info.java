// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.ecommerce.frontoffice2018.activities.person {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.util;
    requires modality.crm.client.activities.login;
    requires modality.crm.client2018.personaldetails;
    requires modality.ecommerce.client2018.bookingprocess;
    requires modality.ecommerce.client2018.businesslogic;
    requires modality.ecommerce.frontoffice2018.activities.summary;
    requires webfx.extras.util.layout;
    requires webfx.kit.util;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.session.state.client.fx;

    // Exported packages
    exports one.modality.ecommerce.frontoffice.activities.person;
    exports one.modality.ecommerce.frontoffice.activities.person.routing;
    exports one.modality.ecommerce.frontoffice.operations.person;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.ecommerce.frontoffice.activities.person.PersonUiRoute;

}