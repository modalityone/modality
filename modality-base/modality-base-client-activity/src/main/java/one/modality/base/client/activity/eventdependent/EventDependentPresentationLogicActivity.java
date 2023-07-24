package one.modality.base.client.activity.eventdependent;

import dev.webfx.platform.util.function.Factory;
import dev.webfx.stack.orm.domainmodel.activity.domainpresentationlogic.impl.DomainPresentationLogicActivityContextFinal;
import one.modality.base.client.activity.ModalityDomainPresentationLogicActivityBase;

/**
 * @author Bruno Salmon
 */
public abstract class EventDependentPresentationLogicActivity<
        PM extends EventDependentPresentationModel>
    extends ModalityDomainPresentationLogicActivityBase<PM>
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
