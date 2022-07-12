// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.client.i18n {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.graphics;
    requires webfx.platform.util;
    requires webfx.stack.i18n;
    requires webfx.stack.i18n.json;
    requires webfx.stack.orm.entity;
    requires webfx.stack.ui.operation;
    requires webfx.stack.ui.util;

    // Exported packages
    exports org.modality_project.base.client.operations.i18n;
    exports org.modality_project.base.client.services.i18n;

    // Resources packages
    opens org.modality_project.base.client.services.i18n.dictionaries;

    // Provided services
    provides dev.webfx.stack.i18n.operations.ChangeLanguageRequestEmitter with org.modality_project.base.client.operations.i18n.ChangeLanguageToEnglishRequest.ProvidedEmitter, org.modality_project.base.client.operations.i18n.ChangeLanguageToFrenchRequest.ProvidedEmitter;
    provides dev.webfx.stack.i18n.spi.I18nProvider with org.modality_project.base.client.services.i18n.ModalityI18nProvider;

}