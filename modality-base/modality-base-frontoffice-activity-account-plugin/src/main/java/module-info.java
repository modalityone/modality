// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The front-office Account activity.
 */
module modality.base.frontoffice.activity.account.plugin {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.brand;
    requires modality.base.client.css;
    requires modality.base.client.mainframe.fx;
    requires modality.base.frontoffice.utility;
    requires modality.base.shared.entities;
    requires modality.crm.client.authn.fx;
    requires modality.crm.client.personaldetails;
    requires modality.ecommerce.client.i18n;
    requires webfx.extras.controlfactory;
    requires webfx.extras.i18n;
    requires webfx.extras.i18n.controls;
    requires webfx.extras.operation;
    requires webfx.extras.operation.action;
    requires webfx.extras.panes;
    requires webfx.extras.styles.bootstrap;
    requires webfx.extras.util.control;
    requires webfx.kit.util;
    requires webfx.platform.resource;
    requires webfx.platform.util;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.orm.reactive.entities;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.base.frontoffice.activities.account;
    exports one.modality.base.frontoffice.activities.account.friendsfamily;
    exports one.modality.base.frontoffice.activities.account.friendsfamily.edit;
    exports one.modality.base.frontoffice.activities.account.personalinfo;
    exports one.modality.base.frontoffice.activities.account.settings;
    exports one.modality.base.frontoffice.operations.routes.account;

    // Resources packages
    opens one.modality.base.frontoffice.activities.account.images;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.base.frontoffice.activities.account.AccountRouting.AccountUiRoute, one.modality.base.frontoffice.activities.account.personalinfo.AccountPersonalInformationRouting.AccountPersonalInformationUiRoute, one.modality.base.frontoffice.activities.account.settings.AccountSettingsRouting.AccountSettingsUiRoute, one.modality.base.frontoffice.activities.account.friendsfamily.AccountFriendsAndFamilyRouting.AccountFriendsAndFamilyUiRoute, one.modality.base.frontoffice.activities.account.friendsfamily.edit.AccountFriendsAndFamilyEditRouting.AccountFriendsAndFamilyEditUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.base.frontoffice.activities.account.AccountRouting.RouteToAccountRequestEmitter, one.modality.base.frontoffice.activities.account.settings.AccountSettingsRouting.RouteToAccountSettingsRequestEmitter, one.modality.base.frontoffice.activities.account.personalinfo.AccountPersonalInformationRouting.RouteToAccountPersonalInformationEmitter, one.modality.base.frontoffice.activities.account.friendsfamily.AccountFriendsAndFamilyRouting.RouteToAccountFriendsAndFamilyEmitter, one.modality.base.frontoffice.activities.account.friendsfamily.edit.AccountFriendsAndFamilyEditRouting.RouteToAccountFriendsAndFamilyEditEmitter;

}