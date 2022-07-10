// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.ecommerce.frontoffice.activities.payment {

    // Direct dependencies modules
    requires java.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.aggregates;
    requires modality.base.client.entities;
    requires modality.base.client.util;
    requires modality.base.shared.domainmodel;
    requires modality.base.shared.entities;
    requires modality.ecommerce.frontoffice.activities.cart.routing;
    requires modality.event.client.sectionpanel;
    requires webfx.extras.webtext.controls;
    requires webfx.framework.client.action;
    requires webfx.framework.client.activity;
    requires webfx.framework.client.controls;
    requires webfx.framework.client.i18n;
    requires webfx.framework.client.orm.domainmodel.activity;
    requires webfx.framework.client.uirouter;
    requires webfx.framework.client.util;
    requires webfx.framework.shared.orm.entity;
    requires webfx.platform.client.uischeduler;
    requires webfx.platform.client.websocketbus;
    requires webfx.platform.client.windowhistory;
    requires webfx.platform.client.windowlocation;
    requires webfx.platform.shared.async;
    requires webfx.platform.shared.bus;
    requires webfx.platform.shared.log;
    requires webfx.platform.shared.submit;
    requires webfx.platform.shared.util;

    // Exported packages
    exports org.modality_project.ecommerce.frontoffice.activities.payment;
    exports org.modality_project.ecommerce.frontoffice.activities.payment.routing;
    exports org.modality_project.ecommerce.frontoffice.operations.payment;

    // Provided services
    provides dev.webfx.stack.framework.client.ui.uirouter.UiRoute with org.modality_project.ecommerce.frontoffice.activities.payment.PaymentUiRoute;

}