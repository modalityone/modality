// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.ecommerce.client.businesslogic {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.aggregates;
    requires modality.base.client.entities;
    requires modality.base.shared.entities;
    requires modality.event.client.calendar;
    requires modality.hotel.shared.time;
    requires webfx.platform.async;
    requires webfx.platform.util;
    requires webfx.stack.db.query;
    requires webfx.stack.db.submit;
    requires webfx.stack.orm.entity;

    // Exported packages
    exports one.modality.ecommerce.client.businessdata.feesgroup;
    exports one.modality.ecommerce.client.businessdata.preselection;
    exports one.modality.ecommerce.client.businessdata.workingdocument;
    exports one.modality.ecommerce.client.businesslogic.feesgroup;
    exports one.modality.ecommerce.client.businesslogic.option;
    exports one.modality.ecommerce.client.businesslogic.pricing;
    exports one.modality.ecommerce.client.businesslogic.rules;
    exports one.modality.ecommerce.client.businesslogic.workingdocument;

}