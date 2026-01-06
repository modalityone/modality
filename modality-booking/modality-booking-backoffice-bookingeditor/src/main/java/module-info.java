// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.booking.backoffice.bookingeditor {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.mainframe.fx;
    requires modality.base.shared.domainmodel;
    requires modality.base.shared.entities;
    requires modality.base.shared.knownitems;
    requires modality.booking.client.workingbooking;
    requires modality.ecommerce.policy.service;
    requires webfx.extras.async;
    requires webfx.extras.controlfactory;
    requires webfx.extras.i18n.controls;
    requires webfx.extras.styles.bootstrap;
    requires webfx.extras.util.border;
    requires webfx.extras.util.dialog;
    requires webfx.kit.util;
    requires webfx.platform.async;
    requires webfx.platform.service;
    requires webfx.platform.util;
    requires webfx.stack.orm.datasourcemodel.service;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.entity;
    requires webfx.stack.orm.entity.controls;

    // Exported packages
    exports one.modality.booking.backoffice.bookingeditor;
    exports one.modality.booking.backoffice.bookingeditor.family;
    exports one.modality.booking.backoffice.bookingeditor.multi;
    exports one.modality.booking.backoffice.bookingeditor.spi;

    // Used services
    uses one.modality.booking.backoffice.bookingeditor.spi.BookingEditorProvider;

}