package org.modality_project.ecommerce.frontoffice.activities.cart;

import org.modality_project.ecommerce.frontoffice.activities.cart.routing.CartRouting;
import dev.webfx.stack.framework.client.activity.impl.combinations.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.framework.client.ui.uirouter.UiRoute;
import dev.webfx.stack.framework.client.ui.uirouter.impl.UiRouteImpl;

/**
 * @author Bruno Salmon
 */
public final class CartUiRoute extends UiRouteImpl {

    public CartUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(CartRouting.getPath()
                , false
                , CartActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
