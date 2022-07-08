// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.event.frontoffice.activities.terms {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.icons;
    requires modality.base.client.util;
    requires modality.ecommerce.client.bookingprocess;
    requires modality.event.client.sectionpanel;
    requires webfx.extras.visual.base;
    requires webfx.extras.visual.controls.grid.peers.base;
    requires webfx.framework.client.activity;
    requires webfx.framework.client.i18n;
    requires webfx.framework.client.orm.domainmodel.activity;
    requires webfx.framework.client.orm.reactive.visual;
    requires webfx.framework.client.uirouter;
    requires webfx.framework.client.util;
    requires webfx.framework.shared.orm.dql;
    requires webfx.platform.client.windowhistory;
    requires webfx.platform.shared.util;

    // Exported packages
    exports org.modality_project.event.frontoffice.activities.terms;
    exports org.modality_project.event.frontoffice.activities.terms.routing;
    exports org.modality_project.event.frontoffice.operations.terms;

    // Provided services
    provides dev.webfx.framework.client.ui.uirouter.UiRoute with org.modality_project.event.frontoffice.activities.terms.TermsUiRoute;

}