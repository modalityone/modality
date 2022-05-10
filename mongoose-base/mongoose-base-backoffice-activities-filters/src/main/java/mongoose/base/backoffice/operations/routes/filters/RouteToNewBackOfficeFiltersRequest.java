package mongoose.base.backoffice.operations.routes.filters;

import dev.webfx.framework.shared.operation.HasOperationCode;
import dev.webfx.framework.client.operations.route.RouteRequestBase;
import dev.webfx.platform.client.services.windowhistory.spi.BrowsingHistory;
import dev.webfx.platform.shared.async.AsyncFunction;

public final class RouteToNewBackOfficeFiltersRequest extends RouteRequestBase<RouteToNewBackOfficeFiltersRequest>
        implements HasOperationCode {

    private final static String OPERATION_CODE = "RouteToNewBackOfficeFilters";

    private Object eventId;

    public RouteToNewBackOfficeFiltersRequest(Object eventId, BrowsingHistory history) {
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

    public RouteToNewBackOfficeFiltersRequest setEventId(Object eventId) {
        this.eventId = eventId;
        return this;
    }

    @Override
    public AsyncFunction<RouteToNewBackOfficeFiltersRequest, Void> getOperationExecutor() {
        return RouteToNewBackOfficeFiltersExecutor::executeRequest;
    }

}
