package one.modality.base.backoffice.activities.superadmin.home;

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
public final class SuperAdminHomeRouting {

    private final static String PATH = "/superadmin";
    private final static String OPERATION_CODE = "RouteToSuperAdmin";

    public static String getPath() {
        return PATH;
    }

    public static final class SuperAdminHomeUiRoute extends UiRouteImpl {

        public SuperAdminHomeUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.createRegex(PathBuilder.toRegexPath(SuperAdminHomeRouting.getPath())
                    , true
                    , SuperAdminHomeActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static final class RouteToSuperAdminHomeRequest extends RoutePushRequest implements HasOperationCode, HasI18nKey {

        public RouteToSuperAdminHomeRequest(BrowsingHistory history) {
            super(getPath(), history);
        }

        @Override
        public Object getOperationCode() {
            return OPERATION_CODE;
        }

        @Override
        public Object getI18nKey() {
            return SuperAdminHomeI18nKeys.SuperAdminHomeMenu;
        }

    }

    public static final class RouteToSuperAdminHomeRequestEmitter implements RouteRequestEmitter {

        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToSuperAdminHomeRequest(context.getHistory());
        }
    }
}
