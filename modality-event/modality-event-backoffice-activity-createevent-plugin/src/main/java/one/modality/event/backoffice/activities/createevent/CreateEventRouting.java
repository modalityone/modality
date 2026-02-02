package one.modality.event.backoffice.activities.createevent;

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
public final class CreateEventRouting {

    private final static String PATH = "/create-event";
    private final static String OPERATION_CODE = "RouteToCreateEvent";

    public static String getPath() {
        return PATH;
    }

    public static final class CreateEventUiRoute extends UiRouteImpl {

        public CreateEventUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.createRegex(PathBuilder.toRegexPath(CreateEventRouting.getPath())
                    , true
                    , CreateEventActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static final class RouteToCreateEventRequest extends RoutePushRequest implements HasOperationCode, HasI18nKey {

        public RouteToCreateEventRequest(BrowsingHistory history) {
            super(getPath(), history);
        }

        @Override
        public Object getOperationCode() {
            return OPERATION_CODE;
        }

        @Override
        public Object getI18nKey() {
            return CreateEventI18nKeys.CreateEventTile;
        }

    }

    public static final class RouteToCreateEventRequestEmitter implements RouteRequestEmitter {

        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToCreateEventRequest(context.getHistory());
        }
    }
}
