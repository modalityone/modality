package one.modality.event.backoffice.operations.routes.events;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;

import one.modality.event.backoffice.activities.events.routing.EventsRouting;

/**
 * @author Bruno Salmon
 */
public final class RouteToEventsRequest extends RoutePushRequest implements HasOperationCode {

    private static final String OPERATION_CODE = "RouteToEvents";

    public RouteToEventsRequest(BrowsingHistory history) {
        super(EventsRouting.getAllEventsPath(), history);
    }

    public RouteToEventsRequest(Object organizationId, BrowsingHistory history) {
        super(EventsRouting.getOrganizationEventsPath(organizationId), history);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }
}
