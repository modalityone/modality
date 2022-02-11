// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.event.frontoffice.activities.terms {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires mongoose.base.client.icons;
    requires mongoose.base.client.util;
    requires mongoose.ecommerce.client.bookingprocess;
    requires mongoose.event.client.sectionpanel;
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
    exports mongoose.event.frontoffice.activities.terms;
    exports mongoose.event.frontoffice.activities.terms.routing;
    exports mongoose.event.frontoffice.operations.terms;

    // Provided services
    provides dev.webfx.framework.client.ui.uirouter.UiRoute with mongoose.event.frontoffice.activities.terms.TermsUiRoute;

}