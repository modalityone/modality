// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.crm.frontoffice.activity.members.plugin {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.mainframe.fx;
    requires modality.base.shared.entities;
    requires modality.crm.client.authn.fx;
    requires modality.crm.client.i18n;
    requires modality.crm.frontoffice.activity.userprofile.plugin;
    requires modality.crm.frontoffice.help;
    requires modality.crm.shared.authn;
    requires webfx.extras.async;
    requires webfx.extras.i18n;
    requires webfx.extras.i18n.controls;
    requires webfx.extras.operation;
    requires webfx.extras.panes;
    requires webfx.extras.styles.bootstrap;
    requires webfx.extras.util.dialog;
    requires webfx.extras.util.layout;
    requires webfx.extras.validation;
    requires webfx.kit.util;
    requires webfx.platform.console;
    requires webfx.platform.uischeduler;
    requires webfx.platform.windowhistory;
    requires webfx.stack.authn.logout.client;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.binding;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.crm.frontoffice.activities.members;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.crm.frontoffice.activities.members.MembersRouting.MembersUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.crm.frontoffice.activities.members.MembersRouting.RouteToUserProfileRequestEmitter;

}