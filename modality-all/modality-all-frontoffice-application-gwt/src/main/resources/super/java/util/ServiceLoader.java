// File managed by WebFX (DO NOT EDIT MANUALLY)
package java.util;

import java.util.Iterator;
import java.util.logging.Logger;
import dev.webfx.platform.util.function.Factory;

public class ServiceLoader<S> implements Iterable<S> {

    public static <S> ServiceLoader<S> load(Class<S> serviceClass) {
        switch (serviceClass.getName()) {
            case "dev.webfx.kit.launcher.spi.WebFxKitLauncherProvider": return new ServiceLoader<S>(dev.webfx.kit.launcher.spi.impl.gwt.GwtWebFxKitLauncherProvider::new);
            case "dev.webfx.kit.mapper.spi.WebFxKitMapperProvider": return new ServiceLoader<S>(dev.webfx.kit.mapper.spi.impl.gwt.GwtWebFxKitHtmlMapperProvider::new);
            case "dev.webfx.platform.boot.spi.ApplicationBooterProvider": return new ServiceLoader<S>(dev.webfx.platform.boot.spi.impl.gwt.GwtApplicationBooterProvider::new);
            case "dev.webfx.platform.boot.spi.ApplicationJob": return new ServiceLoader<S>(dev.webfx.stack.querypush.client.simple.SimpleQueryPushClientJob::new, org.modality_project.base.client.jobs.sessionrecorder.ClientSessionRecorderJob::new);
            case "dev.webfx.platform.boot.spi.ApplicationModuleBooter": return new ServiceLoader<S>(dev.webfx.kit.launcher.WebFxKitLauncherModuleBooter::new, dev.webfx.platform.boot.spi.impl.ApplicationJobsBooter::new, dev.webfx.platform.resource.spi.impl.gwt.GwtResourceModuleBooter::new, dev.webfx.stack.com.buscall.BusCallModuleBooter::new, dev.webfx.stack.com.serial.SerialCodecModuleBooter::new, dev.webfx.stack.orm.dql.query.interceptor.DqlQueryInterceptorModuleBooter::new, dev.webfx.stack.orm.dql.submit.interceptor.DqlSubmitInterceptorModuleBooter::new, org.modality_project.base.client.operationactionsloading.ModalityClientOperationActionsLoader::new);
            case "dev.webfx.platform.console.spi.ConsoleProvider": return new ServiceLoader<S>(dev.webfx.platform.console.spi.impl.gwt.GwtConsoleProvider::new);
            case "dev.webfx.platform.resource.spi.ResourceProvider": return new ServiceLoader<S>(dev.webfx.platform.resource.spi.impl.gwt.GwtResourceProvider::new);
            case "dev.webfx.platform.resource.spi.impl.gwt.GwtResourceBundle": return new ServiceLoader<S>(modality.all.frontoffice.application.gwt.embed.EmbedResourcesBundle.ProvidedGwtResourceBundle::new);
            case "dev.webfx.platform.scheduler.spi.SchedulerProvider": return new ServiceLoader<S>(dev.webfx.platform.uischeduler.spi.impl.gwt.GwtUiSchedulerProvider::new);
            case "dev.webfx.platform.shutdown.spi.ShutdownProvider": return new ServiceLoader<S>(dev.webfx.platform.shutdown.spi.impl.gwt.GwtShutdownProvider::new);
            case "dev.webfx.platform.storage.spi.LocalStorageProvider": return new ServiceLoader<S>(dev.webfx.platform.storage.spi.impl.gwt.GwtLocalStorageProvider::new);
            case "dev.webfx.platform.storage.spi.SessionStorageProvider": return new ServiceLoader<S>(dev.webfx.platform.storage.spi.impl.gwt.GwtSessionStorageProvider::new);
            case "dev.webfx.platform.uischeduler.spi.UiSchedulerProvider": return new ServiceLoader<S>(dev.webfx.platform.uischeduler.spi.impl.gwt.GwtUiSchedulerProvider::new);
            case "dev.webfx.stack.authn.spi.AuthenticationServiceProvider": return new ServiceLoader<S>(org.modality_project.crm.client.services.authn.ModalityAuthenticationServiceProvider::new);
            case "dev.webfx.stack.authz.spi.AuthorizationServiceProvider": return new ServiceLoader<S>(org.modality_project.crm.client.services.authz.ModalityAuthorizationServiceProvider::new);
            case "dev.webfx.stack.com.bus.spi.BusServiceProvider": return new ServiceLoader<S>(dev.webfx.stack.com.websocketbus.web.WebWebsocketBusServiceProvider::new);
            case "dev.webfx.stack.com.buscall.spi.BusCallEndpoint": return new ServiceLoader<S>(dev.webfx.stack.db.query.ExecuteQueryBatchBusCallEndpoint::new, dev.webfx.stack.db.query.ExecuteQueryBusCallEndpoint::new, dev.webfx.stack.db.submit.ExecuteSubmitBatchBusCallEndpoint::new, dev.webfx.stack.db.submit.ExecuteSubmitBusCallEndpoint::new, dev.webfx.stack.querypush.ExecuteQueryPushBusCallEndpoint::new);
            case "dev.webfx.stack.com.serial.spi.SerialCodec": return new ServiceLoader<S>(dev.webfx.stack.com.buscall.BusCallArgument.ProvidedSerialCodec::new, dev.webfx.stack.com.buscall.BusCallResult.ProvidedSerialCodec::new, dev.webfx.stack.com.buscall.SerializableAsyncResult.ProvidedSerialCodec::new, dev.webfx.stack.com.serial.spi.impl.ProvidedBatchSerialCodec::new, dev.webfx.stack.db.datascope.aggregate.AggregateScope.ProvidedSerialCodec::new, dev.webfx.stack.db.query.QueryArgument.ProvidedSerialCodec::new, dev.webfx.stack.db.query.QueryResult.ProvidedSerialCodec::new, dev.webfx.stack.db.submit.GeneratedKeyBatchIndex.ProvidedSerialCodec::new, dev.webfx.stack.db.submit.SubmitArgument.ProvidedSerialCodec::new, dev.webfx.stack.db.submit.SubmitResult.ProvidedSerialCodec::new, dev.webfx.stack.querypush.QueryPushArgument.ProvidedSerialCodec::new, dev.webfx.stack.querypush.QueryPushResult.ProvidedSerialCodec::new, dev.webfx.stack.querypush.diff.impl.QueryResultTranslation.ProvidedSerialCodec::new);
            case "dev.webfx.stack.com.websocket.spi.WebSocketServiceProvider": return new ServiceLoader<S>(dev.webfx.stack.com.websocket.spi.impl.gwt.GwtWebSocketServiceProvider::new);
            case "dev.webfx.stack.db.datasource.spi.LocalDataSourceProvider": return new ServiceLoader<S>();
            case "dev.webfx.stack.db.query.spi.QueryServiceProvider": return new ServiceLoader<S>(dev.webfx.stack.db.query.spi.impl.remote.RemoteQueryServiceProvider::new);
            case "dev.webfx.stack.db.submit.spi.SubmitServiceProvider": return new ServiceLoader<S>(dev.webfx.stack.db.submit.spi.impl.remote.RemoteSubmitServiceProvider::new);
            case "dev.webfx.stack.i18n.operations.ChangeLanguageRequestEmitter": return new ServiceLoader<S>(org.modality_project.base.client.operations.i18n.ChangeLanguageToEnglishRequest.ProvidedEmitter::new, org.modality_project.base.client.operations.i18n.ChangeLanguageToFrenchRequest.ProvidedEmitter::new);
            case "dev.webfx.stack.i18n.spi.I18nProvider": return new ServiceLoader<S>(org.modality_project.base.client.services.i18n.ModalityI18nProvider::new);
            case "dev.webfx.stack.orm.datasourcemodel.service.spi.DataSourceModelProvider": return new ServiceLoader<S>(org.modality_project.base.shared.services.datasourcemodel.ModalityDataSourceModelProvider::new);
            case "dev.webfx.stack.orm.domainmodel.service.spi.DomainModelProvider": return new ServiceLoader<S>(org.modality_project.base.shared.services.domainmodel.ModalityDomainModelProvider::new);
            case "dev.webfx.stack.orm.entity.EntityFactoryProvider": return new ServiceLoader<S>(org.modality_project.base.shared.entities.impl.AttendanceImpl.ProvidedFactory::new, org.modality_project.base.shared.entities.impl.CartImpl.ProvidedFactory::new, org.modality_project.base.shared.entities.impl.CountryImpl.ProvidedFactory::new, org.modality_project.base.shared.entities.impl.CurrencyImpl.ProvidedFactory::new, org.modality_project.base.shared.entities.impl.DateInfoImpl.ProvidedFactory::new, org.modality_project.base.shared.entities.impl.DocumentImpl.ProvidedFactory::new, org.modality_project.base.shared.entities.impl.DocumentLineImpl.ProvidedFactory::new, org.modality_project.base.shared.entities.impl.EventImpl.ProvidedFactory::new, org.modality_project.base.shared.entities.impl.FilterImpl.ProvidedFactory::new, org.modality_project.base.shared.entities.impl.GatewayParameterImpl.ProvidedFactory::new, org.modality_project.base.shared.entities.impl.HistoryImpl.ProvidedFactory::new, org.modality_project.base.shared.entities.impl.ImageImpl.ProvidedFactory::new, org.modality_project.base.shared.entities.impl.ItemFamilyImpl.ProvidedFactory::new, org.modality_project.base.shared.entities.impl.ItemImpl.ProvidedFactory::new, org.modality_project.base.shared.entities.impl.LabelImpl.ProvidedFactory::new, org.modality_project.base.shared.entities.impl.MailImpl.ProvidedFactory::new, org.modality_project.base.shared.entities.impl.MethodImpl.ProvidedFactory::new, org.modality_project.base.shared.entities.impl.MoneyAccountImpl.ProvidedFactory::new, org.modality_project.base.shared.entities.impl.MoneyAccountTypeImpl.ProvidedFactory::new, org.modality_project.base.shared.entities.impl.MoneyFlowImpl.ProvidedFactory::new, org.modality_project.base.shared.entities.impl.MoneyTransferImpl.ProvidedFactory::new, org.modality_project.base.shared.entities.impl.OptionImpl.ProvidedFactory::new, org.modality_project.base.shared.entities.impl.OrganizationImpl.ProvidedFactory::new, org.modality_project.base.shared.entities.impl.OrganizationTypeImpl.ProvidedFactory::new, org.modality_project.base.shared.entities.impl.PersonImpl.ProvidedFactory::new, org.modality_project.base.shared.entities.impl.RateImpl.ProvidedFactory::new, org.modality_project.base.shared.entities.impl.SiteImpl.ProvidedFactory::new, org.modality_project.base.shared.entities.impl.SnapshotImpl.ProvidedFactory::new, org.modality_project.base.shared.entities.impl.SystemMetricsEntityImpl.ProvidedFactory::new, org.modality_project.base.shared.entities.impl.TeacherImpl.ProvidedFactory::new);
            case "dev.webfx.stack.orm.push.client.spi.PushClientServiceProvider": return new ServiceLoader<S>(dev.webfx.stack.orm.push.client.spi.impl.simple.SimplePushClientServiceProvider::new);
            case "dev.webfx.stack.platform.json.spi.JsonProvider": return new ServiceLoader<S>(dev.webfx.stack.platform.json.spi.impl.gwt.GwtJsonObject::create);
            case "dev.webfx.stack.platform.windowhistory.spi.WindowHistoryProvider": return new ServiceLoader<S>(dev.webfx.stack.platform.windowhistory.spi.impl.web.WebWindowHistoryProvider::new);
            case "dev.webfx.stack.platform.windowhistory.spi.impl.web.JsWindowHistory": return new ServiceLoader<S>(dev.webfx.stack.platform.windowhistory.spi.impl.gwt.GwtJsWindowHistory::new);
            case "dev.webfx.stack.platform.windowlocation.spi.WindowLocationProvider": return new ServiceLoader<S>(dev.webfx.stack.platform.windowlocation.spi.impl.gwt.GwtWindowLocationProvider::new);
            case "dev.webfx.stack.querypush.spi.QueryPushServiceProvider": return new ServiceLoader<S>(dev.webfx.stack.querypush.client.simple.SimpleQueryPushClientServiceProvider::new);
            case "dev.webfx.stack.routing.uirouter.UiRoute": return new ServiceLoader<S>(org.modality_project.crm.client.activities.login.LoginUiRoute::new, org.modality_project.crm.client.activities.unauthorized.UnauthorizedUiRoute::new, org.modality_project.ecommerce.frontoffice.activities.cart.CartUiRoute::new, org.modality_project.ecommerce.frontoffice.activities.contactus.ContactUsUiRoute::new, org.modality_project.ecommerce.frontoffice.activities.payment.PaymentUiRoute::new, org.modality_project.ecommerce.frontoffice.activities.person.PersonUiRoute::new, org.modality_project.ecommerce.frontoffice.activities.summary.SummaryUiRoute::new, org.modality_project.event.frontoffice.activities.fees.FeesUiRoute::new, org.modality_project.event.frontoffice.activities.options.OptionsUiRoute::new, org.modality_project.event.frontoffice.activities.program.ProgramUiRoute::new, org.modality_project.event.frontoffice.activities.startbooking.StartBookingUiRoute::new, org.modality_project.event.frontoffice.activities.terms.TermsUiRoute::new);
            case "dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter": return new ServiceLoader<S>();
            case "javafx.application.Application": return new ServiceLoader<S>(org.modality_project.all.frontoffice.application.ModalityFrontOfficeApplication::new);

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