package mongoose.base.client.activity.eventdependent;

import mongoose.base.client.activity.MongooseDomainPresentationLogicActivityBase;
import dev.webfx.framework.client.activity.impl.combinations.domainpresentationlogic.impl.DomainPresentationLogicActivityContextFinal;
import dev.webfx.platform.shared.util.function.Factory;

/**
 * @author Bruno Salmon
 */
public abstract class EventDependentPresentationLogicActivity
        <PM extends EventDependentPresentationModel>

        extends MongooseDomainPresentationLogicActivityBase<PM>
        implements EventDependentActivityMixin<DomainPresentationLogicActivityContextFinal<PM>> {

    public EventDependentPresentationLogicActivity(Factory<PM> presentationModelFactory) {
        super(presentationModelFactory);
    }

    @Override
    public PM getPresentationModel() {
        return getActivityContext().getPresentationModel();
    }

    @Override
    protected void updatePresentationModelFromContextParameters(PM pm) {
        updateEventDependentPresentationModelFromContextParameters();
    }
}
