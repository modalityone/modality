package org.modality_project.ecommerce.backoffice.activities.income;

import org.modality_project.ecommerce.backoffice.activities.income.routing.IncomeRouting;
import dev.webfx.framework.client.activity.impl.combinations.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.framework.client.ui.uirouter.UiRoute;
import dev.webfx.framework.client.ui.uirouter.impl.UiRouteImpl;

/**
 * @author Bruno Salmon
 */
public final class IncomeUiRoute extends UiRouteImpl {

    public IncomeUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(IncomeRouting.getPath()
                , true
                , IncomeActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
