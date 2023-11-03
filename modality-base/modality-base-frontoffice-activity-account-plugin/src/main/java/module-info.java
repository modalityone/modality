// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The front-office Account activity.
 */
module modality.base.frontoffice.activity.account.plugin {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.mainframe.dialogarea.fx;
    requires modality.base.frontoffice.utility;
    requires modality.base.shared.entities;
    requires modality.crm.client.authn.fx;
    requires modality.crm.client.personaldetails;
    requires webfx.extras.util.control;
    requires webfx.extras.util.layout;
    requires webfx.platform.resource;
    requires webfx.platform.windowhistory;
    requires webfx.stack.i18n;
    requires webfx.stack.i18n.controls;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.controls;
    requires webfx.stack.ui.operation;
    requires webfx.stack.ui.operation.action;

    // Exported packages
    exports one.modality.base.frontoffice.activities.account;
    exports one.modality.base.frontoffice.activities.account.friendsfamily;
    exports one.modality.base.frontoffice.activities.account.friendsfamily.edit;
    exports one.modality.base.frontoffice.activities.account.personalinfo;
    exports one.modality.base.frontoffice.activities.account.routing;
    exports one.modality.base.frontoffice.activities.account.settings;
    exports one.modality.base.frontoffice.operations.routes.account;

    // Resources packages
    opens one.modality.base.frontoffice.activities.account.images;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.base.frontoffice.activities.account.AccountUiRoute, one.modality.base.frontoffice.activities.account.personalinfo.AccountPersonalInformationUiRoute, one.modality.base.frontoffice.activities.account.settings.AccountSettingsUiRoute, one.modality.base.frontoffice.activities.account.friendsfamily.AccountFriendsAndFamilyUiRoute, one.modality.base.frontoffice.activities.account.friendsfamily.edit.AccountFriendsAndFamilyEditUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.base.frontoffice.activities.account.RouteToAccountRequestEmitter, one.modality.base.frontoffice.activities.account.settings.RouteToAccountSettingsRequestEmitter, one.modality.base.frontoffice.activities.account.personalinfo.RouteToAccountPersonalInformationEmitter, one.modality.base.frontoffice.activities.account.friendsfamily.RouteToAccountFriendsAndFamilyEmitter, one.modality.base.frontoffice.activities.account.friendsfamily.edit.RouteToAccountFriendsAndFamilyEditEmitter;

}