package one.modality.ecommerce.backoffice.activities.moneyflows;

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
import one.modality.base.client.util.routing.ModalityRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class MoneyFlowsRouting {

    private final static String ANY_PATH = "/money-flows(/organization/:organizationId)?";
    private final static String ORGANIZATION_PATH = "/money-flows/organization/:organizationId";
    private final static String OPERATION_CODE = "RouteToMoneyFlows";

    public static String getAnyPath() {
        return ANY_PATH;
    }

    public static String getOrganizationIncomePath(Object organizationId) {
        return organizationId == null ? "/money-flows" : ModalityRoutingUtil.interpolateOrganizationIdInPath(organizationId, ORGANIZATION_PATH);
    }

    public static final class MoneyFlowsUiRoute extends UiRouteImpl {

        public MoneyFlowsUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.createRegex(PathBuilder.toRegexPath(getAnyPath())
                    , true
                    , MoneyFlowsActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static final class RouteToMoneyFlowsRequest extends RoutePushRequest implements HasOperationCode {

        public RouteToMoneyFlowsRequest(Object organizationId, BrowsingHistory history) {
            super(getOrganizationIncomePath(organizationId), history);
        }

        @Override
        public Object getOperationCode() {
            return OPERATION_CODE;
        }

    }

    public static final class RouteToMoneyFlowsRequestEmitter implements RouteRequestEmitter {

        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToMoneyFlowsRequest(context.getParameter("organizationId"), context.getHistory());
        }
    }
}
