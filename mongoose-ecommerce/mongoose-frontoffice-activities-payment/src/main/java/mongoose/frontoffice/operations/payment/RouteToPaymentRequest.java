package mongoose.frontoffice.operations.payment;

import mongoose.frontoffice.activities.payment.routing.PaymentRouting;
import dev.webfx.framework.client.operations.route.RoutePushRequest;
import dev.webfx.platform.client.services.windowhistory.spi.BrowsingHistory;

/**
 * @author Bruno Salmon
 */
public final class RouteToPaymentRequest extends RoutePushRequest {

    public RouteToPaymentRequest(Object cartUuidOrDocument, BrowsingHistory history) {
        super(PaymentRouting.getPaymentPath(cartUuidOrDocument), history);
    }

}
