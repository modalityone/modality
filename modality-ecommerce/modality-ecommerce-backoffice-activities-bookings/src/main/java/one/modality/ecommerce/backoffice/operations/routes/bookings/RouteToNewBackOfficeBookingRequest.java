package one.modality.ecommerce.backoffice.operations.routes.bookings;

import dev.webfx.platform.async.AsyncFunction;
import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestBase;
import dev.webfx.stack.ui.operation.HasOperationCode;

/**
 * @author Bruno Salmon
 */
public final class RouteToNewBackOfficeBookingRequest
        extends RouteRequestBase<RouteToNewBackOfficeBookingRequest> implements HasOperationCode {

    private static final String OPERATION_CODE = "RouteToNewBackOfficeBooking";

    private Object eventId;

    public RouteToNewBackOfficeBookingRequest(Object eventId, BrowsingHistory history) {
        super(history);
        this.eventId = eventId;
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    public Object getEventId() {
        return eventId;
    }

    public RouteToNewBackOfficeBookingRequest setEventId(Object eventId) {
        this.eventId = eventId;
        return this;
    }

    @Override
    public AsyncFunction<RouteToNewBackOfficeBookingRequest, Void> getOperationExecutor() {
        return RouteToNewBackOfficeBookingExecutor::executeRequest;
    }
}
