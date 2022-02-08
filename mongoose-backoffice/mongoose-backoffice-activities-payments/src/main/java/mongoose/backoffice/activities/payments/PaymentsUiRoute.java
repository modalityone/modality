package mongoose.backoffice.activities.payments;

import mongoose.backoffice.activities.payments.routing.PaymentsRouting;
import dev.webfx.framework.client.activity.impl.combinations.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.framework.client.ui.uirouter.UiRoute;
import dev.webfx.framework.client.ui.uirouter.impl.UiRouteImpl;
import dev.webfx.framework.shared.router.util.PathBuilder;

/**
 * @author Bruno Salmon
 */
public final class PaymentsUiRoute extends UiRouteImpl {

    public PaymentsUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.createRegex(PathBuilder.toRegexPath(PaymentsRouting.getPath())
                , false
                , PaymentsActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
