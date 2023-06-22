package one.modality.event.frontoffice.activities.alerts;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import one.modality.event.frontoffice.activities.alerts.routing.AlertsRouting;

public class AlertsUiRoute extends UiRouteImpl {

    public AlertsUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(AlertsRouting.getPath()
                , false
                , AlertsActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
