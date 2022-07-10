package org.modality_project.catering.backoffice.operations.routes.diningareas;

import org.modality_project.catering.backoffice.activities.diningareas.routing.DiningAreasRouting;
import dev.webfx.stack.framework.client.operations.route.RoutePushRequest;
import dev.webfx.stack.framework.shared.operation.HasOperationCode;
import dev.webfx.stack.platform.windowhistory.spi.BrowsingHistory;

/**
 * @author Bruno Salmon
 */
public final class RouteToDiningAreasRequest extends RoutePushRequest implements HasOperationCode {

    private final static String OPERATION_CODE = "RouteToDiningAreas";

    public RouteToDiningAreasRequest(Object eventId, BrowsingHistory history) {
        super(DiningAreasRouting.getEventPath(eventId), history);
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

}
