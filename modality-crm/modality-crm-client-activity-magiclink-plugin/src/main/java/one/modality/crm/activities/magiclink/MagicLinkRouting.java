package one.modality.crm.activities.magiclink;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import dev.webfx.stack.ui.operation.HasOperationCode;

public final class MagicLinkRouting {

    static final String PATH_TOKEN_PARAMETER_NAME = "token";

    private final static String PATH = "/magic-link/:" + PATH_TOKEN_PARAMETER_NAME;
    private final static String OPERATION_CODE = "RouteToMagicLink";

    public static String getPath() {
        return PATH;
    }

    public static class MagicLinkUiRoute extends UiRouteImpl {

        public MagicLinkUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.create(MagicLinkRouting.getPath()
                    , false
                    , MagicLinkActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static class RouteToMagicLinkRequest extends RoutePushRequest implements HasOperationCode {

        public RouteToMagicLinkRequest(BrowsingHistory browsingHistory) {
            super(getPath(), browsingHistory);
        }

        @Override
        public Object getOperationCode() {
            return OPERATION_CODE;
        }
    }

    public static class RouteToMagicLinkRequestEmitter implements RouteRequestEmitter {

        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToMagicLinkRequest(context.getHistory());
        }
    }
}
