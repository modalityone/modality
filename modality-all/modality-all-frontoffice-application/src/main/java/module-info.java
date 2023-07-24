// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.all.frontoffice.application {

    // Direct dependencies modules
    requires javafx.graphics;
    requires modality.base.client.application;

    // Exported packages
    exports one.modality.all.frontoffice.application;

    // Provided services
    provides javafx.application.Application with
            one.modality.all.frontoffice.application.ModalityFrontOfficeApplication;
}
