package one.modality.base.frontoffice.activities.account;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import one.modality.base.frontoffice.operations.routes.account.RouteToAccountRequest;

/**
 * @author Bruno Salmon
 */
public final class AccountRouting {

    private final static String PATH = "/account";

    public static String getPath() {
        return PATH;
    }

    public static final class AccountUiRoute extends UiRouteImpl {

        public AccountUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.create(AccountRouting.getPath()
                    , true
                    , AccountActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static final class RouteToAccountRequestEmitter implements RouteRequestEmitter {

        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToAccountRequest(context.getHistory());
        }
    }
}
