package one.modality.all.backoffice.activities.event.clone.openjfx;

import dev.webfx.stack.orm.domainmodel.activity.domainpresentation.impl.DomainPresentationActivityImpl;
import one.modality.event.backoffice.activities.cloneevent.CloneEventPresentationLogicActivity;
import one.modality.event.backoffice.activities.cloneevent.CloneEventPresentationModel;

/**
 * @author Bruno Salmon
 */
final class FxCloneEventPresentationActivity
    extends DomainPresentationActivityImpl<CloneEventPresentationModel> {

  FxCloneEventPresentationActivity() {
    super(FxCloneEventPresentationViewActivity::new, CloneEventPresentationLogicActivity::new);
  }
}
