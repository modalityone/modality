package mongoose.frontoffice.activities.person;

import mongoose.frontoffice.activities.person.routing.PersonRouting;
import dev.webfx.framework.client.activity.impl.combinations.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.framework.client.ui.uirouter.UiRoute;
import dev.webfx.framework.client.ui.uirouter.impl.UiRouteImpl;

/**
 * @author Bruno Salmon
 */
public final class PersonUiRoute extends UiRouteImpl {

    public PersonUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(PersonRouting.getPath()
                , false
                , PersonActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
