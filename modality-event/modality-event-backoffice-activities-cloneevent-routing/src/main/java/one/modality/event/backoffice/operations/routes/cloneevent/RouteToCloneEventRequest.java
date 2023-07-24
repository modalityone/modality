package one.modality.event.backoffice.operations.routes.cloneevent;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;

import one.modality.event.backoffice.activities.cloneevent.routing.CloneEventRouting;

/**
 * @author Bruno Salmon
 */
public final class RouteToCloneEventRequest extends RoutePushRequest implements HasOperationCode {

    private static final String OPERATION_CODE = "RouteToCloneEvent";

    public RouteToCloneEventRequest(Object eventId, BrowsingHistory history) {
        super(CloneEventRouting.getCloneEventPath(eventId), history);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }
}
