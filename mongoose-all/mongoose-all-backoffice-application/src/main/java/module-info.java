// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.all.backoffice.application {

    // Direct dependencies modules
    requires javafx.graphics;
    requires mongoose.base.backoffice.masterslave;
    requires mongoose.base.client.application;
    requires mongoose.crm.backoffice.bookingdetailspanel;

    // Exported packages
    exports mongoose.all.backoffice.application;

    // Provided services
    provides javafx.application.Application with mongoose.all.backoffice.application.MongooseBackOfficeApplication;

}