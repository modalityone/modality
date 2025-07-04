package one.modality.base.frontoffice.activities.account.settings;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import one.modality.base.frontoffice.operations.routes.account.RouteToAccountSettingsRequest;

/**
 * @author Bruno Salmon
 */
public final class AccountSettingsRouting {

    private final static String PATH = "/account/settings";

    public static String getPath() {
        return PATH;
    }

    public static final class AccountSettingsUiRoute extends UiRouteImpl {

        public AccountSettingsUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.create(AccountSettingsRouting.getPath()
                    , true
                    , AccountSettingsActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static class RouteToAccountSettingsRequestEmitter implements RouteRequestEmitter {
        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToAccountSettingsRequest(context.getHistory());
        }
    }
}
