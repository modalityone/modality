package one.modality.ecommerce.backoffice2018.activities.statistics;

import dev.webfx.stack.routing.router.util.PathBuilder;
import one.modality.ecommerce.backoffice2018.activities.statistics.routing.StatisticsRouting;
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
        return UiRoute.createRegex(PathBuilder.toRegexPath(StatisticsRouting.getAnyPath())
                , false
                , StatisticsActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
