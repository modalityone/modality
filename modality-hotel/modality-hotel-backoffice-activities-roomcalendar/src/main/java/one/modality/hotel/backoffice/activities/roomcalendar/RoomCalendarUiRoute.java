package one.modality.hotel.backoffice.activities.roomcalendar;

import one.modality.hotel.backoffice.activities.roomcalendar.routing.RoomCalendarRouting;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import dev.webfx.stack.routing.router.util.PathBuilder;

/**
 * @author Bruno Salmon
 */
public final class RoomCalendarUiRoute extends UiRouteImpl {

    public RoomCalendarUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.createRegex(PathBuilder.toRegexPath(RoomCalendarRouting.getAnyPath())
                , false
                , RoomCalendarActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
