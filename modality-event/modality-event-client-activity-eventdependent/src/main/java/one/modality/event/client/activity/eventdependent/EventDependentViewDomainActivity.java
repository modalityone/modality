package one.modality.event.client.activity.eventdependent;

import one.modality.base.client.activity.ModalityButtonFactoryMixin;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;

/**
 * @author Bruno Salmon
 */
public abstract class EventDependentViewDomainActivity
        extends ViewDomainActivityBase
        implements EventDependentActivityMixin<ViewDomainActivityContextFinal>,
        ModalityButtonFactoryMixin {

    protected EventDependentPresentationModel pm;

    @Override
    public EventDependentPresentationModel getPresentationModel() {
        if (pm == null)
            pm = new EventDependentPresentationModelImpl();
        return pm;
    }

    @Override
    protected void updateModelFromContextParameters() {
        updateEventDependentPresentationModelFromContextParameters();
        super.updateModelFromContextParameters();
    }
}
