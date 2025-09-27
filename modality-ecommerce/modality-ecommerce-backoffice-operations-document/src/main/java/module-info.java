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
    requires modality.ecommerce.client.scheduleditemsselector;
    requires modality.ecommerce.client.scheduleditemsselector.box;
    requires modality.ecommerce.client.workingbooking;
    requires modality.ecommerce.document.service;
    requires webfx.extras.exceptions;
    requires webfx.extras.i18n;
    requires webfx.extras.operation;
    requires webfx.extras.util.dialog;
    requires webfx.kit.launcher;
    requires webfx.platform.async;
    requires webfx.platform.console;
    requires webfx.platform.util;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.controls;

    // Exported packages
    exports one.modality.ecommerce.backoffice.operations.entities.document;
    exports one.modality.ecommerce.backoffice.operations.entities.document.cart;
    exports one.modality.ecommerce.backoffice.operations.entities.document.multiplebookings;
    exports one.modality.ecommerce.backoffice.operations.entities.document.registration;
    exports one.modality.ecommerce.backoffice.operations.entities.document.security;

}