package one.modality.base.client.activity.organizationdependent;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import one.modality.base.client.activity.ModalityButtonFactoryMixin;
import one.modality.base.client.activity.eventdependent.EventDependentPresentationModelImpl;

/**
 * @author Bruno Salmon
 */
public abstract class OrganizationDependentViewDomainActivity
        extends ViewDomainActivityBase
        implements OrganizationDependentActivityMixin<ViewDomainActivityContextFinal>,
        ModalityButtonFactoryMixin {

    protected OrganizationDependentPresentationModel pm;

    @Override
    public OrganizationDependentPresentationModel getPresentationModel() {
        if (pm == null)
            pm = new EventDependentPresentationModelImpl();
        return pm;
    }

    @Override
    protected void updateModelFromContextParameters() {
        updateOrganizationDependentPresentationModelFromContextParameters();
        super.updateModelFromContextParameters();
    }
}
