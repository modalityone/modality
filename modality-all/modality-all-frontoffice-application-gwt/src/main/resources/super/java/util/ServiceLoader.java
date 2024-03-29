// File managed by WebFX (DO NOT EDIT MANUALLY)
package java.util;

import java.util.Iterator;
import java.util.logging.Logger;
import dev.webfx.platform.util.function.Factory;

public class ServiceLoader<S> implements Iterable<S> {

    public static <S> ServiceLoader<S> load(Class<S> serviceClass) {
        switch (serviceClass.getName()) {
            case "dev.webfx.kit.launcher.spi.WebFxKitLauncherProvider": return new ServiceLoader<S>(dev.webfx.kit.launcher.spi.impl.gwtj2cl.GwtJ2clWebFxKitLauncherProvider::new);
            case "dev.webfx.kit.mapper.spi.WebFxKitMapperProvider": return new ServiceLoader<S>(dev.webfx.kit.mapper.spi.impl.gwtj2cl.GwtJ2clWebFxKitHtmlMapperProvider::new);
            case "dev.webfx.platform.ast.spi.factory.AstFactoryProvider": return new ServiceLoader<S>(dev.webfx.platform.ast.spi.factory.impl.gwt.GwtAstFactoryProvider::new);
            case "dev.webfx.platform.ast.spi.formatter.AstFormatterProvider": return new ServiceLoader<S>(dev.webfx.platform.ast.json.formatter.JsonFormatterProvider::new);
            case "dev.webfx.platform.ast.spi.parser.AstParserProvider": return new ServiceLoader<S>(dev.webfx.platform.ast.json.parser.JsonParserProvider::new);
            case "dev.webfx.platform.boot.spi.ApplicationBooterProvider": return new ServiceLoader<S>(dev.webfx.platform.boot.spi.impl.gwt.GwtApplicationBooterProvider::new);
            case "dev.webfx.platform.boot.spi.ApplicationJob": return new ServiceLoader<S>(one.modality.base.client.entities.util.functions.ClientFunctionsRegisteringApplicationJob::new);
            case "dev.webfx.platform.boot.spi.ApplicationModuleBooter": return new ServiceLoader<S>(dev.webfx.kit.launcher.WebFxKitLauncherModuleBooter::new, dev.webfx.platform.boot.spi.impl.ApplicationJobsBooter::new, dev.webfx.platform.resource.spi.impl.gwt.GwtResourceModuleBooter::new, dev.webfx.stack.com.bus.call.BusCallModuleBooter::new, dev.webfx.stack.com.bus.spi.impl.json.client.JsonClientBusModuleBooter::new, dev.webfx.stack.com.serial.SerialCodecModuleBooter::new, dev.webfx.stack.db.querypush.client.simple.SimpleQueryPushClientJob::new, dev.webfx.stack.orm.dql.query.interceptor.DqlQueryInterceptorModuleBooter::new, dev.webfx.stack.orm.dql.querypush.interceptor.DqlQueryPushInterceptorModuleBooter::new, dev.webfx.stack.orm.dql.submit.interceptor.DqlSubmitInterceptorModuleBooter::new, dev.webfx.stack.ui.fxraiser.json.JsonFXRaiserModuleBooter::new, one.modality.base.client.operationactionsloading.ModalityClientOperationActionsLoader::new, one.modality.crm.client.services.authz.ModalityAuthorizationClientModuleBooter::new);
            case "dev.webfx.platform.browser.spi.BrowserProvider": return new ServiceLoader<S>(dev.webfx.platform.browser.spi.impl.gwtj2cl.GwtJ2clBrowserProvider::new);
            case "dev.webfx.platform.conf.spi.ConfigLoaderProvider": return new ServiceLoader<S>();
            case "dev.webfx.platform.console.spi.ConsoleProvider": return new ServiceLoader<S>(dev.webfx.platform.console.spi.impl.gwtj2cl.GwtJ2clConsoleProvider::new);
            case "dev.webfx.platform.os.spi.OperatingSystemProvider": return new ServiceLoader<S>(dev.webfx.platform.os.spi.impl.gwtj2cl.GwtJ2clOperatingSystemProvider::new);
            case "dev.webfx.platform.resource.spi.ResourceProvider": return new ServiceLoader<S>(dev.webfx.platform.resource.spi.impl.gwt.GwtResourceProvider::new);
            case "dev.webfx.platform.resource.spi.impl.gwt.GwtResourceBundle": return new ServiceLoader<S>(modality.all.frontoffice.application.gwt.embed.EmbedResourcesBundle.ProvidedGwtResourceBundle::new);
            case "dev.webfx.platform.scheduler.spi.SchedulerProvider": return new ServiceLoader<S>(dev.webfx.platform.uischeduler.spi.impl.gwtj2cl.GwtJ2clUiSchedulerProvider::new);
            case "dev.webfx.platform.shutdown.spi.ShutdownProvider": return new ServiceLoader<S>(dev.webfx.platform.shutdown.spi.impl.gwtj2cl.GwtJ2clShutdownProvider::new);
            case "dev.webfx.platform.storage.spi.LocalStorageProvider": return new ServiceLoader<S>(dev.webfx.platform.storage.spi.impl.gwtj2cl.GwtJ2clLocalStorageProvider::new);
            case "dev.webfx.platform.storage.spi.SessionStorageProvider": return new ServiceLoader<S>(dev.webfx.platform.storage.spi.impl.gwtj2cl.GwtJ2clSessionStorageProvider::new);
            case "dev.webfx.platform.substitution.spi.SubstitutorProvider": return new ServiceLoader<S>(dev.webfx.platform.substitution.spi.impl.var.VariablesSubstitutorProvider::new);
            case "dev.webfx.platform.substitution.var.spi.VariablesResolver": return new ServiceLoader<S>(dev.webfx.platform.substitution.var.spi.impl.localstorage.LocalStorageVariablesResolver::new, dev.webfx.platform.substitution.var.spi.impl.windowlocation.WindowLocationVariablesResolver::new);
            case "dev.webfx.platform.uischeduler.spi.UiSchedulerProvider": return new ServiceLoader<S>(dev.webfx.platform.uischeduler.spi.impl.gwtj2cl.GwtJ2clUiSchedulerProvider::new);
            case "dev.webfx.platform.useragent.spi.UserAgentProvider": return new ServiceLoader<S>(dev.webfx.platform.useragent.spi.impl.gwtj2cl.GwtJ2clUserAgentProvider::new);
            case "dev.webfx.platform.windowhistory.spi.WindowHistoryProvider": return new ServiceLoader<S>(dev.webfx.platform.windowhistory.spi.impl.web.WebWindowHistoryProvider::new);
            case "dev.webfx.platform.windowhistory.spi.impl.web.JsWindowHistory": return new ServiceLoader<S>(dev.webfx.platform.windowhistory.spi.impl.gwtj2cl.GwtJ2clJsWindowHistory::new);
            case "dev.webfx.platform.windowlocation.spi.WindowLocationProvider": return new ServiceLoader<S>(dev.webfx.platform.windowlocation.spi.impl.gwtj2cl.GwtJ2clWindowLocationProvider::new);
            case "dev.webfx.stack.authn.login.spi.LoginServiceProvider": return new ServiceLoader<S>(dev.webfx.stack.authn.login.spi.impl.remote.RemoteLoginServiceProvider::new);
            case "dev.webfx.stack.authn.login.ui.spi.UiLoginServiceProvider": return new ServiceLoader<S>(dev.webfx.stack.authn.login.ui.spi.impl.portal.UiLoginPortalProvider::new);
            case "dev.webfx.stack.authn.login.ui.spi.impl.gateway.UiLoginGatewayProvider": return new ServiceLoader<S>(dev.webfx.stack.authn.login.ui.spi.impl.gateway.facebook.FacebookUiLoginGatewayProvider::new, dev.webfx.stack.authn.login.ui.spi.impl.gateway.google.GoogleUiLoginGatewayProvider::new, dev.webfx.stack.authn.login.ui.spi.impl.gateway.mojoauth.MojoAuthUiLoginGatewayProvider::new, dev.webfx.stack.authn.login.ui.spi.impl.gateway.password.PasswordUiLoginGatewayProvider::new);
            case "dev.webfx.stack.authn.login.ui.spi.impl.gateway.webview.spi.LoginWebViewProvider": return new ServiceLoader<S>(dev.webfx.stack.authn.login.ui.spi.impl.gateway.webview.spi.impl.gwt.GwtLoginWebViewProvider::new);
            case "dev.webfx.stack.authn.spi.AuthenticationServiceProvider": return new ServiceLoader<S>(dev.webfx.stack.authn.spi.impl.remote.RemoteAuthenticationServiceProvider::new);
            case "dev.webfx.stack.authz.client.spi.AuthorizationClientServiceProvider": return new ServiceLoader<S>(one.modality.crm.client.services.authz.ModalityAuthorizationClientServiceProvider::new);
            case "dev.webfx.stack.com.bus.call.spi.BusCallEndpoint": return new ServiceLoader<S>(dev.webfx.stack.authn.buscall.AuthenticateMethodEndpoint::new, dev.webfx.stack.authn.buscall.GetUserClaimsMethodEndpoint::new, dev.webfx.stack.authn.buscall.LogoutMethodEndpoint::new, dev.webfx.stack.authn.login.buscall.GetLoginUiInputMethodEndpoint::new, dev.webfx.stack.db.query.buscall.ExecuteQueryBatchMethodEndpoint::new, dev.webfx.stack.db.query.buscall.ExecuteQueryMethodEndpoint::new, dev.webfx.stack.db.querypush.buscall.ExecuteQueryPushMethodEndpoint::new, dev.webfx.stack.db.submit.buscall.ExecuteSubmitBatchMethodEndpoint::new, dev.webfx.stack.db.submit.buscall.ExecuteSubmitMethodEndpoint::new);
            case "dev.webfx.stack.com.bus.spi.BusServiceProvider": return new ServiceLoader<S>(dev.webfx.stack.com.bus.spi.impl.json.client.websocket.web.WebWebsocketBusServiceProvider::new);
            case "dev.webfx.stack.com.serial.spi.SerialCodec": return new ServiceLoader<S>(dev.webfx.stack.authn.login.buscall.serial.LoginUiContextSerialCodec::new, dev.webfx.stack.authn.serial.UserClaimsSerialCodec::new, dev.webfx.stack.authn.serial.UsernamePasswordCredentialsSerialCodec::new, dev.webfx.stack.com.bus.call.BusCallArgument.ProvidedSerialCodec::new, dev.webfx.stack.com.bus.call.BusCallResult.ProvidedSerialCodec::new, dev.webfx.stack.com.bus.call.SerializableAsyncResult.ProvidedSerialCodec::new, dev.webfx.stack.com.serial.spi.impl.ProvidedBatchSerialCodec::new, dev.webfx.stack.db.datascope.aggregate.AggregateScope.ProvidedSerialCodec::new, dev.webfx.stack.db.query.buscall.serial.PairSerialCodec::new, dev.webfx.stack.db.query.buscall.serial.QueryArgumentSerialCodec::new, dev.webfx.stack.db.query.buscall.serial.QueryResultSerialCodec::new, dev.webfx.stack.db.querypush.buscall.serial.QueryPushArgumentSerialCodec::new, dev.webfx.stack.db.querypush.buscall.serial.QueryPushResultSerialCodec::new, dev.webfx.stack.db.querypush.buscall.serial.QueryResultTranslationSerialCodec::new, dev.webfx.stack.db.submit.buscall.serial.GeneratedKeyBatchIndexSerialCodec::new, dev.webfx.stack.db.submit.buscall.serial.SubmitArgumentSerialCodec::new, dev.webfx.stack.db.submit.buscall.serial.SubmitResultSerialCodec::new, one.modality.crm.shared.services.authn.serial.ModalityUserPrincipalSerialCodec::new);
            case "dev.webfx.stack.com.websocket.spi.WebSocketServiceProvider": return new ServiceLoader<S>(dev.webfx.stack.com.websocket.spi.impl.gwtj2cl.GwtJ2clWebSocketServiceProvider::new);
            case "dev.webfx.stack.db.query.spi.QueryServiceProvider": return new ServiceLoader<S>(dev.webfx.stack.db.query.spi.impl.remote.RemoteQueryServiceProvider::new);
            case "dev.webfx.stack.db.querypush.spi.QueryPushServiceProvider": return new ServiceLoader<S>(dev.webfx.stack.db.querypush.client.simple.SimpleQueryPushClientServiceProvider::new);
            case "dev.webfx.stack.db.submit.spi.SubmitServiceProvider": return new ServiceLoader<S>(dev.webfx.stack.db.submit.spi.impl.remote.RemoteSubmitServiceProvider::new);
            case "dev.webfx.stack.i18n.operations.ChangeLanguageRequestEmitter": return new ServiceLoader<S>(one.modality.base.client.operations.i18n.ChangeLanguageToEnglishRequest.ProvidedEmitter::new, one.modality.base.client.operations.i18n.ChangeLanguageToFrenchRequest.ProvidedEmitter::new);
            case "dev.webfx.stack.i18n.spi.I18nProvider": return new ServiceLoader<S>(one.modality.base.client.services.i18n.ModalityI18nProvider::new);
            case "dev.webfx.stack.orm.datasourcemodel.service.spi.DataSourceModelProvider": return new ServiceLoader<S>(one.modality.base.shared.services.datasourcemodel.ModalityDataSourceModelProvider::new);
            case "dev.webfx.stack.orm.domainmodel.service.spi.DomainModelProvider": return new ServiceLoader<S>(one.modality.base.shared.services.domainmodel.ModalityDomainModelProvider::new);
            case "dev.webfx.stack.orm.entity.EntityFactoryProvider": return new ServiceLoader<S>(one.modality.base.shared.entities.impl.AttendanceImpl.ProvidedFactory::new, one.modality.base.shared.entities.impl.CartImpl.ProvidedFactory::new, one.modality.base.shared.entities.impl.ChannelImpl.ProvidedFactory::new, one.modality.base.shared.entities.impl.CountryImpl.ProvidedFactory::new, one.modality.base.shared.entities.impl.CurrencyImpl.ProvidedFactory::new, one.modality.base.shared.entities.impl.DocumentImpl.ProvidedFactory::new, one.modality.base.shared.entities.impl.DocumentLineImpl.ProvidedFactory::new, one.modality.base.shared.entities.impl.EventImpl.ProvidedFactory::new, one.modality.base.shared.entities.impl.EventTypeImpl.ProvidedFactory::new, one.modality.base.shared.entities.impl.FilterImpl.ProvidedFactory::new, one.modality.base.shared.entities.impl.GatewayParameterImpl.ProvidedFactory::new, one.modality.base.shared.entities.impl.HistoryImpl.ProvidedFactory::new, one.modality.base.shared.entities.impl.ImageImpl.ProvidedFactory::new, one.modality.base.shared.entities.impl.ItemFamilyImpl.ProvidedFactory::new, one.modality.base.shared.entities.impl.ItemImpl.ProvidedFactory::new, one.modality.base.shared.entities.impl.KdmCenterImpl.ProvidedFactory::new, one.modality.base.shared.entities.impl.LabelImpl.ProvidedFactory::new, one.modality.base.shared.entities.impl.MailImpl.ProvidedFactory::new, one.modality.base.shared.entities.impl.MethodImpl.ProvidedFactory::new, one.modality.base.shared.entities.impl.MoneyAccountImpl.ProvidedFactory::new, one.modality.base.shared.entities.impl.MoneyAccountTypeImpl.ProvidedFactory::new, one.modality.base.shared.entities.impl.MoneyFlowImpl.ProvidedFactory::new, one.modality.base.shared.entities.impl.MoneyTransferImpl.ProvidedFactory::new, one.modality.base.shared.entities.impl.NewsImpl.ProvidedFactory::new, one.modality.base.shared.entities.impl.OrganizationImpl.ProvidedFactory::new, one.modality.base.shared.entities.impl.OrganizationTypeImpl.ProvidedFactory::new, one.modality.base.shared.entities.impl.PersonImpl.ProvidedFactory::new, one.modality.base.shared.entities.impl.PodcastImpl.ProvidedFactory::new, one.modality.base.shared.entities.impl.RateImpl.ProvidedFactory::new, one.modality.base.shared.entities.impl.ResourceConfigurationImpl.ProvidedFactory::new, one.modality.base.shared.entities.impl.ResourceImpl.ProvidedFactory::new, one.modality.base.shared.entities.impl.ScheduledItemImpl.ProvidedFactory::new, one.modality.base.shared.entities.impl.ScheduledResourceImpl.ProvidedFactory::new, one.modality.base.shared.entities.impl.SiteImpl.ProvidedFactory::new, one.modality.base.shared.entities.impl.SiteItemFamilyImpl.ProvidedFactory::new, one.modality.base.shared.entities.impl.SnapshotImpl.ProvidedFactory::new, one.modality.base.shared.entities.impl.TeacherImpl.ProvidedFactory::new, one.modality.base.shared.entities.impl.TopicImpl.ProvidedFactory::new);
            case "dev.webfx.stack.orm.push.client.spi.PushClientServiceProvider": return new ServiceLoader<S>(dev.webfx.stack.orm.push.client.spi.impl.simple.SimplePushClientServiceProvider::new);
            case "dev.webfx.stack.routing.router.spi.RouterFactoryProvider": return new ServiceLoader<S>(dev.webfx.stack.routing.router.spi.impl.client.ClientRouterFactoryProvider::new);
            case "dev.webfx.stack.routing.uirouter.UiRoute": return new ServiceLoader<S>(one.modality.base.frontoffice.activities.account.AccountUiRoute::new, one.modality.base.frontoffice.activities.account.friendsfamily.AccountFriendsAndFamilyUiRoute::new, one.modality.base.frontoffice.activities.account.friendsfamily.edit.AccountFriendsAndFamilyEditUiRoute::new, one.modality.base.frontoffice.activities.account.personalinfo.AccountPersonalInformationUiRoute::new, one.modality.base.frontoffice.activities.account.settings.AccountSettingsUiRoute::new, one.modality.base.frontoffice.activities.alerts.AlertsUiRoute::new, one.modality.crm.client.activities.login.LoginUiRoute::new, one.modality.crm.client.activities.unauthorized.UnauthorizedUiRoute::new, one.modality.event.frontoffice.activities.booking.BookingUiRoute::new, one.modality.event.frontoffice.activities.booking.process.event.BookEventUiRoute::new);
            case "dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter": return new ServiceLoader<S>(one.modality.base.frontoffice.activities.account.RouteToAccountRequestEmitter::new, one.modality.base.frontoffice.activities.account.friendsfamily.RouteToAccountFriendsAndFamilyEmitter::new, one.modality.base.frontoffice.activities.account.friendsfamily.edit.RouteToAccountFriendsAndFamilyEditEmitter::new, one.modality.base.frontoffice.activities.account.personalinfo.RouteToAccountPersonalInformationEmitter::new, one.modality.base.frontoffice.activities.account.settings.RouteToAccountSettingsRequestEmitter::new, one.modality.base.frontoffice.activities.alerts.RouteToAlertsRequestEmitter::new, one.modality.event.frontoffice.activities.booking.RouteToBookingRequestEmitter::new);
            case "dev.webfx.stack.session.spi.SessionServiceProvider": return new ServiceLoader<S>(dev.webfx.stack.session.spi.impl.client.ClientSessionServiceProvider::new);
            case "javafx.application.Application": return new ServiceLoader<S>(one.modality.base.frontoffice.application.ModalityFrontOfficeApplication::new);
            case "one.modality.base.backoffice.mainframe.headernode.MainFrameHeaderNodeProvider": return new ServiceLoader<S>();

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