// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.hotel.backoffice.activity.reception.plugin {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.activity.organizationdependent;
    requires modality.base.client.gantt.fx;
    requires modality.base.client.icons;
    requires modality.base.client.mainframe.fx;
    requires modality.base.shared.entities;
    requires modality.crm.backoffice.organization.fx;
    requires webfx.extras.controlfactory;
    requires webfx.extras.i18n;
    requires webfx.extras.i18n.controls;
    requires webfx.extras.operation;
    requires webfx.extras.styles.bootstrap;
    requires webfx.extras.util.dialog;
    requires webfx.kit.util;
    requires webfx.platform.console;
    requires webfx.platform.windowhistory;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.orm.reactive.entities;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.hotel.backoffice.activities.reception;
    exports one.modality.hotel.backoffice.activities.reception.data;
    exports one.modality.hotel.backoffice.activities.reception.i18n;
    exports one.modality.hotel.backoffice.activities.reception.modal;
    exports one.modality.hotel.backoffice.activities.reception.row;
    exports one.modality.hotel.backoffice.activities.reception.util;
    exports one.modality.hotel.backoffice.activities.reception.view;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.hotel.backoffice.activities.reception.ReceptionRouting.ReceptionUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.hotel.backoffice.activities.reception.ReceptionRouting.RouteToReceptionRequestEmitter;

}