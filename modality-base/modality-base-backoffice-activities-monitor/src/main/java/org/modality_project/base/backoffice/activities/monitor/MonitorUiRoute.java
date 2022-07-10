package org.modality_project.base.backoffice.activities.monitor;

import org.modality_project.base.backoffice.activities.monitor.routing.MonitorRouting;
import dev.webfx.stack.framework.client.activity.impl.combinations.domainpresentation.impl.DomainPresentationActivityContextFinal;
import dev.webfx.stack.framework.client.ui.uirouter.UiRoute;
import dev.webfx.stack.framework.client.ui.uirouter.impl.UiRouteImpl;

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
