// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.client.i18n {

    // Direct dependencies modules
    requires javafx.base;
    requires webfx.platform.util;
    requires webfx.stack.i18n;
    requires webfx.stack.i18n.json;
    requires webfx.stack.orm.entity;
    requires webfx.stack.ui.operation;

    // Exported packages
    exports one.modality.base.client.operations.i18n;
    exports one.modality.base.client.services.i18n;

    // Resources packages
    opens one.modality.base.client.services.i18n.dictionaries;

    // Provided services
    provides dev.webfx.stack.i18n.operations.ChangeLanguageRequestEmitter with one.modality.base.client.operations.i18n.ChangeLanguageToEnglishRequest.ProvidedEmitter, one.modality.base.client.operations.i18n.ChangeLanguageToFrenchRequest.ProvidedEmitter;
    provides dev.webfx.stack.i18n.spi.I18nProvider with one.modality.base.client.services.i18n.ModalityI18nProvider;

}