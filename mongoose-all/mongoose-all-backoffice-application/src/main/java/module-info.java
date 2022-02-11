// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.all.backoffice.application {

    // Direct dependencies modules
    requires javafx.graphics;
    requires mongoose.base.backoffice.activities.monitor;
    requires mongoose.base.backoffice.activities.operations;
    requires mongoose.base.backoffice.masterslave;
    requires mongoose.base.client.application;
    requires mongoose.catering.backoffice.activities.diningareas;
    requires mongoose.crm.backoffice.activities.authorizations;
    requires mongoose.crm.backoffice.activities.letter;
    requires mongoose.crm.backoffice.activities.letters;
    requires mongoose.crm.backoffice.activities.organizations;
    requires mongoose.crm.backoffice.activities.users;
    requires mongoose.crm.backoffice.bookingdetailspanel;
    requires mongoose.ecommerce.backoffice.activities.bookings;
    requires mongoose.ecommerce.backoffice.activities.income;
    requires mongoose.ecommerce.backoffice.activities.payments;
    requires mongoose.ecommerce.backoffice.activities.statements;
    requires mongoose.ecommerce.backoffice.activities.statistics;
    requires mongoose.event.backoffice.activities.cloneevent;
    requires mongoose.event.backoffice.activities.events;
    requires mongoose.event.backoffice.activities.options;
    requires mongoose.hotel.backoffice.activities.roomsgraphic;

    // Exported packages
    exports mongoose.all.backoffice.application;

    // Provided services
    provides javafx.application.Application with mongoose.all.backoffice.application.MongooseBackOfficeApplication;

}