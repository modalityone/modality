// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.shared.entities {

    // Direct dependencies modules
    requires java.base;
    requires modality.base.shared.domainmodel;
    requires webfx.platform.util;
    requires webfx.stack.orm.dql;
    requires webfx.stack.orm.entity;

    // Exported packages
    exports one.modality.base.shared.entities;
    exports one.modality.base.shared.entities.converters;
    exports one.modality.base.shared.entities.formatters;
    exports one.modality.base.shared.entities.impl;
    exports one.modality.base.shared.entities.markers;

    // Provided services
    provides dev.webfx.stack.orm.entity.EntityFactoryProvider with one.modality.base.shared.entities.impl.AttendanceImpl.ProvidedFactory, one.modality.base.shared.entities.impl.CartImpl.ProvidedFactory, one.modality.base.shared.entities.impl.ChannelImpl.ProvidedFactory, one.modality.base.shared.entities.impl.CountryImpl.ProvidedFactory, one.modality.base.shared.entities.impl.CurrencyImpl.ProvidedFactory, one.modality.base.shared.entities.impl.DocumentImpl.ProvidedFactory, one.modality.base.shared.entities.impl.DocumentLineImpl.ProvidedFactory, one.modality.base.shared.entities.impl.EventImpl.ProvidedFactory, one.modality.base.shared.entities.impl.FilterImpl.ProvidedFactory, one.modality.base.shared.entities.impl.GatewayParameterImpl.ProvidedFactory, one.modality.base.shared.entities.impl.HistoryImpl.ProvidedFactory, one.modality.base.shared.entities.impl.ImageImpl.ProvidedFactory, one.modality.base.shared.entities.impl.ItemFamilyImpl.ProvidedFactory, one.modality.base.shared.entities.impl.ItemImpl.ProvidedFactory, one.modality.base.shared.entities.impl.KdmCenterImpl.ProvidedFactory, one.modality.base.shared.entities.impl.LabelImpl.ProvidedFactory, one.modality.base.shared.entities.impl.MailImpl.ProvidedFactory, one.modality.base.shared.entities.impl.MethodImpl.ProvidedFactory, one.modality.base.shared.entities.impl.MoneyTransferImpl.ProvidedFactory, one.modality.base.shared.entities.impl.MoneyAccountImpl.ProvidedFactory, one.modality.base.shared.entities.impl.MoneyAccountTypeImpl.ProvidedFactory, one.modality.base.shared.entities.impl.MoneyFlowImpl.ProvidedFactory, one.modality.base.shared.entities.impl.NewsImpl.ProvidedFactory, one.modality.base.shared.entities.impl.OrganizationImpl.ProvidedFactory, one.modality.base.shared.entities.impl.OrganizationTypeImpl.ProvidedFactory, one.modality.base.shared.entities.impl.PersonImpl.ProvidedFactory, one.modality.base.shared.entities.impl.PodcastImpl.ProvidedFactory, one.modality.base.shared.entities.impl.ResourceConfigurationImpl.ProvidedFactory, one.modality.base.shared.entities.impl.ResourceImpl.ProvidedFactory, one.modality.base.shared.entities.impl.RateImpl.ProvidedFactory, one.modality.base.shared.entities.impl.ScheduledItemImpl.ProvidedFactory, one.modality.base.shared.entities.impl.ScheduledResourceImpl.ProvidedFactory, one.modality.base.shared.entities.impl.SiteImpl.ProvidedFactory, one.modality.base.shared.entities.impl.SnapshotImpl.ProvidedFactory, one.modality.base.shared.entities.impl.TeacherImpl.ProvidedFactory;

}