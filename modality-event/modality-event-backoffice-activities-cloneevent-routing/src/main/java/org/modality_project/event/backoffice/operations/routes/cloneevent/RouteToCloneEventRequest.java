package org.modality_project.event.backoffice.operations.routes.cloneevent;

import org.modality_project.event.backoffice.activities.cloneevent.routing.CloneEventRouting;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.platform.windowhistory.spi.BrowsingHistory;

/**
 * @author Bruno Salmon
 */
public final class RouteToCloneEventRequest extends RoutePushRequest implements HasOperationCode {

    private final static String OPERATION_CODE = "RouteToCloneEvent";

    public RouteToCloneEventRequest(Object eventId, BrowsingHistory history) {
        super(CloneEventRouting.getCloneEventPath(eventId), history);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }
}
