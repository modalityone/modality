package one.modality.ecommerce.frontoffice.operations.contactus;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;

import one.modality.ecommerce.frontoffice.activities.contactus.routing.ContactUsRouting;

/**
 * @author Bruno Salmon
 */
public final class RouteToContactUsRequest extends RoutePushRequest {

    public RouteToContactUsRequest(Object documentId, BrowsingHistory history) {
        super(ContactUsRouting.getContactUsPath(documentId), history);
    }
}
