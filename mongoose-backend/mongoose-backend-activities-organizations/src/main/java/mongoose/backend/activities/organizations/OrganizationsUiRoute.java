package mongoose.backend.activities.organizations;

import mongoose.backend.activities.organizations.routing.OrganizationsRouting;
import dev.webfx.framework.client.activity.impl.combinations.domainpresentation.impl.DomainPresentationActivityContextFinal;
import dev.webfx.framework.client.ui.uirouter.UiRoute;
import dev.webfx.framework.client.ui.uirouter.impl.UiRouteImpl;

/**
 * @author Bruno Salmon
 */
public final class OrganizationsUiRoute extends UiRouteImpl {

    public OrganizationsUiRoute() {
        super(uiRoute());
    }

    public static UiRoute<?> uiRoute() {
        return UiRoute.create(OrganizationsRouting.getPath()
                , false
                , OrganizationsActivity::new
                , DomainPresentationActivityContextFinal::new
        );
    }
}
