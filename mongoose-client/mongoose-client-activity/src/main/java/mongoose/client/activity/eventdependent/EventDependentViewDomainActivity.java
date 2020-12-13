package mongoose.client.activity.eventdependent;

import mongoose.client.activity.MongooseButtonFactoryMixin;
import dev.webfx.framework.client.activity.impl.combinations.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.framework.client.activity.impl.combinations.viewdomain.impl.ViewDomainActivityBase;

/**
 * @author Bruno Salmon
 */
public abstract class EventDependentViewDomainActivity
    extends ViewDomainActivityBase
    implements EventDependentActivityMixin<ViewDomainActivityContextFinal>,
        MongooseButtonFactoryMixin {

    private EventDependentPresentationModel presentationModel;

    @Override
    public EventDependentPresentationModel getPresentationModel() {
        if (presentationModel == null)
            presentationModel = new EventDependentPresentationModelImpl();
        return presentationModel;
    }

    @Override
    protected void updateModelFromContextParameters() {
        updateEventDependentPresentationModelFromContextParameters();
        super.updateModelFromContextParameters();
    }
}
