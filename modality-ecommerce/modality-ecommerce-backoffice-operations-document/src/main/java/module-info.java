// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.ecommerce.backoffice.operations.document {

    // Direct dependencies modules
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.backoffice.operations.generic;
    requires modality.base.shared.entities;
    requires modality.crm.client.personaldetails;
    requires webfx.framework.client.controls;
    requires webfx.framework.shared.operation;
    requires webfx.framework.shared.orm.domainmodel;
    requires webfx.kit.launcher;
    requires webfx.stack.async;
    requires webfx.stack.db.submit;

    // Exported packages
    exports org.modality_project.ecommerce.backoffice.operations.entities.document;
    exports org.modality_project.ecommerce.backoffice.operations.entities.document.cart;
    exports org.modality_project.ecommerce.backoffice.operations.entities.document.multiplebookings;
    exports org.modality_project.ecommerce.backoffice.operations.entities.document.registration;
    exports org.modality_project.ecommerce.backoffice.operations.entities.document.security;

}