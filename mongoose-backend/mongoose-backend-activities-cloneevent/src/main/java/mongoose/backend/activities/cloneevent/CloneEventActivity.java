package mongoose.backend.activities.cloneevent;

import dev.webfx.framework.client.activity.impl.combinations.domainpresentation.impl.DomainPresentationActivityImpl;

/**
 * @author Bruno Salmon
 */
final class CloneEventActivity extends DomainPresentationActivityImpl<CloneEventPresentationModel> {

    CloneEventActivity() {
        super(CloneEventPresentationViewActivity::new, CloneEventPresentationLogicActivity::new);
    }
}
