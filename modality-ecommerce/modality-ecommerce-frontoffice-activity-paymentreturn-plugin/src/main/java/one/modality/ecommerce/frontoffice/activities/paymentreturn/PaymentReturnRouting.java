package one.modality.ecommerce.frontoffice.activities.paymentreturn;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;

/**
 * @author Bruno Salmon
 */
public final class PaymentReturnRouting {

    private final static String PATH = "/payment-return/:moneyTransferId";

    public static String getPath() {
        return PATH;
    }

    public static final class PaymentReturnUiRoute extends UiRouteImpl {

        public PaymentReturnUiRoute() {
            super(uiRoute());
        }

        public static UiRoute<?> uiRoute() {
            return UiRoute.create(PaymentReturnRouting.getPath()
                    , false
                    , PaymentReturnActivity::new
                    , ViewDomainActivityContextFinal::new
            );
        }
    }

}
