package one.modality.event.backoffice.activities.roomsetup;

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
public final class EventRoomSetupRouting {

    private final static String PATH = "/event-room-setup";
    private final static String OPERATION_CODE = "RouteToEventRoomSetup";

    public static String getPath() {
        return PATH;
    }

    public static final class EventRoomSetupUiRoute extends UiRouteImpl {

        public EventRoomSetupUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.createRegex(PathBuilder.toRegexPath(EventRoomSetupRouting.getPath())
                    , true
                    , EventRoomSetupActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static final class RouteToEventRoomSetupRequest extends RoutePushRequest implements HasOperationCode, HasI18nKey {

        public RouteToEventRoomSetupRequest(BrowsingHistory history) {
            super(getPath(), history);
        }

        @Override
        public Object getOperationCode() {
            return OPERATION_CODE;
        }

        @Override
        public Object getI18nKey() {
            return EventRoomSetupI18nKeys.EventRoomSetupTile;
        }

    }

    public static final class RouteToEventRoomSetupRequestEmitter implements RouteRequestEmitter {

        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToEventRoomSetupRequest(context.getHistory());
        }
    }
}
