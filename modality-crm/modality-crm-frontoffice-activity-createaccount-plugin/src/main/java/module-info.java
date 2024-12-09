// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.crm.frontoffice.activity.createaccount.plugin {

    // Direct dependencies modules
    requires javafx.graphics;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.crm.frontoffice.activities.createaccount;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.crm.frontoffice.activities.createaccount.CreateAccountRouting.CreateAccountUiRoute;

}