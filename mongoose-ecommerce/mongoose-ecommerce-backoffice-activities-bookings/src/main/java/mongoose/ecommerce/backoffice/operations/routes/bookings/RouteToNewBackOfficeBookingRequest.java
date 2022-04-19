package mongoose.ecommerce.backoffice.operations.routes.bookings;

import dev.webfx.framework.shared.operation.HasOperationCode;
import dev.webfx.framework.client.operations.route.RouteRequestBase;
import dev.webfx.platform.client.services.windowhistory.spi.BrowsingHistory;
import dev.webfx.platform.shared.async.AsyncFunction;

/**
 * @author Bruno Salmon
 */
public final class RouteToNewBackOfficeBookingRequest extends RouteRequestBase<RouteToNewBackOfficeBookingRequest>
        implements HasOperationCode {

    private final static String OPERATION_CODE = "RouteToNewBackOfficeBooking";

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
