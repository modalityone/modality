package org.modality_project.all.backoffice.activities.event.clone.openjfx;

import org.modality_project.event.backoffice.activities.cloneevent.CloneEventPresentationLogicActivity;
import org.modality_project.event.backoffice.activities.cloneevent.CloneEventPresentationModel;
import dev.webfx.stack.framework.client.activity.impl.combinations.domainpresentation.impl.DomainPresentationActivityImpl;

/**
 * @author Bruno Salmon
 */
final class FxCloneEventPresentationActivity extends DomainPresentationActivityImpl<CloneEventPresentationModel> {

    FxCloneEventPresentationActivity() {
        super(FxCloneEventPresentationViewActivity::new, CloneEventPresentationLogicActivity::new);
    }
}
