package one.modality.hotel.backoffice.activities.household;

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
public final class HouseholdRouting {

    private final static String ANY_PATH = "/household";
    private final static String OPERATION_CODE = "RouteToHousehold";

    public static String getAnyPath() {
        return ANY_PATH;
    }

    public static final class HouseholdUiRoute extends UiRouteImpl {

        public HouseholdUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.createRegex(PathBuilder.toRegexPath(getAnyPath())
                    , true
                    , HouseholdActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static final class RouteToHouseholdRequest extends RoutePushRequest implements HasOperationCode {

        public RouteToHouseholdRequest(BrowsingHistory history) {
            super(getAnyPath(), history);
        }

        @Override
        public Object getOperationCode() {
            return OPERATION_CODE;
        }

    }

    public static final class RouteToHouseholdRequestEmitter implements RouteRequestEmitter {

        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToHouseholdRequest(context.getHistory());
        }
    }
}
