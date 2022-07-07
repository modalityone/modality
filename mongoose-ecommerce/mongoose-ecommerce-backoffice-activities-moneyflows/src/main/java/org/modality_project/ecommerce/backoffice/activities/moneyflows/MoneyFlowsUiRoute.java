package org.modality_project.ecommerce.backoffice.activities.moneyflows;

import dev.webfx.framework.client.activity.impl.combinations.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.framework.client.ui.uirouter.UiRoute;
import dev.webfx.framework.client.ui.uirouter.impl.UiRouteImpl;
import org.modality_project.ecommerce.backoffice.activities.moneyflows.routing.MoneyFlowsRouting;

/**
 * @author Bruno Salmon
 */
public final class MoneyFlowsUiRoute extends UiRouteImpl {

    public MoneyFlowsUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(MoneyFlowsRouting.getPath()
                , false
                , MoneyFlowsActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
