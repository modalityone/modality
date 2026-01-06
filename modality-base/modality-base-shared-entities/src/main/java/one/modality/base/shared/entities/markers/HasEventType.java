package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.EventType;

/**
 * @author Bruno Salmon
 */
public interface HasEventType {

    void setEventType(Object event);

    EntityId getEventTypeId();

    EventType getEventType();

}
