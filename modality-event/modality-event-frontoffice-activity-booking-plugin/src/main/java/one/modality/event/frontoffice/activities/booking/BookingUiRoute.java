package one.modality.event.frontoffice.activities.booking;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import one.modality.event.frontoffice.activities.booking.routing.BookingRouting;

public class BookingUiRoute extends UiRouteImpl {

    public BookingUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(BookingRouting.getPath()
                , false
                , BookingActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
