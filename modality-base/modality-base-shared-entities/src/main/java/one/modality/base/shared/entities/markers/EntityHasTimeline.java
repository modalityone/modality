package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.Timeline;

/**
 * @author Bruno Salmon
 */
public interface EntityHasTimeline extends Entity, HasTimeline {

    String timeline = "timeline";

    @Override
    default void setTimeline(Object value) {
        setForeignField(timeline, value);
    }

    @Override
    default EntityId getTimelineId() {
        return getForeignEntityId(timeline);
    }

    @Override
    default Timeline getTimeline() {
        return getForeignEntity(timeline);
    }

}