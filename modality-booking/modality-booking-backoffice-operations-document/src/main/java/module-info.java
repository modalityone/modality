// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.booking.backoffice.operations.document {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.mainframe.fx;
    requires modality.base.shared.entities;
    requires modality.booking.client.scheduleditemsselector;
    requires modality.booking.client.scheduleditemsselector.box;
    requires modality.booking.client.workingbooking;
    requires modality.ecommerce.backoffice.operations.document;
    requires modality.ecommerce.document.service;
    requires webfx.extras.exceptions;
    requires webfx.extras.i18n;
    requires webfx.extras.operation;
    requires webfx.extras.util.dialog;
    requires webfx.platform.async;
    requires webfx.platform.console;
    requires webfx.platform.util;
    requires webfx.stack.orm.entity;

    // Exported packages
    exports one.modality.booking.backoffice.operations.entities.document.registration;

}