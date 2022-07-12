package org.modality_project.ecommerce.frontoffice.activities.contactus;

import org.modality_project.ecommerce.frontoffice.activities.contactus.routing.ContactUsRouting;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;

/**
 * @author Bruno Salmon
 */
public final class ContactUsUiRoute extends UiRouteImpl {

    public ContactUsUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(ContactUsRouting.getPath()
                , false
                , ContactUsActivity::new
                , ViewDomainActivityContextFinal::new
        );
    }
}
