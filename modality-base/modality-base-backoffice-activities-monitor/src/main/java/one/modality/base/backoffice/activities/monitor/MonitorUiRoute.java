package one.modality.base.backoffice.activities.monitor;

import dev.webfx.stack.orm.domainmodel.activity.domainpresentation.impl.DomainPresentationActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;

import one.modality.base.backoffice.activities.monitor.routing.MonitorRouting;

/**
 * @author Bruno Salmon
 */
public final class MonitorUiRoute extends UiRouteImpl {

    public MonitorUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(
                MonitorRouting.getPath(),
                true,
                MonitorActivity::new,
                DomainPresentationActivityContextFinal::new);
    }
}
