// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.base.shared.entities {

    // Direct dependencies modules
    requires java.base;
    requires modality.base.shared.domainmodel;
    requires modality.hotel.shared.time;
    requires webfx.framework.shared.orm.dql;
    requires webfx.framework.shared.orm.entity;
    requires webfx.platform.shared.util;

    // Exported packages
    exports org.modality_project.base.shared.entities;
    exports org.modality_project.base.shared.entities.converters;
    exports org.modality_project.base.shared.entities.formatters;
    exports org.modality_project.base.shared.entities.impl;
    exports org.modality_project.base.shared.entities.markers;
    exports org.modality_project.base.shared.services.systemmetrics;

    // Provided services
    provides dev.webfx.framework.shared.orm.entity.EntityFactoryProvider with org.modality_project.base.shared.entities.impl.AttendanceImpl.ProvidedFactory, org.modality_project.base.shared.entities.impl.CartImpl.ProvidedFactory, org.modality_project.base.shared.entities.impl.CountryImpl.ProvidedFactory, org.modality_project.base.shared.entities.impl.CurrencyImpl.ProvidedFactory, org.modality_project.base.shared.entities.impl.DateInfoImpl.ProvidedFactory, org.modality_project.base.shared.entities.impl.DocumentImpl.ProvidedFactory, org.modality_project.base.shared.entities.impl.DocumentLineImpl.ProvidedFactory, org.modality_project.base.shared.entities.impl.EventImpl.ProvidedFactory, org.modality_project.base.shared.entities.impl.FilterImpl.ProvidedFactory, org.modality_project.base.shared.entities.impl.GatewayParameterImpl.ProvidedFactory, org.modality_project.base.shared.entities.impl.HistoryImpl.ProvidedFactory, org.modality_project.base.shared.entities.impl.ImageImpl.ProvidedFactory, org.modality_project.base.shared.entities.impl.ItemFamilyImpl.ProvidedFactory, org.modality_project.base.shared.entities.impl.ItemImpl.ProvidedFactory, org.modality_project.base.shared.entities.impl.LabelImpl.ProvidedFactory, org.modality_project.base.shared.entities.impl.MailImpl.ProvidedFactory, org.modality_project.base.shared.entities.impl.MethodImpl.ProvidedFactory, org.modality_project.base.shared.entities.impl.MoneyTransferImpl.ProvidedFactory, org.modality_project.base.shared.entities.impl.MoneyAccountImpl.ProvidedFactory, org.modality_project.base.shared.entities.impl.MoneyAccountTypeImpl.ProvidedFactory, org.modality_project.base.shared.entities.impl.MoneyFlowImpl.ProvidedFactory, org.modality_project.base.shared.entities.impl.OptionImpl.ProvidedFactory, org.modality_project.base.shared.entities.impl.OrganizationImpl.ProvidedFactory, org.modality_project.base.shared.entities.impl.OrganizationTypeImpl.ProvidedFactory, org.modality_project.base.shared.entities.impl.PersonImpl.ProvidedFactory, org.modality_project.base.shared.entities.impl.RateImpl.ProvidedFactory, org.modality_project.base.shared.entities.impl.SiteImpl.ProvidedFactory, org.modality_project.base.shared.entities.impl.SnapshotImpl.ProvidedFactory, org.modality_project.base.shared.entities.impl.SystemMetricsEntityImpl.ProvidedFactory, org.modality_project.base.shared.entities.impl.TeacherImpl.ProvidedFactory;

}