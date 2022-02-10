// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.all.backoffice.application {

    // Direct dependencies modules
    requires javafx.graphics;
    requires mongoose.backoffice.activities.authorizations;
    requires mongoose.backoffice.activities.bookings;
    requires mongoose.backoffice.activities.cloneevent;
    requires mongoose.backoffice.activities.diningareas;
    requires mongoose.backoffice.activities.events;
    requires mongoose.backoffice.activities.income;
    requires mongoose.backoffice.activities.letter;
    requires mongoose.backoffice.activities.letters;
    requires mongoose.backoffice.activities.monitor;
    requires mongoose.backoffice.activities.operations;
    requires mongoose.backoffice.activities.options;
    requires mongoose.backoffice.activities.organizations;
    requires mongoose.backoffice.activities.payments;
    requires mongoose.backoffice.activities.roomsgraphic;
    requires mongoose.backoffice.activities.statements;
    requires mongoose.backoffice.activities.statistics;
    requires mongoose.backoffice.activities.users;
    requires mongoose.backoffice.bookingdetailspanel;
    requires mongoose.backoffice.masterslave;
    requires mongoose.client.application;

    // Exported packages
    exports mongoose.all.backoffice.application;

    // Provided services
    provides javafx.application.Application with mongoose.all.backoffice.application.MongooseBackOfficeApplication;

}