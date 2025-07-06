// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * The Modality implementation of i18n with English &amp; French dictionaries.
 */
module modality.base.client.i18n {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires modality.base.client.entities;
    requires modality.base.shared.entities;
    requires webfx.extras.i18n;
    requires webfx.extras.i18n.ast;
    requires webfx.extras.i18n.controls;
    requires webfx.extras.operation;
    requires webfx.extras.styles.bootstrap;
    requires webfx.kit.util;
    requires webfx.platform.ast;
    requires webfx.platform.conf;
    requires webfx.platform.console;
    requires webfx.stack.orm.entity;

    // Exported packages
    exports one.modality.base.client.i18n;
    exports one.modality.base.client.operations.i18n;
    exports one.modality.base.client.services.i18n;

    // Provided services
    provides dev.webfx.extras.i18n.operations.ChangeLanguageRequestEmitter with one.modality.base.client.operations.i18n.ChangeLanguageToEnglishRequest.ProvidedEmitter, one.modality.base.client.operations.i18n.ChangeLanguageToFrenchRequest.ProvidedEmitter;
    provides dev.webfx.extras.i18n.spi.I18nProvider with one.modality.base.client.services.i18n.ModalityI18nProvider;

}