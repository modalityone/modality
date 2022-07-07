package org.modality_project.ecommerce.frontoffice.activities.contactus;

import org.modality_project.ecommerce.frontoffice.activities.contactus.routing.ContactUsRouting;
import dev.webfx.framework.client.activity.impl.combinations.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.framework.client.ui.uirouter.UiRoute;
import dev.webfx.framework.client.ui.uirouter.impl.UiRouteImpl;

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
