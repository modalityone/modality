// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.frontoffice.activities.person {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires mongoose.client.activities.login;
    requires mongoose.client.bookingprocess;
    requires mongoose.client.businesslogic;
    requires mongoose.client.personaldetails;
    requires mongoose.client.util;
    requires mongoose.frontoffice.activities.summary;
    requires webfx.framework.client.action;
    requires webfx.framework.client.activity;
    requires webfx.framework.client.orm.domainmodel.activity;
    requires webfx.framework.client.uirouter;
    requires webfx.framework.client.util;
    requires webfx.kit.util;
    requires webfx.platform.client.windowhistory;
    requires webfx.platform.shared.util;

    // Exported packages
    exports mongoose.frontoffice.activities.person;
    exports mongoose.frontoffice.activities.person.routing;
    exports mongoose.frontoffice.operations.person;

    // Provided services
    provides dev.webfx.framework.client.ui.uirouter.UiRoute with mongoose.frontoffice.activities.person.PersonUiRoute;

}