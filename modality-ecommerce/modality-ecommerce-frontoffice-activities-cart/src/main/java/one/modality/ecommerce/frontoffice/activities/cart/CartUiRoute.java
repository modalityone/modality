package one.modality.ecommerce.frontoffice.activities.cart;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;

import one.modality.ecommerce.frontoffice.activities.cart.routing.CartRouting;

/**
 * @author Bruno Salmon
 */
public final class CartUiRoute extends UiRouteImpl {

    public CartUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(
                CartRouting.getPath(),
                false,
                CartActivity::new,
                ViewDomainActivityContextFinal::new);
    }
}
