// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.event.frontoffice2018.activities.terms {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.icons;
    requires modality.base.client.util;
    requires modality.ecommerce.client2018.bookingprocess;
    requires modality.event.client2018.sectionpanel;
    requires webfx.extras.util.layout;
    requires webfx.extras.visual;
    requires webfx.extras.visual.grid.peers.base;
    requires webfx.platform.windowhistory;
    requires webfx.stack.i18n;
    requires webfx.stack.orm.domainmodel.activity;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.reactive.visual;
    requires webfx.stack.routing.uirouter;

    // Exported packages
    exports one.modality.event.frontoffice2018.activities.terms;
    exports one.modality.event.frontoffice2018.activities.terms.routing;
    exports one.modality.event.frontoffice2018.operations.terms;

    // Provided services
    provides dev.webfx.stack.routing.uirouter.UiRoute with one.modality.event.frontoffice2018.activities.terms.TermsUiRoute;

}