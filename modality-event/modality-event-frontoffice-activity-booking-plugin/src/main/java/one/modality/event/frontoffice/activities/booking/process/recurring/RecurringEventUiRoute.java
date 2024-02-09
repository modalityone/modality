package one.modality.event.frontoffice.activities.booking.process.recurring;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;

/**
 * @author Bruno Salmon
 */
public final class RecurringEventUiRoute extends UiRouteImpl {

    public RecurringEventUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(RecurringEventRouting.getPath()
                , false
                , RecurringEventActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }

}
