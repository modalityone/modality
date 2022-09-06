package one.modality.ecommerce.backoffice.activities.statistics;

import one.modality.ecommerce.backoffice.activities.statistics.routing.StatisticsRouting;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;

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
