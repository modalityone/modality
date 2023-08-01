package one.modality.base.client.activity.organizationdependent;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import one.modality.base.client.activity.ModalityButtonFactoryMixin;

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
            pm = new OrganizationDependentPresentationModelImpl();
        return pm;
    }

    @Override
    protected void updateModelFromContextParameters() {
        updateOrganizationDependentPresentationModelFromContextParameters();
        super.updateModelFromContextParameters();
    }
}
