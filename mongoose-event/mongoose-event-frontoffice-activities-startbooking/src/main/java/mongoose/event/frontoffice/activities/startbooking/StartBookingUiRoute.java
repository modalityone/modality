package mongoose.event.frontoffice.activities.startbooking;

import mongoose.event.frontoffice.activities.startbooking.routing.StartBookingRouting;
import dev.webfx.framework.client.activity.impl.combinations.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.framework.client.ui.uirouter.UiRoute;
import dev.webfx.framework.client.ui.uirouter.impl.UiRouteImpl;

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
