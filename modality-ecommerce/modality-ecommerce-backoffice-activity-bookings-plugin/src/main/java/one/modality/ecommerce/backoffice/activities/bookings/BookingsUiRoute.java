package one.modality.ecommerce.backoffice.activities.bookings;

import one.modality.ecommerce.backoffice.activities.bookings.routing.BookingsRouting;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;
import dev.webfx.stack.routing.router.util.PathBuilder;

/**
 * @author Bruno Salmon
 */
public final class BookingsUiRoute extends UiRouteImpl {

    public BookingsUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.createRegex(PathBuilder.toRegexPath(BookingsRouting.getAnyPath())
                , true
                , BookingsActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
