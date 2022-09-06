package one.modality.base.client.activity.organizationdependent;

import dev.webfx.stack.orm.domainmodel.activity.domain.DomainActivityContext;
import dev.webfx.stack.orm.domainmodel.activity.domain.DomainActivityContextMixin;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContextMixin;

public interface OrganizationDependentActivityMixin
        <C extends DomainActivityContext<C> & UiRouteActivityContext<C>>

        extends UiRouteActivityContextMixin<C>,
        DomainActivityContextMixin<C>,
        OrganizationDependentPresentationModelMixin {

    default void updateOrganizationDependentPresentationModelFromContextParameters() {
        setOrganizationId(getParameter("organizationId"));
    }
}
