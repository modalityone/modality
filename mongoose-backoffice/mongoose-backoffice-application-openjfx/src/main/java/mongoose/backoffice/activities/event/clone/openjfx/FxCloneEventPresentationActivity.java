package mongoose.backoffice.activities.event.clone.openjfx;

import mongoose.backoffice.activities.cloneevent.CloneEventPresentationLogicActivity;
import mongoose.backoffice.activities.cloneevent.CloneEventPresentationModel;
import dev.webfx.framework.client.activity.impl.combinations.domainpresentation.impl.DomainPresentationActivityImpl;

/**
 * @author Bruno Salmon
 */
final class FxCloneEventPresentationActivity extends DomainPresentationActivityImpl<CloneEventPresentationModel> {

    FxCloneEventPresentationActivity() {
        super(FxCloneEventPresentationViewActivity::new, CloneEventPresentationLogicActivity::new);
    }
}
