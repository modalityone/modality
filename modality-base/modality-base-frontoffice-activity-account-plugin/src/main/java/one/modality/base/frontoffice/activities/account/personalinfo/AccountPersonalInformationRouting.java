package one.modality.base.frontoffice.activities.account.personalinfo;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import one.modality.base.frontoffice.operations.routes.account.RouteToAccountPersonalInformationRequest;

/**
 * @author Bruno Salmon
 */
public final class AccountPersonalInformationRouting {

    private final static String PATH = "/account/personal-information";

    public static String getPath() {
        return PATH;
    }

    public static final class AccountPersonalInformationUiRoute extends UiRouteImpl {

        public AccountPersonalInformationUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.create(AccountPersonalInformationRouting.getPath()
                    , true
                    , AccountPersonalInformationActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static class RouteToAccountPersonalInformationEmitter implements RouteRequestEmitter {
        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToAccountPersonalInformationRequest(context.getHistory());
        }
    }
}
