// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The UI panel that displays all booking details, including the personal details.
 */
module modality.crm.backoffice.bookingdetailspanel {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.backoffice.masterslave;
    requires modality.base.backoffice.operations.generic;
    requires modality.base.client.presentationmodel;
    requires modality.base.shared.entities;
    requires modality.crm.backoffice.operations.mail;
    requires modality.ecommerce.backoffice.operations.document;
    requires modality.ecommerce.backoffice.operations.documentline;
    requires modality.ecommerce.backoffice.operations.moneytransfer;
    requires webfx.extras.imagestore;
    requires webfx.extras.label;
    requires webfx.extras.visual.grid;
    requires webfx.kit.util;
    requires webfx.platform.util;
    requires webfx.stack.i18n.controls;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.reactive.visual;
    requires webfx.stack.routing.activity;
    requires webfx.stack.ui.action;
    requires webfx.stack.ui.controls;
    requires webfx.stack.ui.operation.action;

    // Exported packages
    exports one.modality.crm.backoffice.controls.bookingdetailspanel;

}