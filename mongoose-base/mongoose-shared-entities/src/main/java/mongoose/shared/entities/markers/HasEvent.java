package mongoose.shared.entities.markers;

import mongoose.shared.entities.Event;
import dev.webfx.framework.shared.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface HasEvent {

    void setEvent(Object event);

    EntityId getEventId();

    Event getEvent();

}
