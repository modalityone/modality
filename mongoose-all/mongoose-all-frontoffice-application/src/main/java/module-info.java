// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.all.frontoffice.application {

    // Direct dependencies modules
    requires javafx.graphics;
    requires mongoose.client.application;
    requires mongoose.frontoffice.activities.cart;
    requires mongoose.frontoffice.activities.contactus;
    requires mongoose.frontoffice.activities.fees;
    requires mongoose.frontoffice.activities.options;
    requires mongoose.frontoffice.activities.payment;
    requires mongoose.frontoffice.activities.person;
    requires mongoose.frontoffice.activities.program;
    requires mongoose.frontoffice.activities.startbooking;
    requires mongoose.frontoffice.activities.summary;
    requires mongoose.frontoffice.activities.terms;

    // Exported packages
    exports mongoose.all.frontoffice.application;

    // Provided services
    provides javafx.application.Application with mongoose.all.frontoffice.application.MongooseFrontOfficeApplication;

}