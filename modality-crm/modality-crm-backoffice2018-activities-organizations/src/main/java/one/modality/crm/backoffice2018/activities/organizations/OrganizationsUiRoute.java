package one.modality.crm.backoffice2018.activities.organizations;

import one.modality.crm.backoffice2018.activities.organizations.routing.OrganizationsRouting;
import dev.webfx.stack.orm.domainmodel.activity.domainpresentation.impl.DomainPresentationActivityContextFinal;
import dev.webfx.stack.routing.uirouter.UiRoute;
import dev.webfx.stack.routing.uirouter.impl.UiRouteImpl;

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
