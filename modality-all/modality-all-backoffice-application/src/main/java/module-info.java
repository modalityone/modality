// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.all.backoffice.application {

    // Direct dependencies modules
    requires javafx.graphics;
    requires modality.base.backoffice.masterslave;
    requires modality.base.client.application;
    requires modality.crm.backoffice.bookingdetailspanel;

    // Exported packages
    exports org.modality_project.all.backoffice.application;

    // Provided services
    provides javafx.application.Application with org.modality_project.all.backoffice.application.MongooseBackOfficeApplication;

}