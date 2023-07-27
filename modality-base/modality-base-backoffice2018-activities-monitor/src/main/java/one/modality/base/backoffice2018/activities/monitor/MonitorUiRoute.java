package one.modality.base.backoffice2018.activities.monitor;

import one.modality.base.backoffice2018.activities.monitor.routing.MonitorRouting;
import dev.webfx.stack.orm.domainmodel.activity.domainpresentation.impl.DomainPresentationActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;

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
