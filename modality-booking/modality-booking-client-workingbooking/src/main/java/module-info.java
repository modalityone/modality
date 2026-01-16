// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.booking.client.workingbooking {

    // Direct dependencies modules
    requires javafx.base;
    requires modality.base.client.time;
    requires modality.base.shared.entities;
    requires modality.base.shared.knownitems;
    requires modality.crm.client.authn.fx;
    requires transitive modality.ecommerce.document.service;
    requires modality.ecommerce.policy.service;
    requires modality.ecommerce.shared.pricecalculator;
    requires webfx.extras.i18n;
    requires webfx.kit.util;
    requires webfx.platform.async;
    requires webfx.platform.meta;
    requires webfx.platform.scheduler;
    requires webfx.platform.uischeduler;
    requires webfx.platform.util;
    requires webfx.stack.orm.entity;

    // Exported packages
    exports one.modality.booking.client.workingbooking;

}