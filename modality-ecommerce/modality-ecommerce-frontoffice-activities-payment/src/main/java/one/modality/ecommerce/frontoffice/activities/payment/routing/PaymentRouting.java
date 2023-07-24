package one.modality.ecommerce.frontoffice.activities.payment.routing;

import one.modality.base.client.util.routing.ModalityRoutingUtil;
import one.modality.ecommerce.frontoffice.activities.cart.routing.CartRouting;

/**
 * @author Bruno Salmon
 */
public final class PaymentRouting {

    private static final String PATH = CartRouting.getPath() + "/payment";

    public static String getPath() {
        return PATH;
    }

    public static String getPaymentPath(Object cartUuidOrDocument) {
        return ModalityRoutingUtil.interpolateCartUuidInPath(
                CartRouting.getCartUuid(cartUuidOrDocument), getPath());
    }
}
