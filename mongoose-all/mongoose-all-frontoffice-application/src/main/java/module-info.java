// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.all.frontoffice.application {

    // Direct dependencies modules
    requires javafx.graphics;
    requires mongoose.base.client.application;

    // Exported packages
    exports org.modality_project.all.frontoffice.application;

    // Provided services
    provides javafx.application.Application with org.modality_project.all.frontoffice.application.MongooseFrontOfficeApplication;

}