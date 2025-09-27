// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The UI panel that displays all booking details, including the personal details.
 */
module modality.crm.backoffice.bookingdetailspanel {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.backoffice.masterslave;
    requires modality.base.backoffice.operations.generic;
    requires modality.base.client.i18n;
    requires modality.base.client.mainframe.fx;
    requires modality.base.client.presentationmodel;
    requires modality.base.shared.entities;
    requires modality.booking.backoffice.operations.document;
    requires modality.crm.backoffice.operations.mail;
    requires modality.crm.client.personaldetails;
    requires modality.ecommerce.backoffice.operations.document;
    requires modality.ecommerce.backoffice.operations.documentline;
    requires modality.ecommerce.backoffice.operations.moneytransfer;
    requires webfx.extras.action;
    requires webfx.extras.controlfactory;
    requires webfx.extras.i18n.controls;
    requires webfx.extras.operation;
    requires webfx.extras.operation.action;
    requires webfx.extras.panes;
    requires webfx.extras.visual;
    requires webfx.extras.visual.grid;
    requires webfx.kit.util;
    requires webfx.platform.boot;
    requires webfx.platform.util;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.orm.reactive.visual;
    requires webfx.stack.routing.activity;

    // Exported packages
    exports one.modality.crm.backoffice.controls.bookingdetailspanel;

    // Provided services
    provides dev.webfx.platform.boot.spi.ApplicationJob with one.modality.crm.backoffice.controls.bookingdetailspanel.BookingDetailsPanelApplicationJob;

}