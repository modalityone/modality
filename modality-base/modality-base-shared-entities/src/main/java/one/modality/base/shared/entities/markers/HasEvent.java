package one.modality.base.shared.entities.markers;

import dev.webfx.stack.orm.entity.EntityId;
import one.modality.base.shared.entities.Event;

/**
 * @author Bruno Salmon
 */
public interface HasEvent {

  void setEvent(Object event);

  EntityId getEventId();

  Event getEvent();
}
