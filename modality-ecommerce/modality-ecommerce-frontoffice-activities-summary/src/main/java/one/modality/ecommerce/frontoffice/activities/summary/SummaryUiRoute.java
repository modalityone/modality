package one.modality.ecommerce.frontoffice.activities.summary;

import one.modality.ecommerce.frontoffice.activities.summary.routing.SummaryRouting;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;

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
