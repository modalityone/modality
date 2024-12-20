package one.modality.base.frontoffice.activities.alerts;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import dev.webfx.stack.ui.operation.HasOperationCode;

public class AlertsRouting {

    private final static String PATH = "/alerts";
    private final static String OPERATION_CODE = "RouteToAlerts";

    public static String getPath() {
        return PATH;
    }

    public static class AlertsUiRoute extends UiRouteImpl {

        public AlertsUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.create(AlertsRouting.getPath()
                    , true
                    , AlertsActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static class RouteToAlertsRequest extends RoutePushRequest implements HasOperationCode {

        public RouteToAlertsRequest(BrowsingHistory browsingHistory) {
            super(getPath(), browsingHistory);
        }

        @Override
        public Object getOperationCode() {
            return OPERATION_CODE;
        }
    }

    public static class RouteToAlertsRequestEmitter implements RouteRequestEmitter {

        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToAlertsRequest(context.getHistory());
        }
    }
}
