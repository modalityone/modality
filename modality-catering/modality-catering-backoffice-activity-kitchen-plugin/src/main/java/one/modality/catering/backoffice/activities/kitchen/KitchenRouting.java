package one.modality.catering.backoffice.activities.kitchen;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.router.util.PathBuilder;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import dev.webfx.stack.ui.operation.HasOperationCode;

/**
 * @author Bruno Salmon
 */
public final class KitchenRouting {

    private final static String PATH = "/kitchen";
    private final static String OPERATION_CODE = "RouteToKitchen";

    public static String getPath() {
        return PATH;
    }

    public static final class KitchenUiRoute extends UiRouteImpl {

        public KitchenUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.createRegex(PathBuilder.toRegexPath(KitchenRouting.getPath())
                    , true
                    , KitchenActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static final class RouteToKitchenRequest extends RoutePushRequest implements HasOperationCode {

        public RouteToKitchenRequest(BrowsingHistory history) {
            super(getPath(), history);
        }

        @Override
        public Object getOperationCode() {
            return OPERATION_CODE;
        }

    }

    public static final class RouteToKitchenRequestEmitter implements RouteRequestEmitter {

        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToKitchenRequest(context.getHistory());
        }
    }
}
