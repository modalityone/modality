package mongoose.ecommerce.frontoffice.activities.summary;

import mongoose.ecommerce.frontoffice.activities.summary.routing.SummaryRouting;
import dev.webfx.framework.client.activity.impl.combinations.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.framework.client.ui.uirouter.UiRoute;
import dev.webfx.framework.client.ui.uirouter.impl.UiRouteImpl;

/**
 * @author Bruno Salmon
 */
public final class SummaryUiRoute extends UiRouteImpl {

    public SummaryUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(SummaryRouting.getPath()
                , false
                , SummaryActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
