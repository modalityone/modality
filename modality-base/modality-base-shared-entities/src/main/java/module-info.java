// File managed by WebFX (DO NOT EDIT MANUALLY)

/**
 * Module that defines a set of Java wrappers for domain entities (easier to interact with Java code).
 */
module modality.base.shared.entities {

    // Direct dependencies modules
    requires modality.base.shared.domainmodel;
    requires modality.base.shared.knownitems;
    requires webfx.extras.media.metadata;
    requires webfx.platform.substitution;
    requires webfx.platform.util;
    requires webfx.platform.util.time;
    requires webfx.stack.db.submit;
    requires webfx.stack.orm.domainmodel;
    requires webfx.stack.orm.dql;
    requires transitive webfx.stack.orm.entity;

    // Exported packages
    exports one.modality.base.shared.entities;
    exports one.modality.base.shared.entities.converters;
    exports one.modality.base.shared.entities.formatters;
    exports one.modality.base.shared.entities.impl;
    exports one.modality.base.shared.entities.markers;
    exports one.modality.base.shared.entities.triggers;

    // Provided services
    provides dev.webfx.stack.orm.entity.EntityFactoryProvider with one.modality.base.shared.entities.impl.AttendanceImpl.ProvidedFactory, one.modality.base.shared.entities.impl.BookablePeriodImpl.ProvidedFactory, one.modality.base.shared.entities.impl.BookImpl.ProvidedFactory, one.modality.base.shared.entities.impl.CartImpl.ProvidedFactory, one.modality.base.shared.entities.impl.ChannelImpl.ProvidedFactory, one.modality.base.shared.entities.impl.CountryImpl.ProvidedFactory, one.modality.base.shared.entities.impl.CurrencyImpl.ProvidedFactory, one.modality.base.shared.entities.impl.DayTemplateImpl.ProvidedFactory, one.modality.base.shared.entities.impl.DocumentImpl.ProvidedFactory, one.modality.base.shared.entities.impl.DocumentLineImpl.ProvidedFactory, one.modality.base.shared.entities.impl.ErrorImpl.ProvidedFactory, one.modality.base.shared.entities.impl.EventImpl.ProvidedFactory, one.modality.base.shared.entities.impl.EventTypeImpl.ProvidedFactory, one.modality.base.shared.entities.impl.FilterImpl.ProvidedFactory, one.modality.base.shared.entities.impl.FrontendAccountImpl.ProvidedFactory, one.modality.base.shared.entities.impl.GatewayCompanyImpl.ProvidedFactory, one.modality.base.shared.entities.impl.GatewayParameterImpl.ProvidedFactory, one.modality.base.shared.entities.impl.HistoryImpl.ProvidedFactory, one.modality.base.shared.entities.impl.ImageImpl.ProvidedFactory, one.modality.base.shared.entities.impl.ItemFamilyImpl.ProvidedFactory, one.modality.base.shared.entities.impl.ItemImpl.ProvidedFactory, one.modality.base.shared.entities.impl.KdmCenterImpl.ProvidedFactory, one.modality.base.shared.entities.impl.LanguageImpl.ProvidedFactory, one.modality.base.shared.entities.impl.LabelImpl.ProvidedFactory, one.modality.base.shared.entities.impl.LetterImpl.ProvidedFactory, one.modality.base.shared.entities.impl.MailImpl.ProvidedFactory, one.modality.base.shared.entities.impl.MagicLinkImpl.ProvidedFactory, one.modality.base.shared.entities.impl.MediaImpl.ProvidedFactory, one.modality.base.shared.entities.impl.MediaConsumptionImpl.ProvidedFactory, one.modality.base.shared.entities.impl.MethodImpl.ProvidedFactory, one.modality.base.shared.entities.impl.MoneyTransferImpl.ProvidedFactory, one.modality.base.shared.entities.impl.MoneyAccountImpl.ProvidedFactory, one.modality.base.shared.entities.impl.MoneyAccountTypeImpl.ProvidedFactory, one.modality.base.shared.entities.impl.MoneyFlowImpl.ProvidedFactory, one.modality.base.shared.entities.impl.NewsImpl.ProvidedFactory, one.modality.base.shared.entities.impl.OrganizationImpl.ProvidedFactory, one.modality.base.shared.entities.impl.OrganizationTypeImpl.ProvidedFactory, one.modality.base.shared.entities.impl.PersonImpl.ProvidedFactory, one.modality.base.shared.entities.impl.PodcastImpl.ProvidedFactory, one.modality.base.shared.entities.impl.ResourceConfigurationImpl.ProvidedFactory, one.modality.base.shared.entities.impl.ResourceImpl.ProvidedFactory, one.modality.base.shared.entities.impl.RateImpl.ProvidedFactory, one.modality.base.shared.entities.impl.ScheduledItemImpl.ProvidedFactory, one.modality.base.shared.entities.impl.ScheduledResourceImpl.ProvidedFactory, one.modality.base.shared.entities.impl.SiteImpl.ProvidedFactory, one.modality.base.shared.entities.impl.SiteItemFamilyImpl.ProvidedFactory, one.modality.base.shared.entities.impl.SnapshotImpl.ProvidedFactory, one.modality.base.shared.entities.impl.TeacherImpl.ProvidedFactory, one.modality.base.shared.entities.impl.TimeLineImpl.ProvidedFactory, one.modality.base.shared.entities.impl.TopicImpl.ProvidedFactory, one.modality.base.shared.entities.impl.VideoImpl.ProvidedFactory;

}