package org.modality_project.ecommerce.frontoffice.activities.payment;

import org.modality_project.ecommerce.frontoffice.activities.payment.routing.PaymentRouting;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;

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
