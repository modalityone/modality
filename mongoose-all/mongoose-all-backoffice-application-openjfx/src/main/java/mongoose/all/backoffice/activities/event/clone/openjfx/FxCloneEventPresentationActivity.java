package mongoose.all.backoffice.activities.event.clone.openjfx;

import mongoose.event.backoffice.activities.cloneevent.CloneEventPresentationLogicActivity;
import mongoose.event.backoffice.activities.cloneevent.CloneEventPresentationModel;
import dev.webfx.framework.client.activity.impl.combinations.domainpresentation.impl.DomainPresentationActivityImpl;

/**
 * @author Bruno Salmon
 */
final class FxCloneEventPresentationActivity extends DomainPresentationActivityImpl<CloneEventPresentationModel> {

    FxCloneEventPresentationActivity() {
        super(FxCloneEventPresentationViewActivity::new, CloneEventPresentationLogicActivity::new);
    }
}
