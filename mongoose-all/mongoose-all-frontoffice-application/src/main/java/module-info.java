// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.all.frontoffice.application {

    // Direct dependencies modules
    requires javafx.graphics;
    requires mongoose.base.client.application;
    requires mongoose.ecommerce.frontoffice.activities.cart;
    requires mongoose.ecommerce.frontoffice.activities.contactus;
    requires mongoose.ecommerce.frontoffice.activities.payment;
    requires mongoose.ecommerce.frontoffice.activities.person;
    requires mongoose.ecommerce.frontoffice.activities.summary;
    requires mongoose.event.frontoffice.activities.fees;
    requires mongoose.event.frontoffice.activities.options;
    requires mongoose.event.frontoffice.activities.program;
    requires mongoose.event.frontoffice.activities.startbooking;
    requires mongoose.event.frontoffice.activities.terms;

    // Exported packages
    exports mongoose.all.frontoffice.application;

    // Provided services
    provides javafx.application.Application with mongoose.all.frontoffice.application.MongooseFrontOfficeApplication;

}