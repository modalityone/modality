package org.modality_project.ecommerce.frontoffice.operations.summary;

import org.modality_project.ecommerce.frontoffice.activities.summary.routing.SummaryRouting;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.platform.windowhistory.spi.BrowsingHistory;

/**
 * @author Bruno Salmon
 */
public final class RouteToSummaryRequest extends RoutePushRequest {

    public RouteToSummaryRequest(Object eventId, BrowsingHistory history) {
        super(SummaryRouting.getSummaryPath(eventId), history);
    }

}
