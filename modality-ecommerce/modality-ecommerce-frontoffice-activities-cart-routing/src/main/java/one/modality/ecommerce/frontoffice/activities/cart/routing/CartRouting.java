package one.modality.ecommerce.frontoffice.activities.cart.routing;

import one.modality.base.client.util.routing.ModalityRoutingUtil;
import one.modality.base.shared.entities.Document;

/**
 * @author Bruno Salmon
 */
public final class CartRouting {

    private static final String PATH = "/book/cart/:cartUuid";

    public static String getPath() {
        return PATH;
    }

    public static String getCartPath(Object cartUuidOrDocument) {
        return ModalityRoutingUtil.interpolateCartUuidInPath(
                getCartUuid(cartUuidOrDocument), getPath());
    }

    public static Object getCartUuid(Object cartUuidOrDocument) {
        if (cartUuidOrDocument instanceof Document)
            return ((Document) cartUuidOrDocument).evaluate("cart.uuid");
        return cartUuidOrDocument;
    }
}
