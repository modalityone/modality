package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.Timeline;

/**
 * @author Bruno Salmon
 */
public interface HasTimeline {

    void setTimeline(Object timeline);

    EntityId getTimelineId();

    Timeline getTimeline();

}
