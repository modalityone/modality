// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.event.frontoffice2018.activities.fees {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.activity;
    requires modality.base.client.entities;
    requires modality.base.client.icons;
    requires modality.base.client.util;
    requires modality.base.client2018.aggregates;
    requires modality.base.shared.entities;
    requires modality.ecommerce.client2018.bookingprocess;
    requires modality.ecommerce.client2018.businesslogic;
    requires modality.event.client2018.sectionpanel;
    requires modality.event.frontoffice2018.activities.options;
    requires webfx.extras.cell;
    requires webfx.extras.imagestore;
    requires webfx.extras.type;
    requires webfx.extras.util.layout;
    requires webfx.extras.visual;
    requires webfx.extras.visual.grid;
    requires webfx.extras.visual.grid.peers.base;
    requires webfx.kit.util;
    requires webfx.platform.console;
    requires webfx.platform.json;
    requires webfx.platform.uischeduler;
    requires webfx.platform.util;
    requires webfx.platform.windowhistory;
    requires webfx.stack.i18n;
    requires webfx.stack.i18n.controls;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.entity;
    requires webfx.stack.routing.uirouter;
    requires webfx.stack.ui.json;

    // Exported packages
    exports one.modality.event.frontoffice2018.activities.fees;
    exports one.modality.event.frontoffice2018.activities.fees.routing;
    exports one.modality.event.frontoffice2018.operations.fees;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.event.frontoffice2018.activities.fees.FeesUiRoute;

}