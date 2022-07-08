// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.event.frontoffice.activities.fees {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.activity;
    requires modality.base.client.aggregates;
    requires modality.base.client.entities;
    requires modality.base.client.icons;
    requires modality.base.client.util;
    requires modality.base.shared.entities;
    requires modality.ecommerce.client.bookingprocess;
    requires modality.ecommerce.client.businesslogic;
    requires modality.event.client.sectionpanel;
    requires modality.event.frontoffice.activities.options;
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
    requires webfx.platform.shared.async;
    requires webfx.platform.shared.json;
    requires webfx.platform.shared.log;
    requires webfx.platform.shared.query;
    requires webfx.platform.shared.util;

    // Exported packages
    exports org.modality_project.event.frontoffice.activities.fees;
    exports org.modality_project.event.frontoffice.activities.fees.routing;
    exports org.modality_project.event.frontoffice.operations.fees;

    // Provided services
    provides dev.webfx.framework.client.ui.uirouter.UiRoute with org.modality_project.event.frontoffice.activities.fees.FeesUiRoute;

}