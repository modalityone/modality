// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.event.frontoffice.activities.account {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.frontoffice.utility;
    requires modality.base.shared.entities;
    requires webfx.extras.util.layout;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.controls;
    requires webfx.stack.ui.operation;
    requires webfx.stack.ui.operation.action;

    // Exported packages
    exports one.modality.event.frontoffice.activities.account;
    exports one.modality.event.frontoffice.activities.account.routing;
    exports one.modality.event.frontoffice.operations.routes.account;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.event.frontoffice.activities.account.AccountUiRoute, one.modality.event.frontoffice.activities.account.AccountPersonalInformationUiRoute, one.modality.event.frontoffice.activities.account.AccountSettingsUiRoute, one.modality.event.frontoffice.activities.account.AccountFriendsAndFamilyUiRoute, one.modality.event.frontoffice.activities.account.AccountFriendsAndFamilyEditUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.event.frontoffice.activities.account.RouteToAccountRequestEmitter, one.modality.event.frontoffice.activities.account.RouteToAccountSettingsRequestEmitter, one.modality.event.frontoffice.activities.account.RouteToAccountPersonalInformationEmitter, one.modality.event.frontoffice.activities.account.RouteToAccountFriendsAndFamilyEmitter, one.modality.event.frontoffice.activities.account.RouteToAccountFriendsAndFamilyEditEmitter;

}