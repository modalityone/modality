// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.crm.client.profile.plugin {

    // Direct dependencies modules
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.activity;
    requires modality.base.client.mainframe.fx;
    requires modality.base.client.profile.fx;
    requires modality.base.shared.entities;
    requires modality.crm.backoffice.organization.fx;
    requires modality.crm.client.authn.fx;
    requires modality.crm.client.personaldetails;
    requires webfx.extras.action;
    requires webfx.extras.i18n;
    requires webfx.extras.operation.action;
    requires webfx.extras.panes;
    requires webfx.extras.switches;
    requires webfx.extras.theme;
    requires webfx.extras.util.layout;
    requires webfx.extras.visual.grid;
    requires webfx.kit.util;
    requires webfx.platform.boot;
    requires webfx.platform.util;
    requires webfx.stack.authn;
    requires webfx.stack.authn.logout.client;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.orm.reactive.visual;
    requires webfx.stack.session.state.client.fx;

    // Exported packages
    exports one.modality.crm.client.profile;

    // Provided services
    provides dev.webfx.platform.boot.spi.ApplicationJob with one.modality.crm.client.profile.ModalityClientProfileInitJob;

}