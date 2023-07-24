package one.modality.ecommerce.backoffice.activities.income;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;

import one.modality.ecommerce.backoffice.activities.income.routing.IncomeRouting;

/**
 * @author Bruno Salmon
 */
public final class IncomeUiRoute extends UiRouteImpl {

    public IncomeUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(
                IncomeRouting.getPath(),
                true,
                IncomeActivity::new,
                ViewDomainActivityContextFinal::new);
    }
}
