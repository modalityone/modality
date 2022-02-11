package mongoose.base.backoffice.activities.monitor;

import mongoose.base.backoffice.activities.monitor.routing.MonitorRouting;
import dev.webfx.framework.client.activity.impl.combinations.domainpresentation.impl.DomainPresentationActivityContextFinal;
import dev.webfx.framework.client.ui.uirouter.UiRoute;
import dev.webfx.framework.client.ui.uirouter.impl.UiRouteImpl;

/**
 * @author Bruno Salmon
 */
public final class MonitorUiRoute extends UiRouteImpl {

    public MonitorUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(MonitorRouting.getPath()
                , true
                , MonitorActivity::new
                , DomainPresentationActivityContextFinal::new
        );
    }
}
