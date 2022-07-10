package org.modality_project.ecommerce.frontoffice.operations.contactus;

import org.modality_project.ecommerce.frontoffice.activities.contactus.routing.ContactUsRouting;
import dev.webfx.stack.framework.client.operations.route.RoutePushRequest;
import dev.webfx.stack.platform.windowhistory.spi.BrowsingHistory;

/**
 * @author Bruno Salmon
 */
public final class RouteToContactUsRequest extends RoutePushRequest {

    public RouteToContactUsRequest(Object documentId, BrowsingHistory history) {
        super(ContactUsRouting.getContactUsPath(documentId), history);
    }

}
