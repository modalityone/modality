package org.modality_project.ecommerce.frontoffice.activities.cart;

import org.modality_project.ecommerce.frontoffice.activities.cart.routing.CartRouting;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;

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
