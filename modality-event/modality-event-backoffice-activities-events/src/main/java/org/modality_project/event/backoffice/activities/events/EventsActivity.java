package org.modality_project.event.backoffice.activities.events;

import dev.webfx.stack.orm.domainmodel.activity.domainpresentation.impl.DomainPresentationActivityImpl;

/**
 * @author Bruno Salmon
 */
final class EventsActivity extends DomainPresentationActivityImpl<EventsPresentationModel> {

    EventsActivity() {
        super(EventsPresentationViewActivity::new, EventsPresentationLogicActivity::new);
    }
}
