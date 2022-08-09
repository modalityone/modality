package org.modality_project.event.frontoffice.operations.terms;

import org.modality_project.event.frontoffice.activities.terms.routing.TermsRouting;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.platform.windowhistory.spi.BrowsingHistory;

/**
 * @author Bruno Salmon
 */
public final class RouteToTermsRequest extends RoutePushRequest {

    public RouteToTermsRequest(Object eventId, BrowsingHistory history) {
        super(TermsRouting.getTermsPath(eventId), history);
    }

}
