package one.modality.crm.backoffice.activities.administrators;

import dev.webfx.extras.i18n.HasI18nKey;
import dev.webfx.extras.operation.HasOperationCode;
import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;

/**
 * @author Bruno Salmon
 */
public final class AdministratorsRouting {

    private final static String PATH = "/administrators";
    private final static String OPERATION_CODE = "RouteToAdministrators";
    public static String getPath() {
        return PATH;
    }

    public static final class AdministratorsUiRoute extends UiRouteImpl {

        public AdministratorsUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.create(AdministratorsRouting.getPath()
                    , true
                    , AdministratorsActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static class RouteToAdministratorsRequest extends RoutePushRequest implements HasOperationCode, HasI18nKey {

        public RouteToAdministratorsRequest(BrowsingHistory browsingHistory) {
            super(getPath(), browsingHistory);
        }

        @Override
        public Object getOperationCode() {
            return OPERATION_CODE;
        }

        @Override
        public Object getI18nKey() {
            return AdministratorsI18nKeys.AdministratorsTile;
        }
    }

    public static class RouteToAdministratorsRequestEmitter implements RouteRequestEmitter {

        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToAdministratorsRequest(context.getHistory());
        }
    }
}
