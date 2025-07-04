package one.modality.crm.frontoffice.activities.orders;

import dev.webfx.extras.i18n.HasI18nKey;
import dev.webfx.extras.operation.HasOperationCode;
import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.router.util.PathBuilder;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;

/**
 * @author Bruno Salmon
 */
public class OrdersRouting {

    private final static String PATH = "/orders";
    private final static String OPERATION_CODE = "RouteToOrders";

    public static String getPath() {
        return PATH;
    }

    public static class UserProfileUiRoute extends UiRouteImpl<ViewDomainActivityContextFinal> {

        public UserProfileUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<ViewDomainActivityContextFinal> uiRoute() {
            return UiRoute.createRegex(PathBuilder.toRegexPath(OrdersRouting.getPath())
                    , true
                    , OrdersActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static class RouteToUserProfileRequest extends RoutePushRequest implements HasOperationCode, HasI18nKey {

        public RouteToUserProfileRequest(BrowsingHistory browsingHistory) {
            super(getPath(), browsingHistory);
        }

        @Override
        public Object getOperationCode() {
            return OPERATION_CODE;
        }

        @Override
        public Object getI18nKey() {
            return OrdersI18nKeys.OrdersMenu;
        }
    }

    public static class RouteToUserProfileRequestEmitter implements RouteRequestEmitter {

        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToUserProfileRequest(context.getHistory());
        }
    }
}
