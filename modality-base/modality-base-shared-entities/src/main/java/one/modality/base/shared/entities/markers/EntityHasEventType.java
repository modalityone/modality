package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.EventType;

/**
 * @author Bruno Salmon
 */
public interface EntityHasEventType extends Entity, HasEventType {

    String eventType = "eventType";

    @Override
    default void setEventType(Object value) {
        setForeignField(eventType, value);
    }

    @Override
    default EntityId getEventTypeId() {
        return getForeignEntityId(eventType);
    }

    @Override
    default EventType getEventType() {
        return getForeignEntity(eventType);
    }

}