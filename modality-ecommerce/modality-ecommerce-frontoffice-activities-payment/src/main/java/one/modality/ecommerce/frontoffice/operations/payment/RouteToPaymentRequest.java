package one.modality.ecommerce.frontoffice.operations.payment;

import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;

import one.modality.ecommerce.frontoffice.activities.payment.routing.PaymentRouting;

/**
 * @author Bruno Salmon
 */
public final class RouteToPaymentRequest extends RoutePushRequest {

    public RouteToPaymentRequest(Object cartUuidOrDocument, BrowsingHistory history) {
        super(PaymentRouting.getPaymentPath(cartUuidOrDocument), history);
    }
}
