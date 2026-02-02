package one.modality.event.backoffice.activities.eventsetup.home;

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
public final class EventSetupHomeRouting {

    private final static String PATH = "/event-setup";
    private final static String OPERATION_CODE = "RouteToEventSetup";

    public static String getPath() {
        return PATH;
    }

    public static final class EventSetupHomeUiRoute extends UiRouteImpl {

        public EventSetupHomeUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.createRegex(PathBuilder.toRegexPath(EventSetupHomeRouting.getPath())
                    , true
                    , EventSetupHomeActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static final class RouteToEventSetupHomeRequest extends RoutePushRequest implements HasOperationCode, HasI18nKey {

        public RouteToEventSetupHomeRequest(BrowsingHistory history) {
            super(getPath(), history);
        }

        @Override
        public Object getOperationCode() {
            return OPERATION_CODE;
        }

        @Override
        public Object getI18nKey() {
            return EventSetupHomeI18nKeys.EventSetupTile;
        }

    }

    public static final class RouteToEventSetupHomeRequestEmitter implements RouteRequestEmitter {

        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToEventSetupHomeRequest(context.getHistory());
        }
    }
}
