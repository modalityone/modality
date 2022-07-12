package org.modality_project.ecommerce.frontoffice.activities.person;

import org.modality_project.ecommerce.frontoffice.activities.person.routing.PersonRouting;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;

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
