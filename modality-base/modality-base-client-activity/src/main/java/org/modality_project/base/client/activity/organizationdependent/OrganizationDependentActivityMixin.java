package org.modality_project.base.client.activity.organizationdependent;

import dev.webfx.framework.client.activity.impl.elementals.domain.DomainActivityContext;
import dev.webfx.framework.client.activity.impl.elementals.domain.DomainActivityContextMixin;
import dev.webfx.framework.client.activity.impl.elementals.uiroute.UiRouteActivityContext;
import dev.webfx.framework.client.activity.impl.elementals.uiroute.UiRouteActivityContextMixin;

public interface OrganizationDependentActivityMixin
        <C extends DomainActivityContext<C> & UiRouteActivityContext<C>>

        extends UiRouteActivityContextMixin<C>,
        DomainActivityContextMixin<C>,
        OrganizationDependentPresentationModelMixin {

    default void updateOrganizationDependentPresentationModelFromContextParameters() {
        setOrganizationId(getParameter("organizationId"));
    }
}
