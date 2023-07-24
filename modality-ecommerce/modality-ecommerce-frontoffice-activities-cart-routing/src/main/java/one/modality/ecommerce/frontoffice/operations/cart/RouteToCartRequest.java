package one.modality.ecommerce.frontoffice.operations.cart;

import dev.webfx.platform.json.Json;
import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import java.time.Instant;
import one.modality.ecommerce.frontoffice.activities.cart.routing.CartRouting;

/**
 * @author Bruno Salmon
 */
public final class RouteToCartRequest extends RoutePushRequest {

  public RouteToCartRequest(Object cartUuidOrDocument, BrowsingHistory history) {
    super(
        CartRouting.getCartPath(cartUuidOrDocument),
        history,
        Json.createObject().set("refresh", Instant.now()));
  }
}
