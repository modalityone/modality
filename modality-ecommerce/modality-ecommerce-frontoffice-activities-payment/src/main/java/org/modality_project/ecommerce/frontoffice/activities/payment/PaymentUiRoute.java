package org.modality_project.ecommerce.frontoffice.activities.payment;

import org.modality_project.ecommerce.frontoffice.activities.payment.routing.PaymentRouting;
import dev.webfx.stack.framework.client.activity.impl.combinations.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.framework.client.ui.uirouter.UiRoute;
import dev.webfx.stack.framework.client.ui.uirouter.impl.UiRouteImpl;

/**
 * @author Bruno Salmon
 */
public final class PaymentUiRoute extends UiRouteImpl {

    public PaymentUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(PaymentRouting.getPath()
                , false
                , PaymentActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
