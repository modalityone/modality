package one.modality.base.frontoffice.activities.account.friendsfamily.edit;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import one.modality.base.frontoffice.operations.routes.account.RouteToEditAccountFriendsAndFamilyRequest;

public final class AccountFriendsAndFamilyEditRouting {
    private final static String PATH = "/account/friends-family/edit";

    public static String getPath() {
        return PATH;
    }

    /**
     * @author Bruno Salmon
     */
    public static final class AccountFriendsAndFamilyEditUiRoute extends UiRouteImpl {

        public AccountFriendsAndFamilyEditUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.create(AccountFriendsAndFamilyEditRouting.getPath()
                    , true
                    , AccountFriendsAndFamilyEditActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static class RouteToAccountFriendsAndFamilyEditEmitter implements RouteRequestEmitter {
        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToEditAccountFriendsAndFamilyRequest(context.getHistory());
        }
    }
}
