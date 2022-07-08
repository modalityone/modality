package org.modality_project.ecommerce.backoffice.operations.routes.bookings;

import org.modality_project.ecommerce.backoffice.activities.bookings.routing.BookingsRouting;
import dev.webfx.framework.shared.operation.HasOperationCode;
import dev.webfx.framework.client.operations.route.RoutePushRequest;
import dev.webfx.platform.client.services.windowhistory.spi.BrowsingHistory;

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
