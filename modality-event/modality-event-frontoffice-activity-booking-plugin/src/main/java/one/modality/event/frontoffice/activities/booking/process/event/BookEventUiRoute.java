package one.modality.event.frontoffice.activities.booking.process.event;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;

/**
 * @author Bruno Salmon
 */
public final class BookEventUiRoute extends UiRouteImpl {

    public BookEventUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(BookEventRouting.getPath()
                , false
                , BookEventActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }

}
