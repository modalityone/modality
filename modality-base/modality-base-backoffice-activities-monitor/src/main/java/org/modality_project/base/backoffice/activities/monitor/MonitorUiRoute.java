package org.modality_project.base.backoffice.activities.monitor;

import org.modality_project.base.backoffice.activities.monitor.routing.MonitorRouting;
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
