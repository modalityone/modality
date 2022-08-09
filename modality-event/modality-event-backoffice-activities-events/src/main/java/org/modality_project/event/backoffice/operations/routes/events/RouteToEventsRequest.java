package org.modality_project.event.backoffice.operations.routes.events;

import org.modality_project.event.backoffice.activities.events.routing.EventsRouting;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.platform.windowhistory.spi.BrowsingHistory;

/**
 * @author Bruno Salmon
 */
public final class RouteToEventsRequest extends RoutePushRequest implements HasOperationCode {

    private final static String OPERATION_CODE = "RouteToEvents";

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
