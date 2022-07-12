package org.modality_project.event.frontoffice.activities.startbooking;

import org.modality_project.event.frontoffice.activities.startbooking.routing.StartBookingRouting;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;

/**
 * @author Bruno Salmon
 */
public final class StartBookingUiRoute extends UiRouteImpl {

    public StartBookingUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(StartBookingRouting.getPath()
                , false
                , StartBookingActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
