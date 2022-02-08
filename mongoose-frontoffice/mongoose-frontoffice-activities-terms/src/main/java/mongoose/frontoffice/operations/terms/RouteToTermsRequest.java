package mongoose.frontoffice.operations.terms;

import mongoose.frontoffice.activities.terms.routing.TermsRouting;
import dev.webfx.framework.client.operations.route.RoutePushRequest;
import dev.webfx.platform.client.services.windowhistory.spi.BrowsingHistory;

/**
 * @author Bruno Salmon
 */
public final class RouteToTermsRequest extends RoutePushRequest {

    public RouteToTermsRequest(Object eventId, BrowsingHistory history) {
        super(TermsRouting.getTermsPath(eventId), history);
    }

}
