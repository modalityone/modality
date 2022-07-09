package org.modality_project.ecommerce.frontoffice.activities.payment.routing;

import org.modality_project.ecommerce.frontoffice.activities.cart.routing.CartRouting;
import org.modality_project.base.client.util.routing.ModalityRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class PaymentRouting {

    private final static String PATH = CartRouting.getPath() + "/payment";

    public static String getPath() {
        return PATH;
    }

    public static String getPaymentPath(Object cartUuidOrDocument) {
        return ModalityRoutingUtil.interpolateCartUuidInPath(CartRouting.getCartUuid(cartUuidOrDocument), getPath());
    }

}
