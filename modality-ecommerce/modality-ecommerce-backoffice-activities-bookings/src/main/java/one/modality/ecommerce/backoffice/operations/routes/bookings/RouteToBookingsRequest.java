package one.modality.ecommerce.backoffice.operations.routes.bookings;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;

import one.modality.ecommerce.backoffice.activities.bookings.routing.BookingsRouting;

/**
 * @author Bruno Salmon
 */
public final class RouteToBookingsRequest extends RoutePushRequest implements HasOperationCode {

    private static final String OPERATION_CODE = "RouteToBookings";

    public RouteToBookingsRequest(Object eventId, BrowsingHistory history) {
        super(BookingsRouting.getEventBookingsPath(eventId), history);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }
}
