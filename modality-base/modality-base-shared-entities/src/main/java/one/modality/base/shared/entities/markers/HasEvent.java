package one.modality.base.shared.entities.markers;

import one.modality.base.shared.entities.Event;
import dev.webfx.stack.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface HasEvent {

    void setEvent(Object event);

    EntityId getEventId();

    Event getEvent();

}
