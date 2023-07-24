package one.modality.event.frontoffice.activities.startbooking;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;

import one.modality.event.frontoffice.activities.startbooking.routing.StartBookingRouting;

/**
 * @author Bruno Salmon
 */
public final class StartBookingUiRoute extends UiRouteImpl {

    public StartBookingUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(
                StartBookingRouting.getPath(),
                false,
                StartBookingActivity::new,
                ViewDomainActivityContextFinal::new);
    }
}
