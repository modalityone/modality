package org.modality_project.base.shared.entities.markers;

import org.modality_project.base.shared.entities.Event;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface EntityHasEvent extends Entity, HasEvent {

    @Override
    default void setEvent(Object event) {
        setForeignField("event", event);
    }

    @Override
    default EntityId getEventId() {
        return getForeignEntityId("event");
    }

    @Override
    default Event getEvent() {
        return getForeignEntity("event");
    }

}
