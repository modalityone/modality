package one.modality.crm.client.activities.login;

import dev.webfx.platform.util.function.Factory;
import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.authn.login.ui.spi.impl.gateway.password.PasswordUiLoginGateway;
import dev.webfx.stack.i18n.HasI18nKey;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.activity.Activity;
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

    public static Factory<Activity<ViewDomainActivityContextFinal>> LOGIN_ACTIVITY_FACTORY = LoginActivity::new;

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
                    , LOGIN_ACTIVITY_FACTORY
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static class RouteToLoginRequest extends RoutePushRequest implements HasOperationCode, HasI18nKey, LogoutOnlyActionTuner {

        public RouteToLoginRequest(BrowsingHistory browsingHistory) {
            super(getPath(), browsingHistory);
        }

        @Override
        public Object getOperationCode() {
            return OPERATION_CODE;
        }

        @Override
        public Object getI18nKey() {
            return LoginI18nKeys.LoginMenu;
        }
    }

    public static class RouteToLoginRequestEmitter implements RouteRequestEmitter {

        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToLoginRequest(context.getHistory());
        }
    }

    static {
        PasswordUiLoginGateway.setCreateAccountEmailConsumer((s) -> {s.toString();});
    }

}
