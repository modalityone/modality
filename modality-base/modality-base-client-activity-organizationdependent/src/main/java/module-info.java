// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * Define some class materials to build organization-dependent client activities.
 */
module modality.base.client.activity.organizationdependent {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires modality.base.client.activity;
    requires modality.base.client.presentationmodel;
    requires modality.crm.backoffice.organization.fx;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.base.client.activity.organizationdependent;

}