package one.modality.hotel.backoffice.operations.routes.roomcalendar;

import one.modality.hotel.backoffice.activities.roomcalendar.routing.RoomCalendarRouting;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.platform.windowhistory.spi.BrowsingHistory;

/**
 * @author Bruno Salmon
 */
public final class RouteToRoomCalendarRequest extends RoutePushRequest implements HasOperationCode {

    private final static String OPERATION_CODE = "RouteToRoomCalendar";

    public RouteToRoomCalendarRequest(BrowsingHistory history) {
        super(RoomCalendarRouting.getAnyPath(), history);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

}
