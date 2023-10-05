// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.crm.client.profile.plugin {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.backoffice.mainframe.fx;
    requires modality.base.client.activity;
    requires modality.base.client.profile.fx;
    requires modality.base.shared.entities;
    requires modality.crm.backoffice.organization.fx;
    requires modality.crm.client.authn.fx;
    requires modality.crm.client.personaldetails;
    requires modality.crm.shared.authn;
    requires webfx.extras.switches;
    requires webfx.extras.theme;
    requires webfx.extras.util.layout;
    requires webfx.extras.util.pane;
    requires webfx.kit.launcher;
    requires webfx.kit.util;
    requires webfx.platform.boot;
    requires webfx.platform.console;
    requires webfx.platform.uischeduler;
    requires webfx.platform.util;
    requires webfx.stack.authn;
    requires webfx.stack.authn.logout.client;
    requires webfx.stack.i18n;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.session.state.client.fx;
    requires webfx.stack.ui.action;
    requires webfx.stack.ui.operation.action;

    // Exported packages
    exports one.modality.crm.client.profile;

    // Provided services
    provides dev.webfx.platform.boot.spi.ApplicationJob with one.modality.crm.client.profile.ModalityClientProfileInitJob;

}