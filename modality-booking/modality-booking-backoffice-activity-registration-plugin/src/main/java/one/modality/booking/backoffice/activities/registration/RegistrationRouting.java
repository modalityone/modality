package one.modality.booking.backoffice.activities.registration;

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
public final class RegistrationRouting {

    private final static String PATH = "/registration";
    private final static String OPERATION_CODE = "RouteToRegistration";
    public static String getPath() {
        return PATH;
    }

    public static final class RegistrationUiRoute extends UiRouteImpl {

        public RegistrationUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.create(RegistrationRouting.getPath()
                    , true
                    , RegistrationActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static class RouteToRegistrationRequest extends RoutePushRequest implements HasOperationCode, HasI18nKey {

        public RouteToRegistrationRequest(BrowsingHistory browsingHistory) {
            super(getPath(), browsingHistory);
        }

        @Override
        public Object getOperationCode() {
            return OPERATION_CODE;
        }

        @Override
        public Object getI18nKey() {
            return RegistrationI18nKeys.RegistrationMenu;
        }
    }

    public static class RouteToRegistrationRequestEmitter implements RouteRequestEmitter {

        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToRegistrationRequest(context.getHistory());
        }
    }
}
