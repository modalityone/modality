package one.modality.ecommerce.frontoffice.operations.cart;

import one.modality.ecommerce.frontoffice.activities.cart.routing.CartRouting;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.platform.json.Json;
import dev.webfx.platform.windowhistory.spi.BrowsingHistory;

import java.time.Instant;

/**
 * @author Bruno Salmon
 */
public final class RouteToCartRequest extends RoutePushRequest {

    public RouteToCartRequest(Object cartUuidOrDocument, BrowsingHistory history) {
        super(CartRouting.getCartPath(cartUuidOrDocument), history, Json.createObject().set("refresh", Instant.now()));
    }

}
