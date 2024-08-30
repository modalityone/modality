// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The UI operations for mails: opening a mail, and composing a new mail.
 */
module modality.crm.backoffice.operations.mail {

    // Direct dependencies modules
    requires javafx.graphics;
    requires modality.base.client.mainframe.fx;
    requires modality.base.shared.entities;
    requires webfx.platform.async;
    requires webfx.stack.ui.operation;

    // Exported packages
    exports one.modality.crm.backoffice.operations.entities.mail;

}