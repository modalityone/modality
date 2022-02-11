package mongoose.base.shared.entities.markers;

import mongoose.base.shared.entities.Event;
import dev.webfx.framework.shared.orm.entity.EntityId;

/**
 * @author Bruno Salmon
 */
public interface HasEvent {

    void setEvent(Object event);

    EntityId getEventId();

    Event getEvent();

}
