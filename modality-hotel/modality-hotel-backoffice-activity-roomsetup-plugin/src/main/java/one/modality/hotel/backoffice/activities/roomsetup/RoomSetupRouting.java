package one.modality.hotel.backoffice.activities.roomsetup;

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
public final class RoomSetupRouting {

    private final static String PATH = "/room-setup";
    private final static String OPERATION_CODE = "RouteToRoomSetup";

    public static String getPath() {
        return PATH;
    }

    public static final class RoomSetupUiRoute extends UiRouteImpl {

        public RoomSetupUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.create(RoomSetupRouting.getPath()
                    , true
                    , RoomSetupActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

    public static class RouteToRoomSetupRequest extends RoutePushRequest implements HasOperationCode, HasI18nKey {

        public RouteToRoomSetupRequest(BrowsingHistory browsingHistory) {
            super(getPath(), browsingHistory);
        }

        @Override
        public Object getOperationCode() {
            return OPERATION_CODE;
        }

        @Override
        public Object getI18nKey() {
            return RoomSetupI18nKeys.RoomSetupTile;
        }
    }

    public static class RouteToRoomSetupRequestEmitter implements RouteRequestEmitter {

        @Override
        public RouteRequest instantiateRouteRequest(UiRouteActivityContext context) {
            return new RouteToRoomSetupRequest(context.getHistory());
        }
    }
}
