package one.modality.hotel.backoffice.activities.household;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.util.PathBuilder;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;

import one.modality.hotel.backoffice.activities.household.routing.HouseholdRouting;

/**
 * @author Bruno Salmon
 */
public final class HouseholdUiRoute extends UiRouteImpl {

    public HouseholdUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.createRegex(
                PathBuilder.toRegexPath(HouseholdRouting.getAnyPath()),
                false,
                HouseholdActivity::new,
                ViewDomainActivityContextFinal::new);
    }
}
