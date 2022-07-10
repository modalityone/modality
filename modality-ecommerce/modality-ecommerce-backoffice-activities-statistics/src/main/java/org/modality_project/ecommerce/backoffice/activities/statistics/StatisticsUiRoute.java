package org.modality_project.ecommerce.backoffice.activities.statistics;

import org.modality_project.ecommerce.backoffice.activities.statistics.routing.StatisticsRouting;
import dev.webfx.stack.framework.client.activity.impl.combinations.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.framework.client.ui.uirouter.UiRoute;
import dev.webfx.stack.framework.client.ui.uirouter.impl.UiRouteImpl;

/**
 * @author Bruno Salmon
 */
public final class StatisticsUiRoute extends UiRouteImpl {

    public StatisticsUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(StatisticsRouting.getPath()
                , false
                , StatisticsActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
