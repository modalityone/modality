package one.modality.event.backoffice.activities.cloneevent;

import dev.webfx.stack.orm.domainmodel.activity.domainpresentation.impl.DomainPresentationActivityImpl;

/**
 * @author Bruno Salmon
 */
final class CloneEventActivity extends DomainPresentationActivityImpl<CloneEventPresentationModel> {

  CloneEventActivity() {
    super(CloneEventPresentationViewActivity::new, CloneEventPresentationLogicActivity::new);
  }
}
