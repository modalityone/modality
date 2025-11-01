package one.modality.crm.backoffice.activities.customers;

import dev.webfx.extras.i18n.HasI18nKey;
import dev.webfx.extras.operation.HasOperationCode;
import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;

/**
 * @author Bruno Salmon
 */
public final class CustomersRouting {

    private final static String PATH = "/customers";
    private final static String OPERATION_CODE = "RouteToCustomers";

    public static String getPath() {
        return PATH;
    }

    public static final class CustomersUiRoute extends UiRouteImpl {

        public CustomersUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.create(CustomersRouting.getPath()
                    , true
                    , CustomersActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static class RouteToCustomersRequest extends RoutePushRequest implements HasOperationCode, HasI18nKey {

        public RouteToCustomersRequest(BrowsingHistory browsingHistory) {
            super(getPath(), browsingHistory);
        }

        @Override
        public Object getOperationCode() {
            return OPERATION_CODE;
        }

        @Override
        public Object getI18nKey() {
            //return Customers18nKeys.CustomersMenu;
            return "CustomersMenu";
        }
    }

    public static class RouteToCustomersRequestEmitter implements RouteRequestEmitter {

        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToCustomersRequest(context.getHistory());
        }
    }
}
