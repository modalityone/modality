// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.backoffice.application {

    // Direct dependencies modules
    requires javafx.graphics;
    requires modality.base.backoffice.mainframe.activity;
    requires modality.base.client.application;

    // Exported packages
    exports one.modality.base.backoffice.application;

    // Provided services
    provides javafx.application.Application with one.modality.base.backoffice.application.ModalityBackOfficeApplication;

}