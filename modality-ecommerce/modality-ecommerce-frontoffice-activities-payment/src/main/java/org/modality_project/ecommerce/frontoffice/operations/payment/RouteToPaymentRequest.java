package org.modality_project.ecommerce.frontoffice.operations.payment;

import org.modality_project.ecommerce.frontoffice.activities.payment.routing.PaymentRouting;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.platform.windowhistory.spi.BrowsingHistory;

/**
 * @author Bruno Salmon
 */
public final class RouteToPaymentRequest extends RoutePushRequest {

    public RouteToPaymentRequest(Object cartUuidOrDocument, BrowsingHistory history) {
        super(PaymentRouting.getPaymentPath(cartUuidOrDocument), history);
    }

}
