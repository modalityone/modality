// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.ecommerce.client2018.businesslogic {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.entities;
    requires transitive modality.base.client2018.aggregates;
    requires modality.base.shared.entities;
    requires modality.event.client2018.calendar;
    requires modality.hotel.shared2018.time;
    requires transitive webfx.platform.async;
    requires webfx.platform.util;
    requires webfx.stack.db.query;
    requires webfx.stack.db.submit;
    requires webfx.stack.orm.entity;

    // Exported packages
    exports one.modality.ecommerce.client2018.businessdata.feesgroup;
    exports one.modality.ecommerce.client2018.businessdata.preselection;
    exports one.modality.ecommerce.client2018.businessdata.workingdocument;
    exports one.modality.ecommerce.client2018.businesslogic.feesgroup;
    exports one.modality.ecommerce.client2018.businesslogic.option;
    exports one.modality.ecommerce.client2018.businesslogic.pricing;
    exports one.modality.ecommerce.client2018.businesslogic.rules;
    exports one.modality.ecommerce.client2018.businesslogic.workingdocument;

}