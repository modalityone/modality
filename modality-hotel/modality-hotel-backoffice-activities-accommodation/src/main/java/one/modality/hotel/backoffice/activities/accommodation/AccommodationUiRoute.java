package one.modality.hotel.backoffice.activities.accommodation;

import one.modality.hotel.backoffice.activities.accommodation.routing.AccommodationRouting;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import dev.webfx.stack.routing.router.util.PathBuilder;

/**
 * @author Bruno Salmon
 */
public final class AccommodationUiRoute extends UiRouteImpl {

    public AccommodationUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.createRegex(PathBuilder.toRegexPath(AccommodationRouting.getAnyPath())
                , true
                , AccommodationActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
