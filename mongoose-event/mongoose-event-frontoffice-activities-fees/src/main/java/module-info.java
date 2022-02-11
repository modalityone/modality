// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.event.frontoffice.activities.fees {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires mongoose.base.client.activity;
    requires mongoose.base.client.aggregates;
    requires mongoose.base.client.entities;
    requires mongoose.base.client.icons;
    requires mongoose.base.client.util;
    requires mongoose.base.shared.entities;
    requires mongoose.ecommerce.client.bookingprocess;
    requires mongoose.ecommerce.client.businesslogic;
    requires mongoose.event.client.sectionpanel;
    requires mongoose.event.frontoffice.activities.options;
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
    exports mongoose.event.frontoffice.activities.fees;
    exports mongoose.event.frontoffice.activities.fees.routing;
    exports mongoose.event.frontoffice.operations.fees;

    // Provided services
    provides dev.webfx.framework.client.ui.uirouter.UiRoute with mongoose.event.frontoffice.activities.fees.FeesUiRoute;

}