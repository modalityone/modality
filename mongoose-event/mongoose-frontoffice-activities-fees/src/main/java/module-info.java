// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.frontoffice.activities.fees {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires mongoose.client.activity;
    requires mongoose.client.aggregates;
    requires mongoose.client.bookingprocess;
    requires mongoose.client.businesslogic;
    requires mongoose.client.entities;
    requires mongoose.client.icons;
    requires mongoose.client.sectionpanel;
    requires mongoose.client.util;
    requires mongoose.frontoffice.activities.options;
    requires mongoose.shared.entities;
    requires webfx.extras.cell;
    requires webfx.extras.imagestore;
    requires webfx.extras.type;
    requires webfx.extras.visual.base;
    requires webfx.extras.visual.controls.grid;
    requires webfx.extras.visual.controls.grid.peers.base;
    requires webfx.framework.client.activity;
    requires webfx.framework.client.i18n;
    requires webfx.framework.client.orm.domainmodel.activity;
    requires webfx.framework.client.uirouter;
    requires webfx.framework.client.util;
    requires webfx.framework.shared.orm.entity;
    requires webfx.kit.util;
    requires webfx.platform.client.uischeduler;
    requires webfx.platform.client.windowhistory;
    requires webfx.platform.shared.json;
    requires webfx.platform.shared.log;
    requires webfx.platform.shared.query;
    requires webfx.platform.shared.util;

    // Exported packages
    exports mongoose.frontoffice.activities.fees;
    exports mongoose.frontoffice.activities.fees.routing;
    exports mongoose.frontoffice.operations.fees;

    // Provided services
    provides dev.webfx.framework.client.ui.uirouter.UiRoute with mongoose.frontoffice.activities.fees.FeesUiRoute;

}