// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.crm.backoffice.activity.administrators.plugin {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.bootstrap;
    requires modality.base.client.i18n;
    requires modality.base.client.icons;
    requires modality.base.client.mainframe.fx;
    requires modality.base.shared.domainmodel;
    requires modality.base.shared.entities;
    requires modality.crm.backoffice.activity.superadmin.plugin;
    requires webfx.extras.cell;
    requires webfx.extras.controlfactory;
    requires webfx.extras.i18n;
    requires webfx.extras.i18n.controls;
    requires webfx.extras.operation;
    requires webfx.extras.panes;
    requires webfx.extras.styles.bootstrap;
    requires webfx.extras.switches;
    requires webfx.extras.util.dialog;
    requires webfx.extras.visual;
    requires webfx.extras.visual.grid;
    requires webfx.kit.util;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.orm.reactive.entities;
    requires webfx.stack.orm.reactive.visual;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.crm.backoffice.activities.administrators;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.crm.backoffice.activities.administrators.AdministratorsRouting.AdministratorsUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.crm.backoffice.activities.administrators.AdministratorsRouting.RouteToAdministratorsRequestEmitter;

}