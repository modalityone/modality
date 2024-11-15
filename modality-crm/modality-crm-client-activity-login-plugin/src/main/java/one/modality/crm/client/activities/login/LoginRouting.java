package one.modality.crm.client.activities.login;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.ProvidedLoginUiRoute;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import dev.webfx.stack.ui.action.tuner.logout.LogoutOnlyActionTuner;
import dev.webfx.stack.ui.operation.HasOperationCode;

/**
 * @author Bruno Salmon
 */
public final class LoginRouting {

    private static final String PATH = "/login";
    private final static String OPERATION_CODE = "RouteToLogin";

    public static String getPath() {
        return PATH;
    }

    public static final class LoginUiRoute extends UiRouteImpl implements ProvidedLoginUiRoute {

        public LoginUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.create(LoginRouting.getPath()
                    , true
                    , LoginActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static class RouteToLoginRequest extends RoutePushRequest implements HasOperationCode, LogoutOnlyActionTuner {

        public RouteToLoginRequest(BrowsingHistory browsingHistory) {
            super(getPath(), browsingHistory);
        }

        @Override
        public Object getOperationCode() {
            return OPERATION_CODE;
        }
    }

    public static class RouteToLoginRequestEmitter implements RouteRequestEmitter {

        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToLoginRequest(context.getHistory());
        }
    }

}
