package one.modality.base.shared.entities;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.markers.*;

/**
 * @author Bruno Salmon
 */
public interface ScheduledItem extends Entity,
        EntityHasEvent,
        EntityHasLocalDate,
        EntityHasSiteAndItem,
        EntityHasStartAndEndTime {

    default void setTimeLine(Object timeline) {
        setForeignField("timeline", timeline);
    }

    default EntityId getTimelineId() {
        return getForeignEntityId("timeline");
    }

    default Timeline getTimeline() {
        return getForeignEntity("timeline");
    }


}
