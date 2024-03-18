package one.modality.event.backoffice.activities.recurringevents;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import one.modality.event.backoffice.activities.recurringevents.routing.RecurringEventsRouting;

public final class BookingUiRoute extends UiRouteImpl {

    public BookingUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(RecurringEventsRouting.getPath()
                , false
                , RecurringEventsActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
