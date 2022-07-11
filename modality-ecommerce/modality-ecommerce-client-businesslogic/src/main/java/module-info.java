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
    requires webfx.framework.shared.orm.entity;
    requires webfx.platform.shared.util;
    requires webfx.stack.async;
    requires webfx.stack.db.query;
    requires webfx.stack.db.submit;

    // Exported packages
    exports org.modality_project.ecommerce.client.businessdata.feesgroup;
    exports org.modality_project.ecommerce.client.businessdata.preselection;
    exports org.modality_project.ecommerce.client.businessdata.workingdocument;
    exports org.modality_project.ecommerce.client.businesslogic.feesgroup;
    exports org.modality_project.ecommerce.client.businesslogic.option;
    exports org.modality_project.ecommerce.client.businesslogic.pricing;
    exports org.modality_project.ecommerce.client.businesslogic.rules;
    exports org.modality_project.ecommerce.client.businesslogic.workingdocument;

}