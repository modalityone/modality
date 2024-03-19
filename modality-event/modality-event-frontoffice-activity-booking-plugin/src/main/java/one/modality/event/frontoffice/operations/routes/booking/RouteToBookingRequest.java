package one.modality.event.frontoffice.operations.routes.booking;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;
import one.modality.event.frontoffice.activities.booking.routing.BookingRouting;

public final class RouteToBookingRequest extends RoutePushRequest implements HasOperationCode {

    public RouteToBookingRequest(BrowsingHistory browsingHistory) {
        super(BookingRouting.getPath(), browsingHistory);
    }

    @Override
    public Object getOperationCode() {
        return "RouteToBooking";
    }
}
