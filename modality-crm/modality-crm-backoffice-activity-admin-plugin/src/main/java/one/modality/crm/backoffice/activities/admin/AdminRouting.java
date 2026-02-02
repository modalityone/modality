package one.modality.crm.backoffice.activities.admin;

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
public final class AdminRouting {

    private final static String PATH = "/admin";
    private final static String OPERATION_CODE = "RouteToAdmin";

    public static String getPath() {
        return PATH;
    }

    public static final class AdminUiRoute extends UiRouteImpl {

        public AdminUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.create(AdminRouting.getPath()
                    , true
                    , AdminActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static class RouteToAdminRequest extends RoutePushRequest implements HasOperationCode, HasI18nKey {

        public RouteToAdminRequest(BrowsingHistory browsingHistory) {
            super(getPath(), browsingHistory);
        }

        @Override
        public Object getOperationCode() {
            return OPERATION_CODE;
        }

        @Override
        public Object getI18nKey() {
            return Admin18nKeys.AdminMenu;
        }
    }

    public static class RouteToAdminRequestEmitter implements RouteRequestEmitter {

        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToAdminRequest(context.getHistory());
        }
    }
}
