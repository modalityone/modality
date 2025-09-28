// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.crm.frontoffice.activity.userprofile.plugin {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.activity;
    requires modality.base.client.cloudinary;
    requires modality.base.client.icons;
    requires modality.base.client.mainframe.fx;
    requires modality.base.client.time;
    requires modality.base.frontoffice.utility;
    requires modality.base.shared.entities;
    requires modality.crm.client.authn.fx;
    requires modality.crm.client.i18n;
    requires modality.crm.frontoffice.activity.createaccount.plugin;
    requires modality.crm.frontoffice.help;
    requires webfx.extras.async;
    requires webfx.extras.controlfactory;
    requires webfx.extras.filepicker;
    requires webfx.extras.i18n;
    requires webfx.extras.i18n.controls;
    requires webfx.extras.operation;
    requires webfx.extras.panes;
    requires webfx.extras.responsive;
    requires webfx.extras.styles.bootstrap;
    requires webfx.extras.styles.materialdesign;
    requires webfx.extras.time.format;
    requires webfx.extras.time.pickers;
    requires webfx.extras.util.animation;
    requires webfx.extras.util.control;
    requires webfx.extras.util.dialog;
    requires webfx.extras.util.layout;
    requires webfx.extras.validation;
    requires webfx.kit.util;
    requires webfx.platform.blob;
    requires webfx.platform.console;
    requires webfx.platform.file;
    requires webfx.platform.uischeduler;
    requires webfx.platform.windowhistory;
    requires webfx.platform.windowlocation;
    requires webfx.stack.authn;
    requires webfx.stack.authn.login.ui;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.binding;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.crm.frontoffice.activities.userprofile;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.crm.frontoffice.activities.userprofile.UserProfileRouting.UserProfileUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.crm.frontoffice.activities.userprofile.UserProfileRouting.RouteToUserProfileRequestEmitter;

}