package org.modality_project.ecommerce.backoffice.operations.routes.bookings;

import org.modality_project.ecommerce.backoffice.activities.bookings.routing.BookingsRouting;
import dev.webfx.stack.framework.shared.operation.HasOperationCode;
import dev.webfx.stack.framework.client.operations.route.RoutePushRequest;
import dev.webfx.stack.platform.windowhistory.spi.BrowsingHistory;

/**
 * @author Bruno Salmon
 */
public final class RouteToBookingsRequest extends RoutePushRequest implements HasOperationCode {

    private final static String OPERATION_CODE = "RouteToBookings";

    public RouteToBookingsRequest(Object eventId, BrowsingHistory history) {
        super(BookingsRouting.getEventBookingsPath(eventId), history);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

}
