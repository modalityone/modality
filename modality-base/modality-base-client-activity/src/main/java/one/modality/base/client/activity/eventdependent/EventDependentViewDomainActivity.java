package one.modality.base.client.activity.eventdependent;

import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import one.modality.base.client.activity.ModalityButtonFactoryMixin;

/**
 * @author Bruno Salmon
 */
public abstract class EventDependentViewDomainActivity extends ViewDomainActivityBase
    implements EventDependentActivityMixin<ViewDomainActivityContextFinal>,
        ModalityButtonFactoryMixin {

  protected EventDependentPresentationModel pm;

  @Override
  public EventDependentPresentationModel getPresentationModel() {
    if (pm == null) pm = new EventDependentPresentationModelImpl();
    return pm;
  }

  @Override
  protected void updateModelFromContextParameters() {
    updateEventDependentPresentationModelFromContextParameters();
    super.updateModelFromContextParameters();
  }
}
