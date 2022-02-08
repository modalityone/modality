package mongoose.frontoffice.activities.payment;

import mongoose.frontoffice.activities.payment.routing.PaymentRouting;
import dev.webfx.framework.client.activity.impl.combinations.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.framework.client.ui.uirouter.UiRoute;
import dev.webfx.framework.client.ui.uirouter.impl.UiRouteImpl;

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
