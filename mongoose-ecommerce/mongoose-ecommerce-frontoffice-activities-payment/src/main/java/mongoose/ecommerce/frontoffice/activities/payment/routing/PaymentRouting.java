package mongoose.ecommerce.frontoffice.activities.payment.routing;

import mongoose.ecommerce.frontoffice.activities.cart.routing.CartRouting;
import mongoose.base.client.util.routing.MongooseRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class PaymentRouting {

    private final static String PATH = CartRouting.getPath() + "/payment";

    public static String getPath() {
        return PATH;
    }

    public static String getPaymentPath(Object cartUuidOrDocument) {
        return MongooseRoutingUtil.interpolateCartUuidInPath(CartRouting.getCartUuid(cartUuidOrDocument), getPath());
    }

}
