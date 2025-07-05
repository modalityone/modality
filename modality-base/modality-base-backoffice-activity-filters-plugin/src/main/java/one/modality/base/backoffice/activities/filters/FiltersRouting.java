package one.modality.base.backoffice.activities.filters;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.router.util.PathBuilder;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import dev.webfx.extras.operation.HasOperationCode;

public final class FiltersRouting {

    private final static String PATH = "/filters";
    private final static String OPERATION_CODE = "RouteToFilters";

    public static String getPath() {
        return PATH;
    }

    public static final class FiltersUiRoute extends UiRouteImpl {

        public FiltersUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.createRegex(PathBuilder.toRegexPath(FiltersRouting.getPath())
                    , true
                    , FiltersActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static final class RouteToFiltersRequest extends RoutePushRequest implements HasOperationCode {

        public RouteToFiltersRequest(BrowsingHistory history) {
            super(getPath(), history);
        }

        @Override
        public Object getOperationCode() {
            return OPERATION_CODE;
        }

    }

    public static final class RouteToFiltersRequestEmitter implements RouteRequestEmitter {

        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToFiltersRequest(context.getHistory());
        }
    }
}
