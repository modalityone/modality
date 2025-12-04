package one.modality.base.backoffice.activities.organizations;

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
public final class OrganizationsRouting {

    private final static String PATH = "/organizations";
    private final static String OPERATION_CODE = "RouteToOrganizations";

    public static String getPath() {
        return PATH;
    }

    public static final class OrganizationsUiRoute extends UiRouteImpl {

        public OrganizationsUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.create(OrganizationsRouting.getPath()
                    , true
                    , OrganizationsActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static class RouteToOrganizationsRequest extends RoutePushRequest implements HasOperationCode, HasI18nKey {

        public RouteToOrganizationsRequest(BrowsingHistory browsingHistory) {
            super(getPath(), browsingHistory);
        }

        @Override
        public Object getOperationCode() {
            return OPERATION_CODE;
        }

        @Override
        public Object getI18nKey() {
            return OrganizationsI18nKeys.OrganizationsTile;
        }
    }

    public static class RouteToOrganizationsRequestEmitter implements RouteRequestEmitter {

        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToOrganizationsRequest(context.getHistory());
        }
    }
}
