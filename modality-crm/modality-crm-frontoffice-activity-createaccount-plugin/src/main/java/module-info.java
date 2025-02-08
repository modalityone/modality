// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.crm.frontoffice.activity.createaccount.plugin {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.activity;
    requires modality.base.client.mainframe.fx;
    requires modality.base.shared.domainmodel;
    requires modality.base.shared.entities;
    requires modality.crm.client.activity.login.plugin;
    requires modality.crm.client.activity.magiclink.plugin;
    requires modality.crm.client.i18n;
    requires webfx.extras.panes;
    requires webfx.extras.styles.bootstrap;
    requires webfx.extras.styles.materialdesign;
    requires webfx.extras.util.control;
    requires webfx.extras.util.layout;
    requires webfx.kit.launcher;
    requires webfx.kit.util;
    requires webfx.platform.console;
    requires webfx.platform.uischeduler;
    requires webfx.platform.windowhistory;
    requires webfx.stack.authn;
    requires webfx.stack.authn.login.ui.gateway.password.plugin;
    requires webfx.stack.i18n;
    requires webfx.stack.i18n.controls;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.binding;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.validation;

    // Exported packages
    exports one.modality.crm.frontoffice.activities.createaccount;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.crm.frontoffice.activities.createaccount.CreateAccountRouting.CreateAccountUiRoute;

}