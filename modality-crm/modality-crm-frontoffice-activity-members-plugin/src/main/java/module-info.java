// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.crm.frontoffice.activity.members.plugin {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.activity;
    requires modality.base.client.bootstrap;
    requires modality.base.client.icons;
    requires modality.base.client.mainframe.fx;
    requires modality.base.client.time;
    requires modality.base.shared.entities;
    requires modality.base.shared.util;
    requires modality.crm.client.authn.fx;
    requires modality.crm.client.i18n;
    requires modality.crm.frontoffice.activity.createaccount.plugin;
    requires modality.crm.frontoffice.help;
    requires modality.crm.shared.authn;
    requires webfx.extras.async;
    requires webfx.extras.controlfactory;
    requires webfx.extras.i18n;
    requires webfx.extras.i18n.controls;
    requires webfx.extras.operation;
    requires webfx.extras.panes;
    requires webfx.extras.styles.bootstrap;
    requires webfx.extras.styles.materialdesign;
    requires webfx.extras.time.format;
    requires webfx.extras.time.pickers;
    requires webfx.extras.util.dialog;
    requires webfx.extras.validation;
    requires webfx.kit.util;
    requires webfx.platform.async;
    requires webfx.platform.console;
    requires webfx.platform.resource;
    requires webfx.platform.uischeduler;
    requires webfx.platform.util;
    requires webfx.platform.windowhistory;
    requires webfx.platform.windowlocation;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.orm.entity.binding;
    requires webfx.extras.responsive;

    // Exported packages
    exports one.modality.crm.frontoffice.activities.members;
    exports one.modality.crm.frontoffice.activities.members.controller;
    exports one.modality.crm.frontoffice.activities.members.model;
    exports one.modality.crm.frontoffice.activities.members.view;

    // Resources packages
    opens one.modality.crm.frontoffice.activities.members.emails;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.crm.frontoffice.activities.members.MembersRouting.MembersUiRoute, one.modality.crm.frontoffice.activities.members.ApproveInvitationRouting.ApproveInvitationUiRoute, one.modality.crm.frontoffice.activities.members.DeclineInvitationRouting.DeclineInvitationUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.crm.frontoffice.activities.members.MembersRouting.RouteToUserProfileRequestEmitter;

}