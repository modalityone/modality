package one.modality.base.backoffice.activities.home;

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
public final class HomeRouting {

    private final static String PATH = "/home";
    private final static String OPERATION_CODE = "RouteToHome";

    public static String getPath() {
        return PATH;
    }

    public static final class HomeUiRoute extends UiRouteImpl {

        public HomeUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.createRegex(PathBuilder.toRegexPath(HomeRouting.getPath())
                    , true
                    , HomeActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static final class RouteToHomeRequest extends RoutePushRequest implements HasOperationCode {

        public RouteToHomeRequest(BrowsingHistory history) {
            super(getPath(), history);
        }

        @Override
        public Object getOperationCode() {
            return OPERATION_CODE;
        }

    }

    public static final class RouteToHomeRequestEmitter implements RouteRequestEmitter {

        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToHomeRequest(context.getHistory());
        }
    }
}
