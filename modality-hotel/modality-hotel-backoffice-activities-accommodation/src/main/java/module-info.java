// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The back-office Accommodation activity.
 */
module modality.hotel.backoffice.activities.accommodation {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.backoffice.activity.home.plugin;
    requires modality.base.backoffice.mainframe.fx;
    requires modality.base.client.activity.organizationdependent;
    requires modality.base.client.gantt.fx;
    requires modality.base.client.i18n;
    requires modality.base.client.mainframe.fx;
    requires modality.base.client.tile;
    requires modality.base.client.time;
    requires modality.base.shared.domainmodel;
    requires modality.base.shared.entities;
    requires modality.crm.backoffice.organization.fx;
    requires modality.hotel.backoffice.accommodation;
    requires modality.hotel.backoffice.icons;
    requires webfx.extras.geometry;
    requires webfx.extras.theme;
    requires webfx.extras.time.format;
    requires webfx.extras.time.layout;
    requires webfx.extras.type;
    requires webfx.extras.util.control;
    requires webfx.extras.visual;
    requires webfx.extras.visual.grid;
    requires webfx.kit.util;
    requires webfx.platform.console;
    requires webfx.platform.util;
    requires webfx.platform.windowhistory;
    requires webfx.stack.i18n;
    requires webfx.stack.i18n.controls;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.controls;
    requires webfx.stack.orm.reactive.entities;
    requires webfx.stack.orm.reactive.visual;
    requires webfx.stack.routing.activity;
    requires webfx.stack.routing.router;
    requires webfx.stack.routing.router.client;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.controls;
    requires webfx.stack.ui.dialog;
    requires webfx.stack.ui.operation;
    requires webfx.stack.ui.operation.action;

    // Exported packages
    exports one.modality.hotel.backoffice.activities.accommodation;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.hotel.backoffice.activities.accommodation.AccommodationRouting.AccommodationUiRoute;
    provides dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter with one.modality.hotel.backoffice.activities.accommodation.AccommodationRouting.RouteToAccommodationRequestEmitter;

}