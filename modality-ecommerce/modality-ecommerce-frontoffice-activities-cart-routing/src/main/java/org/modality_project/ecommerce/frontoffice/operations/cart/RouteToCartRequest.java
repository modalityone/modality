package org.modality_project.ecommerce.frontoffice.operations.cart;

import org.modality_project.ecommerce.frontoffice.activities.cart.routing.CartRouting;
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
