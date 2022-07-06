// File managed by WebFX (DO NOT EDIT MANUALLY)
package java.util;

import java.util.Iterator;
import java.util.logging.Logger;
import dev.webfx.platform.shared.util.function.Factory;

public class ServiceLoader<S> implements Iterable<S> {

    public static <S> ServiceLoader<S> load(Class<S> serviceClass) {
        switch (serviceClass.getName()) {
            case "dev.webfx.framework.client.operations.i18n.ChangeLanguageRequestEmitter": return new ServiceLoader<S>(mongoose.base.client.operations.i18n.ChangeLanguageToEnglishRequest.ProvidedEmitter::new, mongoose.base.client.operations.i18n.ChangeLanguageToFrenchRequest.ProvidedEmitter::new);
            case "dev.webfx.framework.client.operations.route.RouteRequestEmitter": return new ServiceLoader<S>();
            case "dev.webfx.framework.client.services.i18n.spi.I18nProvider": return new ServiceLoader<S>(mongoose.base.client.services.i18n.MongooseI18nProvider::new);
            case "dev.webfx.framework.client.services.push.spi.PushClientServiceProvider": return new ServiceLoader<S>(dev.webfx.framework.client.services.push.spi.impl.simple.SimplePushClientServiceProvider::new);
            case "dev.webfx.framework.client.ui.uirouter.UiRoute": return new ServiceLoader<S>(mongoose.crm.client.activities.login.LoginUiRoute::new, mongoose.crm.client.activities.unauthorized.UnauthorizedUiRoute::new, mongoose.ecommerce.frontoffice.activities.cart.CartUiRoute::new, mongoose.ecommerce.frontoffice.activities.contactus.ContactUsUiRoute::new, mongoose.ecommerce.frontoffice.activities.payment.PaymentUiRoute::new, mongoose.ecommerce.frontoffice.activities.person.PersonUiRoute::new, mongoose.ecommerce.frontoffice.activities.summary.SummaryUiRoute::new, mongoose.event.frontoffice.activities.fees.FeesUiRoute::new, mongoose.event.frontoffice.activities.options.OptionsUiRoute::new, mongoose.event.frontoffice.activities.program.ProgramUiRoute::new, mongoose.event.frontoffice.activities.startbooking.StartBookingUiRoute::new, mongoose.event.frontoffice.activities.terms.TermsUiRoute::new);
            case "dev.webfx.framework.shared.orm.entity.EntityFactoryProvider": return new ServiceLoader<S>(mongoose.base.shared.entities.impl.AttendanceImpl.ProvidedFactory::new, mongoose.base.shared.entities.impl.CartImpl.ProvidedFactory::new, mongoose.base.shared.entities.impl.CountryImpl.ProvidedFactory::new, mongoose.base.shared.entities.impl.CurrencyImpl.ProvidedFactory::new, mongoose.base.shared.entities.impl.DateInfoImpl.ProvidedFactory::new, mongoose.base.shared.entities.impl.DocumentImpl.ProvidedFactory::new, mongoose.base.shared.entities.impl.DocumentLineImpl.ProvidedFactory::new, mongoose.base.shared.entities.impl.EventImpl.ProvidedFactory::new, mongoose.base.shared.entities.impl.FilterImpl.ProvidedFactory::new, mongoose.base.shared.entities.impl.GatewayParameterImpl.ProvidedFactory::new, mongoose.base.shared.entities.impl.HistoryImpl.ProvidedFactory::new, mongoose.base.shared.entities.impl.ImageImpl.ProvidedFactory::new, mongoose.base.shared.entities.impl.ItemFamilyImpl.ProvidedFactory::new, mongoose.base.shared.entities.impl.ItemImpl.ProvidedFactory::new, mongoose.base.shared.entities.impl.LabelImpl.ProvidedFactory::new, mongoose.base.shared.entities.impl.MailImpl.ProvidedFactory::new, mongoose.base.shared.entities.impl.MethodImpl.ProvidedFactory::new, mongoose.base.shared.entities.impl.MoneyAccountImpl.ProvidedFactory::new, mongoose.base.shared.entities.impl.MoneyAccountTypeImpl.ProvidedFactory::new, mongoose.base.shared.entities.impl.MoneyFlowImpl.ProvidedFactory::new, mongoose.base.shared.entities.impl.MoneyTransferImpl.ProvidedFactory::new, mongoose.base.shared.entities.impl.OptionImpl.ProvidedFactory::new, mongoose.base.shared.entities.impl.OrganizationImpl.ProvidedFactory::new, mongoose.base.shared.entities.impl.OrganizationTypeImpl.ProvidedFactory::new, mongoose.base.shared.entities.impl.PersonImpl.ProvidedFactory::new, mongoose.base.shared.entities.impl.RateImpl.ProvidedFactory::new, mongoose.base.shared.entities.impl.SiteImpl.ProvidedFactory::new, mongoose.base.shared.entities.impl.SnapshotImpl.ProvidedFactory::new, mongoose.base.shared.entities.impl.SystemMetricsEntityImpl.ProvidedFactory::new, mongoose.base.shared.entities.impl.TeacherImpl.ProvidedFactory::new);
            case "dev.webfx.framework.shared.services.authn.spi.AuthenticationServiceProvider": return new ServiceLoader<S>(mongoose.crm.client.services.authn.MongooseAuthenticationServiceProvider::new);
            case "dev.webfx.framework.shared.services.authz.spi.AuthorizationServiceProvider": return new ServiceLoader<S>(mongoose.crm.client.services.authz.MongooseAuthorizationServiceProvider::new);
            case "dev.webfx.framework.shared.services.datasourcemodel.spi.DataSourceModelProvider": return new ServiceLoader<S>(mongoose.base.shared.services.datasourcemodel.MongooseDataSourceModelProvider::new);
            case "dev.webfx.framework.shared.services.domainmodel.spi.DomainModelProvider": return new ServiceLoader<S>(mongoose.base.shared.services.domainmodel.MongooseDomainModelProvider::new);
            case "dev.webfx.framework.shared.services.querypush.spi.QueryPushServiceProvider": return new ServiceLoader<S>(dev.webfx.framework.client.jobs.querypush.QueryPushClientServiceProvider::new);
            case "dev.webfx.kit.launcher.spi.WebFxKitLauncherProvider": return new ServiceLoader<S>(dev.webfx.kit.launcher.spi.gwt.GwtWebFxKitLauncherProvider::new);
            case "dev.webfx.kit.mapper.spi.WebFxKitMapperProvider": return new ServiceLoader<S>(dev.webfx.kit.mapper.spi.gwt.GwtWebFxKitHtmlMapperProvider::new);
            case "dev.webfx.platform.client.services.storage.spi.LocalStorageProvider": return new ServiceLoader<S>(dev.webfx.platform.gwt.services.storage.spi.impl.GwtLocalStorageProvider::new);
            case "dev.webfx.platform.client.services.storage.spi.SessionStorageProvider": return new ServiceLoader<S>(dev.webfx.platform.gwt.services.storage.spi.impl.GwtSessionStorageProvider::new);
            case "dev.webfx.platform.client.services.uischeduler.spi.UiSchedulerProvider": return new ServiceLoader<S>(dev.webfx.platform.gwt.services.uischeduler.spi.impl.GwtUiSchedulerProvider::new);
            case "dev.webfx.platform.client.services.websocket.spi.WebSocketServiceProvider": return new ServiceLoader<S>(dev.webfx.platform.gwt.services.websocket.spi.impl.GwtWebSocketServiceProvider::new);
            case "dev.webfx.platform.client.services.windowhistory.spi.WindowHistoryProvider": return new ServiceLoader<S>(dev.webfx.platform.web.services.windowhistory.spi.impl.WebWindowHistoryProvider::new);
            case "dev.webfx.platform.client.services.windowlocation.spi.WindowLocationProvider": return new ServiceLoader<S>(dev.webfx.platform.gwt.services.windowlocation.spi.impl.GwtWindowLocationProvider::new);
            case "dev.webfx.platform.gwt.services.resource.spi.impl.GwtResourceBundle": return new ServiceLoader<S>(mongoose.all.frontoffice.application.gwt.embed.EmbedResourcesBundle.ProvidedGwtResourceBundle::new);
            case "dev.webfx.platform.shared.services.boot.spi.ApplicationBooterProvider": return new ServiceLoader<S>(dev.webfx.platform.gwt.services.boot.spi.impl.GwtApplicationBooterProvider::new);
            case "dev.webfx.platform.shared.services.boot.spi.ApplicationJob": return new ServiceLoader<S>(dev.webfx.framework.client.jobs.querypush.QueryPushClientJob::new, mongoose.base.client.jobs.sessionrecorder.ClientSessionRecorderJob::new);
            case "dev.webfx.platform.shared.services.boot.spi.ApplicationModuleBooter": return new ServiceLoader<S>(dev.webfx.framework.shared.interceptors.dqlquery.DqlQueryInterceptorModuleBooter::new, dev.webfx.framework.shared.interceptors.dqlquerypush.DqlQueryPushInterceptorModuleBooter::new, dev.webfx.framework.shared.interceptors.dqlsubmit.DqlSubmitInterceptorModuleBooter::new, dev.webfx.kit.launcher.WebFxKitLauncherModuleBooter::new, dev.webfx.platform.gwt.services.resource.spi.impl.GwtResourceModuleBooter::new, dev.webfx.platform.shared.services.boot.spi.impl.ApplicationJobsBooter::new, dev.webfx.platform.shared.services.buscall.BusCallModuleBooter::new, dev.webfx.platform.shared.services.serial.SerialCodecModuleBooter::new, mongoose.base.client.operationactionsloading.MongooseClientOperationActionsLoader::new);
            case "dev.webfx.platform.shared.services.bus.spi.BusServiceProvider": return new ServiceLoader<S>(dev.webfx.platform.client.services.websocketbus.web.WebWebsocketBusServiceProvider::new);
            case "dev.webfx.platform.shared.services.buscall.spi.BusCallEndpoint": return new ServiceLoader<S>(dev.webfx.framework.shared.services.querypush.ExecuteQueryPushBusCallEndpoint::new, dev.webfx.platform.shared.services.query.ExecuteQueryBatchBusCallEndpoint::new, dev.webfx.platform.shared.services.query.ExecuteQueryBusCallEndpoint::new, dev.webfx.platform.shared.services.submit.ExecuteSubmitBatchBusCallEndpoint::new, dev.webfx.platform.shared.services.submit.ExecuteSubmitBusCallEndpoint::new);
            case "dev.webfx.platform.shared.services.datasource.spi.LocalDataSourceProvider": return new ServiceLoader<S>();
            case "dev.webfx.platform.shared.services.json.spi.JsonProvider": return new ServiceLoader<S>(dev.webfx.platform.gwt.services.json.spi.impl.GwtJsonObject::create);
            case "dev.webfx.platform.shared.services.log.spi.LoggerProvider": return new ServiceLoader<S>(dev.webfx.platform.gwt.services.log.spi.impl.GwtLoggerProvider::new);
            case "dev.webfx.platform.shared.services.query.spi.QueryServiceProvider": return new ServiceLoader<S>(dev.webfx.platform.shared.services.query.spi.impl.remote.RemoteQueryServiceProvider::new);
            case "dev.webfx.platform.shared.services.resource.spi.ResourceServiceProvider": return new ServiceLoader<S>(dev.webfx.platform.gwt.services.resource.spi.impl.GwtResourceServiceProvider::new);
            case "dev.webfx.platform.shared.services.scheduler.spi.SchedulerProvider": return new ServiceLoader<S>(dev.webfx.platform.gwt.services.uischeduler.spi.impl.GwtUiSchedulerProvider::new);
            case "dev.webfx.platform.shared.services.serial.spi.SerialCodec": return new ServiceLoader<S>(dev.webfx.framework.shared.services.querypush.QueryPushArgument.ProvidedSerialCodec::new, dev.webfx.framework.shared.services.querypush.QueryPushResult.ProvidedSerialCodec::new, dev.webfx.framework.shared.services.querypush.diff.impl.QueryResultTranslation.ProvidedSerialCodec::new, dev.webfx.platform.shared.datascope.aggregate.AggregateScope.ProvidedSerialCodec::new, dev.webfx.platform.shared.services.buscall.BusCallArgument.ProvidedSerialCodec::new, dev.webfx.platform.shared.services.buscall.BusCallResult.ProvidedSerialCodec::new, dev.webfx.platform.shared.services.buscall.SerializableAsyncResult.ProvidedSerialCodec::new, dev.webfx.platform.shared.services.query.QueryArgument.ProvidedSerialCodec::new, dev.webfx.platform.shared.services.query.QueryResult.ProvidedSerialCodec::new, dev.webfx.platform.shared.services.serial.spi.impl.ProvidedBatchSerialCodec::new, dev.webfx.platform.shared.services.submit.GeneratedKeyBatchIndex.ProvidedSerialCodec::new, dev.webfx.platform.shared.services.submit.SubmitArgument.ProvidedSerialCodec::new, dev.webfx.platform.shared.services.submit.SubmitResult.ProvidedSerialCodec::new);
            case "dev.webfx.platform.shared.services.shutdown.spi.ShutdownProvider": return new ServiceLoader<S>(dev.webfx.platform.gwt.services.shutdown.spi.impl.GwtShutdownProvider::new);
            case "dev.webfx.platform.shared.services.submit.spi.SubmitServiceProvider": return new ServiceLoader<S>(dev.webfx.platform.shared.services.submit.spi.impl.remote.RemoteSubmitServiceProvider::new);
            case "dev.webfx.platform.web.services.windowhistory.spi.impl.JsWindowHistory": return new ServiceLoader<S>(dev.webfx.platform.gwt.services.windowhistory.spi.impl.GwtJsWindowHistory::new);
            case "javafx.application.Application": return new ServiceLoader<S>(mongoose.all.frontoffice.application.MongooseFrontOfficeApplication::new);

            // UNKNOWN SPI
            default:
                Logger.getLogger(ServiceLoader.class.getName()).warning("Unknown " + serviceClass + " SPI - returning no provider");
                return new ServiceLoader<S>();
        }
    }

    private final Factory[] factories;

    public ServiceLoader(Factory... factories) {
        this.factories = factories;
    }

    public Iterator<S> iterator() {
        return new Iterator<S>() {
            int index = 0;
            @Override
            public boolean hasNext() {
                return index < factories.length;
            }

            @Override
            public S next() {
                return (S) factories[index++].create();
            }
        };
    }
}