// File managed by WebFX (DO NOT EDIT MANUALLY)

module mongoose.base.client.i18n {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.graphics;
    requires webfx.framework.client.i18n;
    requires webfx.framework.client.i18n.json;
    requires webfx.framework.client.util;
    requires webfx.framework.shared.operation;
    requires webfx.framework.shared.orm.entity;
    requires webfx.platform.shared.util;

    // Exported packages
    exports org.modality_project.base.client.operations.i18n;
    exports org.modality_project.base.client.services.i18n;

    // Resources packages
    opens org.modality_project.base.client.services.i18n.dictionaries;

    // Provided services
    provides dev.webfx.framework.client.operations.i18n.ChangeLanguageRequestEmitter with org.modality_project.base.client.operations.i18n.ChangeLanguageToEnglishRequest.ProvidedEmitter, org.modality_project.base.client.operations.i18n.ChangeLanguageToFrenchRequest.ProvidedEmitter;
    provides dev.webfx.framework.client.services.i18n.spi.I18nProvider with org.modality_project.base.client.services.i18n.MongooseI18nProvider;

}