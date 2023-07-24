package one.modality.hotel.backoffice.activities.roomsgraphic;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.util.PathBuilder;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;

import one.modality.hotel.backoffice.activities.roomsgraphic.routing.RoomsGraphicRouting;

/**
 * @author Bruno Salmon
 */
public final class RoomsGraphicUiRoute extends UiRouteImpl {

    public RoomsGraphicUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.createRegex(
                PathBuilder.toRegexPath(RoomsGraphicRouting.getAnyPath()),
                false,
                RoomsGraphicActivity::new,
                ViewDomainActivityContextFinal::new);
    }
}
