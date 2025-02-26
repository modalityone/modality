// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * Defines the different operations that a back-office user can possibly execute on a booking itself.
 */
module modality.ecommerce.backoffice.operations.document {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.backoffice.operations.generic;
    requires modality.base.client.i18n;
    requires modality.base.client.mainframe.fx;
    requires modality.base.shared.entities;
    requires modality.crm.client.personaldetails;
    requires modality.ecommerce.client.workingbooking;
    requires modality.ecommerce.document.service;
    requires modality.event.client.booking;
    requires modality.event.client.recurringevents;
    requires webfx.kit.launcher;
    requires webfx.platform.async;
    requires webfx.platform.console;
    requires webfx.platform.uischeduler;
    requires webfx.stack.i18n;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.ui.controls;
    requires webfx.stack.ui.dialog;
    requires webfx.stack.ui.exceptions;
    requires webfx.stack.ui.operation;

    // Exported packages
    exports one.modality.ecommerce.backoffice.operations.entities.document;
    exports one.modality.ecommerce.backoffice.operations.entities.document.cart;
    exports one.modality.ecommerce.backoffice.operations.entities.document.multiplebookings;
    exports one.modality.ecommerce.backoffice.operations.entities.document.registration;
    exports one.modality.ecommerce.backoffice.operations.entities.document.security;

}