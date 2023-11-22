// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The UI panel that displays the personal details of a booking.
 */
module modality.crm.client.personaldetails {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.activity;
    requires modality.base.client.validation;
    requires modality.base.shared.domainmodel;
    requires modality.base.shared.entities;
    requires modality.crm.client.authn.fx;
    requires webfx.extras.materialdesign;
    requires webfx.extras.panes;
    requires webfx.extras.util.control;
    requires webfx.extras.util.layout;
    requires webfx.extras.util.scene;
    requires webfx.kit.launcher;
    requires webfx.kit.util;
    requires webfx.platform.console;
    requires webfx.platform.uischeduler;
    requires webfx.platform.util;
    requires webfx.stack.i18n;
    requires webfx.stack.i18n.controls;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.ui.dialog;

    // Exported packages
    exports one.modality.crm.client.controls.personaldetails;

}